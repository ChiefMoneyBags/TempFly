package com.moneybags.tempfly.fly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
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
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.combat.CombatHandler;
import com.moneybags.tempfly.environment.FlightEnvironment;
import com.moneybags.tempfly.event.FlightUserInitializedEvent;
import com.moneybags.tempfly.fly.RequirementProvider.InquiryType;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.user.UserLoader;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.Reloadable;

public class FlightManager implements Listener, Reloadable {

	private final TempFly tempfly;
	private final FlightEnvironment environment;
	//private StructureProximity structures;
	private final CombatHandler combat;

	private final List<RequirementProvider> providers = new LinkedList<>();

	public FlightManager(final TempFly tempfly) {
		this.tempfly = tempfly;

		providers.add(this.environment = new FlightEnvironment(this));
		providers.add(this.combat = new CombatHandler(this));
		//try { providers.add(this.structures = new StructureProximity(this)); } catch (Exception e) {
		//	e.printStackTrace();
		//}

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
	
	/**
	public StructureProximity getStructureProximity() {
		return this.structures;
	}
	*/

	@Override
	public void onTempflyReload() {
		for (RequirementProvider provider : providers) {
			if (provider instanceof TempFlyHook) {
				continue;
			}
			provider.onTempflyReload();
		}

		for (FlightUser user : getUsers()) {
			user.evaluateFlightRequirements(user.getPlayer().getLocation(), user.hasFlightEnabled());
			user.applySpeedCorrect(true, 0);
		}

	}

	/**
	 * 
	 * --=------------=-- User Control --=------------=--
	 * 
	 */

	private final Map<UUID, FlightUser> users = new HashMap<>();
	private final Map<UUID, UserLoader> loaders = new HashMap<>();

	public synchronized boolean hasUser(Player p) {
		return ((p != null) && users.containsKey(p.getUniqueId()))
				|| ((p != null) && loaders.containsKey(p.getUniqueId()) && loaders.get(p.getUniqueId()).isReady());
	}

	public synchronized FlightUser getUser(UUID u) {
		if (!users.containsKey(u) && loaders.containsKey(u) && loaders.get(u).isReady()) {
			UserLoader loader = loaders.get(u);
			loaders.remove(u);
			users.put(u, loader.buildUser());
		}
		return users.containsKey(u) ? users.get(u) : null;
	}

	public synchronized FlightUser getUser(Player p) {
		if (p == null) {
			return null;
		}

		UUID u = p.getUniqueId();
		if (!users.containsKey(u) && loaders.containsKey(u) && loaders.get(u).isReady()) {
			UserLoader loader = loaders.get(u);
			loaders.remove(u);
			users.put(u, loader.buildUser(p));
		}
		return users.containsKey(u) ? users.get(u) : null;
	}

	public synchronized FlightUser[] getUsers() {
		return users.values().toArray(new FlightUser[users.size()]);
	}

	/**
	 * Should be called asyncrounously
	 * 
	 * @param u
	 */
	public synchronized void addUser(UUID u, boolean async) {
		Console.debug("------Add User UUID------");
		if (!users.containsKey(u) && !loaders.containsKey(u)) {
			Console.debug("--| Starting to load player data...");
			UserLoader loader = new UserLoader(u, this, async);
			loaders.put(u, loader);
			if (async) {
				Bukkit.getScheduler().runTaskAsynchronously(tempfly, loader);
			} else {
				loader.run();
			}
		}
	}

	public synchronized void addUser(Player p) {
		Console.debug("------Add User Player------");
		UUID u = p.getUniqueId();
		if (p != null && users.containsKey(p.getUniqueId())) {
			Console.debug("--|> User is already registered!");
			return;
		}
		UserLoader loader = loaders.get(u);
		if (loader == null) {
			Console.debug(
					"--|> User has not been loaded! Starting loader async, This may cause NullPointers on player join event...");
			addUser(u, true);
			return;
		}
		loaders.remove(u);
		if (!p.isOnline()) {
			Console.debug("--| Player is no longer online...");
			if (users.containsKey(u)) {
				users.get(u).onQuit(false);
				users.remove(u);
			}
			return;
		}
		if (!users.containsKey(u)) {
			Console.debug("--| Building and registering the FlightUser...");
			FlightUser user = loader.buildUser();
			users.put(u, user);
			Bukkit.getScheduler().runTask(tempfly, () -> {
				// TODO change the order in which this occurs to prevent any indiscrepencies in
				// the requirementproviders if the time gets changed by time manager on user
				// join.
				for (RequirementProvider provider : providers) {
					provider.onUserInitialized(user);
				}
				Bukkit.getServer().getPluginManager().callEvent(new FlightUserInitializedEvent(user));
			});
		}
	}

	public synchronized void removeUser(Player p, boolean reload) {
		removeUser(p.getUniqueId(), reload);
	}

	public synchronized void removeUser(UUID u, boolean reload) {
		if (users.containsKey(u)) {
			users.get(u).onQuit(reload);
			users.remove(u);
		}
		if (loaders.containsKey(u)) {
			loaders.remove(u);
		}
	}

	/**
	 * Called on plugin disable, saves users and cleans up.
	 */
	public void onDisable() {
		for (FlightUser user : getUsers()) {
			removeUser(user.getPlayer(), true);
		}
	}

	/**
	 * 
	 * --=------------=-- Requirements --=------------=--
	 * 
	 * The flight inquiry methods in the FlightManager will process the requirements
	 * from every RequirementProvider available. All of the hooks, the environment
	 * tracker for disabled regions etc...
	 * 
	 * --=------------=--
	 */

	/**
	 * Register a new requirement provider with tempfly. All users will
	 * automatically be updated with the new requirements.
	 * 
	 * @param provider
	 *            The new requirements
	 */
	public void registerRequirementProvider(RequirementProvider provider) {
		if (providers.contains(provider)) {
			throw new IllegalArgumentException("A requirement provider can only be registered once!");
		}
		providers.add(provider);
		for (FlightUser user : getUsers()) {
			user.evaluateFlightRequirement(provider, user.getPlayer().getLocation());
		}
	}

	/**
	 * unregister an existing requirement provider in tempfly. All users will
	 * automatically be updated and the requirements removed.
	 * 
	 * @param provider
	 *            The new requirements
	 */
	public void unregisterRequirementProvider(RequirementProvider provider) {
		if (providers.remove(provider)) {
			for (FlightUser user : getUsers()) {
				if (user.removeFlightRequirement(provider)) {
					user.updateRequirements(V.requirePassDefault);
				}
			}
		}
	}

	/**
	 * Check if a player can fly in a set of given regions.
	 * 
	 * @param user
	 * @param regions
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, CompatRegion[] regions) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement : providers) {
			if (requirement.handles(InquiryType.REGION)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, regions));
		}
		return results;
	}

	/**
	 * Check if a player can fly in a single region.
	 * 
	 * @param user
	 * @param r
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, CompatRegion region) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement : providers) {
			if (requirement.handles(InquiryType.REGION)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, region));
		}
		return results;
	}

	/**
	 * Check if a player can fly in a world.
	 * 
	 * @param user
	 * @param world
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, World world) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement : providers) {
			if (requirement.handles(InquiryType.WORLD)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, world));
		}
		return results;
	}

	/**
	 * Check if a player can fly at a given location and process all requirements
	 * for said location. Does not check regions and worlds, you need to use the
	 * specified methods for regions and worlds.
	 * 
	 * @param user
	 * @param loc
	 * @param invokeHooks
	 * @return
	 */
	public List<FlightResult> inquireFlight(FlightUser user, Location loc) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement : providers) {
			if (requirement.handles(InquiryType.LOCATION)) {
				continue;
			}
			results.add(requirement.handleFlightInquiry(user, loc));
		}
		return results;
	}
	
	public List<FlightResult> inquireFlightBeyondScope(FlightUser user) {
		List<FlightResult> results = new ArrayList<>();
		for (RequirementProvider requirement : providers) {
			results.add(requirement.handleFlightInquiry(user));
		}
		return results;
	}

	/**
	 * 
	 * --=--------------=-- Event Handling --=--------------=--
	 * 
	 * General event handling for users.
	 * 
	 * Provides location tracking for users and handles flight inquiries based on
	 * location.
	 * 
	 * --=--------------=--
	 * 
	 */

	/**
	 * Method to tie in with the EventHandlers. Responsible for all location
	 * tracking in the plugin. Takes in a player and their new location. Proceeds to
	 * process the information for all RequirementProviders and handles flight
	 * restrictions.
	 * 
	 * This method will probably be the bulk of tempfly's resource usage, it looks
	 * like its doing alot but realistically it wont do much unless some server goes
	 * way overboard with stacking up features from the config. like adding hundreds
	 * of ReletiveTimeRegions and having multiple hooks enabled.
	 * 
	 * @param p
	 *            The player to process
	 * @param to
	 *            The new location
	 */
	public void updateLocation(FlightUser user, Location from, Location to, boolean forceWorld, boolean forceRegion) {
		if (V.bugInfiniteA) {
			if (user.getPlayer().isFlying()
					&& !user.hasTimer()
					&& !user.getPlayer().hasPermission("tempfly.workaround.infinite.bypass.fix_a")) {
				user.enforce(0);
			}
		} else if (V.bugInfiniteB) {
			Console.debug(0);
			if (user.getPlayer().isFlying()
					&& !user.hasTimer()
					&& !user.getPlayer().hasPermission("tempfly.workaround.infinite.bypass.fix_b")) {
				if (!user.enableFlight()) {
					user.enforce(0);
				}
			}
		}
		
		final List<FlightResult> results = new ArrayList<>();

		if (getTempFly().getHookManager().hasRegionProvider()) {
			List<CompatRegion> regions = Arrays
					.asList(getTempFly().getHookManager().getRegionProvider().getApplicableRegions(to));
			if (forceRegion || !user.getEnvironment().checkIdenticalRegions(regions)) {
				// Process regions
				results.addAll(inquireFlight(user, regions.toArray(new CompatRegion[regions.size()])));
				// Update the users current regions.
				user.getEnvironment().updateCurrentRegionSet(regions.toArray(new CompatRegion[regions.size()]));
				
				if (user.hasFlightEnabled()) {
					user.applySpeedCorrect(true, 0);	
				}
			}
		}

		// Check flight requirements if player entered a new world.
		// Process world
		if (!from.getWorld().equals(to.getWorld()) || forceWorld) {
			results.addAll((inquireFlight(user, to.getWorld())));
			user.getEnvironment().asessRtWorld();
			user.getEnvironment().asessInfiniteFlight();
		}
		// Check flight requirements at player location. Doesn't really do anything if
		// no hooks are enabled.
		// Used mainly for things like islands in skyblock, faction land, etc...
		// Process location
		results.addAll(inquireFlight(user, user.getPlayer().getLocation()));

		// Submit the flight results and see if auto fly can be enabled.
		user.submitFlightResults(results, user.hasFlightEnabled());
	}

	/**
	 * Evaluate flight requirements on teleport
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onTeleport(PlayerTeleportEvent e) {
		Console.debug("------on teleport------", "--|> " + e.getPlayer().getUniqueId());
		if (!hasUser(e.getPlayer())) {
			return;
		}
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {
			return;
		}
		user.resetIdleTimer();
		if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) {
			updateLocation(user, e.getFrom(), e.getTo(), false, false);
		}
		user.applyFlightCorrect();
	}

	/**
	 * Evaluate flight requirements on respawn
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e) {
		if (!hasUser(e.getPlayer())) {
			return;
		}
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {
			return;
		}
		user.resetIdleTimer();
		updateLocation(user, e.getPlayer().getLocation(), e.getRespawnLocation(), false, false);
		// If the user has flight enabled, we need to correct their speed so it doesnt
		// reset to 1.
		if (user.hasFlightEnabled()) {
			user.applyFlightCorrect();
			user.applySpeedCorrect(false, 1);
		}
		user.enforce(1);
	}

	/**
	 * Evaluate flight requirements on changing worlds.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChangedWorld(PlayerChangedWorldEvent e) {
		if (!hasUser(e.getPlayer())) {
			return;
		}
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {
			return;
		}
		user.resetIdleTimer();
		// The from coordinate really doesn't matter here, just the world.
		updateLocation(user, new Location(e.getFrom(), 0, 0, 0), user.getPlayer().getLocation(), true, false);
		// If the user has flight enabled, we need to correct their speed so it doesnt
		// reset to 1.
		if (user.hasFlightEnabled()) {
			user.applyFlightCorrect();
			user.applySpeedCorrect(true, 10);

		}
		// TODO flight cannot just be enforced on every world change as it will break
		// essentials fly compatibility. Must be enforced
		// when something happens to actually disable the flight, impossible to check if
		// it should be disabled here...

		// else if (!user.hasFlightEnabled() && user.getPlayer().getAllowFlight()){
		// user.enforce(1);
		// }
	}

	/**
	 * Fix the players flight when they change gamemodes.
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChangedGamemode(PlayerGameModeChangeEvent e) {
		if (!hasUser(e.getPlayer())) {
			return;
		}
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {
			return;
		}
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
	 * 
	 * @param e
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent e) {
		Entity vic = e.getEntity();
		if (!e.getCause().equals(DamageCause.FALL) || !(vic instanceof Player)) {
			return;
		}
		FlightUser user = getUser((Player) vic);
		if (user == null) {
			return;
		}
		user.resetIdleTimer();
		if (!user.hasDamageProtection()) {
			return;
		}
		e.setCancelled(true);
		user.removeDamageProtection();
	}

	/**
	 * I will listen to player join on lowest priority so that i can initialize the
	 * flight user before other plugins try to access it on higher priorities.
	 */
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onJoin(PlayerJoinEvent e) {
		Console.debug("------------ On PlayerjOIN event ------------");
		addUser(e.getPlayer());
	}

	/**
	 * @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = false)
	 *               public void onSpawn(PlayerSpawnLocationEvent e) {
	 *               e.setSpawnLocation(new Location(e.getPlayer().getWorld(), 100,
	 *               200, 100)); Console.debug("------------ On PlayerSpawn event
	 *               ------------"); Console.debug("--------------->>>>>>>>> " +
	 *               String.valueOf(Bukkit.getPlayer(e.getPlayer().getUniqueId())));
	 *               //e.getPlayer().teleport(new Location(e.getPlayer().getWorld(),
	 *               100, 200, 100)); e.getPlayer().setGameMode(GameMode.SPECTATOR);
	 * 
	 *               }
	 */

	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPreLogin(AsyncPlayerPreLoginEvent e) {
		Console.debug("------------ On AsyncPlayerPreLogin event ------------");
		addUser(e.getUniqueId(), false);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onAsyncPreLoginMonitor(AsyncPlayerPreLoginEvent e) {
		UUID u = e.getUniqueId();
		if (e.getLoginResult() != Result.ALLOWED) {
			users.get(u);
			removeUser(u, false);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		removeUser(p, false);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent e) {
		if (!e.getFrom().getBlock().equals(e.getTo().getBlock())) {
			FlightUser user = getUser(e.getPlayer());
			if (user == null) {
				return;
			}
			user.resetIdleTimer();
			updateLocation(user, e.getFrom(), e.getTo(), false, false);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {
			return;
		}
		user.resetIdleTimer();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onAsyncChat(AsyncPlayerChatEvent e) {
		FlightUser user = getUser(e.getPlayer());
		if (user == null) {
			return;
		}
		user.resetIdleTimer();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onInventoryClick(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			FlightUser user = getUser((Player) e.getWhoClicked());
			if (user == null) {
				return;
			}
			user.resetIdleTimer();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void onFlyMonitor(PlayerToggleFlightEvent e) {
		Console.debug("----------- Monitor ------------", e.getPlayer().getName() + " toggled flight!", "Cancelled: " + e.isCancelled(), "Flying: " + e.isFlying());
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void onFly(PlayerToggleFlightEvent e) {
		Console.debug("----------- Normal ------------", e.getPlayer().getName() + " toggled flight!", "Cancelled: " + e.isCancelled(), "Flying: " + e.isFlying());
	}
}
