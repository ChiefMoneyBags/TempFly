package com.moneybags.tempfly.util;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class F {

	public static enum C {
		CONFIG,
		LANG,
		DATA,
		PAGE;
	}
	
	private static File
	configf,
	langf,
	dataf,
	pagef;
	
	public static FileConfiguration
	config,
	lang,
	data,
	page;
	
	public static void createFiles(Plugin plugin){
	    configf = new File(plugin.getDataFolder(), "config.yml");
	    langf = new File(plugin.getDataFolder(), "lang.yml");
	    dataf = new File(plugin.getDataFolder(), "data.yml");
	    pagef = new File(plugin.getDataFolder(), "page.yml");
	    
	    if (!configf.exists()){
	    	configf.getParentFile().mkdirs();
	        plugin.saveResource("config.yml", false);
	    }
	    if (!langf.exists()){
	    	langf.getParentFile().mkdirs();
	        plugin.saveResource("lang.yml", false);
	    }
	    if (!dataf.exists()){
	    	dataf.getParentFile().mkdirs();
	        plugin.saveResource("data.yml", false);
	    }
	    if (!pagef.exists()){
	    	pagef.getParentFile().mkdirs();
	        plugin.saveResource("page.yml", false);
	    }
	    
	    config = new YamlConfiguration();
	    lang = new YamlConfiguration();
	    data = new YamlConfiguration();
	    page = new YamlConfiguration();
	    
	    try {
	        config.load(configf);
	    } catch (IOException | InvalidConfigurationException e1){
	    	U.logS("There is a problem inside the config.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	    }
	    try {
	        lang.load(langf);
	    } catch (IOException | InvalidConfigurationException e1){
	    	U.logS("There is a problem inside the lang.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	    }
	    try {
	        data.load(dataf);
	    } catch (IOException | InvalidConfigurationException e1){
	    	U.logS("There is a problem inside the data.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	    }
	    try {
	        page.load(pagef);
	    } catch (IOException | InvalidConfigurationException e1){
	    	U.logS("There is a problem inside the page.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	    }
	}
	
	public static void saveData() {
		try {
			data.save(dataf);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
}
