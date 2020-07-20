package moneybags.tempfly.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import moneybags.tempfly.TempFly;

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
	    formatDataFile();
	}
	
	public static void saveData() {
		try {
			data.save(dataf);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void createConfig(InputStream stream, File file) throws IOException {
		byte[] buffer = new byte[stream.available()];
		stream.read(buffer);
		OutputStream outStream = new FileOutputStream(file);
		outStream.write(buffer);
	}
	
	private static void formatDataFile() {
		double version = data.getDouble("version", 0.0);
		if (version < 2.0) {
			U.logW("Your data file needs to update to support the current version. Updating to version 2.0 now...");
			if (!createBackup()) {
				Bukkit.getPluginManager().disablePlugin(TempFly.plugin);
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
		}
	}
	
	private static boolean createBackup() {
		U.logI("Creating a backup of your data file...");
		File f = new File(TempFly.plugin.getDataFolder(), "data_backup_" + UUID.randomUUID().toString() + ".yml");
		try {
			data.save(f);
		} catch (Exception e) {
			U.logS(U.cc("&c-----------------------------------"));
			U.logS("There was an error while trying to backup the data file");
			U.logS("For your safety the plugin will disable. Please contact the developer.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
