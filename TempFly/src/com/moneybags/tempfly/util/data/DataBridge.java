package com.moneybags.tempfly.util.data;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.configuration.file.FileConfiguration;

import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.config.SpigotConfigProvider.SpigotConfigSection;
import com.moneybags.tempfly.util.data.files.DataFileHolder;
import com.moneybags.tempfly.util.data.files.ResourceProvider;
import com.moneybags.tempfly.util.data.provider.DataProvider;
import com.moneybags.tempfly.util.data.provider.SqlProvider;
import com.moneybags.tempfly.util.data.provider.YamlProvider;
import com.moneybags.tempfly.util.data.values.DataPointer;
import com.moneybags.tempfly.util.data.values.DataValue;
import com.moneybags.tempfly.util.data.values.StagedChange;

/**
 * DataBridge provides the means of saving and retrieving data asynchronously through various
 * means of data storage.
 * @author Kevin
 *
 */
public class DataBridge implements DataFileHolder {

	private ResourceProvider resources;
	private ExecutorService executor;
	
	private DataProvider primary;
	
	private List<DataProvider> providers = new ArrayList<>();
	
	// Staged changes are held in local memory until either the autosave runs, or they are forcefully committed.
	// The databridge will act like these changes are part of the database even though they are local. 
	// It will look to see if there is data here first before it queries the database or YAML file.

	private Map<DataPointer, StagedChange> changes = new ConcurrentHashMap<>();
	
	public ResourceProvider getResources() {
		return resources;
	}
	
	public DataBridge(ResourceProvider resources) throws SQLException, IOException {
		this.resources = resources;
		DataProvider yml = new YamlProvider(resources);
		providers.add(yml);
		if (resources.getConfigProvider().getDefaultConfig().getBoolean("system.mysql.enabled")) {
			this.primary = new SqlProvider(resources);
			providers.add(primary);
		} else {
			this.primary = yml;
			resources.getConfigProvider().loadConfig("data.yml");
		}
		
		this.executor = Executors.newCachedThreadPool();
	}
	
	/**
	 * Get whether the primary data provider of this bridge is an SQL database.
	 * @return
	 */
	public boolean usingSql() {
		return primary instanceof SqlProvider;
	}
	
	/**
	 * Get the default data provider for this data bridge.
	 * @return The data provider this bridge is currently using.
	 */
	public DataProvider getPrimaryDataProvider() {
		return primary;
	}
	
	public void addDataProvider(DataProvider provider) {
		if (providers.stream().anyMatch(registered -> provider.getClass().equals(registered.getClass()) )) {
			throw new IllegalArgumentException("Cannot register more than one data provider of the same type...");
		}
		providers.add(provider);
	}
	
	public void setPrimaryDataProvider(DataProvider provider) {
		if (!providers.contains(provider)) {
			providers.add(provider);
		}
		primary = provider;
	}
	
	public <T extends DataProvider> T getDataProvider(Class<T> type) {
		for (DataProvider provider: providers) {
			if (!type.isAssignableFrom(provider.getClass())) {
				continue;
			}
			return type.cast(provider);
		}
		return null;
	}
	
	/**
	 * Stage a data change to be sent to the database later.
	 * @param pointer
	 * @param data
	 */
	public void stageChange(DataPointer pointer, Object data) {
		stageChange(pointer, data, null);
	}
	
	/**
	 * Stage a change to be sent to the database later.
	 * @param pointer The type and path of the data
	 * @param data the data.
	 */
	public void stageChange(DataPointer pointer, Object data, DataFileHolder fileHolder) {
		DataValue value = pointer.getValue();
		String[] path = pointer.getPath();
		if (V.debug) {//Console.debug(""); Console.debug("-----------Staging new change-----------"); Console.debug("--| Type: " + value.toString()); Console.debug("--| Path: " + U.arrayToString(pointer.getPath(), " | ")); Console.debug("--| Data: " + String.valueOf(data));
		}
		
		
		changes.put(pointer, new StagedChange(value, data, path, fileHolder));
	}
	
	/**
	 * Checks if there is a data change pending.
	 * @param pointer
	 * @return
	 */
	public boolean isStaged(DataPointer pointer) {
		return changes.containsKey(pointer);
	}
	
