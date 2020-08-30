package com.moneybags.tempfly.hook.skyblock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.FlightResult.DenyReason;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.moneybags.tempfly.hook.HookManager.Genre;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.CompatMaterial;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

/**
 * SkyblockHook is a class that represents an object who's purpose is to introduce
 * flight requirements centered around a skyblock plugin. The logic, config, data and requirements
 * are all taken care of for you. simply extend the class and do the following;
 * 
 * 1) Fill in the abstract methods introduced by this class, do not return null unless it is otherwise specified you may do so.
 * 2) Invoke the super methods onIslandEnter and onIslandExit when players enter and leave islands.
 * 3) Profit?
 */
public abstract class SkyblockHook extends TempFlyHook {

	private Map<String, Boolean> basePerms;
	private boolean wilderness, settingsHook;
	private ItemStack settingsButton;
	public String
	requireIsland,
	requireLevelSelf,
	requireLevelOther,
	requireChallengeSelf,
	requireChallengeOther;
	
	
	
	private Map<SkyblockRequirementType, SkyblockRequirement[]> requirements;
	
	public SkyblockHook(TempFly plugin) {
		super(plugin);
	}
	
	@Override
	public void onTempflyReload() {
		super.onTempflyReload();
		loadValues();
		for (FlightUser user: getTempFly().getFlightManager().getUsers()) {
			user.submitFlightResult(checkFlightRequirements(user.getPlayer().getUniqueId(), user.getPlayer().getLocation()));
		}
	}
	
	@Override
	public boolean initializeFiles() throws Exception {
		if (!super.initializeFiles()) {
			return false;
		}
		//tempfly.getDataBridge().initializeHookData(this, DataTable.ISLAND_SETTINGS);
		return true;
	}
	
