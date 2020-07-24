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
import moneybags.tempfly.hook.region.CompatRegion;
import moneybags.tempfly.util.Console;
import moneybags.tempfly.util.V;

public abstract class SkyblockHook extends TempFlyHook {

	private Map<String, Boolean> basePerms = new HashMap<>();
	private boolean wilderness;
	public String
	requireIsland,
	requireLevelSelf,
	requireLevelOther,
	requireChallengeSelf,
	requireChallengeOther;
	
	private Map<SkyblockRequirementType, SkyblockRequirement[]> requirements = new HashMap<>();
	
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
		String name = getHookType().getConfigName();
		requireIsland			= V.st(config, "language.invalid.island", name);
		requireChallengeSelf	= V.st(config, "language.requirements.challenge_self", name);
		requireChallengeOther	= V.st(config, "language.requirements.challenge_other", name);
		requireLevelSelf		= V.st(config, "language.requirements.level_self", name);
		requireLevelOther		= V.st(config, "language.requirements.level_other", name);
		
		String path;
		if (config.contains(path = "unlockables.environment.wilderness")) {
			requirements.put(SkyblockRequirementType.WILDERNESS, new SkyblockRequirement[] {
					new SkyblockRequirement(
							config.getStringList(path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							null, SkyblockRequirementType.WILDERNESS)
			});
		}
		
		ConfigurationSection csRequireWorld = config.getConfigurationSection("unlockables.environment.worlds");
		if (csRequireWorld != null) {
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String world: csRequireWorld.getKeys(false)) {
				path = "unlockables.environment.worlds." + world;
				list.add(new SkyblockRequirement(
							config.getStringList(path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							world, SkyblockRequirementType.WORLD));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.WORLD, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		
		ConfigurationSection csRequireRegion = config.getConfigurationSection("unlockables.environment.regions");
		if (csRequireRegion != null) {
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String region: csRequireRegion.getKeys(false)) {
				path = "unlockables.environment.worlds." + region;
				list.add(new SkyblockRequirement(
							config.getStringList(path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							region, SkyblockRequirementType.REGION));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.REGION, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		
		ConfigurationSection csRequireRole = config.getConfigurationSection("unlockables.island_roles");
		if (csRequireRole != null) {
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String role: csRequireRole.getKeys(false)) {
				if (!islandRoleExists(role)) {
					Console.severe("An island role specified in the config does not exist (" + role + "). Skipping...");
				}
				path = "unlockables.environment.worlds." + role;
				list.add(new SkyblockRequirement(
							config.getStringList(path + ".challenges"), config.getStringList(path + ".owner_challenges"),
							config.getLong(path + ".island_level"), config.getLong(path + ".owner_level"),
							role, SkyblockRequirementType.ISLAND_ROLE));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.ISLAND_ROLE, list.toArray(new SkyblockRequirement[list.size()]));
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
	
	public boolean hasRequirement(SkyblockRequirementType type) {
		return requirements.containsKey(type);
	}
	
	public SkyblockRequirement[] getRequirements(SkyblockRequirementType type) {
		return requirements.get(type);
	}
	
	public SkyblockRequirement[] getRequirements(CompatRegion[] regions) {
		List<SkyblockRequirement> found = new ArrayList<>();
		SkyblockRequirement[] iter = getRequirements(SkyblockRequirementType.REGION);
		for (CompatRegion region: regions) {
			for (SkyblockRequirement require: iter) {
				if (region.getId().equals(require.getName())) {
					found.add(require);
				}
			}
		}
		return found.toArray(new SkyblockRequirement[found.size()]);
	}
	
	public boolean hasRequirement(SkyblockRequirementType type, String name) {
		if (!hasRequirement(type)) {
			return false;
		}
		for (SkyblockRequirement require: getRequirements(type)) {
			if (require.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public SkyblockRequirement getRequirement(SkyblockRequirementType type, String name) {
		for (SkyblockRequirement require: getRequirements(type)) {
			if (require.getName().equals(name)) {
				return require;
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * Island Settings
	 * 
	 */
	
	
	private Map<Player, IslandWrapper> locationCache = new HashMap<>();
	private Map<Object, IslandWrapper> wrapperCache = new HashMap<>();
	
	public IslandWrapper getIslandWrapper(Object rawIsland) {
		if (rawIsland == null) {
			return null;
		}
		return wrapperCache.containsKey(rawIsland) ? wrapperCache.get(rawIsland) : new IslandWrapper(getHookType(), rawIsland, this);
	}
	
	public void onIslandEnter(Player p, Object rawIsland) {
		if (locationCache.containsKey(p)) {
			if (locationCache.get(p).getIsland() == rawIsland) {
				return;
			}
			Console.severe("If you are seeing this message there may be a bug. Please contact the tempfly dev with this info: SkyblockHook | onIslandEnter()");
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
	
	public static enum SkyblockRequirementType {
		REGION,
		WORLD,
		WILDERNESS,
		ISLAND_ROLE;
	}
	
	public abstract IslandWrapper getIslandOwnedBy(UUID id);
	
	public abstract IslandWrapper getIslandAt(Location loc);

	public abstract boolean isChallengeCompleted(UUID id, String challenge);
	
	public abstract boolean islandRoleExists(String role);
	
	public abstract String getIslandRole(IslandWrapper island, Player p);
	
	public abstract UUID getIslandOwner(IslandWrapper island);
	
	public abstract String getIslandIdentifier(IslandWrapper island);

	public abstract boolean isIslandMember(IslandWrapper island, Player p);
}
