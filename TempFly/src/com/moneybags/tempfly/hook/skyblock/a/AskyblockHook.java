package com.moneybags.tempfly.hook.skyblock.a;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;
import com.moneybags.tempfly.hook.FlightResult;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.moneybags.tempfly.hook.FlightResult.DenyReason;
import com.moneybags.tempfly.hook.skyblock.AskyblockRequirement;
import com.moneybags.tempfly.hook.skyblock.IslandSettings;
import com.moneybags.tempfly.util.F;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.IslandEnterEvent;
import com.wasteofplastic.askyblock.events.IslandExitEvent;

public class AskyblockHook implements Listener, TempFlyHook {
	
	private TempFly plugin;
	
	public static String
	requireIsland,
	requireLevelSelf,
	requireLevelOther,
	requireChallenge;
	
	private File
	hookDataf,
	hookConfigf;
	
	public FileConfiguration
	hookData,
	hookConfig;
	
	private boolean
	wilderness,
	team,
	coop,
	visitor;
	
	private ASkyBlockAPI api;
	
	private Map<RequirementType, AskyblockRequirement[]> requirements = new HashMap<>();
	
	public AskyblockHook(TempFly plugin) {
		this.plugin = plugin;
		try { initializeFiles(); } catch (Exception e) {
			U.logS("An error occured while trying to initilize the Askyblock hook. Please contact the developer.");
			e.printStackTrace();
			return;
		}
		if (Bukkit.getPluginManager().getPlugin("ASkyBlock") == null) {
			return;
		}
		
		U.logI("Initializing ASkyBlock hook...");
		
		this.api = ASkyBlockAPI.getInstance();
		
		PageAskyblock.initialize();
		loadValues();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private void initializeFiles() throws Exception {
		String parent = plugin.getDataFolder() + File.separator + "hooks" + File.separator + "Askyblock";
		hookConfigf = new File(parent, "askyblock_config.yml");
	    if (!hookConfigf.exists()) {
	    	hookConfigf.getParentFile().mkdirs();
	    	F.createConfig(plugin.getResource("askyblock_config.yml"), hookConfigf);
	    }
	    
	    hookConfig = new YamlConfiguration();
    	hookConfig.load(hookConfigf);
		if (!hookConfig.getBoolean("enable_hook")) return;
		
		hookDataf = new File(parent, "askyblock_data.yml");
	    if (!hookDataf.exists()) {
	    	hookDataf.getParentFile().mkdirs();
	    	hookDataf.createNewFile();
	    }
	    hookData = new YamlConfiguration();
	    hookData.load(hookDataf);
	}
	
	private void loadValues() {
		this.wilderness = hookConfig.getBoolean("flight_settings.wilderness");
		this.team 		= hookConfig.getBoolean("flight_settings.base_permissions.team");
		this.coop 		= hookConfig.getBoolean("flight_settings.base_permissions.coop");
		this.visitor 	= hookConfig.getBoolean("flight_settings.base_permissions.visitor");
		
		requireIsland		= V.st(hookConfig, "language.invalid.island");
		requireChallenge	= V.st(hookConfig, "language.requirements.challenge");
		requireLevelSelf	= V.st(hookConfig, "language.requirements.level_self");
		requireLevelOther	= V.st(hookConfig, "language.requirements.level_other");
		
		ConfigurationSection csRequire = hookConfig.getConfigurationSection("unlockables");
		if (csRequire != null) {
			for (String require : csRequire.getKeys(false)) {
				RequirementType rt = RequirementType.valueOf(require.toUpperCase());
				if (rt == null) {
					U.logW("An unlockable flight area set for askyblock does not exist: " + require);
					continue;
				}
				
				List<AskyblockRequirement> list = new ArrayList<>();
				if (rt == RequirementType.REGION || rt == RequirementType.WORLD) {
					ConfigurationSection csWR = hookConfig.getConfigurationSection("unlockables." + require);
					if (csWR != null) {
						for (String name: csWR.getKeys(false)) {
							String path = "unlockables." + require + "." + name;
							list.add(new AskyblockRequirement(hookConfig.getStringList(path + ".challenges"), hookConfig.getLong(path + ".island_level", 0), name, rt));
						}
					}
				} else {
					String path = "unlockables." + require;
					list.add(new AskyblockRequirement(hookConfig.getStringList(path + ".challenges"), hookConfig.getLong(path + ".island_level", 0), rt));
				}
				requirements.put(rt, list.toArray(new AskyblockRequirement[list.size()]));
			}
		}
	}
	
	@Override
	public boolean isEnabled() {
		return api != null;
	}
	
	@Override
	public FileConfiguration getHookConfig() {
		return hookConfig;
	}
	
	
	/**
	 * 
	 * Global Values
	 * 
	 */
	
	
	public boolean canFlyWilderness() {
		return wilderness;
	}
	
	public boolean getDefaultTeam() {
		return team;
	}
	
	public boolean getDefaultCoop() {
		return coop;
	}
	
	public boolean getDefaultVisitor() {
		return visitor;
	}
	
	public AskyblockRequirement[] getRequirements(RequirementType type) {
		return requirements.get(type);
	}
	
	public boolean hasRequirement(RequirementType type) {
		return requirements.containsKey(type);
	}
	
	public FileConfiguration getIslandData() {
		return hookData;
	}
	
	
	/**
	 * 
	 * Island Settings
	 * 
	 */
	
	
	private static Map<Player, Island> locations = new HashMap<>();
	private static Map<Island, IslandSettings> loadedSettings = new HashMap<>();
	
	public IslandSettings getIslandSettings(Island island) {
		return loadedSettings.getOrDefault(island, generateIslandSettings(island));
	}
	
	private IslandSettings generateIslandSettings(Island island) {
		IslandSettings settings = new IslandSettings(island.getCenter());
		loadedSettings.put(island, settings);
		return settings;
	}
	
	public void updateLoadedSettings(IslandSettings s) {
		if (!locations.containsValue(s.getIsland())) {
			loadedSettings.remove(s.getIsland());
		}
	}
	
	public void saveIslandData() {
		try { hookData.save(hookDataf); } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * 
	 *  Event Handling
	 * 
	 */
	
	
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(IslandEnterEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		if (locations.containsKey(p)) {
			locations.remove(p);
		}
		locations.put(p, e.getIsland());
		if (FlyHandle.getFlyer(p) != null) {
			FlightResult result = checkFlightRequirements(p, e.getIslandLocation());
			if (!result.isAllowed()) {
				FlyHandle.removeFlyerDelay(FlyHandle.getFlyer(p), 1);
				U.m(p, result.getMessage());
			}
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		if (api.getIslandAt(p.getLocation()) != null) {
			locations.put(p, api.getIslandAt(p.getLocation()));
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Island island = api.getIslandAt(p.getLocation());
		if (island != null) {
			locations.remove(p, island);
			if (!locations.containsValue(island)) {
				loadedSettings.remove(island);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(IslandExitEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		locations.remove(p);
	}
	
	
	
	/**
	 * 
	 * Flight Requirements and processing
	 * 
	 */
	
	/**
	 * Check the flight requirements of a location. Checks for island level and challenge requirements for
	 * different team members.
	 * @param p
	 * @param loc
	 * @return
	 */
	public FlightResult checkFlightRequirements(Player p, Location loc) {
		if (!isEnabled()) {
			return new FlightResult(true);
		}
		
		ASkyBlockAPI api = ASkyBlockAPI.getInstance();
		Island island = api.getIslandAt(loc);
		if (island == null) {
			return wilderness ? new FlightResult(true) : new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf);
		}
		
		IslandSettings settings = getIslandSettings(island);	
		if (island.getMembers().contains(p.getUniqueId())) {
			if (island.getOwner().equals(p.getUniqueId())) {
				return hasRequirement(RequirementType.OWNER) ? runRequirement(requirements.get(RequirementType.OWNER)[0], island, p, false) : new FlightResult(true);
			}
			return !settings.getTeamCanFly() ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
				(hasRequirement(RequirementType.TEAM) ? runRequirement(requirements.get(RequirementType.TEAM)[0], island, p, false) : new FlightResult(true));
				
		} else if (api.getCoopIslands(p).contains(api.getIslandLocation(island.getOwner()))) {
			return !settings.getCoopCanFly() ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
					(hasRequirement(RequirementType.COOP) ? runRequirement(requirements.get(RequirementType.COOP)[0], island, p, false) : new FlightResult(true));
		} else {
			return !settings.getVisitorCanFly() ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
					(hasRequirement(RequirementType.VISITOR) ? runRequirement(requirements.get(RequirementType.VISITOR)[0], island, p, false) : new FlightResult(true));
		}
	}
	
	/**
	 * Process the requirements for a player to check if a player can fly on the given island.
	 * @param ir
	 * @param island
	 * @param p
	 * @return
	 */
	private FlightResult runRequirement(AskyblockRequirement ir, Island island, Player p, boolean isMessageSelf) {
		U.logS("run requirements");
		ASkyBlockAPI api = ASkyBlockAPI.getInstance();
		
		if (ir.getRequiredLevel() > api.getLongIslandLevel(island.getOwner())) {
			U.logS("fail island level: " + ir.getRequiredLevel() + " / " + api.getLongIslandLevel(island.getOwner()));
			return new FlightResult(false, DenyReason.REQUIREMENT, (isMessageSelf ? requireLevelSelf : requireLevelOther)
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getRequiredLevel()))
					.replaceAll("\\{STATUS}", ir.getType().toString()));
		}
		
		Map<String, Boolean> completed = api.getChallengeStatus(p.getUniqueId());
		for (String challenge : ir.getRequiredChallenges()) {										
			if (completed != null && completed.containsKey(challenge) && !completed.get(challenge)) {
				U.logS("fail island challenge: " + challenge);
				return new FlightResult(false, DenyReason.REQUIREMENT, requireChallenge.replaceAll("\\{CHALLENGE}", challenge));
			}
		}	
		return new FlightResult(true);
	}
	
	@Override
	public FlightResult handleFlightInquiry(Player p, World world) {
		if (!isEnabled() || !hasRequirement(RequirementType.WORLD) || world == null) {
			return new FlightResult(true);
		}
		
		Location homeLoc = api.getHomeLocation(p.getUniqueId());
		for (AskyblockRequirement rq: getRequirements(RequirementType.WORLD)) {
			if (!rq.getName().equals(world.getName())) {
				continue;
			}
			if (homeLoc == null) {
				return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
			}
			Island homeIsland = api.getIslandAt(homeLoc);
			return runRequirement(rq, homeIsland, p, true);
		}
		return new FlightResult(true);
	}

	@Override
	public FlightResult handleFlightInquiry(Player p, ProtectedRegion r) {
		if (!isEnabled() || !hasRequirement(RequirementType.REGION) || r == null) {
			return new FlightResult(true);
		}
		
		Location homeLoc = api.getHomeLocation(p.getUniqueId());
		for (AskyblockRequirement rq: getRequirements(RequirementType.REGION)) {
			if (!rq.getName().equals(r.getId())) {
				continue;
			}
			if (homeLoc == null) {
				return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
			}
			Island homeIsland = api.getIslandAt(homeLoc);
			return runRequirement(rq, homeIsland, p, true);
		}
		return new FlightResult(true);
	}
	
	@Override
	public FlightResult handleFlightInquiry(Player p, ApplicableRegionSet regions) {
		if (!isEnabled() || !hasRequirement(RequirementType.REGION) || regions == null || regions.size() == 0) {
			return new FlightResult(true);
		}
		Map<String, ProtectedRegion> idIndex = new HashMap<>();
		for (ProtectedRegion r: regions) {
			idIndex.put(r.getId(), r);
		}
		
		Location homeLoc = api.getHomeLocation(p.getUniqueId());
		for (AskyblockRequirement rq: getRequirements(RequirementType.REGION)) {
			if (!idIndex.containsKey(rq.getName())) {
				continue;
			}
			if (homeLoc == null) {
				return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
			}
			Island homeIsland = api.getIslandAt(homeLoc);
			return runRequirement(rq, homeIsland, p, true);
		}
		return new FlightResult(true);
	}
	
	@Override
	public FlightResult handleFlightInquiry(Player p, Location loc) {
		if (!isEnabled() || loc == null) {
			return new FlightResult(true);
		}
		return checkFlightRequirements(p, loc);
	}

	
	
	public static enum RequirementType {
		REGION,
		WORLD,
		WILDERNESS,
		OWNER,
		TEAM,
		COOP,
		VISITOR;
	}
}