	@Override
	public boolean initializeHook() {
		loadValues();
		return true;
	}
	
	
	public void loadValues() {
		Console.debug("", "----Loading Skyblock Settings----");
		FileConfiguration config = getConfig();
		basePerms = new HashMap<>();
		requirements = new HashMap<>();
		this.wilderness = config.getBoolean("flight_settings.wilderness");
		String pathPerms = "flight_settings.base_permissions";
		ConfigurationSection csPerms = config.getConfigurationSection(pathPerms);
		if (csPerms != null) {
			for (String key: csPerms.getKeys(false)) {
				basePerms.put(key, config.getBoolean(pathPerms + "." + key));
			}	
		}
		
		String name = getConfigName();
		requireIsland			= V.st(config, "language.invalid.island", name);
		requireChallengeSelf	= V.st(config, "language.requirements.challenge_self", name);
		requireChallengeOther	= V.st(config, "language.requirements.challenge_other", name);
		requireLevelSelf		= V.st(config, "language.requirements.level_self", name);
		requireLevelOther		= V.st(config, "language.requirements.level_other", name);
		
		settingsHook			= config.getBoolean("gui.hook_settings.enabled");
		settingsButton			= U.getConfigItem(config, "gui.hook_settings.button");
		CompatMaterial.setType(settingsButton, CompatMaterial.FEATHER);
		
		String path;
		if (config.contains(path = "unlockables.environment.wilderness")) {
			Console.debug("", "<< Loading wilderness requirements >>");
			
			requirements.put(SkyblockRequirementType.WILDERNESS, new SkyblockRequirement[] {
					new SkyblockRequirement(
							loadChallenges(config, path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							null, SkyblockRequirementType.WILDERNESS)
			});
		}
		
		ConfigurationSection csRequireWorld = config.getConfigurationSection("unlockables.environment.worlds");
		if (csRequireWorld != null) {
			Console.debug("", "<< Loading world requirements >>");
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String world: csRequireWorld.getKeys(false)) {
				path = "unlockables.environment.worlds." + world;
				list.add(new SkyblockRequirement(
							loadChallenges(config, path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							world, SkyblockRequirementType.WORLD));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.WORLD, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		
		ConfigurationSection csRequireRegion = config.getConfigurationSection("unlockables.environment.regions");
		if (csRequireRegion != null) {
			Console.debug("", "<< Loading region requirements >>");
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String region: csRequireRegion.getKeys(false)) {
				path = "unlockables.environment.regions." + region;
				list.add(new SkyblockRequirement(
							loadChallenges(config, path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							region, SkyblockRequirementType.REGION));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.REGION, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		
		ConfigurationSection csRequireRole = config.getConfigurationSection("unlockables.island_roles");
		if (csRequireRole != null) {
			Console.debug("", "<< Loading island_role requirements >>");
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String role: csRequireRole.getKeys(false)) {
				if (!islandRoleExists(role)) {
					Console.severe("An island role specified in the config does not exist (" + role + "). Skipping...");
				}
				
				path = "unlockables.island_roles." + role;
				list.add(new SkyblockRequirement(
							loadChallenges(config, path + ".challenges"), loadChallenges(config, path + ".owner_challenges"),
							config.getLong(path + ".island_level"), config.getLong(path + ".owner_level"),
							role.toUpperCase(), SkyblockRequirementType.ISLAND_ROLE));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.ISLAND_ROLE, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		Console.debug("----END Skyblock Settings----", "");
		PageIslandSettings.initialize(this);
	}
	
	private SkyblockChallenge[] loadChallenges(FileConfiguration config, String path) {
		ConfigurationSection csChallenges = config.getConfigurationSection(path);
		List<SkyblockChallenge> challenges = new ArrayList<>();
		if (csChallenges != null) {
			for (String key: csChallenges.getKeys(false)) {
				challenges.add(new SkyblockChallenge(key, 
						config.getInt(path + "." + key + ".progress", 0),
						config.getInt(path + "." + key + ".completed", 0)));
			}
			
		}
		return challenges.size() > 0 ? challenges.toArray(new SkyblockChallenge[challenges.size()]) : null;
	}
	
	/**
	 * 
	 * Global Values
	 * 
	 */
	
	public boolean hasSettingsHook() {
		return settingsHook;
	}
	
	public ItemStack getSettingsButton() {
		return settingsButton;
	}
	
	public boolean canFlyWilderness() {
		return wilderness;
	}
	
	public Map<String, Boolean> getDefaults() {
		return basePerms;
	}
	
	public boolean hasRequirement(SkyblockRequirementType type) {
		return requirements.containsKey(type);
	}
	
	public SkyblockRequirement[] getRequirements(SkyblockRequirementType type) {
		return requirements.get(type);
	}
	
	public SkyblockRequirement[] getRequirements(CompatRegion[] regions) {
		List<SkyblockRequirement> found = new ArrayList<>();
		SkyblockRequirement[] iter = getRequirements(SkyblockRequirementType.REGION);
		for (CompatRegion region: regions) {
			for (SkyblockRequirement require: iter) {
				if (region.getId().equals(require.getName())) {
					found.add(require);
				}
			}
		}
		return found.toArray(new SkyblockRequirement[found.size()]);
	}
	
	public boolean hasRequirement(SkyblockRequirementType type, String name) {
		if (!hasRequirement(type)) {
			Console.debug(false);
			return false;
		}
		for (SkyblockRequirement require: getRequirements(type)) {
			if (require.getName().equals(name)) {
				Console.debug(true);
				return true;
			}
		}
		Console.debug(false);
		return false;
	}
	
	public SkyblockRequirement getRequirement(SkyblockRequirementType type, String name) {
		for (SkyblockRequirement require: getRequirements(type)) {
			if (require.getName().equals(name)) {
				return require;
			}
		}
		return null;
	}
	
	
	/**
	 * 
	 * Island Settings
	 * 
	 */
	private Map<Player, IslandWrapper> locationCache = new HashMap<>();
	private Map<String, IslandWrapper> wrapperCache = new WeakHashMap<>();
	
	public IslandWrapper getIslandWrapper(Object rawIsland) {
		if (rawIsland == null) {
			return null;
		}
		Console.debug(wrapperCache);
		IslandWrapper wrapper;
		if (!wrapperCache.containsKey(getIslandIdentifier(rawIsland))) {
			wrapper = new IslandWrapper(rawIsland, this);
			wrapperCache.put(getIslandIdentifier(rawIsland), wrapper);
		} else {
			 wrapper = wrapperCache.get(getIslandIdentifier(rawIsland));
		}
		return wrapper;
	}
	
	/**
	 * This method is called by the children of SkyblockHook when a player enters an island.
	 * It will track the island each player is on and handle flight requirements for the player.
	 * 
	 * @param p The player who entered the island.
	 * @param rawIsland The raw object representing the island. A wrapper will be created for it.
	 * @param loc The location where the player entered the island.
	 */
	public void onIslandEnter(Player p, Object rawIsland, Location loc) {
		if (V.debug) {
			Console.debug("", "------ On Island Enter ------", "--| Player: " + p.getName());
		}
		if (locationCache.containsKey(p)) {
			// Player already being tracked.
			if (locationCache.get(p).getIsland() == rawIsland) {
				// Player is already on this island...
				return;
			}
			// Player is now on 2 islands at once, this is a bug.
			Console.severe("If you are seeing this message there may be a bug. Please contact the tempfly dev with this info: SkyblockHook | onIslandEnter()");
			IslandWrapper island = locationCache.get(p);
			locationCache.remove(p);
			if(!locationCache.containsValue(island)) {
				wrapperCache.remove(rawIsland);
			}
		}
		
		IslandWrapper island = getIslandWrapper(rawIsland);
		if (V.debug) {Console.debug("--|> Island Identifier: " + getIslandIdentifier(rawIsland), "------ End Island Enter ------", "");}
		
		locationCache.put(p, island);
		FlightUser user = tempfly.getFlightManager().getUser(p);
		user.submitFlightResult(checkFlightRequirements(p.getUniqueId(), island));
	}
	
	/**
	 * This method is called by the children of SkyblockHook when a player enters an island.
	 * It will track the island each player is on and handle flight requirements for the player.
	 * 
	 * @param p The player who entered the island.
	 */
	public void onIslandExit(final Player p) {
		if (V.debug) {
			Console.debug("", "------ On Island Exit ------", "--| Player: " + p.getName());
		}
		if (locationCache.containsKey(p)) {
			IslandWrapper currentIsland = locationCache.get(p);
			locationCache.remove(p);
			if(!locationCache.containsValue(currentIsland)) {
				wrapperCache.remove(getIslandIdentifier(currentIsland.getIsland()));
			}
			if (V.debug) {Console.debug("--|> Island Identifier: " + getIslandIdentifier(currentIsland.getIsland()), "------ End Island Exit ------", "");}
		}
		// On island exit i will wait 1 tick then check if the player still has this requirement.
		// If they are no longer on an island it will be removed.
		final RequirementProvider provider = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!p.isOnline()) {
					return;
				}
				FlightUser user = tempfly.getFlightManager().getUser(p);
				if (user.hasFlightRequirement(provider, InquiryType.OUT_OF_SCOPE) && !locationCache.containsKey(user.getPlayer())) {
					user.submitFlightResult(new ResultAllow(provider, InquiryType.OUT_OF_SCOPE, V.requirePassDefault));
				}
			}
		}.runTaskLater(tempfly, 1);
	}
	
	/**
	 * @param p The player leaving the island.
	 */
	public void onIslandExitManual(Player p) {
		if (V.debug) {
			Console.debug("", "------ On Island Exit Manual ------", "Player: " + p.getName());
		}
		IslandWrapper currentIsland = null;
		if (locationCache.containsKey(p)) {
			currentIsland = locationCache.get(p);
			locationCache.remove(p);
			if(!locationCache.containsValue(currentIsland)) {
				wrapperCache.remove(getIslandIdentifier(currentIsland.getIsland()));
			}
		}
	}
	
	
	/**
	 * This method is called by the children of SkyblockHook when an island level is updated or changes.
	 * It will re-evaluate flight requirements of applicable players.
	 * 
	 * @param island The island.
	 */
	public void onIslandLevelChange(IslandWrapper island) {
		List<Player> members = Arrays.asList(getOnlineMembers(island)); 
		for (Player p: getPlayersOn(island)) {
			if (members.contains(p)) {
				continue;
			}
			FlightUser user = getTempFly().getFlightManager().getUser(p);
			user.submitFlightResult(checkRoleRequirements(p.getUniqueId(), island));
		}
		for (Player p: members) {
			FlightUser user = getTempFly().getFlightManager().getUser(p);
			user.submitFlightResult(checkFlightRequirements(p.getUniqueId(), p.getLocation()));
		}
	}
	
	/**
	 * This method is called by the children of SkyblockHook when a player completes a challenge.
	 * It will re-evaluate flight requirements of the player.
	 * 
	 * @param island The island.
	 */
	public void onChallengeComplete(Player p) {
		checkFlightRequirements(p.getUniqueId(), p.getLocation());
		IslandWrapper island = getIslandOwnedBy(p.getUniqueId());
		if (island != null) {
			for (Player onIsland: getPlayersOn(island)) {
				FlightUser user = getTempFly().getFlightManager().getUser(onIsland);
				user.submitFlightResult(checkRoleRequirements(onIsland.getUniqueId(), island));
			}
		}
	}
	
	/**
	 * Get all the players that are currently being tracked on an island.
	 * @param island The island in question
	 * @return All the players currently on the island.
	 */
	public Player[] getPlayersOn(IslandWrapper island) {
		List<Player> players = new ArrayList<>();
		for (Map.Entry<Player, IslandWrapper> entry: locationCache.entrySet()) {
			if (entry.getValue().equals(island)) {
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
	public IslandWrapper getTrackedIsland(Player p) {
		return locationCache.get(p);
	}
	
	@Override
	public void onUserInitialized(FlightUser user) {
		Player p = user.getPlayer();
		IslandWrapper island = getIslandAt(p.getLocation());
		if (island != null) {
			onIslandEnter(p, island.getIsland(), p.getLocation());
		}
	}
	
	@Override
	public void onUserQuit(FlightUser user) {
		Player p = user.getPlayer();
		IslandWrapper island = getIslandAt(p.getLocation());
		if (island != null) {
			onIslandExit(p);
		}
	}
	
	/**
	 * 
	 * Island Requirements
	 *
	 */
	
	
	public static enum SkyblockRequirementType {
		REGION,
		WORLD,
		WILDERNESS,
		ISLAND_ROLE;
	}
	
	/**
	 * Process the requirements for a player to check if a player can fly on the given island.
	 * @param ir The requirement to check
	 * @param island The island the player is trying to fly on.
	 * @param p The player trying to fly.
	 * @return The flight result.
	 */
	public FlightResult runRequirement(SkyblockRequirement ir, IslandWrapper island, UUID u) {
		Console.debug("", "-----Running island flight requirements-----");
		if (ir.getIslandLevel() > 0 && ir.getIslandLevel() > getIslandLevel(u)) {
			if (V.debug) {
				Console.debug("fail island level: " + ir.getIslandLevel() + " / " + getIslandLevel(u), "-----End flight requirements-----", "");
			}
			return new ResultDeny(DenyReason.REQUIREMENT, this, null,
					requireLevelSelf
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getIslandLevel()))
					.replaceAll("\\{ROLE}", ir.getName()), true);
		}
		
		if (ir.getOwnerLevel() > 0 && ir.getOwnerLevel() > getIslandLevel(getIslandOwner(island))) {
			if (V.debug) {
				Console.debug("fail island level: " + ir.getOwnerLevel() + " / " + getIslandLevel(getIslandOwner(island)), "-----End flight requirements-----", "");	
			}
			return new ResultDeny(DenyReason.REQUIREMENT, this, null,
					requireLevelOther
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getOwnerLevel()))
					.replaceAll("\\{ROLE}", ir.getName()), true);
		}
		
		for (SkyblockChallenge challenge : ir.getChallenges()) {
			if (!isChallengeCompleted(u, challenge)) {
				if (V.debug) {
					Console.debug("fail island challenge: " + challenge, "-----End flight requirements-----", "");	
				}
				return new ResultDeny(DenyReason.REQUIREMENT, this, null,
						requireChallengeSelf
						.replaceAll("\\{CHALLENGE}", challenge.getName())
						.replaceAll("\\{PROGRESS}", String.valueOf(challenge.getRequiredProgress()))
						.replaceAll("\\{ROLE}", ir.getName()), true);
			}
		}	
		for (SkyblockChallenge challenge : ir.getOwnerChallenges()) {
			if (!isChallengeCompleted(getIslandOwner(island), challenge)) {
				if (V.debug) {
					Console.debug("fail island challenge | island owner: " + challenge, "-----End flight requirements-----", "");	
				}
				return new ResultDeny(DenyReason.REQUIREMENT, this, null,
						requireChallengeOther
						.replaceAll("\\{CHALLENGE}", challenge.getName())
						.replaceAll("\\{PROGRESS}", String.valueOf(challenge.getRequiredProgress()))
						.replaceAll("\\{ROLE}", ir.getName()), true);
			}
		}	
		return new ResultAllow(this, null, V.requirePassDefault);
	}
	
	/**
	 * Check the role requirements of a player on a specific island.
	 * @param u
	 * @param island
	 * @return
	 */
	public FlightResult checkRoleRequirements(UUID u, IslandWrapper island) { 
		Console.debug("", "--- SkyblockHook check role requirements ---");
		String role = getIslandRole(u, island);
		Console.debug("--| Role: " + role);
		IslandSettings settings = island.getSettings();	
		return !settings.canFly(role) ? new ResultDeny(DenyReason.DISABLED_REGION, this, InquiryType.OUT_OF_SCOPE,
				V.requireFailDefault, true) :
			(hasRequirement(SkyblockRequirementType.ISLAND_ROLE, role)
					? runRequirement(getRequirement(SkyblockRequirementType.ISLAND_ROLE, role), island, u)
							.setInquiryType(InquiryType.OUT_OF_SCOPE)
					: new ResultAllow(this, null, V.requirePassDefault));
	}
	
	/**
	 * Check all requirements for a player including wilderness.
	 * @param u The player trying to fly
	 * @param loc The location they are trying to fly at.
	 * @return The flight result
	 */
	public FlightResult checkFlightRequirements(UUID u, Location loc) {
		Console.debug("", "--- SkyblockHook check flight requirements A ---");
		IslandWrapper island = getIslandAt(loc);
		if (island == null) {
			return canFlyWilderness() ?
					new ResultAllow(this, null, V.requirePassDefault)
					: new ResultDeny(DenyReason.DISABLED_REGION, this, InquiryType.OUT_OF_SCOPE, V.requireFailDefault, true);
		}
		return checkFlightRequirements(u, island);
	}
	
	/**
	 * Check all requirements for a player.
	 * @param u The player trying to fly
	 * @param loc The location they are trying to fly at.
	 * @return The flight result
	 */
	public FlightResult checkFlightRequirements(UUID u, IslandWrapper island) {
		Console.debug("", "--- SkyblockHook check flight requirements B ---");
		if (!isEnabled()) {
			return new ResultAllow(this, null, V.requirePassDefault);
		}
		return checkRoleRequirements(u, island);
	}
	
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, World world) {
		if (!isEnabled() || world == null || !hasRequirement(SkyblockRequirementType.WORLD, world.getName())) {
			return new ResultAllow(this, InquiryType.WORLD, V.requirePassDefault);
		}
		UUID u = user.getPlayer().getUniqueId();
		IslandWrapper homeIsland = getTeamIsland(u);
		if (homeIsland == null) {
			return new ResultDeny(DenyReason.REQUIREMENT, this, InquiryType.WORLD, requireIsland, true);
		}
		return runRequirement(getRequirement(SkyblockRequirementType.WORLD, world.getName()), homeIsland, u)
				.setInquiryType(InquiryType.WORLD);
	}

	@Override
	public FlightResult handleFlightInquiry(FlightUser user, CompatRegion r) {
		if (!isEnabled() || r == null || !hasRequirement(SkyblockRequirementType.REGION, r.getId())) {
			return new ResultAllow(this, InquiryType.REGION, V.requirePassDefault);
		}
		UUID u = user.getPlayer().getUniqueId();
		IslandWrapper homeIsland = getTeamIsland(u);
		if (homeIsland == null) {
			return new ResultDeny(DenyReason.REQUIREMENT, this, InquiryType.REGION, requireIsland, true);
		}
		return runRequirement(getRequirement(SkyblockRequirementType.REGION, r.getId()), homeIsland, u)
				.setInquiryType(InquiryType.REGION);
	}
	
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, CompatRegion[] regions) {
		if (!isEnabled() || regions == null || regions.length == 0 || !hasRequirement(SkyblockRequirementType.REGION)) {
			return new ResultAllow(this, InquiryType.REGION, V.requirePassDefault);
		}
		
		UUID u = user.getPlayer().getUniqueId();
		IslandWrapper homeIsland = getTeamIsland(u);
		for (SkyblockRequirement rq: getRequirements(regions)) {
			if (homeIsland == null) {
				return new ResultDeny(DenyReason.REQUIREMENT, this, InquiryType.REGION, requireIsland, true);
			}
			return runRequirement(rq, homeIsland, u).setInquiryType(InquiryType.REGION);
		}
		return new ResultAllow(this, InquiryType.REGION, V.requirePassDefault);
	}
	
