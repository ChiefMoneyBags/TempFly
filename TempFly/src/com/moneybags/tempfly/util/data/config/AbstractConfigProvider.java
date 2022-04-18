package com.moneybags.tempfly.util.data.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.moneybags.tempfly.util.data.files.DataFileHolder;
import com.moneybags.tempfly.util.data.files.ResourceProvider;
import com.moneybags.tempfly.util.data.values.DataTable;

public abstract class AbstractConfigProvider implements ConfigProvider {
	
	private ResourceProvider resources;
	private Map<DataTable, DataFileHolder> dataFiles = new HashMap<>();
	protected Map<String, Config> configs = new HashMap<>();
	
	public AbstractConfigProvider(ResourceProvider resources) {
		this.resources = resources;
	}

	@Override
	public ConfigSection getConfig(String name) {
		Config conf = configs.get(name);
		if (conf == null) {
			conf = configs.get(name + ".yml");
		}
		return conf == null ? null : conf.getRootSection();
	}
	
	@Override
	public Collection<Config> getConfigs() {
		return configs.values();
	}
	
	@Override
	public ResourceProvider getResources() {
		return resources;
	}
	
	@Override
	public void registerDataFileHolder(DataFileHolder holder, DataTable table) {
		dataFiles.put(table, holder);
	}

	@Override
	public DataFileHolder getDataFileHolder(DataTable table) {
		return dataFiles.get(table);
	}
	
	
}
