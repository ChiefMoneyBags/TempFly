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
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.IslandEnterEvent;
import com.wasteofplastic.askyblock.events.IslandExitEvent;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.fly.FlyHandle;
import moneybags.tempfly.hook.FlightResult;
import moneybags.tempfly.hook.FlightResult.DenyReason;
import moneybags.tempfly.hook.HookManager.HookType;
import moneybags.tempfly.hook.region.CompatRegion;
import moneybags.tempfly.hook.skyblock.SkyblockRequirement;
import moneybags.tempfly.hook.skyblock.IslandSettings;
import moneybags.tempfly.hook.skyblock.IslandWrapper;
import moneybags.tempfly.hook.skyblock.SkyblockHook;
import moneybags.tempfly.util.Console;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

public class AskyblockHook extends SkyblockHook implements Listener {
	
	public static final String[] ASKYBLOCK_ROLES = new String[] {"OWNER", "TEAM", "COOP", "VISITOR"};
	
	private ASkyBlockAPI api;
	
	public AskyblockHook(TempFly plugin) {
		super(HookType.ASKYBLOCK, plugin);
		if (!super.isEnabled()) {
			return;
		}
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
		if (isIslandMember(island, p)) {
			
			return getIslandOwner(island).equals(p.getUniqueId()) ?
					//owner
					hasRequirement(SkyblockRequirementType.ISLAND_ROLE, "OWNER") ? runRequirement(getRequirement(SkyblockRequirementType.ISLAND_ROLE, "OWNER"), island, p) : new FlightResult(true)
							:
					//team
					!settings.canFly("TEAM") ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
					(hasRequirement(SkyblockRequirementType.ISLAND_ROLE, "TEAM") ? runRequirement(getRequirement(SkyblockRequirementType.ISLAND_ROLE, "TEAM"), island, p) : new FlightResult(true));
		
		} else if (api.getCoopIslands(p).contains(api.getIslandLocation(getIslandOwner(island)))) {
			return 	!settings.canFly("COOP") ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
					(hasRequirement(SkyblockRequirementType.ISLAND_ROLE, "COOP") ? runRequirement(getRequirement(SkyblockRequirementType.ISLAND_ROLE, "COOP"), island, p) : new FlightResult(true));
		}
		return !settings.canFly("VISITOR") ? new FlightResult(false, DenyReason.DISABLED_REGION, V.invalidZoneSelf) :
			(hasRequirement(SkyblockRequirementType.ISLAND_ROLE, "VISITOR") ? runRequirement(getRequirement(SkyblockRequirementType.ISLAND_ROLE, "VISITOR"), island, p) : new FlightResult(true));
	}
	
