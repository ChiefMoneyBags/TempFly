package com.moneybags.tempfly.util.data.provider;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.data.files.DataFileHolder;
import com.moneybags.tempfly.util.data.files.ResourceProvider;
import com.moneybags.tempfly.util.data.values.DataPointer;
import com.moneybags.tempfly.util.data.values.DataTable;
import com.moneybags.tempfly.util.data.values.DataValue;
import com.moneybags.tempfly.util.data.values.StagedChange;

public class YamlProvider implements DataProvider {

	private ResourceProvider resources;
	
	public YamlProvider(ResourceProvider resources) {
		this.resources = resources;
	}
	
	/**
	 * Get a value from the table
	 * @param value
	 * @param row
	 * @return
	 * @throws SQLException 
	 * @throws DataFormatException
	 */
	public Object getValue(DataPointer pointer) {
		Console.debug("--| Yaml provider fetching value...");
		
		DataValue value = pointer.getValue();
		String[] path = pointer.getPath();
		
		int index = 0;
		StringBuilder sb = new StringBuilder();
		for (String s: value.getYamlPath()) {
			sb.append((sb.length() > 0 ? "." : "") + s);
			if (path.length > index) {
				sb.append("." + path[index]);
			}
			index++;
		}
		
		return getHolder(value.getTable()).getDataConfiguration().get(sb.toString());
	}
	
	public DataFileHolder getHolder(DataTable table) {
		return resources.getConfigProvider().getDataFileHolder(table);
	}
	
	@Deprecated
	public Map<String, Object> getValues(DataTable table, String path, String row) {
		return getValues(table, null, path, row);
	}
	
	/**
	 * Get all values from the table for the given row.
	 * Assumes the row is path to the ConfigurationSection in yaml
	 * @param value
	 * @param row
	 * @return
	 */
	//TODO this system is trash redesign it
	@Deprecated
	public Map<String, Object> getValues(DataTable table, DataFileHolder fileHolder, String path, String identifier) {
		Map<String, Object> values = new HashMap<>();
		FileConfiguration df = fileHolder == null ?
				getHolder(table).getDataConfiguration()
				: fileHolder.getDataConfiguration();
		ConfigurationSection csValues = df.getConfigurationSection(path);
		if (csValues != null) {
			for (String key: csValues.getKeys(false)) {
				values.put(key, df.get(path + "." + key));
			}		
		}
		
		//fix this cast yamlprovider needs isolated from the tempfly plugin
		for (StagedChange local: ((TempFly) resources).getDataBridge().getChanges()) {
			if (local.comparePathPartial(identifier)) {
				values.put(local.getPath()[local.getPath().length-1], local.getData());
			}
		}	
		return values;
	}
	
	public void setValue(StagedChange change) {
		DataValue value = change.getValue();
		String[] path = change.getPath();
		int index = 0;
		StringBuilder sb = new StringBuilder();
		for (String s: value.getYamlPath()) {
			sb.append((sb.length() > 0 ? "." : "") + s);
			if (path.length > index) {
				sb.append("." + path[index]);
			}
			index++;
		}
		FileConfiguration yaml = change.getFileHolder() == null ?
				getHolder(value.getTable()).getDataConfiguration()
				: change.getFileHolder().getDataConfiguration();
		if (!yaml.contains(sb.toString())) {
			yaml.createSection(sb.toString());
		}
		yaml.set(sb.toString(), change.getData());
	}
	
}