	@Override
	public boolean handles(InquiryType type) {
		switch (type) {
		case LOCATION:
			return true;
		case REGION:
		case WORLD:
		default:
			return false;
		}
	}
	
	
	public void openIslandSettings(Player p) {
		new PageIslandSettings(tempfly.getGuiManager().createSession(p));
	}
	
	
	/**
	 * 
	 * TempFlyHook Inheritance
	 * 
	 */
	
	@Override
	public String getEmbeddedConfigName() {
		return "skyblock_config";
	}
	
	@Override
	public Genre getGenre() {
		return Genre.SKYBLOCK;
	}
	
	@Override
	public String getConfigName() {
		return getPluginName();
	}
	
	
	/**
	 * 
	 * Abstract
	 * 
	 */
	
	
	/**
	 * @param island The island in question.
	 * @return All the team members that are currently online.
	 */
	public abstract Player[] getOnlineMembers(IslandWrapper island);
	
	/**
	 * @param island The island in question
	 * @return All team members of the island.
	 */
	public abstract UUID[] getIslandMembers(IslandWrapper island);
	
	/**
	 * @param p The player who owns the island or is the primary leader.
	 * @return The island owned by the player or null if they dont own an island.
	 */
	public abstract IslandWrapper getIslandOwnedBy(UUID p);
	
