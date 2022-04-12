package com.moneybags.tempfly.hook.skyblock.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthUpdateEvent;
import com.bgsoftware.superiorskyblock.api.events.MissionCompleteEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockChallenge;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;
import com.moneybags.tempfly.util.Console;

public class SuperiorHook extends SkyblockHook implements Listener {

	private SuperiorSkyblock superior;
	
	private String[] roles;
	
	public SuperiorHook(TempFly plugin) {
		super(plugin);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnter(IslandEnterEvent e) {
		Player player = Bukkit.getPlayer(e.getPlayer().getUniqueId());
		super.onIslandEnter(player, e.getIsland(), player.getLocation());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLeave(IslandLeaveEvent e) {
		super.onIslandExit(Bukkit.getPlayer(e.getPlayer().getUniqueId()));
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onWorth(IslandWorthUpdateEvent e) {
		Island island = e.getIsland();
		onIslandLevelChange(getIslandWrapper(island));
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMission(MissionCompleteEvent e) {
		super.onChallengeComplete(Bukkit.getPlayer(e.getPlayer().getUniqueId()));
	}
	
	
	@Override
	public boolean initializeHook() {
		superior = SuperiorSkyblockAPI.getSuperiorSkyblock();
		if (superior == null) {
			Console.severe("There was an error while initializing the SuperiorSkyblock Plugin hook!");
			return false;
		}
		if (!super.initializeHook()) {
			return false;
		}
		
		tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
		
		List<String> roles = new ArrayList<>();		
		for (PlayerRole role: superior.getRoles().getRoles()) {
			roles.add(role.getName());
		}
		this.roles = roles.toArray(new String[roles.size()]);
		
		
		return true;
	}

	@Override
	public Player[] getOnlineMembers(IslandWrapper island) {
		Island is = (Island) island.getRawIsland();
		List<Player> players = new ArrayList<>();
		for (SuperiorPlayer player: is.getIslandMembers(true)) {
			if (player.isOnline()) {
				players.add(Bukkit.getPlayer(player.getUniqueId()));
			}
		}
		return players.toArray(new Player[players.size()]);
	}

	@Override
	public UUID[] getIslandMembers(IslandWrapper island) {
		Island is = (Island) island.getRawIsland();
		List<UUID> uids = new ArrayList<>();
		for (SuperiorPlayer player: is.getIslandMembers(true)) {
			uids.add(player.getUniqueId());
		}
		return uids.toArray(new UUID[uids.size()]);
	}

	@Override
	public IslandWrapper getIslandOwnedBy(UUID playerId) {
		IslandWrapper island = getTeamIsland(playerId);
		if (!((Island) island.getRawIsland()).getOwner().getUniqueId().equals(playerId)) {
			return null;
		}
		return island;
	}

	@Override
	public IslandWrapper getTeamIsland(UUID playerId) {
		return getIslandWrapper(superior.getGrid().getIsland(superior.getPlayers().getSuperiorPlayer(playerId)));
	}

	@Override
	public IslandWrapper getIslandAt(Location loc) {
		Island island = superior.getGrid().getIslandAt(loc);
		return island == null ? null : getIslandWrapper(island);
	}

	@Override
	public boolean isIslandWorld(Location loc) {
		return superior.getGrid().isIslandsWorld(loc.getWorld());
	}

	@Override
	public boolean isChallengeCompleted(UUID playerId, SkyblockChallenge challenge) {
		Mission<?> mission = superior.getMissions().getMission(challenge.getName());
		if (mission == null) {
			Console.debug("mission is null");
			return false;
		}
		Console.debug("--| mission: " + challenge.getName(), "--| Players mission progress: " + mission.getProgress(superior.getPlayers().getSuperiorPlayer(playerId)));
		
		return superior.getPlayers().getSuperiorPlayer(playerId).hasCompletedMission(mission);
	}

	@Override
	public boolean islandRoleExists(IslandWrapper island, String role) {
		return islandRoleExists(role);
	}

	@Override
	public boolean islandRoleExists(String role) {
		try {
			return superior.getRoles().getPlayerRole(role) != null;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String getIslandRole(UUID playerId, IslandWrapper island) {
		SuperiorPlayer player = superior.getPlayers().getSuperiorPlayer(playerId);
		Island is = ((Island) island.getRawIsland());
		
		if (!is.isMember(player)) {
			return is.isCoop(player) ? superior.getRoles().getCoopRole().toString() : superior.getRoles().getGuestRole().toString();
		}
		return player.getPlayerRole().toString();
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		return ((Island) island.getRawIsland()).getOwner().getUniqueId();
	}

	@Override
	public String getIslandIdentifier(Object rawIsland) {
		return ((Island) rawIsland).getUniqueId().toString();
	}

	@Override
	public IslandWrapper getIslandFromIdentifier(String identifier) {
		Console.debug("---", identifier, superior.getGrid().getIslandByUUID(UUID.fromString(identifier)));
		return getIslandWrapper(superior.getGrid().getIslandByUUID(UUID.fromString(identifier)));
	}

	@Override
	public boolean isIslandMember(UUID playerId, IslandWrapper island) {
		return ((Island) island).isMember(superior.getPlayers().getSuperiorPlayer(playerId));
	}

	@Override
	public double getIslandLevel(UUID playerId) {
		IslandWrapper island = getTeamIsland(playerId);
		return island == null ? 0 : getIslandLevel(island);
	}

	@Override
	public double getIslandLevel(IslandWrapper island) {
		if (island == null) {
			return 0;
		}
		return ((Island) island.getRawIsland()).getIslandLevel().doubleValue();
	}

	@Override
	public String[] getRoles() {
		return roles;
	}

	@Override
	public boolean isInIsland(IslandWrapper island, Location loc) {
		return island.getRawIsland().equals(superior.getGrid().getIslandAt(loc));
	}

	@Override
	public String getPluginName() {
		return "SuperiorSkyblock2";
	}
	
	@Override
	public String getEmbeddedConfigName() {
		return "skyblock_preset_superior";
	}



}
