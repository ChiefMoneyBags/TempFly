package com.moneybags.tempfly.fly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.combat.CombatHandler;
import com.moneybags.tempfly.environment.FlightEnvironment;
import com.moneybags.tempfly.fly.RequirementProvider.InquiryType;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.Reloadable;

public class FlightManager implements Listener, Reloadable {

	private final TempFly tempfly;
	private final FlightEnvironment environment;
	private final CombatHandler combat;
	
	private final Map<Player, FlightUser> users = new HashMap<>();
	private final List<RequirementProvider> providers = new LinkedList<>();
	
	public FlightManager(final TempFly tempfly) {
		this.tempfly = tempfly;
		
		providers.add(this.environment = new FlightEnvironment(this));
		providers.add(this.combat = new CombatHandler(this));
		
		tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
	}// /tf give 1m
	
	public TempFly getTempFly() {
		return tempfly;
	}
	
	public FlightEnvironment getFlightEnvironment() {
		return environment;
	}
	
	public CombatHandler getCombatHandler() {
		return combat;
	}
	
	@Override
	public void onTempflyReload() {
		for (RequirementProvider provider: providers) {
			provider.onTempflyReload();
		}
		
		for (FlightUser user: getUsers()) {
			user.evaluateFlightRequirements(user.getPlayer().getLocation(), user.hasFlightEnabled());
		}
		
	}
	
	
	/**
	 * 
	 * --=------------=--
	 *    User Control
	 * --=------------=--
	 * 
	 */
	
	public FlightUser getUser(Player p) {
		return users.containsKey(p) ? users.get(p) : null;
	}
	
	public FlightUser[] getUsers() {
		return users.values().toArray(new FlightUser[users.size()]);
	}
	
	public FlightUser addUser(Player p) {
		if (!users.containsKey(p)) {
			FlightUser user = new FlightUser(p, this);
			users.put(p, user);
			for (RequirementProvider provider: providers) {
				provider.onUserInitialized(user);
			}
			return user;
		}
		return users.get(p);
	}
	
	public void removeUser(Player p, boolean reload) {
		if (users.containsKey(p)) {
			users.get(p).onQuit(reload);
			users.remove(p);
		}
	}
	
	/**
	 * Called on plugin disable, saves users and cleans up.
	 */
	public void onDisable() {
		for (FlightUser user: getUsers()) {
			removeUser(user.getPlayer(), true);
		}
	}
	
	
	
	/**
	 * 
	 * --=------------=--
	 *    Requirements
	 * --=------------=--
	 * 
	 * The flight inquiry methods in the FlightManager will process the requirements
	 * from every RequirementProvider available. 
	 * All of the hooks, the environment tracker for disabled regions etc...
	 * 
	 * --=------------=--
	 */
	
	
	/**
	 * Register a new requirement provider with tempfly.
	 * All users will automatically be updated with the new requirements.
	 * @param provider The new requirements
	 */
	public void registerRequirementProvider(RequirementProvider provider) {
		if (providers.contains(provider)) {
			throw new IllegalArgumentException("A requirement provider can only be registered once!");
		}
		providers.add(provider);
		for (FlightUser user: getUsers()) {
			user.evaluateFlightRequirement(provider, user.getPlayer().getLocation());
		}
	}
	
	/**
	 * unregister an existing requirement provider in tempfly.
	 * All users will automatically be updated and the requirements removed.
	 * @param provider The new requirements
	 */
	public void unregisterRequirementProvider(RequirementProvider provider) {
		if (providers.remove(provider)) {
			for (FlightUser user: getUsers()) {
				if (user.removeFlightRequirement(provider)) {
					user.updateRequirements(V.requirePassDefault);
				}
			}
		}
	}
	
	/**
	 * Check if a player can fly in a set of given regions.
	 * @param user
	 * @param regions
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, CompatRegion[] regions) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement: providers) {
			if (requirement.handles(InquiryType.REGION)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, regions));
		}
		return results;
	}
	
	/**
	 * Check if a player can fly in a single region.
	 * @param user
	 * @param r
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, CompatRegion region) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement: providers) {
			if (requirement.handles(InquiryType.REGION)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, region));
		}
		return results;
	}
	
	/**
	 * Check if a player can fly in a world.
	 * @param user
	 * @param world
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, World world) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement: providers) {
			if (requirement.handles(InquiryType.WORLD)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, world));
		}
		return results;
	}
	
	/**
	 * Check if a player can fly at a given location and process all requirements for said location.
	 * Does not check regions and worlds, you need to use the specified methods for regions and worlds.
	 * @param user
	 * @param loc
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, Location loc) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement: providers) {
			if (requirement.handles(InquiryType.LOCATION)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, loc));
		}
		return results;
	}
	
	
	
	/**
	 * 
	 * --=--------------=--
	 *    Event Handling
	 * --=--------------=--
	 * 
	 * General event handling for users.
	 * 
	 * Provides location tracking for users and handles
	 * flight inquiries based on location.
	 * 
	 * --=--------------=--
	 * 
	 */
	
