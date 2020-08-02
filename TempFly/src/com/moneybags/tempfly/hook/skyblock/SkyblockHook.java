package com.moneybags.tempfly.hook.skyblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.FlightResult.DenyReason;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.moneybags.tempfly.hook.HookManager.HookType;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;

public abstract class SkyblockHook extends TempFlyHook {

	private Map<String, Boolean> basePerms = new HashMap<>();
	private boolean wilderness;
	public String
	requireIsland,
	requireLevelSelf,
	requireLevelOther,
	requireChallengeSelf,
	requireChallengeOther;
	
	private Map<SkyblockRequirementType, SkyblockRequirement[]> requirements = new HashMap<>();
	
	public SkyblockHook(HookType hookType, TempFly plugin) {
		super(hookType, plugin);
		if (!super.isEnabled()) {
			return;
		}
		loadValues();
		PageIslandSettings.initialize(this);
	}
	
	private void loadValues() {
		Console.debug("");
		Console.debug("----Loading Skyblock Settings----");
		FileConfiguration config = getConfig();
		
		this.wilderness = config.getBoolean("flight_settings.wilderness");
		String pathPerms = "flight_settings.base_permissions";
		ConfigurationSection csPerms = config.getConfigurationSection(pathPerms);
		if (csPerms != null) {
			for (String key: csPerms.getKeys(false)) {
				basePerms.put(key, config.getBoolean(pathPerms + "." + key));
			}	
		}
		
		String name = getHookType().getConfigName();
		requireIsland			= V.st(config, "language.invalid.island", name);
		requireChallengeSelf	= V.st(config, "language.requirements.challenge_self", name);
		requireChallengeOther	= V.st(config, "language.requirements.challenge_other", name);
		requireLevelSelf		= V.st(config, "language.requirements.level_self", name);
		requireLevelOther		= V.st(config, "language.requirements.level_other", name);
		
		String path;
		if (config.contains(path = "unlockables.environment.wilderness")) {
			Console.debug("");
			Console.debug("<< Loading wilderness requirements >>");
			requirements.put(SkyblockRequirementType.WILDERNESS, new SkyblockRequirement[] {
					new SkyblockRequirement(
							config.getStringList(path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							null, SkyblockRequirementType.WILDERNESS)
			});
		}
		
		ConfigurationSection csRequireWorld = config.getConfigurationSection("unlockables.environment.worlds");
		if (csRequireWorld != null) {
			Console.debug("");
			Console.debug("<< Loading world requirements >>");
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String world: csRequireWorld.getKeys(false)) {
				path = "unlockables.environment.worlds." + world;
				list.add(new SkyblockRequirement(
							config.getStringList(path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							world, SkyblockRequirementType.WORLD));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.WORLD, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		
		ConfigurationSection csRequireRegion = config.getConfigurationSection("unlockables.environment.regions");
		if (csRequireRegion != null) {
			Console.debug("");
			Console.debug("<< Loading region requirements >>");
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String region: csRequireRegion.getKeys(false)) {
				path = "unlockables.environment.regions." + region;
				list.add(new SkyblockRequirement(
							config.getStringList(path + ".challenges"), null,
							config.getLong(path + ".island_level"), 0,
							region, SkyblockRequirementType.REGION));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.REGION, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		
		ConfigurationSection csRequireRole = config.getConfigurationSection("unlockables.island_roles");
		if (csRequireRole != null) {
			Console.debug("");
			Console.debug("<< Loading island_role requirements >>");
			List<SkyblockRequirement> list = new ArrayList<>();
			for (String role: csRequireRole.getKeys(false)) {
				if (!islandRoleExists(role)) {
					Console.severe("An island role specified in the config does not exist (" + role + "). Skipping...");
				}
				
				path = "unlockables.island_roles." + role;
				list.add(new SkyblockRequirement(
							config.getStringList(path + ".challenges"), config.getStringList(path + ".owner_challenges"),
							config.getLong(path + ".island_level"), config.getLong(path + ".owner_level"),
							role.toUpperCase(), SkyblockRequirementType.ISLAND_ROLE));
			}
			if (list.size() > 0) {
				requirements.put(SkyblockRequirementType.ISLAND_ROLE, list.toArray(new SkyblockRequirement[list.size()]));
			}
		}
		Console.debug("----END Skyblock Settings----");
		Console.debug("");
	}
	
	/**
	 * 
	 * Global Values
	 * 
	 */
	
	
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
			if (require.getName().equals(name)) {
				return true;
			}
		}
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
	private Map<Object, IslandWrapper> wrapperCache = new HashMap<>();
	
	public IslandWrapper getIslandWrapper(Object rawIsland) {
		if (rawIsland == null) {
			return null;
		}
		return wrapperCache.containsKey(rawIsland) ? wrapperCache.get(rawIsland) : new IslandWrapper(getHookType(), rawIsland, this);
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
			Console.debug("");
			Console.debug("------ On Island Enter ------");
			Console.debug("Player: " + p.getName());
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
		if (V.debug) {
			Console.debug("Island: " + getIslandIdentifier(island));
			Console.debug("------ End Island Enter ------");
			Console.debug("");
		}
		
		locationCache.put(p, island);
		FlightUser user = tempfly.getFlightManager().getUser(p);
		user.submitFlightResult(checkFlightRequirements(p.getUniqueId(), loc));
	}
	
	//TODO what if the player does onIslandLeave, flight is autoenabled then onIslandEnter it is disabled again
	// fix it.
	/**
	 * This method is called by the children of SkyblockHook when a player enters an island.
	 * It will track the island each player is on and handle flight requirements for the player.
	 * 
	 * @param p The player who entered the island.
	 * @param rawIsland The raw object representing the island. A wrapper will be created for it.
	 */
	public void onIslandExit(Player p, Object rawIsland) {
		if (V.debug) {
			Console.debug("");
			Console.debug("------ On Island Exit ------");
			Console.debug("Player: " + p.getName());
		}
		if (locationCache.containsKey(p)) {
			IslandWrapper island = locationCache.get(p);
			locationCache.remove(p);
			if(!locationCache.containsValue(island)) {
				wrapperCache.remove(rawIsland);
			}
		}
		// On island exit i will wait 1 tick then check if the player still has this requirement.
		// If they are no longer on an island it will be removed.
		final FlightUser user = tempfly.getFlightManager().getUser(p);
		final RequirementProvider provider = this;
		new BukkitRunnable() {
			@Override
			public void run() {
				if (user.hasFlightRequirement(provider, InquiryType.OUT_OF_SCOPE) && !locationCache.containsKey(user.getPlayer())) {
					user.submitFlightResult(new ResultAllow(provider, InquiryType.OUT_OF_SCOPE, V.requirePassDefault));
				}
			}
		}.runTaskLater(tempfly, 1);
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
		Console.debug("");
		Console.debug("-----Running island flight requirements-----");
		if (ir.getIslandLevel() > 0 && ir.getIslandLevel() > getIslandLevel(u)) {
			Console.debug("fail island level: " + ir.getIslandLevel() + " / " + getIslandLevel(u));
			Console.debug("-----End flight requirements-----");
			Console.debug("");
			return new ResultDeny(DenyReason.REQUIREMENT, this, null,
					requireLevelSelf
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getIslandLevel()))
					.replaceAll("\\{ROLE}", ir.getName()), true);
		}
		
		if (ir.getOwnerLevel() > 0 && ir.getOwnerLevel() > getIslandLevel(getIslandOwner(island))) {
			Console.debug("fail island level: " + ir.getOwnerLevel() + " / " + getIslandLevel(getIslandOwner(island)));
			Console.debug("-----End flight requirements-----");
			Console.debug("");
			return new ResultDeny(DenyReason.REQUIREMENT, this, null,
					requireLevelOther
					.replaceAll("\\{LEVEL}", String.valueOf(ir.getOwnerLevel()))
					.replaceAll("\\{ROLE}", ir.getName()), true);
		}
		
		for (String challenge : ir.getChallenges()) {										
			if (!isChallengeCompleted(u, challenge)) {
				Console.debug("fail island challenge: " + challenge);
				Console.debug("-----End flight requirements-----");
				Console.debug("");
				return new ResultDeny(DenyReason.REQUIREMENT, this, null,
						requireChallengeSelf
						.replaceAll("\\{CHALLENGE}", challenge)
						.replaceAll("\\{ROLE}", ir.getName()), true);
			}
		}	
		for (String challenge : ir.getOwnerChallenges()) {								
			if (!isChallengeCompleted(getIslandOwner(island), challenge)) {
				Console.debug("fail island challenge | island owner: " + challenge);
				Console.debug("-----End flight requirements-----");
				Console.debug("");
				return new ResultDeny(DenyReason.REQUIREMENT, this, null,
						requireChallengeOther
						.replaceAll("\\{CHALLENGE}", challenge)
						.replaceAll("\\{ROLE}", ir.getName()), true);
			}
		}	
		return new ResultAllow(this, null, V.requirePassDefault);
	}
	
	/**
	 * 
	 * @param u The player trying to fly
	 * @param loc The location they are trying to fly at.
	 * @return The flight result
	 */
	public FlightResult checkFlightRequirements(UUID u, Location loc) {
		if (!isEnabled()) {
			new ResultAllow(this, null, V.requirePassDefault);
		}
		
		IslandWrapper island = getIslandAt(loc);
		if (island == null) {
			return canFlyWilderness() ?
					new ResultAllow(this, null, V.requirePassDefault)
					: new ResultDeny(DenyReason.DISABLED_REGION, this, InquiryType.OUT_OF_SCOPE, V.requireFailDefault, true);
		}
		
		String role = getIslandRole(u, island);
		IslandSettings settings = island.getSettings();	
		return !settings.canFly(role) ? new ResultDeny(DenyReason.DISABLED_REGION, this, InquiryType.OUT_OF_SCOPE,
				V.requireFailDefault, true) :
			(hasRequirement(SkyblockRequirementType.ISLAND_ROLE, role)
					? runRequirement(getRequirement(SkyblockRequirementType.ISLAND_ROLE, role), island, u)
							.setInquiryType(InquiryType.OUT_OF_SCOPE)
					: new ResultAllow(this, null, V.requirePassDefault));
	}
	
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, World world) {
		if (!isEnabled() || world == null || !hasRequirement(SkyblockRequirementType.WORLD, world.getName())) {
			new ResultAllow(this, InquiryType.WORLD, V.requirePassDefault);
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
	public FlightResult handleFlightInquiry(FlightUser user, Location loc) {
		if (!isEnabled() || loc == null) {
			return new ResultAllow(this, InquiryType.LOCATION, V.requirePassDefault);
		}
		return checkFlightRequirements(user.getPlayer().getUniqueId(), loc).setInquiryType(InquiryType.LOCATION);
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
	
	
	
	/**
	 * @param p
	 * @return The island owned by the player or null.
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
	public abstract boolean isChallengeCompleted(UUID p, String challenge);
	
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
	 * @return The name of the players role on the given island
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
	public abstract String getIslandIdentifier(IslandWrapper island);

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
	public abstract long getIslandLevel(UUID p);
	
	/**
	 * 
	 * @param island The island to be checked
	 * @return the island level
	 */
	public abstract long getIslandLevel(IslandWrapper island);
	
}