	/**
	 * 
	 * @param id The uuid of the player
	 * @return The island they are a member of, null if there isnt one.
	 */
	public abstract IslandWrapper getTeamIsland(UUID p);
	
	/**
	 * 
	 * @param loc The location.
	 * @return The island at the location, null if there isn't one.
	 */
	public abstract IslandWrapper getIslandAt(Location loc);

	/**
	 * 
	 * @param id The player
	 * @param challenge The name of the challenge
	 * @return true if the challenge has been completed, false if not completed or if it doesn't exist.
	 */
	public abstract boolean isChallengeCompleted(UUID p, SkyblockChallenge challenge);
	
	/**
	 * Check if an island has this role present. Island is included as a parameter to support
	 * the ability for certain islands to have extra roles in plugins where permission nodes may be more
	 * complex, and can be added by the island owner at will.
	 * @param role The role to check
	 * @return True if the island has this role.
	 */
	public abstract boolean islandRoleExists(IslandWrapper island, String role);
	
	/**
	 * Check if an island role exists in this skyblock plugin. If this plugin supports potentially unlimited
	 * roles just return true, the method islandRoleExists(UUID, IslandWrapper) will be used on top of this
	 * to check if roles exist during gameplay.
	 * @param role The role to check
	 * @return True if the island has this role.
	 */
	public abstract boolean islandRoleExists(String role);
	
