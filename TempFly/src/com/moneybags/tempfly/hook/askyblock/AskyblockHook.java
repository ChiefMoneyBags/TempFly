package com.moneybags.tempfly.hook.askyblock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.event.FlightEnabledEvent;
import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;
import com.moneybags.tempfly.hook.WorldGuardAPI;
import com.moneybags.tempfly.util.F;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.IslandEnterEvent;
import com.wasteofplastic.askyblock.events.IslandExitEvent;

public class AskyblockHook implements Listener {
	
	private File askyblockdataf;
	
	public FileConfiguration askyblockdata;
	
	private boolean wilderness;
	private boolean team;
	private boolean coop;
	private boolean visitor;
	private ASkyBlockAPI api;
	
	private Map<RequirementType, IslandRequirements> requirements = new HashMap<>();
	
	public AskyblockHook() {
		if (Bukkit.getPluginManager().getPlugin("ASkyBlock") == null) {
			return;
		}
		this.api = ASkyBlockAPI.getInstance();
		PageAskyblock.initialize();
		askyblockdataf = new File(TempFly.plugin.getDataFolder(), "askyblockdata.yml");
	    if (!askyblockdataf.exists()){
	    	askyblockdataf.getParentFile().mkdirs();
	        TempFly.plugin.saveResource("askyblockdata.yml", false);
	    }
	    askyblockdata = new YamlConfiguration();
	    try {
	        askyblockdata.load(askyblockdataf);
	    } catch (IOException | InvalidConfigurationException e1){
	    	U.logS("There is a problem inside the askyblockdata.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	        return;
	    }
	    
		
		TempFly.plugin.enableAskyblock(this);
		
		String path = "hooks.askyblock.flight_settings";
		wilderness = F.config.getBoolean(path + ".wilderness");
		team = F.config.getBoolean(path + ".base_permissions.team");
		coop = F.config.getBoolean(path + ".base_permissions.coop");
		visitor = F.config.getBoolean(path + ".base_permissions.visitor");
		
		ConfigurationSection csR = F.config.getConfigurationSection("hooks.askyblock.unlockables");
		if (csR != null) {
			for (String s : csR.getKeys(false)) {
				RequirementType rt = RequirementType.valueOf(s.toUpperCase());
				if (rt == null) {
					U.logW("An unlockable flight area set for askyblock does not exist: " + s);
					continue;
				}
				if ((rt.equals(RequirementType.REGIONS)) || (rt.equals(RequirementType.WORLDS))) {
					ConfigurationSection csWR = F.config.getConfigurationSection("hooks.askyblock.unlockables." + s);
					if (csWR != null) {
						for (String ss : csWR.getKeys(false)) {
							requirements.put(rt, new IslandRequirements("hooks.askyblock.unlockables." + s + "." + ss, ss));
						}
					}
				} else {
					requirements.put(rt, new IslandRequirements("hooks.askyblock.unlockables." + s, null));
				}
			}
		}
		
	}
	
	
	
	public FileConfiguration getIslandData() {
		return askyblockdata;
	}
	
	public void saveIslandData() {
		try {
			askyblockdata.save(askyblockdataf);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private static Map<Player, Island> locations = new HashMap<>();
	private static Map<Island, IslandSettings> loadedSettings = new HashMap<>();
	
	public IslandSettings getIslandSettings(Island island) {
		if (loadedSettings.containsKey(island)) {
			return loadedSettings.get(island);
		} else {
			return new IslandSettings(island.getCenter());
		}
		
	}
	
	public void updateLoadedSettings(IslandSettings s) {
		if (!locations.containsValue(s.getIsland())) {
			loadedSettings.remove(s.getIsland());
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void on(IslandEnterEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		if (locations.containsKey(p)) {
			locations.remove(p);
		}
		locations.put(p, e.getIsland());
		if (FlyHandle.getFlyer(p) != null) {
			checkFlightRequirement(p, e.getIslandLocation());
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void on(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		Flyer f = FlyHandle.getFlyer(p);
		if (api.getIslandAt(p.getLocation()) != null) {
			locations.put(p, api.getIslandAt(p.getLocation()));
		}
		if (f == null) {
			return;
		}
		checkFlightRequirement(p, p.getLocation());
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
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
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void on(IslandExitEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		if (locations.containsKey(p)) {
			locations.remove(p);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void on(FlightEnabledEvent e) {
		Player p = e.getPlayer();
		if (!checkFlightRequirement(p, p.getLocation())) {
			e.setCancelled(true);
		}
	}
	
	
	public boolean checkFlightRequirement(Player p, Location loc) {
		ASkyBlockAPI api = ASkyBlockAPI.getInstance();
		Island island = api.getIslandAt(loc);
		if (island != null) {
			IslandSettings settings; 
			if (loadedSettings.containsKey(island)) {
				settings = loadedSettings.get(island);
			} else {
				settings = new IslandSettings(api.getIslandLocation(island.getOwner()));
				loadedSettings.put(island, settings);
			}
			if (island.getMembers().contains(p.getUniqueId())) {
				if (island.getOwner().equals(p.getUniqueId())) {
					for (Entry<RequirementType, IslandRequirements> entry : requirements.entrySet()) {
						if (entry.getKey().equals(RequirementType.OWNER)) {
							return runRequirements(entry.getValue(), island, p);
						}
					}
				} else {
					for (Entry<RequirementType, IslandRequirements> entry : requirements.entrySet()) {
						if (entry.getKey().equals(RequirementType.TEAM)) {
							return runRequirements(entry.getValue(), island, p);
						}
					}
				}
			} else if (api.getCoopIslands(p).contains(api.getIslandLocation(island.getOwner()))) {
				for (Entry<RequirementType, IslandRequirements> entry : requirements.entrySet()) {
					if (entry.getKey().equals(RequirementType.COOP)) {
						return runRequirements(entry.getValue(), island, p);
					}
				}
			} else {
				for (Entry<RequirementType, IslandRequirements> entry : requirements.entrySet()) {
					if (entry.getKey().equals(RequirementType.VISITOR)) {
						return runRequirements(entry.getValue(), island, p);
					}
				}
			}
		} else {
			if (!wilderness) {
				FlyHandle.removeFlyer(p);
				U.m(p, V.invalidZoneSelf);
				return false;
			}
			World world = loc.getWorld();
			List<String> regions = new ArrayList<>();
			if (WorldGuardAPI.isEnabled()) {
				ApplicableRegionSet prot = WorldGuardAPI.getRegionSet(loc);
				if (prot != null) {
					for(ProtectedRegion r : prot) {
						regions.add(r.getId());
					}
				}
			}
			for (Entry<RequirementType, IslandRequirements> entry : requirements.entrySet()) {
				Location homeLoc = api.getHomeLocation(p.getUniqueId());
				if (entry.getKey().equals(RequirementType.WORLDS) && (entry.getValue().getName().equals(world.getName()))) {
					if (homeLoc == null) {
						U.m(p, V.flyRequirementFail);
						return false;
					} else {
						Island homeIsland = api.getIslandAt(homeLoc);
						return runRequirements(entry.getValue(), homeIsland, p);
					}
				} else if (entry.getKey().equals(RequirementType.REGIONS) && regions.contains(entry.getValue().getName())) {
					if (homeLoc == null) {
						U.m(p, V.flyRequirementFail);
						return false;
					} else {
						Island homeIsland = api.getIslandAt(homeLoc);
						return runRequirements(entry.getValue(), homeIsland, p);
					}
				}
			}
		}
		return true;
	}
	
	private boolean runRequirements(IslandRequirements ir, Island island, Player p) {
		ASkyBlockAPI api = ASkyBlockAPI.getInstance();
		if (ir.getRequiredLevel() > api.getLongIslandLevel(island.getOwner())) {
			FlyHandle.removeFlyer(p);
			U.m(p, V.flyRequirementFail);
			return false;
		}
		Map<String, Boolean> completed = api.getChallengeStatus(p.getUniqueId());
		for (String challenge : ir.getRequiredChallenges()) {
			if (completed != null && completed.containsKey(challenge) && completed.get(challenge).equals(false)) {
				FlyHandle.removeFlyer(p);
				return false;
			}
		}
		return true;
	}
	
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
	
	public Map<RequirementType, IslandRequirements> getRequirements() {
		return requirements;
	}
	
	public static enum RequirementType {
		REGIONS,
		WORLDS,
		WILDERNESS,
		OWNER,
		TEAM,
		COOP,
		VISITOR;
	}
}
