package com.moneybags.tempfly.util.data.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.bukkit.configuration.InvalidConfigurationException;

public interface Config {
	
	ConfigSection getConfigSection(String path);
	
	ConfigSection getRootSection();
	
	public void reloadConfig() throws FileNotFoundException, IOException, InvalidConfigurationException;
	
	public void saveConfig() throws IOException;
	
}
