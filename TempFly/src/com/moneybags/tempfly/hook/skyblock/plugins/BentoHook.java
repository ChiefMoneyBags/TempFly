package com.moneybags.tempfly.hook.skyblock.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockChallenge;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;
import com.moneybags.tempfly.util.Console;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.events.BentoBoxEvent;
import world.bentobox.bentobox.api.events.IslandBaseEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * BentoBox does not include built in island tracking or events for player location and islands
 * so BentoBoxHook will initiate the super tracker in SkyblockHook.
 * @author Kevin
 *
 */
public abstract class BentoHook extends SkyblockHook implements Listener {
	
	
	public BentoHook(TempFly plugin) {
		super(plugin);
	}

	@Override
	public boolean initializeHook() {
		// initial Bento stuff
		
		//
		if (super.initializeHook()) {
			getTempFly().getServer().getPluginManager().registerEvents(this, getTempFly());
			startManualTracking();
			return true;
		}
		return false;
	}
	
	@Override
	public String getFormattedIslandLevel(double level) {
		Object levels = getLevelsAddon();
		if (levels == null) {
			return super.getFormattedIslandLevel(level);
		}
		try {
			Object manager = levels.getClass().getMethod("getManager").invoke(levels);
			return (String) manager.getClass().getMethod("formatLevel", Long.class).invoke(manager, (long) level);	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.getFormattedIslandLevel(level);
	}
	
	@Override
	public String getChallengeName(SkyblockChallenge challenge) {
		try {
			Object challenges = getChallengeAddon();
			if (challenges == null) {
				return super.getChallengeName(challenge);
			}
			Object manager = challenges.getClass().getMethod("getChallengesManager").invoke(challenges); 
			Object bentoChallenge = manager.getClass().getMethod("getChallenge", String.class).invoke(manager, challenge.getName());
			if (bentoChallenge == null) {
				return super.getChallengeName(challenge);
			}
			return (String) bentoChallenge.getClass().getMethod("getFriendlyName").invoke(bentoChallenge);
		} catch (Exception e) {
			e.printStackTrace();
			return super.getChallengeName(challenge);
		}
	}
	
	@EventHandler
	public void on(IslandBaseEvent e) {
		Class<?> clazz = e.getClass();
		if (clazz.getSimpleName().equals("IslandLevelCalculatedEvent")) {
			Island rawIsland = e.getIsland();
			Bukkit.getScheduler().runTaskLater(getTempFly(), () ->
			super.onIslandLevelChange(getIslandWrapper(rawIsland))
			, 1);
		}
	}
	
	@EventHandler
	public void on(BentoBoxEvent e) {
		Console.debug("--- BentoBoxEvent ---");
		Class<?> clazz = e.getClass();
		if (clazz.getSimpleName().equals("ChallengeCompletedEvent")) {
			try {
				UUID id = (UUID) clazz.getMethod("getPlayerUUID").invoke(e);
				Player p = Bukkit.getPlayer(id);
				if (p == null) {
					return;
				}
				super.onChallengeComplete(p);
			} catch (Exception exception) {	exception.printStackTrace();}
		}
	}
	
	@Override
	public String getPluginName() {
		return "BentoBox";
	}
	
	@Override
	public String getEmbeddedConfigName() {
		return "skyblock_preset_bento";
	}
	
	public User getBentoUser(UUID u) {
		return BentoBox.getInstance().getPlayers().getUser(u);
	}
	
	public Object getLevelsAddon() {
		Optional<Addon> t = BentoBox.getInstance().getAddonsManager().getAddonByName("Level");
		return t.isPresent() ? t.get() : null;
	}
	
	public Object getChallengeAddon() {
		Optional<Addon> t = BentoBox.getInstance().getAddonsManager().getAddonByName("Challenges");
		return t.isPresent() ? t.get() : null;
	}

	@Override
	public IslandWrapper getIslandAt(Location loc) {
		Optional<Island> t = BentoBox.getInstance().getIslands().getIslandAt(loc);
		return t.isPresent() ? getIslandWrapper(t.get()) : null;
	}

	@Override
	public boolean islandRoleExists(IslandWrapper island, String role) {
		for (String s: getRoles()) {
			if (role.equalsIgnoreCase(s)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean islandRoleExists(String role) {
		return islandRoleExists(null, role);
	}

	@Override
	public String getIslandRole(UUID u, IslandWrapper island) {
		String[] split = BentoBox.getInstance().getRanksManager().getRank(((Island) island.getRawIsland()).getRank(u)).split("\\."); 
		return split.length > 1 ? split[1].toUpperCase() : split[0].toUpperCase();
	}
	
	@Override
	public String[] getRoles() {
		List<String> roles = new ArrayList<>();
		for (String role: BentoBox.getInstance().getRanksManager().getRanks().keySet()) {
			String[] split = role.split("\\.");
			Console.debug("Adding role: " + (split.length > 1 ? split[1].toUpperCase() : split[0].toUpperCase()));
			roles.add(split.length > 1 ? split[1].toUpperCase() : split[0].toUpperCase());
		}
		return roles.toArray(new String[roles.size()]);
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		return ((Island) island.getRawIsland()).getOwner();
	}

	@Override
	public String getIslandIdentifier(Object rawIsland) {
		return ((Island)rawIsland).getUniqueId();
	}

	@Override
	public boolean isIslandMember(UUID u, IslandWrapper island) {
		return ((Island) island.getRawIsland()).getMembers().containsKey(u);
	}

	@Override
	public double getIslandLevel(IslandWrapper island) {
		Object level = getLevelsAddon();
		if (level == null) {
			return 0;
		}
		
		UUID owner = ((Island)island.getRawIsland()).getOwner();
		try {return (double)((long) level.getClass().getMethod("getIslandLevel", World.class, UUID.class)
				.invoke(level, getMainIslandWorld(), owner));} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public Player[] getOnlineMembers(IslandWrapper island) {
		Island rawIsland = (Island) island.getRawIsland();
		List<Player> online = new ArrayList<>();
		for (UUID u: rawIsland.getMembers().keySet()) {
			Player p = Bukkit.getPlayer(u);
			if (p != null && p.isOnline()) {
				online.add(p);
			}
		}
		return online.toArray(new Player[online.size()]);
	}

	@Override
	public UUID[] getIslandMembers(IslandWrapper island) {
		Island rawIsland = (Island) island.getRawIsland();
		return rawIsland.getMemberSet().toArray(new UUID[rawIsland.getMemberSet().size()]);
	}

	@Override
	public IslandWrapper getIslandFromIdentifier(String identifier) {
		Optional<Island> rawIsland = BentoBox.getInstance().getIslands().getIslandById(identifier);
		return rawIsland.isPresent() ? getIslandWrapper(rawIsland.get()) : null;
	}

	@Override
	public boolean isIslandWorld(Location loc) {
		return BentoBox.getInstance().getIWM().inWorld(loc);
	}

	
	@Override
	public IslandWrapper getIslandOwnedBy(UUID playerId) {
		World world = getMainIslandWorld();
		Island rawIsland = BentoBox.getInstance().getIslands().getIsland(world, playerId);
		if (rawIsland == null) {
			Console.debug("--|> The player does not have an island in the given world.");
			return null;
		}
		return rawIsland.getOwner().equals(playerId) ? getIslandWrapper(rawIsland) : null;
	}

	@Override
	public IslandWrapper getTeamIsland(UUID playerId) {
		World world = getMainIslandWorld();
		Island rawIsland = BentoBox.getInstance().getIslands().getIsland(world, playerId);
		if (rawIsland == null) {
			Console.debug("--|> The player does not have an island in the given world.");
			return null;
		}
		return getIslandWrapper(rawIsland);
	}

	@Override
	public boolean isChallengeCompleted(UUID u, SkyblockChallenge challenge) {
		try {
			Object challenges = getChallengeAddon();
			if (challenges == null) {
				Console.debug("--|> Challenges addon for BentoBox does not exist!");
				return true;
			}
			Object manager = challenges.getClass().getMethod("getChallengesManager").invoke(challenges); 
			Object bentoChallenge = manager.getClass().getMethod("getChallenge", String.class).invoke(manager, challenge.getName());
			if (bentoChallenge == null) {
				Console.debug("--|> The challenge specified does not exist!");
				return true;
			}
			User bentoUser = getBentoUser(u);
			if (bentoUser == null) {
				Console.debug("--|> Players bento user is null!");
				return false;
			}
			World world = getMainIslandWorld();
			boolean isCompleted = (boolean) manager.getClass().getMethod("isChallengeComplete", User.class, World.class, bentoChallenge.getClass()).invoke(manager, bentoUser, world, bentoChallenge);
			Console.debug("--| Challenge: " + challenge.getName(), "--| isCompleted: " + isCompleted);
			if (challenge.getRequiredCompletions() == 1 && !isCompleted) {
				return false;
			}
			long completions = (long) manager.getClass().getMethod("getChallengeTimes", User.class, World.class, String.class)
					.invoke(manager, bentoUser, getMainIslandWorld(), challenge.getName());
			Console.debug("--| completions: " + completions);
			if (challenge.getRequiredCompletions() > 0 && completions < challenge.getRequiredCompletions()) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public double getIslandLevel(UUID playerId) {
		Object levels = getLevelsAddon();
		if (levels == null) {
			Console.debug("--|> Levels addon for BentoBox does not exist!");
			return 0;
		}
		World world = getMainIslandWorld();
		try {return (double) ((long) levels.getClass().getMethod("getIslandLevel", World.class, UUID.class)
					.invoke(levels, world, playerId));} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	@Override
	public boolean isInIsland(IslandWrapper island, Location loc) {
		return ((Island)island.getRawIsland()).inIslandSpace(loc);
	}
	
	
	public abstract World getMainIslandWorld();


}
