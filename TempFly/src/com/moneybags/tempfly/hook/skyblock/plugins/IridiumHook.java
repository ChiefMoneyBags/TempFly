package com.moneybags.tempfly.hook.skyblock.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.Island;
import com.iridium.iridiumskyblock.IslandManager;
import com.iridium.iridiumskyblock.User;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockChallenge;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class IridiumHook extends SkyblockHook implements Listener {

	public static final String[] ROLES = new String[] {"OWNER", "COOWNER", "MODERATOR", "MEMBER", "COOP", "VISITOR"};
	
	public IridiumHook(TempFly tempfly) {
		super(tempfly);
	}
	
	@Override
	public boolean initializeHook() {
		// initial iridium hook stuff.
		
		//
		if (super.initializeHook()) {
			tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
			setEnabled(true);
			return true;
		}
		return false;
	}
	
	/**
	 * Iridium skyblock has no internal tracking or events for player location and islands
	 * >:(
	 * @param e
	 */
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerMoveEvent e) {
		Location to = e.getTo();
		if (e.getFrom().getBlock().equals(to.getBlock())) {
			return;
		}
		super.updateLocation(e.getPlayer(), e.getTo());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerRespawnEvent e) {
		super.updateLocation(e.getPlayer(), e.getRespawnLocation());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerTeleportEvent e) {
		super.updateLocation(e.getPlayer(), e.getTo());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerChangedWorldEvent e) {
		super.updateLocation(e.getPlayer(), e.getPlayer().getLocation());
	}
	
	
	
	
	
	

	@Override
	public Player[] getOnlineMembers(IslandWrapper island) {
		List<Player> online = new ArrayList<>();
		Island rawIsland = (Island) island.getIsland();
		for (String s: rawIsland.getMembers()) {
			Player p = Bukkit.getPlayer(UUID.fromString(s));
			if (p != null & p.isOnline()) {
				online.add(p);
			}
		}
		return online.toArray(new Player[online.size()]);
	}

	@Override
	public UUID[] getIslandMembers(IslandWrapper island) {
		List<UUID> ids = new ArrayList<>();
		Island rawIsland = (Island) island.getIsland();
		for (String s: rawIsland.getMembers()) {
			ids.add(UUID.fromString(s));
		}
		return ids.toArray(new UUID[ids.size()]);
	}

	@Override
	public IslandWrapper getIslandOwnedBy(UUID u) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(u);
		User user = User.getUser(p);
		Island rawIsland = user.getIsland();
		if (rawIsland == null || !rawIsland.getOwner().equals(u.toString())) {
			return null;
		}
		return getIslandWrapper(rawIsland);
	}

	@Override
	public IslandWrapper getTeamIsland(UUID u) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(u);
		User user = User.getUser(p);
		Island rawIsland = user.getIsland();
		if (rawIsland == null) {
			return null;
		}
		return getIslandWrapper(rawIsland);
	}

	@Override
	public IslandWrapper getIslandAt(Location loc) {
		IslandManager islandManager;
		return getIslandWrapper((islandManager = IridiumSkyblock.getIslandManager()) == null ? null : islandManager.getIslandViaLocation(loc));
	}

	@Override
	public boolean isChallengeCompleted(UUID p, SkyblockChallenge challenge) {
		
		return false;
	}

	@Override
	public boolean islandRoleExists(IslandWrapper island, String role) {
		return islandRoleExists(role);
	}

	@Override
	public boolean islandRoleExists(String role) {
		for (String s: ROLES) {
			if (s.equalsIgnoreCase(role)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getIslandRole(UUID u, IslandWrapper island) {
		Island rawIsland = (Island) island.getIsland();
		IslandWrapper playerIsland = getTeamIsland(u);
		if (playerIsland != null) {
			if (rawIsland.getCoop().contains(((Island)rawIsland).getId())) {
				return "COOP";
			}
		}
		
		if (isIslandMember(u, island)) {
			return User.getUser(Bukkit.getOfflinePlayer(u)).getRole().toString().toUpperCase();
		} else {
			return "VISITOR";
		}
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		return UUID.fromString(((Island)island.getIsland()).getOwner());
	}

	/**
	 * Each island in iridium skyblock has an int ID. this will be our identifier.
	 */
	@Override
	public String getIslandIdentifier(Object rawIsland) {
		return String.valueOf(((Island)rawIsland).getId());
	}
	
	@Override
	public IslandWrapper getIslandFromIdentifier(String identifier) {
		return getIslandWrapper(IridiumSkyblock.getIslandManager().getIslandViaId(Integer.valueOf(identifier)));
	}
	

	@Override
	public boolean isIslandMember(UUID u, IslandWrapper island) {
		return ((Island)island.getIsland()).getMembers().contains(u.toString());
	}

	@Override
	public double getIslandLevel(UUID u) {
		IslandWrapper island = getTeamIsland(u);
		return island == null ? 0 : ((Island)island.getIsland()).getValue();
	}

	@Override
	public double getIslandLevel(IslandWrapper island) {
		return ((Island)island.getIsland()).getValue();
	}

	@Override
	public String[] getRoles() {
		return ROLES;
	}

	@Override
	public String getPluginName() {
		return "IridiumSkyblock";
	}

	@Override
	public boolean isIslandWorld(Location loc) {
		return IridiumSkyblock.getIslandManager().isIslandWorld(loc);
	}

}
