package moneybags.tempfly.hook.skyblock;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

public class IslandSettings {

	private SkyblockHook hook;
	private IslandWrapper island;
	
	private Map<String, Boolean> settings = new HashMap<>();
	
	public IslandSettings(IslandWrapper island, SkyblockHook hook) {
		FileConfiguration data = hook.getData();
		String
			id = island.getIdentifier(),
			path = "islands." + id;
		
		if (!data.contains(path)) {
			data.createSection(path);
			for (Entry<String, Boolean> entry: hook.getDefaults().entrySet()) {
				data.set(path + "." + entry.getKey(), entry.getValue());
			}
			hook.saveData();
		} else {
			for (String key: data.getConfigurationSection(path).getKeys(false)) {
				settings.put(key, data.getBoolean(path + ".key"));
			}
		}
	}
	
	public boolean canFly(String status) {
		return settings.get(status);
	}
	
	public void setCanFly(String status, boolean canFly) {
		settings.put(status, canFly);
		saveSettings();
	}
	
	public void toggleCanFly(String rank) {
		settings.put(rank, !settings.get(rank));
	}
	
	public Set<Entry<String, Boolean>> getCurrentState() {
		return settings.entrySet();
	}
	
	public IslandWrapper getIsland() {
		return island;
	}
	
	public void saveSettings() {
		FileConfiguration data = hook.getData();
		String
			id = island.getIdentifier(),
			path = "islands." + id;
		for (Entry<String, Boolean> entry: settings.entrySet()) {
			data.set(path + "." + entry.getKey(), entry.getValue());
		}
		hook.saveData();
	}
	
}
