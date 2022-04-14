package com.moneybags.tempfly.hook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;

public abstract class TerritoryHook extends TempFlyHook {

	public TerritoryHook(TempFly tempfly) {
		super(tempfly);
	}
	
	@Override
	public void onTempflyReload() {
		super.onTempflyReload();
		/**
		for (FlightUser user: getTempFly().getFlightManager().getUsers()) {
			user.submitFlightResult(checkFlightRequirements(user.getPlayer().getUniqueId(), user.getPlayer().getLocation()));
		}
		*/
	}
	
	
	private TerritoryTracker manualTracker = null;
	private Map<Player, TerritoryWrapper> locationCache = new HashMap<>();
	private Map<String, TerritoryWrapper> wrapperCache = new HashMap<>();
	
	public void startManualTracking() {
		Console.debug("---> (" + getHookName() + ") Started manual territory tracking");
		if (manualTracker == null) {
			manualTracker = new TerritoryTracker(this);
		}
	}
	
	public void stopManualTracking() {
		Console.debug("---> (" + getHookName() + ") Stopped manual territory tracking");
		if (manualTracker != null) {
			manualTracker.unregister();
			manualTracker = null;
		}
	}
	
	/**
	 * This method is called by the children of SkyblockHook when a player enters an island.
	 * It will track the island each player is on and handle flight requirements for the player.
	 * 
	 * @param p The player who entered the island.
	 * @param rawTerritory The raw object representing the island. A wrapper will be created for it.
	 * @param loc Nullable, The location where the player entered the island.
	 */
	public void onTerritoryEnter(Player p, Object rawTerritory, Location loc) {
		if (V.debug) {
			Console.debug("", "------ On Territory Enter ------", "--| Player: " + p.getName(), rawTerritory);
		}
		if (rawTerritory instanceof TerritoryWrapper) {
			rawTerritory = ((TerritoryWrapper)rawTerritory).getRawTerritory();
		}
		if (locationCache.containsKey(p)) {
			// Player already being tracked.
			if (locationCache.get(p).getRawTerritory().equals(rawTerritory)) {
				// Player is already on this island...
				return;
			}
			// Player is now on 2 islands at once, this is a bug.
			Console.severe("If you are seeing this message there may be a bug. Please contact the tempfly dev with this info: TerritoryHook | onTerritoeyEnter()");
			TerritoryWrapper territory = locationCache.get(p);
			locationCache.remove(p);
			if(!locationCache.containsValue(territory)) {
				wrapperCache.remove(getTerritoryIdentifier(rawTerritory));
			}
		}
		
		TerritoryWrapper territory = getTerritoryWrapper(rawTerritory);
		if (V.debug) {Console.debug("--|> Island Identifier: " + getTerritoryIdentifier(rawTerritory), "------ End Territory Enter ------", "");}
		
		locationCache.put(p, territory);
		FlightUser user = tempfly.getFlightManager().getUser(p);
		if (user == null) {
			return;
		}
		user.submitFlightResult(checkFlightRequirements(p.getUniqueId(), territory));
	}
	
	public void evaluate(Player p) {
		getUser(p).submitFlightResult(checkFlightRequirements(p.getUniqueId(), p.getLocation()));
	}
	
	public abstract FlightResult checkFlightRequirements(UUID playerId, Location loc);
	
	public abstract FlightResult checkFlightRequirements(UUID playerId, TerritoryWrapper territory);
	
