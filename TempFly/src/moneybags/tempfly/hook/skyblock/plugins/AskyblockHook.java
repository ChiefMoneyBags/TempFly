package moneybags.tempfly.hook.skyblock.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.IslandEnterEvent;
import com.wasteofplastic.askyblock.events.IslandExitEvent;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.fly.FlyHandle;
import moneybags.tempfly.hook.FlightResult;
import moneybags.tempfly.hook.FlightResult.DenyReason;
import moneybags.tempfly.hook.HookManager.HookType;
import moneybags.tempfly.hook.skyblock.SkyblockRequirement;
import moneybags.tempfly.hook.skyblock.IslandSettings;
import moneybags.tempfly.hook.skyblock.IslandWrapper;
import moneybags.tempfly.hook.skyblock.SkyblockHook;
import moneybags.tempfly.util.Console;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

public class AskyblockHook extends SkyblockHook implements Listener {
	
	private ASkyBlockAPI api;
	
	public AskyblockHook(TempFly plugin) {
		super(HookType.ASKYBLOCK, plugin);
		
		this.api = ASkyBlockAPI.getInstance();
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	
	
	/**
	 * 
	 *  Event Handling
	 * 
	 */
	
	
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(IslandEnterEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		Island island = e.getIsland();
		onIslandEnter(p, island);
		
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
		Island rawIsland = api.getIslandAt(p.getLocation());
		if (rawIsland != null) {
			onIslandEnter(p, rawIsland);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Island rawIsland = api.getIslandAt(p.getLocation());
		if (rawIsland != null) {
			onIslandExit(p, rawIsland);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(IslandExitEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		Island rawIsland = api.getIslandAt(p.getLocation());
		if (rawIsland != null) {
			onIslandExit(p, rawIsland);
		}
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
		
		IslandWrapper island = getIslandAt(loc);
		if (island == null) {
			return canFlyWilderness() ? new FlightResult(true) : new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf);
		}
		
		IslandSettings settings = island.getSettings();	
		if (island.isMember(p.getUniqueId())) {
			if (island.getOwner().equals(p.getUniqueId())) {
				return hasRequirement(RequirementType.OWNER) ? runRequirement(getRequirements(RequirementType.OWNER)[0], island, p, false) : new FlightResult(true);
			}
			return !settings.canFly(AskyblockStatus.TEAM.toString()) ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
				(hasRequirement(RequirementType.TEAM) ? runRequirement(getRequirements(RequirementType.TEAM)[0], island, p, false) : new FlightResult(true));
				
		} else if (api.getCoopIslands(p).contains(api.getIslandLocation(island.getOwner()))) {
			return !settings.canFly(AskyblockStatus.COOP.toString()) ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
					(hasRequirement(RequirementType.COOP) ? runRequirement(getRequirements(RequirementType.COOP)[0], island, p, false) : new FlightResult(true));
		} else {
			return !settings.canFly(AskyblockStatus.VISITOR.toString()) ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
					(hasRequirement(RequirementType.VISITOR) ? runRequirement(getRequirements(RequirementType.VISITOR)[0], island, p, false) : new FlightResult(true));
		}
	}
	
	/**
	 * Process the requirements for a player to check if a player can fly on the given island.
	 * @param ir
	 * @param island
	 * @param p
	 * @return
	 */
	private FlightResult runRequirement(SkyblockRequirement ir, IslandWrapper island, Player p, boolean isMessageSelf) {
		if (ir.getRequiredLevel() > api.getLongIslandLevel(island.getOwner())) {
			Console.debug("fail island level: " + ir.getRequiredLevel() + " / " + api.getLongIslandLevel(island.getOwner()));
			return new FlightResult(false, DenyReason.REQUIREMENT, (isMessageSelf ? requireLevelSelf : requireLevelOther)
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getRequiredLevel()))
					.replaceAll("\\{STATUS}", ir.getType().toString()));
		}
		
		Map<String, Boolean> completed = api.getChallengeStatus(p.getUniqueId());
		for (String challenge : ir.getRequiredChallenges()) {										
			if (completed != null && completed.containsKey(challenge) && !completed.get(challenge)) {
				Console.debug("fail island challenge: " + challenge);
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
		for (SkyblockRequirement rq: getRequirements(RequirementType.WORLD)) {
			if (!rq.getName().equals(world.getName())) {
				continue;
			}
			if (homeLoc == null) {
				return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
			}
			IslandWrapper homeIsland = getIslandAt(homeLoc);
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
		for (SkyblockRequirement rq: getRequirements(RequirementType.REGION)) {
			if (!rq.getName().equals(r.getId())) {
				continue;
			}
			if (homeLoc == null) {
				return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
			}
			IslandWrapper homeIsland = getIslandAt(homeLoc);
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
		for (SkyblockRequirement rq: getRequirements(RequirementType.REGION)) {
			if (!idIndex.containsKey(rq.getName())) {
				continue;
			}
			if (homeLoc == null) {
				return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
			}
			IslandWrapper homeIsland = getIslandAt(homeLoc);
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

	public static enum AskyblockStatus {
		OWNER,
		TEAM,
		COOP,
		VISITOR;
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



	@Override
	public IslandWrapper getIslandOwnedBy(UUID id) {
		return getIslandWrapper(api.getIslandOwnedBy(id));
	}



	@Override
	public IslandWrapper getIslandAt(Location loc) {
		return getIslandWrapper(api.getIslandAt(loc));
	}



	@Override
	public boolean isChallengeCompleted(UUID id, String challenge) {
		return api.getChallengeStatus(id).getOrDefault(challenge, false);
	}
}
