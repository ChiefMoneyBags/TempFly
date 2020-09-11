package com.moneybags.tempfly.util.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.moneybags.tempfly.util.Console;

/**
 * This class is going to be reworked in a future version. Too much static abuse and repetition.
 *
 */
public class Files {

	public static enum C {
		CONFIG,
		LANG,
		DATA,
		PAGE;
	}
	
	private static File
	configf,
	langf,
	pagef;
	
	public static FileConfiguration
	config,
	lang,
	page;
	
	public static void createFiles(Plugin plugin){
	    configf = new File(plugin.getDataFolder(), "config.yml");
	    langf = new File(plugin.getDataFolder(), "lang.yml");
	    pagef = new File(plugin.getDataFolder(), "page.yml");
	    
	    if (!configf.exists()){
	    	configf.getParentFile().mkdirs();
	        plugin.saveResource("config.yml", false);
	    }
	    if (!langf.exists()){
	    	langf.getParentFile().mkdirs();
	        plugin.saveResource("lang.yml", false);
	    }
	    if (!pagef.exists()){
	    	pagef.getParentFile().mkdirs();
	        plugin.saveResource("page.yml", false);
	    }
	    
	    config = new YamlConfiguration();
	    lang = new YamlConfiguration();
	    page = new YamlConfiguration();
	    
	    try {
	        config.load(configf);
	    } catch (IOException | InvalidConfigurationException e1){
	    	Console.severe("There is a problem inside the config.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	    }
	    try {
	        lang.load(langf);
	    } catch (IOException | InvalidConfigurationException e1){
	    	Console.severe("There is a problem inside the lang.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	    }
	    try {
	        page.load(pagef);
	    } catch (IOException | InvalidConfigurationException e1){
	    	Console.severe("There is a problem inside the page.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	    }
	}
	
	
	public static void createConfig(InputStream stream, File file) throws IOException {
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);
		OutputStream outStream = new FileOutputStream(file);
		outStream.write(buffer);
		outStream.close();
	}
	
}