	/**
	 * Method to tie in with the EventHandlers. Responsible for all location tracking in the plugin.
	 * Takes in a player and their new location.
	 * Proceeds to process the information for all RequirementProviders and handles flight restrictions.
	 * 
	 *  This method will probably be the bulk of tempfly's resource usage, it looks like its doing alot
	 *  but realistically it wont do much unless some server goes way overboard with stacking up features
	 *  from the config. like adding hundreds of ReletiveTimeRegions and having multiple hooks enabled.
	 * @param p The player to process
	 * @param to The new location
	 */
	public void updateLocation(FlightUser user, Location from, Location to, boolean forceWorld) {
		final List<FlightResult> results = new ArrayList<>();
		
		if (getTempFly().getHookManager().hasRegionProvider()) {
			List<CompatRegion> regions = Arrays.asList(getTempFly().getHookManager().getRegionProvider().getApplicableRegions(to));
			if (!user.getEnvironment().checkIdenticalRegions(regions)) {
				// Process regions
				results.addAll(inquireFlight(user, regions.toArray(new CompatRegion[regions.size()])));
				// Update the users current regions.
				user.getEnvironment().updateCurrentRegionSet(regions.toArray(new CompatRegion[regions.size()]));	
			}
		}
		
		// Check flight requirements if player entered a new world.
		// Process world
		if (!from.getWorld().equals(to.getWorld()) || forceWorld) {
			results.addAll((inquireFlight(user, to.getWorld())));
			user.getEnvironment().asessRtWorld();
		}
		// Check flight requirements at player location. Doesn't really do anything if no hooks are enabled.
		// Used mainly for things like islands in skyblock, faction land, etc...
		// Process location
		results.addAll(inquireFlight(user, user.getPlayer().getLocation()));
		
		
		// Submit the flight results and see if auto fly can be enabled.
		user.submitFlightResults(results, user.hasFlightEnabled());
	}
	
	/**
	 * Evaluate flight requirements on teleport
	 */
	@EventHandler (priority = EventPriority.MONITOR)
	public void on(PlayerTeleportEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {return;}
		user.resetIdleTimer();
		if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) {
			updateLocation(user, e.getFrom(), e.getTo(), false);
		}
	}
	
	/**
	 * Evaluate flight requirements on respawn
	 */
	@EventHandler (priority = EventPriority.MONITOR)
	public void on(PlayerRespawnEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {return;}
		user.resetIdleTimer();
		updateLocation(user, e.getPlayer().getLocation(), e.getRespawnLocation(), false);
		// If the user has flight enabled, we need to correct their speed so it doesnt reset to 1.
		if (user.hasFlightEnabled()) {
			user.applyFlightCorrect();
			user.applySpeedCorrect();
		}
		user.enforce(1);
	}
	
	/**
	 * Evaluate flight requirements on changing worlds.
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerChangedWorldEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {return;}
		user.resetIdleTimer();
		// The from coordinate really doesn't matter here, just the world.
		updateLocation(user, new Location(e.getFrom(), 0, 0, 0), user.getPlayer().getLocation(), true);
		// If the user has flight enabled, we need to correct their speed so it doesnt reset to 1.
		if (user.hasFlightEnabled()) {
			user.applyFlightCorrect();
			user.applySpeedCorrect();
		}
		user.enforce(1);
	}
	
	/**
	 * Fix the players flight when they change gamemodes.
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerGameModeChangeEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {return;}
		user.resetIdleTimer();
		user.applyFlightCorrect();
		if (e.getNewGameMode() == GameMode.CREATIVE && V.creativeTimer) {
			if (!user.hasFlightEnabled() && !user.enableFlight()) {
				user.enforce(1);
			}
		}
	}
	
	/**
	 * Handles removal of damage protection.
	 * @param e
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(EntityDamageEvent e) {
		Entity vic = e.getEntity();
		if (!e.getCause().equals(DamageCause.FALL) || !(vic instanceof Player)) {
			return;
		}
		FlightUser user = getUser((Player)vic);
		if (user == null) {return;}
		user.resetIdleTimer();
		if (!user.hasDamageProtection()) {return;}
		e.setCancelled(true);
		user.removeDamageProtection();
	}
	
	/**
	 *  I will listen to player join on lowest priority so that i can initialize the flight user before
	 *  other plugins try to access it on higher priorities.
	 */
	@EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void on(PlayerJoinEvent e) {
		addUser(e.getPlayer());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		removeUser(p, false);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerMoveEvent e) {
		if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) {
			FlightUser user = getUser(e.getPlayer());
			if (user == null) {return;}
			user.resetIdleTimer();
			updateLocation(user, e.getFrom(), e.getTo(), false);
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerInteractEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {return;}
		user.resetIdleTimer();
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(AsyncPlayerChatEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {return;}
		user.resetIdleTimer();
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			FlightUser user = getUser((Player)e.getWhoClicked());
			if (user == null) {return;}
			user.resetIdleTimer();
		}
	}

}
