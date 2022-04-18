package com.moneybags.tempfly.util.data.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.bukkit.configuration.InvalidConfigurationException;

import com.moneybags.tempfly.util.data.files.DataFileHolder;
import com.moneybags.tempfly.util.data.files.ResourceProvider;
import com.moneybags.tempfly.util.data.values.DataTable;

public interface ConfigProvider {

	ResourceProvider getResources();
	
	default ConfigSection getDefaultConfig() {
		return getConfig("config.yml");
	}
	
	ConfigSection getConfig(String name);
	
	void loadConfig(String name) throws IOException;
	
	default void loadConfigs(String... names) throws IOException {
		for (String name: names) {
			loadConfig(name);
		}
	}
	
	void registerDataFileHolder(DataFileHolder holder, DataTable table);
	
	DataFileHolder getDataFileHolder(DataTable table);
	
	default void reload() throws FileNotFoundException, IOException, InvalidConfigurationException {
		for (Config config: getConfigs()) {
			config.reloadConfig();
		}
	}
	
	Collection<Config> getConfigs();
	
	public default void createConfigFile(InputStream stream, File file) throws IOException {
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);
		OutputStream outStream = new FileOutputStream(file);
		outStream.write(buffer);
		outStream.close();
	}

}