	/**
	 * 
	 * @param island The island
	 * @param p The player
	 * @return The name of the players role on the given island.
	 */
	public abstract String getIslandRole(UUID p, IslandWrapper island);
	
	/**
	 * 
	 * @param island The island
	 * @return The UUID of the island owner. Null if there isn't an owner.
	 */
	public abstract UUID getIslandOwner(IslandWrapper island);
	
	/**
	 * The unique identifier of the island. 
	 * Something like the location of the island maybe, not the owner though as that is subject to change
	 * and some plugins may allow the player to have multiple islands. They would overwrite each other.
	 * @param island The island
	 * @return The string identifier of this island to be used for data storage.
	 */
	public abstract String getIslandIdentifier(Object rawIsland);
	
	/**
	 * Get an island from its identifier.
	 * @param identifier
	 * @return The island associated with the unique identifier or null if there isnt one.
	 */
	public abstract IslandWrapper getIslandFromIdentifier(String identifier);

	/**
	 * As plugins may differ vastly in their permissions and team structure, it is up to you to tell me
	 * whether this is true or not. For instance in ASkyBlock a coop player is not technically a team member but
	 * perhaps in a custom skyblock plugin two coop islands merge into one temporarily, meaning they may technically be a team?
	 * 
	 * @param island The island
	 * @param p The player
	 * @return True if the player is a member of the island in any way.
	 */
	public abstract boolean isIslandMember(UUID p, IslandWrapper island);
	
	/**
	 * 
	 * @param owner The player who's island is to be checked
	 * @return The island level of the player.
	 */
	public abstract double getIslandLevel(UUID p);
	
	/**
	 * 
	 * @param island The island to be checked
	 * @return the island level
	 */
	public abstract double getIslandLevel(IslandWrapper island);

	/**
	 * Return all the base roles of the skyblock plugin, for instance; OWNER, TEAM, VISITOR, COOP...
	 * @return
	 */
	public abstract String[] getRoles();
	
}
