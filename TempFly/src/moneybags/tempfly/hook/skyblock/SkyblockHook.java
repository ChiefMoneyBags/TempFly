package moneybags.tempfly.hook.skyblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.hook.HookManager.HookType;
import moneybags.tempfly.hook.TempFlyHook;
import moneybags.tempfly.hook.skyblock.plugins.AskyblockHook.RequirementType;
import moneybags.tempfly.util.Console;
import moneybags.tempfly.util.V;

public abstract class SkyblockHook extends TempFlyHook {

	private Map<String, Boolean> basePerms = new HashMap<>();
	private boolean wilderness;
	public String
	requireIsland,
	requireLevelSelf,
	requireLevelOther,
	requireChallenge;
	
	private Map<RequirementType, SkyblockRequirement[]> requirements = new HashMap<>();
	
	public SkyblockHook(HookType hookType, TempFly plugin) {
		super(hookType, plugin);
		if (!super.isEnabled()) {
			return;
		}
		loadValues();
		PageIslandSettings.initialize(this);
	}
	
	private void loadValues() {
		FileConfiguration config = getConfig();
		this.wilderness = config.getBoolean("flight_settings.wilderness");
		
		String pathPerms = "flight_settings.base_permissions";
		ConfigurationSection csPerms = config.getConfigurationSection(pathPerms);
		if (csPerms != null) {
			for (String key: csPerms.getKeys(false)) {
				basePerms.put(key, config.getBoolean(pathPerms + "." + key));
			}	
		}
		
		requireIsland		= V.st(config, "language.invalid.island");
		requireChallenge	= V.st(config, "language.requirements.challenge");
		requireLevelSelf	= V.st(config, "language.requirements.level_self");
		requireLevelOther	= V.st(config, "language.requirements.level_other");
		
		ConfigurationSection csRequire = config.getConfigurationSection("unlockables");
		if (csRequire != null) {
			for (String require : csRequire.getKeys(false)) {
				RequirementType rt = null;
				try {RequirementType.valueOf(require.toUpperCase());} catch (Exception e) {
					Console.warn("An unlockable flight area set for (" + getHookedPlugin() + ") does not exist: " + require);
					continue;
				}
				
				List<SkyblockRequirement> list = new ArrayList<>();
				if (rt == RequirementType.REGION || rt == RequirementType.WORLD) {
					ConfigurationSection csWR = config.getConfigurationSection("unlockables." + require);
					if (csWR != null) {
						for (String name: csWR.getKeys(false)) {
							String pathRegions = "unlockables." + require + "." + name;
							list.add(new SkyblockRequirement(config.getStringList(pathRegions + ".challenges"), config.getLong(pathRegions + ".island_level", 0), name, rt));
						}
					}
				} else {
					String pathRequire = "unlockables." + require;
					list.add(new SkyblockRequirement(config.getStringList(pathRequire + ".challenges"), config.getLong(pathRequire + ".island_level", 0), rt));
				}
				requirements.put(rt, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
	}
	
	/**
	 * 
	 * Global Values
	 * 
	 */
	
	
	public boolean canFlyWilderness() {
		return wilderness;
	}
	
	public Map<String, Boolean> getDefaults() {
		return basePerms;
	}
	
	public SkyblockRequirement[] getRequirements(RequirementType type) {
		return requirements.get(type);
	}
	
	public boolean hasRequirement(RequirementType type) {
		return requirements.containsKey(type);
	}
	
	
	/**
	 * 
	 * Island Settings
	 * 
	 */
	
	
	private Map<Player, IslandWrapper> locationCache = new HashMap<>();
	private Map<Object, IslandWrapper> wrapperCache = new HashMap<>();
	
	public IslandWrapper getIslandWrapper(Object rawIsland) {
		if (wrapperCache.containsKey(rawIsland)) { return wrapperCache.get(rawIsland); }
		return new IslandWrapper(getHookType(), rawIsland, this);
	}
	
	public void onIslandEnter(Player p, Object rawIsland) {
		// This should never happen if the events are called in proper order.
		// I am leaving this here instead of calling onExit just in case it is used somewhere else and i don't want to invoke it for this.
		if (locationCache.containsKey(p)) {
			if (locationCache.get(p).getIsland() == rawIsland) {
				return;
			}
			Console.severe("If you are seeing this message there may be a bug. Please contact the tempfly dev with this info: SkyblockHook(line: 127)");
				IslandWrapper island = locationCache.get(p);
				locationCache.remove(p);
				if(!locationCache.containsValue(island)) {
					wrapperCache.remove(rawIsland);
				}
		}
		
		IslandWrapper island = getIslandWrapper(rawIsland);
		locationCache.put(p, island);
	}
	
	public void onIslandExit(Player p, Object rawIsland) {
		if (locationCache.containsKey(p)) {
			IslandWrapper island = locationCache.get(p);
			locationCache.remove(p);
			if(!locationCache.containsValue(island)) {
				wrapperCache.remove(rawIsland);
			}
		}
	}
	
	public abstract IslandWrapper getIslandOwnedBy(UUID id);
	
	public abstract IslandWrapper getIslandAt(Location loc);

	public abstract boolean isChallengeCompleted(UUID id, String challenge);
}