	/**
	 * Process the requirements for a player to check if a player can fly on the given island.
	 * @param ir
	 * @param island
	 * @param p
	 * @return
	 */
	private FlightResult runRequirement(SkyblockRequirement ir, IslandWrapper island, Player p) {
		if (ir.getIslandLevel() > 0 && ir.getIslandLevel() > api.getLongIslandLevel(p.getUniqueId())) {
			Console.debug("fail island level: " + ir.getIslandLevel() + " / " + api.getLongIslandLevel(p.getUniqueId()));
			return new FlightResult(false, DenyReason.REQUIREMENT, requireLevelSelf
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getOwnerLevel()))
					.replaceAll("\\{ROLE}", ir.getName()));
		}
		if (ir.getOwnerLevel() > 0 && ir.getOwnerLevel() > api.getLongIslandLevel(getIslandOwner(island))) {
			Console.debug("fail island level: " + ir.getOwnerLevel() + " / " + api.getLongIslandLevel(getIslandOwner(island)));
			return new FlightResult(false, DenyReason.REQUIREMENT, requireLevelOther
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getOwnerLevel()))
					.replaceAll("\\{ROLE}", ir.getName()));
		}
		Map<String, Boolean> completed = api.getChallengeStatus(p.getUniqueId());
		if (completed != null) {
			for (String challenge : ir.getChallenges()) {										
				if (completed.containsKey(challenge) && !completed.get(challenge)) {
					Console.debug("fail island challenge: " + challenge);
					return new FlightResult(false, DenyReason.REQUIREMENT, requireChallengeSelf
							.replaceAll("\\{CHALLENGE}", challenge)
							.replaceAll("\\{ROLE}", ir.getName()));
				}
			}	
		}
		Map<String, Boolean> completedOwner = api.getChallengeStatus(getIslandOwner(island));
		if (completedOwner != null) {
			for (String challenge : ir.getOwnerChallenges()) {								
				if (completedOwner.containsKey(challenge) && !completedOwner.get(challenge)) {
					Console.debug("fail island challenge | island owner: " + challenge);
					return new FlightResult(false, DenyReason.REQUIREMENT, requireChallengeOther
							.replaceAll("\\{CHALLENGE}", challenge)
							.replaceAll("\\{ROLE}", ir.getName()));
				}
			}	
		}
		return new FlightResult(true);
	}
	
	@Override
	public FlightResult handleFlightInquiry(Player p, World world) {
		if (!isEnabled() || world == null || !hasRequirement(SkyblockRequirementType.WORLD, world.getName())) {
			return new FlightResult(true);
		}
		
		Location homeLoc = api.getHomeLocation(p.getUniqueId());
		if (homeLoc == null) {
			return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
		}
		IslandWrapper homeIsland = getIslandAt(homeLoc);
		return runRequirement(getRequirement(SkyblockRequirementType.WORLD, world.getName()), homeIsland, p);
	}

	@Override
	public FlightResult handleFlightInquiry(Player p, CompatRegion r) {
		if (!isEnabled() || r == null || !hasRequirement(SkyblockRequirementType.REGION, r.getId())) {
			return new FlightResult(true);
		}
		
		Location homeLoc = api.getHomeLocation(p.getUniqueId());
		if (homeLoc == null) {
			return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
		}
		IslandWrapper homeIsland = getIslandAt(homeLoc);
		return runRequirement(getRequirement(SkyblockRequirementType.REGION, r.getId()), homeIsland, p);
	}
	
	@Override
	public FlightResult handleFlightInquiry(Player p, CompatRegion[] regions) {
		if (!isEnabled() || regions == null || regions.length == 0 || !hasRequirement(SkyblockRequirementType.REGION)) {
			return new FlightResult(true);
		}
		
		Location homeLoc = api.getHomeLocation(p.getUniqueId());
		for (SkyblockRequirement rq: getRequirements(regions)) {
			if (homeLoc == null) {
				return new FlightResult(false, DenyReason.REQUIREMENT, requireIsland);
			}
			IslandWrapper homeIsland = getIslandAt(homeLoc);
			return runRequirement(rq, homeIsland, p);
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

	@Override
	public boolean islandRoleExists(String role) {
		for (String s: ASKYBLOCK_ROLES) {
			if (s.equalsIgnoreCase(role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getIslandRole(IslandWrapper island, Player p) {
		if (!island.getHook().equals(this)) {
			return null;
		}
		Island rawIsland = (Island) island.getIsland();
		UUID u = p.getUniqueId();
		return rawIsland.getOwner().equals(u) ? "OWNER" : rawIsland.getMembers().contains(u) ? "TEAM" : api.getCoopIslands(p).contains(api.getIslandLocation(getIslandOwner(island))) ? "COOP" : "VISITOR";
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return null;
		}
		return ((Island) island.getIsland()).getOwner();
	}

	@Override
	public String getIslandIdentifier(IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return null;
		}
		return U.locationToString(((Island) island.getIsland()).getCenter());
	}
	
	@Override
	public boolean isIslandMember(IslandWrapper island, Player p) {
		if (!island.getHook().equals(this)) {
			return false;
		}
		return ((Island) island.getIsland()).getMembers().contains(p.getUniqueId());
	}
}
