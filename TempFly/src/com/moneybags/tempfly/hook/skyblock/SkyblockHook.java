package com.moneybags.tempfly.hook.skyblock;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.FlightResult.DenyReason;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
import com.moneybags.tempfly.hook.IslandSettings;
import com.moneybags.tempfly.hook.TerritoryHook;
import com.moneybags.tempfly.hook.TerritoryWrapper;
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
 * 2) Invoke the super methods onIslandEnter and onIslandExit when players enter and leave islands as well as onIslandLevelChange and onChallengeComplete.
 * 3) Profit?
 */
public abstract class SkyblockHook extends TerritoryHook {

	private Map<String, Boolean> basePerms;
	private boolean wilderness, settingsHook;
	private ItemStack settingsButton;
	public String
	requireIsland,
	requireLevelSelf,
	requireLevelOther,
	requireChallengeSelf,
	requireChallengeOther,
	roleDenied;
	
	
	private Map<SkyblockRequirementType, SkyblockRequirement[]> requirements;
	
	public SkyblockHook(TempFly plugin) {
		super(plugin);
	}
	
	@Override
	public void onTempflyReload() {
		super.onTempflyReload();
		
		loadValues();
		for (Player player: Bukkit.getOnlinePlayers()) {
			evaluate(player);
		}
	}
	
	
	
	@Override
	public boolean needsDataFile() {
		return true;
	}
	
