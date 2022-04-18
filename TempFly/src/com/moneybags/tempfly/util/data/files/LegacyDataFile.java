package com.moneybags.tempfly.util.data.files;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.data.DataBridge;

/**
 * Update the data files from legacy tempfly versions.
 * @author Kevin
 *
 */
public class LegacyDataFile {
	
	/**
	 * format the data file from legacy TempFly version.
	 * @param plugin
	 */
	@SuppressWarnings("deprecation")
	public static void formatYamlData(TempFly tempfly, DataBridge bridge, FileConfiguration data) {
		double version = data.getDouble("version", 0.0);
		if (version < 2.0) {
			Console.warn("Your data file needs to update to support the current version. Updating to version 2.0 now...");
			if (!backupLegacyData(tempfly, bridge, data,"update_2_backup_")) {
				Bukkit.getPluginManager().disablePlugin(tempfly);
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
			bridge.saveData();
			
		} else if (version < 3.0) {
			Console.warn("", "This tempfly version has a new data management system, (data.yml) will be backed for your safety.", "");
			if (!backupLegacyData(tempfly, bridge, data,"update_3_backup_")) {
				Bukkit.getPluginManager().disablePlugin(tempfly);
				return;
			}
			data.set("version", 3.0);
			bridge.saveData();
		} else if (version < 4.0) {
			if (!backupLegacyData(tempfly, bridge, data, "update_4_backup_")) {
				Bukkit.getPluginManager().disablePlugin(tempfly);
				return;
			}
			data.set("version", 4.0);
			bridge.saveData();
		}
	}
	
	/**
	 * Create a data backup from legacy TempFly version when updating.
	 * @return
	 */
	protected static boolean backupLegacyData(TempFly tempfly, DataBridge bridge, FileConfiguration data, String file) {
		Console.info("Creating a backup of your data file...");
		File f = new File(tempfly.getDataFolder(), file + String.valueOf(new Random().nextInt(99999)) + ".yml");
		try {
			data.save(f);
		} catch (Exception e) {
			Console.severe("-----------------------------------", "There was an error while trying to backup the data file", "For your safety the plugin will disable. Please contact the tempfly developer.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
