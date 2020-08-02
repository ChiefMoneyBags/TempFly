package com.moneybags.tempfly.util.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.DataFormatException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.moneybags.tempfly.hook.HookManager.Genre;
import com.moneybags.tempfly.hook.HookManager.HookType;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;


public class DataBridge extends Thread {

	private TempFly tempfly;
	private Connection connection;
	
	private static File dataf;
	private static FileConfiguration data;
	
	private static Map<HookType, File> hookFiles = new HashMap<>();
	private static Map<HookType, FileConfiguration> hookData = new HashMap<>();
	
	// Staged changes are held in memory until either the autosave runs, or they are forcefully committed.
	private List<StagedChange> changes = new CopyOnWriteArrayList<>();
	// A list of pointers that tell the asynchronous data handler to manually save the data they point to
	// if it exists but wont commit all changes that have been made. 
	private List<DataPointer> manualCommit = new CopyOnWriteArrayList<>();
	
	public DataBridge(TempFly tempfly) {
		this.tempfly = tempfly;
		if (Files.config.getBoolean("system.sql.enabled")) {
			String
			host = Files.config.getString("system.sql.host"),
			port = Files.config.getString("system.sql.port"),
			name = Files.config.getString("system.sql.name"),
			user = Files.config.getString("system.sql.user"),
			pass = Files.config.getString("system.sql.pass");
			try {
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection(
						"jdbc:mysql://" + host + ":"+ port + "/" + name, user, pass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// If connection is null we will default to yaml storage.
		if (connection == null) {
			dataf = new File(tempfly.getDataFolder(), "data.yml");
		    if (!dataf.exists()){
		    	dataf.getParentFile().mkdirs();
		    	tempfly.saveResource("data.yml", false);
		    }
		    data = new YamlConfiguration();
		    try { data.load(dataf); } catch (Exception e1) {
		    	Console.severe("There is a problem inside the data.yml, If you cannot fix the issue, please contact the developer.");
		        e1.printStackTrace();
		    }
		    formatYamlData(tempfly);
		}
		this.start();
	}
	
	
	/**
	 * format the data file from legacy TempFly version.
	 * @param plugin
	 */
	private void formatYamlData(TempFly plugin) {
		double version = data.getDouble("version", 0.0);
		if (version < 2.0) {
			Console.warn("Your data file needs to update to support the current version. Updating to version 2.0 now...");
			if (!backupLegacyData("update_2_backup_")) {
				Bukkit.getPluginManager().disablePlugin(plugin);
				return;
			}
			
			data.set("version", 2.0);
			ConfigurationSection csPlayers = data.getConfigurationSection("players");
			if (csPlayers != null) {
				Map<String, Double> time = new HashMap<>();
				for (String key: csPlayers.getKeys(false)) {
					time.put(key, data.getDouble("players." + key));
				}
				for (Entry<String, Double> entry: time.entrySet()) {
					String uuid = entry.getKey();
					double value = entry.getValue();
					data.set("players." + uuid + ".time", value);
					data.set("players." + uuid + ".logged_in_flight", false);
					data.set("players." + uuid + ".trail", "");
				}	
			}
			List<String> disco = data.getStringList("flight_disconnect");
			if (disco != null) {
				for (String uuid: disco) {
					data.set("players." + uuid + ".logged_in_flight", true);
				}
			}
			data.set("flight_disconnect", null);
			saveData();
			
		} else if (version < 3.0) {
			Console.warn("");
			Console.warn("This tempfly version has a new data management system, (data.yml) will be backed for your safety.");
			Console.warn("");
			if (!backupLegacyData("update_3_backup_")) {
				Bukkit.getPluginManager().disablePlugin(plugin);
				return;
			}
			data.set("version", 3.0);
			saveData();
		}
	}
	
	/**
	 * Create a data backup from legacy TempFly version when updating.
	 * @return
	 */
	private boolean backupLegacyData(String file) {
		Console.info("Creating a backup of your data file...");
		File f = new File(tempfly.getDataFolder(), file + String.valueOf(new Random().nextInt(99999)) + ".yml");
		try {
			data.save(f);
		} catch (Exception e) {
			Console.severe(U.cc("&c-----------------------------------"));
			Console.severe("There was an error while trying to backup the data file");
			Console.severe("For your safety the plugin will disable. Please contact the tempfly developer.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void saveData() {
		try { data.save(dataf); } catch (Exception e) { e.printStackTrace(); };
	}
	
	public void initializeHookData(TempFlyHook hook, TempFly plugin, DataTable table) throws IOException, InvalidConfigurationException, SQLException {
		HookType hookType = hook.getHookType();
		String target = hook.getHookedPlugin();
		if (connection == null) {
			File hookDataf = new File(hookType.getGenre().getDirectory(), target + "_data.yml");
		    if (!hookDataf.exists()) {
		    	hookDataf.getParentFile().mkdirs();
		    	hookDataf.createNewFile();
		    }
		    FileConfiguration hookData = new YamlConfiguration();
		    hookData.load(hookDataf);	
		} else {
			/**
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet results = meta.getTables(null, null, table.getSqlTable(), null);
			if (!results.next()) {
				PreparedStatement statement = connection.prepareStatement("create table if not exists " + table.getSqlTable());
				statement.executeQuery();
			}
			*/
		}
	}
	
	/**
	 * Stage a change to be sent to the database later.
	 * @param pointer The type and path of the data
	 * @param data the data.
	 */
	public synchronized void stageChange(DataPointer pointer, Object data) {
		DataValue value = pointer.getValue();
		String[] path = pointer.getPath();
		if (V.debug) {
			Console.debug(""); Console.debug("-----------Staging new change-----------"); Console.debug("--| Type: " + value.toString()); Console.debug("--| Path: " + U.arrayToString(pointer.getPath(), " | ")); Console.debug("--| Data: " + String.valueOf(data));
		}
		for (StagedChange change: changes) {
			if (change.isDuplicate(value, path)) {
				Console.debug("--|> Data already exists for this type, overwriting it...");
				changes.remove(change);
			}
		}
		changes.add(new StagedChange(value, data, path));
		Console.debug("-----------End of stage-----------");
	}

	/**
	 * Commit all changes to the database or yaml if applicable.
	 * Adds all the stagedchanges to the manual batch and runs the async batch collector.
	 */
	public void commitAll() {
		Console.debug("");
		Console.debug("--------> DataBridge Commit <--------");
		Console.debug("--|>> Adding (ALL) changes to the commit queue");
		synchronized (this) {
			manualCommit.clear();
			for (StagedChange change: changes) {
				manualCommit.add(change.getPointer());
			}
			this.notifyAll();
		}
	}
	
	/**
	 * An infinitely looping async thread that commits any data in the manual commit queue.
	 * Thread waits until notified by one of the manual commit methods that there is data to collect,
	 * it then collects the data in batches, pulls it out of the sync thread and commits it to the database.
	 */
	@Override
	public void run() {
		while(true) {
			synchronized (this) {
				if (manualCommit.size() == 0) {
					try {wait();} catch (InterruptedException e) {e.printStackTrace();}
				}
				executeCommit();
			}
		}
	}
	
	/**
	 * Collects StagedChanges using the pointers collected in the manual batch and sends data to the database.
	 */
	private void executeCommit() {
		List<StagedChange> commit = new ArrayList<>();
		synchronized (this) {
			if (V.debug) {
			Console.debug(""); Console.debug("-|>>>>> Preparing to execute the commit queue");
			}
			Iterator<DataPointer> itPointer = manualCommit.iterator();
			pointers:
			while (itPointer.hasNext()) {
				DataPointer pointer = itPointer.next();
 				if (V.debug) {
 					Console.debug(""); Console.debug("--| Looking for data type:" + pointer.getValue().toString()); Console.debug("--| Path:" + U.arrayToString(pointer.getPath(), " | "));
 				}
	 			for (StagedChange change: changes) {
	 				if (change.isDuplicate(pointer)) {
						Console.debug("--|> Found a staged change that matches: data=(" + change.getData() + ")");
						commit.add(change);
						changes.remove(change);
						continue pointers;
					}
				}
	 			Console.debug("--|> No changes to save for this type...");
			}
			manualCommit.clear();
		}
		
		if (commit.size() == 0 && V.debug) {Console.debug(">>>>> No changes to save..."); Console.debug("-----------End commit---------"); Console.debug("");
			return;
		}
		
		if (V.debug) Console.debug("Preparing to set value for (" + String.valueOf(commit.size()) + ") change" + (commit.size() > 1 ? "s" : "") + " found...");
		for (StagedChange change: commit) {
			setValue(change);
			saveData();
		}
		Console.debug("-----------End commit---------");
		Console.debug("");
	}
	
	/**
	 * Manually add data pointers to the next manual commit and run the async batch collector.
	 * @param pointers
	 */
	public void manualCommit(DataPointer... pointers) {
		synchronized (this) {
			manualCommit.addAll(Arrays.asList(pointers));
			this.notifyAll();
		}
	}
	
	/**
	 * Drop ALL changes, resets data back to the original state unless it has been commited. 
	 */
	public synchronized void dropChanges() {
		changes.clear();
		manualCommit.clear();
	}
	
	/**
	 * Get a value from the table
	 * @param value
	 * @param row
	 * @return
	 * @throws DataFormatException
	 */
	public Object getValue(DataPointer pointer) {
		DataValue value = pointer.getValue();
		String[] path = pointer.getPath();
		if (V.debug) {
			Console.debug(""); Console.debug("-----Data Bridge Get Value-----"); Console.debug("--| Type: " + value.toString()); Console.debug("--| Path: " + U.arrayToString(pointer.getPath(), " | "));	
		}
		synchronized (this) {
			Console.debug("--| Iterating local staged changes");
			for (StagedChange change: changes) {
				if (change.isDuplicate(value, path)) {
					Console.debug("--|> found duplicate... Returning local data!");
					return change.getData();
				}
			}
			Console.debug("--|> No local data found, prepare for data retrieval!");
		}
		
		if (connection == null) {
			Console.debug("--| Using YAML");
			int index = 0;
			StringBuilder sb = new StringBuilder();
			for (String s: value.getYamlPath()) {
				sb.append((sb.length() > 0 ? "." : "") + s);
				if (path.length > index) {
					sb.append("." + path[index]);
				}
				index++;
			}
			return value.getTable().getYaml().get(sb.toString());
		} else {
			Console.debug("--| Using SQL");
			//TODO sql
		}
		return null;
	}
	
	public Object getOrDefault(DataPointer pointer, Object def) {
		Object object = getValue(pointer);
		if (V.debug) {
			Console.debug(""); Console.debug("-----Data Bridge Get or Default Value-----"); Console.debug("--|> Got: " + object); Console.debug("--|> Returning: " + String.valueOf(object == null ? def : object));
		}
		return object == null ? def : object;
	}
	
	/**
	 * Get all values from the table for the given row.
	 * Assumes the row is path to the ConfigurationSection in yaml
	 * @param value
	 * @param row
	 * @return
	 */
	public Map<String, Object> getValues(DataTable table, String column, String row) {
		Map<String, Object> values = new HashMap<>();
		if (connection == null) {
			FileConfiguration df = table.getYaml();
			try {
				ConfigurationSection csValues = df.getConfigurationSection(column + "." + row);
				for (String key: csValues.getKeys(false)) {
					values.put(key, df.getObject(row + "." + key, Object.class));
				}
			} catch (NullPointerException e) {}
		} else {
			/**
			try {
				
				//TODO PreparedStatement statement = connection.prepareStatement("select * from " + table.getSqlTable() + " where " + column + " = " + row);
				ResultSet rs = statement.executeQuery();
				ResultSetMetaData meta = rs.getMetaData();
				Map<Integer, String> columns = new HashMap<>();
				for (int index = 1; index < meta.getColumnCount(); index++) {
					columns.put(index, meta.getColumnName(index));
				}
				
				int index = 0;
				while (rs.next()) {
					index++;
					values.put(columns.get(index), rs.getObject(index));
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
			*/
		}
		return values;
	}
	
	public void setValue(StagedChange change) {
		DataValue value = change.getValue();
		String[] path = change.getPath();
		if (V.debug) {
			Console.debug(""); Console.debug("-----Data Bridge Set Value-----"); Console.debug("--| Type: " + value.toString()); Console.debug("--| Path: " + U.arrayToString(path, " | "));	
		}
		if (connection == null) {
			int index = 0;
			StringBuilder sb = new StringBuilder();
			for (String s: value.getYamlPath()) {
				sb.append((sb.length() > 0 ? "." : "") + s);
				if (path.length > index) {
					sb.append("." + path[index]);
				}
				index++;
			}
			if (V.debug) {
				Console.debug("--| Setting yaml value: " + sb.toString());
				Console.debug("--| New data: " + String.valueOf(change.getData()));
			}
			FileConfiguration yaml = value.getTable().getYaml();
			if (!yaml.contains(sb.toString())) {
				yaml.createSection(sb.toString());
			}
			yaml.set(sb.toString(), change.getData());
		} else {
			//TODO sql
		}
	}

	
	public static enum DataTable {
		TEMPFLY_DATA,
		ISLAND_SETTINGS;
		
		public FileConfiguration getYaml() {
			switch (this) {
			case TEMPFLY_DATA:
				return data;
			case ISLAND_SETTINGS:
				for (Entry<HookType, FileConfiguration> entry: hookData.entrySet()) {
					if (entry.getKey().getGenre() == Genre.SKYBLOCK) {
						return entry.getValue();
					}
				}
			default:
				return null;
			}
		}
		
		public File getFile() {
			switch (this) {
			case TEMPFLY_DATA:
				return dataf;
			case ISLAND_SETTINGS:
				for (Entry<HookType, File> entry: hookFiles.entrySet()) {
					if (entry.getKey().getGenre() == Genre.SKYBLOCK) {
						return entry.getValue();
					}
				}
			default:
				return null;
			}
		}
		
		public String getSqlTable() {
			switch (this) {
			case TEMPFLY_DATA:
				return "TEMPFLY_DATA";
			case ISLAND_SETTINGS:
				//return tempfly.getHookManager().getGenre(Genre.SKYBLOCK)[0].getHookedPlugin() + "_island_settings";
			default:
				return null;
			}
		}
	}
	
	public static enum DataValue {
		PLAYER_TIME(
				DataTable.TEMPFLY_DATA,
				Long.TYPE,
				"time",
				new String[] {"players", "time"},
				false),
		PLAYER_FLIGHT_LOG(
				DataTable.TEMPFLY_DATA,
				Boolean.TYPE,
				"logged_in_flight",
				new String[] {"players", "logged_in_flight"},
				false),
		PLAYER_DAMAGE_PROTECTION(
				DataTable.TEMPFLY_DATA,
				Boolean.TYPE,
				"damage_protection",
				new String[] {"players", "damage_protection"},
				false),
		PLAYER_DAILY_BONUS(
				DataTable.TEMPFLY_DATA,
				Long.TYPE,
				"last_daily_bonus",
				new String[] {"players", "last_daily_bonus"},
				false),
		PLAYER_TRAIL(
				DataTable.TEMPFLY_DATA,
				String.class,
				"trail",
				new String[] {"players", "trail"},
				false),
		
		
		
		
		ISLAND_SETTING(
				DataTable.ISLAND_SETTINGS,
				Boolean.TYPE,
				null,
				null,
				true);
		
		private DataTable table;
		private Class<?> type;
		private String
		sqlColumn;
		
		private String[]
		yamlPath;
		private boolean dynamic;
		
		private DataValue(DataTable table, Class<?> type, String sqlColumn, String[] yamlPath, boolean dynamic) {
			this.table = table;
			this.type = type;
			this.sqlColumn = sqlColumn;
			this.yamlPath = yamlPath;
			this.dynamic = dynamic;
		}
		
		public DataTable getTable() {
			return table;
		}
		
		public Class<?> getType() {
			return type;
		}
		
		public String getSqlColumn() {
			return sqlColumn;
		}
		
		public String[] getYamlPath() {
			return yamlPath;
		}
		
		public boolean hasDynamicPath() {
			return dynamic;
		}
	}
	
	protected class StagedChange {
		DataValue value;
		String[] path;
		Object data;
		
		public StagedChange(DataValue value, Object data, String[] path) {
			this.value = value;
			this.path = path;
			this.data = data;
		}

		public DataPointer getPointer() {
			return DataPointer.of(value, path);
		}

		public DataValue getValue() {
			return value;
		}
		
		public String[] getPath() {
			return path;
		}
		
		public Object getData() {
			return data;
		}
		
		public boolean isDuplicate(DataPointer pointer) {
			return isDuplicate(pointer.getValue(), pointer.getPath());
		}
		
		public boolean isDuplicate(DataValue value, String[] path) {
			if (!value.equals(this.value) || path.length != this.path.length) {
				return false;
			}
			for (int index = 0; path.length > index && this.path.length > index; index++) {
				if (!path[index].equals(this.path[index])) {
					return false;
				}
			}
			return true;
		}
	}

}