	@Override
	public boolean forceYaml() {
		return true;
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
				Console.debug("--| Loading default role permission: " + key);
				basePerms.put(key.toUpperCase(), config.getBoolean(pathPerms + "." + key));
			}	
		}
		
		String name = getConfigName();
		requireIsland			= V.st(config, "language.invalid.island", name);
		requireChallengeSelf	= V.st(config, "language.requirements.challenge_self", name);
		requireChallengeOther	= V.st(config, "language.requirements.challenge_other", name);
		requireLevelSelf		= V.st(config, "language.requirements.level_self", name);
		requireLevelOther		= V.st(config, "language.requirements.level_other", name);
		roleDenied				= V.st(config, "language.requirements.role_denied", name);
		
		settingsHook			= config.getBoolean("gui.hook_settings.enabled");
		settingsButton			= U.getConfigItem(config, "gui.hook_settings.button");
		CompatMaterial.setType(settingsButton, CompatMaterial.FEATHER);
		
		if (config.contains("unlockables.environment.wilderness")) {
			Console.debug("", "<< Loading wilderness requirements >>");
			requirements.put(SkyblockRequirementType.WILDERNESS, new SkyblockRequirement[] {
					loadRequirement(config, null, "unlockables.environment.wilderness", SkyblockRequirementType.WILDERNESS)
			});
		}
		
		ConfigurationSection csRequireWorld = config.getConfigurationSection("unlockables.environment.worlds");
		if (csRequireWorld != null) {
			Console.debug("", "<< Loading world requirements >>");
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String world: csRequireWorld.getKeys(false)) {
				list.add(loadRequirement(config, world, "unlockables.environment.worlds." + world, SkyblockRequirementType.WORLD));
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
				list.add(loadRequirement(config, region, "unlockables.environment.regions." + region, SkyblockRequirementType.REGION));
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
				if (!islandRoleExists(role)) {Console.severe("An island role specified in the config does not exist (" + role + "). Skipping...");}
				list.add(loadRequirement(config, role.toUpperCase(), "unlockables.island_roles." + role, SkyblockRequirementType.ISLAND_ROLE));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.ISLAND_ROLE, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		Console.debug("----END Skyblock Settings----", "");
		PageIslandSettings.initialize(this);
	}
	
	private SkyblockRequirement loadRequirement(FileConfiguration config, String name, String path, SkyblockRequirementType type) {
		return new SkyblockRequirement(
				loadChallenges(config, path + ".player_"), loadChallenges(config, path + ".island_"),
				loadLevel(config, path + ".player_"), loadLevel(config, path + ".island_"),
				name, type);
	}
	
	private double loadLevel(FileConfiguration config, String path) {
		double level;
		return (level = config.getDouble(path + "level", 0)) > 0 ? level : (level = config.getDouble(path + "worth", 0)) > 0 ? level : config.getDouble(path + "value", 0);
	}
	
	private SkyblockChallenge[] loadChallenges(FileConfiguration config, String path) {
		ConfigurationSection csChallenges = config.getConfigurationSection(path + "challenges");
		if (csChallenges == null) {
			csChallenges = config.getConfigurationSection(path + "missions");
			path = path + "missions";
		} else {
			path = path + "challenges";
		}
		List<SkyblockChallenge> challenges = new ArrayList<>();
		if (config.getStringList(path).size() > 0) {
			Console.debug("--|> Challenges are a StringList, Adding challenges based on completion...");
			for (String challenge: config.getStringList(path)) {
				challenges.add(new SkyblockChallenge(challenge, 0, 1));	
			}
		} else if (csChallenges != null) {
			for (String key: csChallenges.getKeys(false)) {
				Console.debug("--| loading SkyblockChallenge: " + key);
				if (config.isConfigurationSection(path + "." + key)) {
					Console.debug("--|> Challenge is a ConfigurationSection, Adding progress and completions seperately...");
					challenges.add(new SkyblockChallenge(key, 
							config.getInt(path + "." + key + ".progress", 0),
							config.getInt(path + "." + key + ".completed", 0)));	
				} else {
					Console.debug("--|> Challenge has only a single integer value.");
					challenges.add(new SkyblockChallenge(key,
							config.getInt(path + "." + key, 0),
							config.getInt(path + "." + key, 0)));
				}
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
			return false;
		}
		for (SkyblockRequirement require: getRequirements(type)) {
			if (require.getName().equals(name.toUpperCase())) {
				return true;
			}
		}
		return false;
	}
	
	public SkyblockRequirement getRequirement(SkyblockRequirementType type, String name) {
		for (SkyblockRequirement require: getRequirements(type)) {
			if (require.getName().equals(name.toUpperCase())) {
				return require;
			}
		}
		return null;
	}
	
	

	
	public IslandWrapper getIslandWrapper(Object rawIsland) {
		return (IslandWrapper) super.getTerritoryWrapper(rawIsland);
	}
	
	/**
	 * This method is called by the children of SkyblockHook when a player enters an island.
	 * It will track the island each player is on and handle flight requirements for the player.
	 * 
	 * @param p The player who entered the island.
	 * @param rawIsland The raw object representing the island. A wrapper will be created for it.
	 * @param loc Nullable, The location where the player entered the island.
	 */
	public void onIslandEnter(Player p, Object rawIsland, Location loc) {
		super.onTerritoryEnter(p, rawIsland, loc);
	}
	
	/**
	 * This method is called by the children of SkyblockHook when a player enters an island.
	 * It will track the island each player is on and handle flight requirements for the player.
	 * 
	 * @param p The player who entered the island.
	 */
	public void onIslandExit(final Player p) {
		super.onTerritoryExit(p);
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
			user.evaluateFlightRequirement(this, p.getLocation());
		}
	}
	
	/**
	 * This method is called by the children of SkyblockHook when a player completes a challenge.
	 * 
	 * Alternatively it will be called during a multi stage challenge when a player completes a portion
	 * of a challenge or their progress towards completion changes.
	 * 
	 * It will re-evaluate flight requirements of the player.
	 * 
	 * @param island The island.
	 */
	public void onChallengeComplete(Player p) {
		Console.debug("-- On challenge complete ---");
		checkFlightRequirements(p.getUniqueId(), p.getLocation());
		IslandWrapper island = getIslandOwnedBy(p.getUniqueId());
		if (island != null) {
			for (Player onIsland: getPlayersOn(island)) {
				if (onIsland == null || onIsland.equals(p)) {
					continue;
				}
				Console.debug(onIsland.getName());
				Console.debug(p.getName());
				Console.debug(p == onIsland);
				FlightUser user = getTempFly().getFlightManager().getUser(onIsland);
				if (user == null) {
					//TODO why is this returning null users here.
					return;
				}
				user.submitFlightResult(checkRoleRequirements(onIsland.getUniqueId(), island));
				user.evaluateFlightRequirement(this, onIsland.getLocation());
			}
		}
		FlightUser user = getTempFly().getFlightManager().getUser(p);
		user.submitFlightResult(checkRoleRequirements(p.getUniqueId(), island));
		user.evaluateFlightRequirement(this, p.getLocation());
		
		this.getPlayersOn(new IslandWrapper(island, null));
	}
	

	@Override
	public TerritoryWrapper createTerritoryWrapper(Object rawTerritory, TerritoryHook hook) {
		return new IslandWrapper(rawTerritory, this);
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
	
	
	
	public void evaluate(Player p) {
		getUser(p).submitFlightResult(checkFlightRequirements(p.getUniqueId(), p.getLocation()));
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
			Console.debug("--|> Island is null, checking wilderness requirements...");
			return canFlyWilderness() ?
					new ResultAllow(this, null, V.requirePassDefault)
					: new ResultDeny(DenyReason.DISABLED_REGION, this, InquiryType.OUT_OF_SCOPE, V.requireFailDefault, true);
		}
		return checkFlightRequirements(u, island);
	}
	
	/**
	 * Evaluate and update flight restrictions for all players on an island. 
	 * @param island
	 */
	public void evaluate(IslandWrapper island) {
		Console.debug("----- evaluate island -----");
		for (Player player: getPlayersOn(island)) {
			Console.debug("--| Player: " + player.getUniqueId());
			FlightUser user = getTempFly().getFlightManager().getUser(player);
			if (user == null) {
				//TODO why is this returning null users here.
				return;
			}
			user.submitFlightResult(checkRoleRequirements(player.getUniqueId(), island));
			user.evaluateFlightRequirement(this, player.getLocation());
		}
	}
	
	@Override
	public FlightResult checkFlightRequirements(UUID playerId, TerritoryWrapper territory) {
		return checkFlightRequirements(playerId, (IslandWrapper) territory);
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
			Console.debug("--|!!!> Hook is not enabled. Returning allowed flight!");
			return new ResultAllow(this, InquiryType.OUT_OF_SCOPE, V.requirePassDefault);
		}
		return checkRoleRequirements(u, island);
	}
	
	/**
	 * Check the role requirements of a player on a specific island.
	 * @param u
	 * @param island
	 * @return
	 */
	public FlightResult checkRoleRequirements(UUID u, IslandWrapper island) {
		String role = getIslandRole(u, island);
		IslandSettings settings = island.getSettings();
		if (V.debug) {
			Console.debug("", "--- SkyblockHook check role requirements ---", "--| Players Role: " + role, "--| Can role fly: " + settings.canFly(role));	
		}
		return !settings.canFly(role) ? new ResultDeny(DenyReason.DISABLED_REGION, this, InquiryType.OUT_OF_SCOPE,
				roleDenied.replaceAll("\\{ROLE}", role), true) :
			(hasRequirement(SkyblockRequirementType.ISLAND_ROLE, role)
					? runRequirement(getRequirement(SkyblockRequirementType.ISLAND_ROLE, role), island, u)
							.setInquiryType(InquiryType.OUT_OF_SCOPE)
					: new ResultAllow(this, null, V.requirePassDefault));
	}
	
	/**
	 * Process the requirements for a player to check if a player can fly on the given island.
	 * @param ir The requirement to check
	 * @param island The island the player is trying to fly on.
	 * @param p The player trying to fly.
	 * @return The flight result.
	 */
	public FlightResult runRequirement(SkyblockRequirement ir, IslandWrapper island, UUID u) {
		if (V.debug) {Console.debug("", "----- Running island flight requirement -----","--| Name: " + ir.getName(),"--| player level: " + ir.getPlayerLevel(),"--| owner level: " + ir.getOwnerLevel());}
		Console.debug("--| Players level: " + getIslandLevel(u));
		if (ir.getPlayerLevel() > 0 && ir.getPlayerLevel() > getIslandLevel(u)) {
			if (V.debug) {
				Console.debug("--|> Fail island level: " + ir.getPlayerLevel() + " / " + getIslandLevel(u), "-----End flight requirements-----", "");
			}
			return new ResultDeny(DenyReason.REQUIREMENT, this, null, requireLevelSelf
					.replaceAll("\\{LEVEL}", getFormattedIslandLevel(ir.getPlayerLevel()))
					.replaceAll("\\{ROLE}", ir.getName()), true);
		}
		
		if (ir.getOwnerLevel() > 0 && ir.getOwnerLevel() > getIslandLevel(getIslandOwner(island))) {
			if (V.debug) {
				Console.debug("--|> Fail island level: " + ir.getOwnerLevel() + " / " + getIslandLevel(getIslandOwner(island)), "-----End flight requirements-----", "");	
			}
			return new ResultDeny(DenyReason.REQUIREMENT, this, null, requireLevelOther
					.replaceAll("\\{LEVEL}", getFormattedIslandLevel(ir.getOwnerLevel()))
					.replaceAll("\\{ROLE}", ir.getName()), true);
		}
		
		for (SkyblockChallenge challenge : ir.getPlayerChallenges()) {
			if (!isChallengeCompleted(u, challenge)) {
				if (V.debug) {
					Console.debug("--|> Fail island challenge: " + challenge, "-----End flight requirements-----", "");	
				}
				return new ResultDeny(DenyReason.REQUIREMENT, this, null, requireChallengeSelf
						.replaceAll("\\{CHALLENGE}", getChallengeName(challenge))
						.replaceAll("\\{COMPLETIONS}", String.valueOf(challenge.getRequiredCompletions()))
						.replaceAll("\\{PROGRESS}", String.valueOf(challenge.getRequiredProgress()))
						.replaceAll("\\{ROLE}", ir.getName()), true);
			}
		}	
		for (SkyblockChallenge challenge : ir.getOwnerChallenges()) {
			if (!isChallengeCompleted(getIslandOwner(island), challenge)) {
				if (V.debug) {
					Console.debug("--|> Fail island challenge | island owner: " + challenge, "-----End flight requirements-----", "");	
				}
				return new ResultDeny(DenyReason.REQUIREMENT, this, null, requireChallengeOther
						.replaceAll("\\{CHALLENGE}", getChallengeName(challenge))
						.replaceAll("\\{COMPLETIONS}", String.valueOf(challenge.getRequiredCompletions()))
						.replaceAll("\\{PROGRESS}", String.valueOf(challenge.getRequiredProgress()))
						.replaceAll("\\{ROLE}", ir.getName()), true);
			}
		}	
		return new ResultAllow(this, null, V.requirePassDefault);
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
		return "skyblock_preset_generic";
	}
	
	@Override
	public Genre getGenre() {
		return Genre.SKYBLOCK;
	}
	
	@Override
	public String getConfigName() {
		return getPluginName();
	}
	
	@SuppressWarnings("serial")
	@Override
	public Map<String, Class<? extends TempFlyCommand>> getCommands() {
		return new HashMap<String, Class<? extends TempFlyCommand>>() {{
			put("island", CmdIslandSettings.class);
	}};
	}
	
	
	
	/**
	 * 
	 * Abstract
	 * 
	 */
	
	public String getFormattedIslandLevel(double level) {
		return new DecimalFormat("##.##").format(level);
	}
	
	public String getChallengeName(SkyblockChallenge challenge) {
		return challenge.getName();
	}
	
	
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
	public abstract IslandWrapper getIslandOwnedBy(UUID playerId);
	
	/**
	 * 
	 * @param id The uuid of the player
	 * @return The island they are a member of, null if there isnt one.
	 */
	public abstract IslandWrapper getTeamIsland(UUID playerId);
	
	/**
	 * 
	 * @param loc The location.
	 * @return The island at the location, null if there isn't one.
	 */
	public abstract IslandWrapper getIslandAt(Location loc);

	@Override
	public TerritoryWrapper getTerritoryAt(Location loc) {
		return getIslandAt(loc);
	}
	
	/**
	 * @param loc The location to check.
	 * @return True if the world given contains islands.
	 */
	public abstract boolean isIslandWorld(Location loc);
	
	/**
	 * 
	 * @param id The player
	 * @param challenge The name of the challenge
	 * @return true if the challenge has been completed, false if not completed or if it doesn't exist.
	 */
	public abstract boolean isChallengeCompleted(UUID playerId, SkyblockChallenge challenge);
	
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
	public abstract String getIslandRole(UUID playerId, IslandWrapper island);
	
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
	
	@Override
	public String getTerritoryIdentifier(Object rawTerritory) {
		return getIslandIdentifier(rawTerritory);
	}
	
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
	public abstract boolean isIslandMember(UUID playerId, IslandWrapper island);
	
	/**
	 * 
	 * @param owner The player who's island is to be checked
	 * @return The island level of the player.
	 */
	public abstract double getIslandLevel(UUID playerId);
	
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
	
	/**
	 * 
	 * @return True if the location specified is within the boundries of the island given.
	 */
	public abstract boolean isInIsland(IslandWrapper island, Location loc);
	
	@Override
	public boolean isInTerritory(TerritoryWrapper territory, Location loc) {
		return isInIsland((IslandWrapper) territory, loc);
	}
	
}
