package moneybags.tempfly.hook.skyblock.plugins;

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
		onIslandEnter(p, island, e.getLocation());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		Island rawIsland = api.getIslandAt(p.getLocation());
		if (rawIsland != null) {
			onIslandEnter(p, rawIsland, p.getLocation());
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
	 * SkyblockHook Inheritance
	 * 
	 */

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
	public boolean islandRoleExists(IslandWrapper island, String role) {
		return islandRoleExists(role);
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
	public String getIslandRole(UUID u, IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return null;
		}
		Island rawIsland = (Island) island.getIsland();
		return rawIsland.getOwner().equals(u) ? "OWNER" : rawIsland.getMembers().contains(u) ? "TEAM" : api.getCoopIslands(Bukkit.getPlayer(u)).contains(api.getIslandLocation(getIslandOwner(island))) ? "COOP" : "VISITOR";
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
	public boolean isIslandMember(UUID u, IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return false;
		}
		return ((Island) island.getIsland()).getMembers().contains(u);
	}

	@Override
	public IslandWrapper getTeamIsland(UUID u) {
		return getIslandAt(api.getHomeLocation(u));
	}

	@Override
	public long getIslandLevel(UUID u) {
		return api.getLongIslandLevel(u);
	}

	@Override
	public long getIslandLevel(IslandWrapper island) {
		return api.getLongIslandLevel(getIslandOwner(island));
	}
}