	/**
	 * This method is called by the children of SkyblockHook when a player enters an island.
	 * It will track the island each player is on and handle flight requirements for the player.
	 * 
	 * @param p The player who entered the island.
	 */
	public void onTerritoryExit(final Player p) {
		if (V.debug) {
			Console.debug("", "------ On territory Exit ------", "--| Player: " + p.getName());
		}
		if (locationCache.containsKey(p)) {
			TerritoryWrapper currentTerritory = locationCache.get(p);
			locationCache.remove(p);
			if(!locationCache.containsValue(currentTerritory)) {
				Console.debug("----------------- removing wrapper from cache");
				wrapperCache.remove(getTerritoryIdentifier(currentTerritory.getRawTerritory()));
			}
			if (V.debug) {Console.debug("--|> Territory Identifier: " + getTerritoryIdentifier(currentTerritory.getRawTerritory()), "------ End Territory Exit ------", "");}
		}
		// On island exit i will wait 1 tick then check if the player still has this requirement.
		// If they are no longer in a territory it will be removed.
		final RequirementProvider provider = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!p.isOnline()) {
					return;
				}
				FlightUser user = tempfly.getFlightManager().getUser(p);
				if (user == null) {
					return;
				}
				if (user.hasFlightRequirement(provider, InquiryType.OUT_OF_SCOPE) && !locationCache.containsKey(user.getPlayer())) {
					user.submitFlightResult(new ResultAllow(provider, InquiryType.OUT_OF_SCOPE, V.requirePassDefault));
				}
			}
		}.runTaskLater(tempfly, 1);
	}
	
	public TerritoryWrapper getTerritoryWrapper(Object rawTerritory) {
		if (rawTerritory == null) {
			return null;
		}
		TerritoryWrapper wrapper;
		if (!wrapperCache.containsKey(getTerritoryIdentifier(rawTerritory))) {
			wrapper = createTerritoryWrapper(rawTerritory, this);
			wrapperCache.put(getTerritoryIdentifier(rawTerritory), wrapper);
		} else {
			 wrapper = wrapperCache.get(getTerritoryIdentifier(rawTerritory));
		}
		return wrapper;
	}
	
	public abstract TerritoryWrapper createTerritoryWrapper(Object rawTerritory, TerritoryHook hook);
	
	public abstract String getTerritoryIdentifier(Object rawTerritory);
	
	/**
	 * Get all the players that are currently being tracked on an island.
	 * @param island The island in question
	 * @return All the players currently on the island.
	 */
	public Player[] getPlayersOn(TerritoryWrapper territory) {
		List<Player> players = new ArrayList<>();
		for (Map.Entry<Player, TerritoryWrapper> entry: locationCache.entrySet()) {
			if (entry.getValue().equals(territory)) {
				players.add(entry.getKey());
			}
		}
		return players.toArray(new Player[players.size()]);
	}
	
	/**
	 * @return true if the player is currently being tracked on an island
	 */
	public boolean isCurrentlyTracking(Player p) {
		return locationCache.containsKey(p);
	}
	
	/**
	 * @return The island the player is currently being tracked on.
	 */
	public TerritoryWrapper getTrackedTerritory(Player p) {
		return locationCache.get(p);
	}
	
	@Override
	public void onUserInitialized(FlightUser user) {
		Console.debug("", "-- on user initialized --");
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!isCurrentlyTracking(user.getPlayer())) {
					TerritoryWrapper territory = getTerritoryAt(user.getPlayer().getLocation());
					if (territory != null) {
						onTerritoryEnter(user.getPlayer(), territory.getRawTerritory(), user.getPlayer().getLocation());
					}
				}
			}
		}.runTaskLater(getTempFly(), 5);
	}
	
	
	
	@Override
	public void onUserQuit(FlightUser user) {
		Player p = user.getPlayer();
		TerritoryWrapper territory = getTerritoryAt(p.getLocation());
		if (territory != null) {
			onTerritoryExit(p);
		}
	}
	
	/**
	 * This method is used for plugins that do not have internal island tracking or events
	 * such as IridiumSkyblock where we will need to process the player locations ourselves. Onn player teleport, player respawn etc.
	 * @param p The player to update.
	 * @param loc The new location of the player.
	 */
	public void updateLocation(Player p, Location loc) {
		if (isCurrentlyTracking(p)) {
			Console.debug("--| Player is being tracked");
			TerritoryWrapper territory = getTrackedTerritory(p);
			if (!isInTerritory(territory, loc)) {
				Console.debug("--|> Player is no longer in the same territory");
				onTerritoryExit(p);
			}
		}
		TerritoryWrapper territory = getTerritoryAt(loc);
		
		if (territory == null) {
			Console.debug("--| Player is not in a territory");
			return;
		}
		if (!isCurrentlyTracking(p)) {
			Console.debug("--|> Player is in a new territory!");
			onTerritoryEnter(p, territory.getRawTerritory(), loc);	
		}
	}
	
	public abstract TerritoryWrapper getTerritoryAt(Location loc);
	
	public abstract boolean isInTerritory(TerritoryWrapper territory, Location loc);
	
	private class TerritoryTracker implements Listener {
		private TerritoryHook hook;
		
		public TerritoryTracker(TerritoryHook hook) {
			this.hook = hook;
			hook.getTempFly().getServer().getPluginManager().registerEvents(this, hook.getTempFly());
		}
		
		public void unregister() {
			PlayerMoveEvent.getHandlerList().unregister(this);
			PlayerRespawnEvent.getHandlerList().unregister(this);
			PlayerTeleportEvent.getHandlerList().unregister(this);
			PlayerChangedWorldEvent.getHandlerList().unregister(this);
		}
		
		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void on(PlayerMoveEvent e) {
			Location to = e.getTo();
			if (e.getFrom().getBlock().equals(to.getBlock())) {
				return;
			}
			hook.updateLocation(e.getPlayer(), e.getTo());
		}
		
		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void on(PlayerRespawnEvent e) {
			hook.updateLocation(e.getPlayer(), e.getRespawnLocation());
		}
		
		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void on(PlayerTeleportEvent e) {
			hook.updateLocation(e.getPlayer(), e.getTo());
		}
		
		@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
		public void on(PlayerChangedWorldEvent e) {
			hook.updateLocation(e.getPlayer(), e.getPlayer().getLocation());
		}
	}
	

}
