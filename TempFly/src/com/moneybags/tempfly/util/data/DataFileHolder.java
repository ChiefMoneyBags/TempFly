package com.moneybags.tempfly.util.data;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;

public interface DataFileHolder {

	public abstract File getDataFile();
	
	public abstract FileConfiguration getDataConfiguration();
	
	public abstract void setDataFile(File file);
	
	public abstract void setDataConfiguration(FileConfiguration data);
	
	public abstract void saveData();
	
	public default boolean forceYaml() {
		return false;
	}
}
