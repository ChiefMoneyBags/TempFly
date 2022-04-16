package com.moneybags.tempfly.hook.factions.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FPlayerJoinEvent;
import com.massivecraft.factions.event.FPlayerLeaveEvent;
import com.massivecraft.factions.event.FactionDisbandEvent;
import com.massivecraft.factions.event.FactionRelationEvent;
import com.massivecraft.factions.event.LandClaimEvent;
import com.massivecraft.factions.event.LandUnclaimAllEvent;
import com.massivecraft.factions.event.LandUnclaimEvent;
import com.massivecraft.factions.event.PowerLossEvent;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.factions.FactionWrapper;
import com.moneybags.tempfly.hook.factions.FactionsHook;

public class FactionsUUIDHook extends FactionsHook implements Listener {

	public FactionsUUIDHook(TempFly tempfly) {
		super(tempfly);
	}
	
	@Override
	public boolean initializeHook() {
		getTempFly().getServer().getPluginManager().registerEvents(this, tempfly);
		return super.initializeHook();
	}
	
	@Override
	public String getTargetClass() {
		return "com.massivecraft.factions.FactionsPlugin";
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onClaim(LandClaimEvent e) {
		FLocation loc = e.getLocation();
		Faction current = Board.getInstance().getFactionAt(loc);
		Bukkit.getScheduler().runTask(tempfly, () -> {
			Faction claimer = e.getFaction();
			if (!claimer.equals(current)) {
				super.onLandOverClaimed(loc.getChunk(), getFactionWrapper(current), getFactionWrapper(claimer));
			} else {
				super.onLandClaimed(loc.getChunk(), getFactionWrapper(claimer));
			}
		});
	}

	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onUnclaim(LandUnclaimEvent e) {
		Bukkit.getScheduler().runTask(tempfly, () -> {
			super.onLandUnclaimed(e.getLocation().getChunk());
		});
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onUnclaimAll(LandUnclaimAllEvent e) {
		Set<FLocation> claims = new HashSet<>(e.getFaction().getAllClaims());
		Bukkit.getScheduler().runTask(tempfly, () -> {
			for (FLocation loc: claims) {	
				super.onLandUnclaimed(loc.getChunk());
			}
		});
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPower(PowerLossEvent e) {
		Bukkit.getScheduler().runTask(tempfly, () -> {
			super.onPlayerPowerChange(e.getfPlayer().getPlayer());
		});
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRelation(FactionRelationEvent e) {
		super.onFactionRelationshipChange(getFactionWrapper(e.getFaction()), getFactionWrapper(e.getTargetFaction()));
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDisband(FactionDisbandEvent e) {
		super.onFactionDisband(getFactionWrapper(e.getFaction()));
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFactionJoin(FPlayerJoinEvent e) {
		Bukkit.getScheduler().runTask(tempfly, () -> {
			if (e.getfPlayer().getPlayer().isOnline()) {
				super.onPlayerJoinFaction(e.getfPlayer().getPlayer(), getFactionWrapper(e.getFaction()));	
			}
		});
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFactionLeave(FPlayerLeaveEvent e) {
		Bukkit.getScheduler().runTask(tempfly, () -> {
			if (e.getfPlayer().getPlayer().isOnline()) {
				super.onPlayerLeaveFaction(e.getfPlayer().getPlayer(), getFactionWrapper(e.getFaction()));	
			}
		});
	}
	
	
	
	
	
	
	public FPlayer getFPlayer(UUID playerId) {
		return FPlayers.getInstance().getById(playerId.toString());
	}

	@Override
	public double getCurrentPower(UUID playerId) {
		return getFPlayer(playerId).getPower();
	}

	@Override
	public double getMaxPower(UUID playerId) {
		return getFPlayer(playerId).getPowerMax();
	}

	@Override
	public double getCurrentPower(FactionWrapper faction) {
		return ((Faction) faction.getRawTerritory()).getPower();
	}

	@Override
	public double getMaxPower(FactionWrapper faction) {
		return ((Faction) faction.getRawTerritory()).getPowerMax();
	}

	@Override
	public FactionWrapper getFaction(UUID playerId) {
		return getFactionWrapper(getFPlayer(playerId).getFaction());
	}

	@Override
	public boolean isMember(UUID playerId, FactionWrapper faction) {
		if (((Faction) faction.getRawTerritory()).isWilderness()) {
			return false;
		}
		return getFPlayer(playerId).getFaction().equals(faction.getRawTerritory());
	}

	@Override
	public UUID getFactionOwner(FactionWrapper faction) {
		return UUID.fromString(((Faction) faction.getRawTerritory()).getFPlayerAdmin().getId());
	}

	@Override
	public String getRole(UUID playerId, FactionWrapper faction) {
		return getFPlayer(playerId).getRole().toString();
	}

	@Override
	public boolean isEnemy(UUID playerId, FactionWrapper faction) {
		return getFPlayer(playerId).getFaction().getRelationTo((Faction) faction.getRawTerritory()).isEnemy();
	}

	@Override
	public boolean isAllied(UUID playerId, FactionWrapper faction) {
		return getFPlayer(playerId).getFaction().getRelationTo((Faction) faction.getRawTerritory()).isAlly();
	}

	@Override
	public String getFactionIdentifier(Object rawFaction) {
		return ((Faction) rawFaction).getId();
	}

	@Override
	public String getFactionName(FactionWrapper faction) {
		return ((Faction) faction.getRawTerritory()).getTag();
	}

	@Override
	public FactionWrapper getFactionAt(Location loc) {
		return getFactionWrapper(Board.getInstance().getFactionAt(new FLocation(loc)));
	}

	@Override
	public boolean isInFactionLand(FactionWrapper faction, Location loc) {
		Faction rawFaction = Board.getInstance().getFactionAt(new FLocation(loc));
		if (rawFaction == null) {
			return false;
		}
		return rawFaction.equals(faction.getRawTerritory());
	}
	
	@Override
	public UUID[] getAllMembers(FactionWrapper faction) {
		List<UUID> ids = new ArrayList<>();
		for (FPlayer player: ((Faction) faction.getRawTerritory()).getFPlayers()) {
			ids.add(UUID.fromString(player.getId()));
		}
		return ids.toArray(new UUID[ids.size()]);
	}


	@Override
	public String getPluginName() {
		return "Factions";
	}

	@Override
	public String getConfigName() {
		return "FactionsUUID";
	}
	
	@Override
	public String getEmbeddedConfigName() {
		return "factions_preset_uuid";
	}

	@Override
	public boolean isWilderness(FactionWrapper faction) {
		return ((Faction) faction.getRawTerritory()).isWilderness();
	}

}