	/**
	 * Get all pending changes.
	 * @return All local changes.
	 */
	public Collection<StagedChange> getChanges() {
		return changes.values();
	}
	
	/**
	 * Get a value from the data provider. If there is a local change pending the
	 * value from the local cache will be returned.
	 * @param pointer The pointer that represents the data to be retrieved.
	 * @return The requested data or null if it doesn't exist.
	 */
	public Object getValue(DataPointer pointer) {
		StagedChange change = changes.get(pointer);
		if (change != null) {
			Console.debug("--|> found cached value... Returning local data!");
			return change.getData();
		}
		Console.debug("--|> No local data found, prepare for data retrieval!");
		return getPrimaryDataProvider().getValue(pointer);
	}
	
	/**
	 * Commit all pending changes to the data provider.
	 */
	public void commitAll() {
		executor.submit(() -> {
			executeCommit();
		});
	}
	
	/**
	 * Commit specific pending changes to the data provider if applicable.
	 * @param pointers The pointers representing the data you want to commit.
	 */
	public void commit(DataPointer... pointers) {
		executor.submit(() -> {
			executeCommit(pointers);
		});
	}
	
	/**
	 * Send pending changes to the data provider and remove them from the local cache.
	 * @param pointers The pointers representing the data to send.
	 */
	private void executeCommit(DataPointer... pointers) {
		List<StagedChange> commit = new ArrayList<>();
		
		if (V.debug) {Console.debug("", "-|>>>>> Preparing to execute the commit queue");}
		
		if (pointers != null && pointers.length > 0) {
			for (DataPointer pointer: pointers) {
				if (V.debug) {Console.debug("", "--| Looking for data type:" + pointer.getValue().toString(), "--| Path:" + U.arrayToString(pointer.getPath(), " | "));}
				StagedChange change = changes.get(pointer);
				if (change != null) {
					Console.debug("--|> Found a staged change that matches: data=(" + change.getData() + ")");
					commit.add(change);
					changes.remove(pointer);
					continue;
				}
	 			Console.debug("--|> No changes to save for this type...");
			}
		} else {
			Console.debug("", "--------> DataBridge Commit <--------", "--|>> Adding (ALL) changes to the commit queue");
			commit.addAll(changes.values());
			changes.clear();
		}
		
		
		if (commit.size() == 0) {
			Console.debug(">>>>> No changes to save...", "-----------End commit---------", "");
			return;
		}
		
		if (V.debug) { Console.debug("Preparing to set value for (" + String.valueOf(commit.size()) + ") change" + (commit.size() > 1 ? "s" : "") + " found...");}
		List<DataFileHolder> altered = new ArrayList<>();
		for (StagedChange change: commit) {
			DataFileHolder holder = resources.getConfigProvider().getDataFileHolder(change.getValue().getTable());
			if (!altered.contains(holder)) {
				altered.add(holder);
			}
			try {
				if (holder.forceYaml()) {
					getDataProvider(YamlProvider.class).setValue(change);
				} else {
					primary.setValue(change);
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		for (DataFileHolder holder: altered) {
			if (holder.forceYaml() || primary instanceof YamlProvider) {
				holder.saveData();
			}
		}
		Console.debug("-----------End commit---------", "");
	}
	
	
	/**
	 * Drop ALL changes, resets data back to the original state unless it has been commited. 
	 */
	public void dropChanges() {
		changes.clear();
	}

	@Override @Deprecated
	public File getDataFile() {
		return ((SpigotConfigSection) resources.getConfigProvider().getDefaultConfig()).getFile();
	}

	@Override @Deprecated
	public FileConfiguration getDataConfiguration() {
		return ((SpigotConfigSection) resources.getConfigProvider().getDefaultConfig()).getFileConfiguration();
	}

	@Override @Deprecated
	public void setDataFile(File file) {
		return;
	}

	@Override @Deprecated
	public void setDataConfiguration(FileConfiguration data) {
		return;
	}
	
	@Override @Deprecated
	public void saveData() {
		try { resources.getConfigProvider().getDefaultConfig().saveConfig(); } catch (Exception e) {e.printStackTrace();}
	}

}
