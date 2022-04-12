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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockChallenge;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.ChallengeCompleteEvent;
import com.wasteofplastic.askyblock.events.IslandEnterEvent;
import com.wasteofplastic.askyblock.events.IslandExitEvent;
import com.wasteofplastic.askyblock.events.IslandPostLevelEvent;

/**
 * - Dev Notes -
 * Askyblock contains events for island enter and island exit. Therefore it will not need to initiate the
 * super island tracker in SkyblockHook.
 * 
 * @author Kevin
 *
 */
public class AskyblockHook extends SkyblockHook implements Listener {
	
	public static final String[] ROLES = new String[] {"OWNER", "TEAM", "COOP", "VISITOR"};
	
	private ASkyBlockAPI api;
	private ASkyBlock asky;
	
	public AskyblockHook(TempFly plugin) {
		super(plugin);
	}
	
	@Override
	public boolean initializeHook() {
		try {this.api = ASkyBlockAPI.getInstance();} catch (Exception e) {
			Console.severe("There was an error while initializing the ASkyBlockAPI hook!");
			return false;
		}
		
		this.asky = (ASkyBlock) Bukkit.getPluginManager().getPlugin("ASkyBlock");
		if (this.asky == null) {
			Console.severe("There was an error while initializing the ASkyBlock Plugin hook!");
			return false;
		}
		
		if (super.initializeHook()) {
			tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 *  Event Handling
	 * 
	 */
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(InventoryOpenEvent e) {
		if (!hasSettingsHook() || !(e.getPlayer() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getPlayer();
		if (!e.getView().getTitle().equals(asky.myLocale(p.getUniqueId()).igsTitle)) {
			return;
		}
		Inventory inv = e.getInventory();
		int empty = inv.firstEmpty();
		boolean open = false;
		if (empty < 0) {
			open = true;
			e.setCancelled(true);
			Inventory rebuild = Bukkit.createInventory(null, inv.getSize()+9, asky.myLocale(p.getUniqueId()).igsTitle);
			rebuild.setContents(inv.getContents());
			empty = rebuild.firstEmpty();
			inv = rebuild;
		}
		inv.setItem(empty, getSettingsButton());
		if (open) {
			p.openInventory(inv);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void on(InventoryClickEvent e) {
		if (!hasSettingsHook() || !(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getWhoClicked();
		if (!e.getView().getTitle().equals(asky.myLocale(p.getUniqueId()).igsTitle)) {
			return;
		}
		ItemStack clicked = e.getCurrentItem();
		if (getSettingsButton().isSimilar(clicked)) {
			openIslandSettings(p);
		}
	}

	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(IslandEnterEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		if (p == null) {
			return;
		}
		Island island = e.getIsland();
		onIslandEnter(p, island, e.getLocation());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(IslandExitEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		if (p == null || !isCurrentlyTracking(p)) {
			return;
		}
		Island rawIsland = api.getIslandAt(p.getLocation());
		if (rawIsland != null) {
			onIslandExit(p);
		}
	}
	
	/**
	 * Askyblock does not call the island exit event on player respawn...
	 * @param e
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		if (!isCurrentlyTracking(p)) {
			return;
		}
		Island rawIsland = api.getIslandAt(p.getLocation());
		Island newIsland = api.getIslandAt(e.getRespawnLocation());
		if (rawIsland != null && !rawIsland.equals(newIsland)) {
			onIslandExit(p);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(IslandPostLevelEvent e) {
		Island rawIsland = e.getIsland();
		IslandWrapper island = getIslandWrapper(rawIsland);
		onIslandLevelChange(island);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(ChallengeCompleteEvent e) {
		onChallengeComplete(e.getPlayer());
	}
	
	
	/**
	 * 
	 * TempFlyHook Inheritance
	 * 
	 */
	

	@Override
	public String getPluginName() {
		return "ASkyBlock";
	}
	
	@Override
	public String getEmbeddedConfigName() {
		return "skyblock_preset_askyblock";
	}
	
	
	/**
	 * 
	 * SkyblockHook Inheritance
	 * 
	 */

	
	@Override
	public IslandWrapper getIslandOwnedBy(UUID id) {
		return getIslandWrapper(api.getIslandOwnedBy(id));
	}

	@Override
	public IslandWrapper getIslandAt(Location loc) {
		return getIslandWrapper(api.getIslandAt(loc));
	}

	@Override
	public boolean isChallengeCompleted(UUID id, SkyblockChallenge challenge) {
		return challenge.getRequiredCompletions() == 0 && challenge.getRequiredProgress() == 0 || api.getChallengeStatus(id).getOrDefault(challenge.getName(), false);
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
		if (!island.getHook().equals(this)) {
			return null;
		}
		Island rawIsland = (Island) island.getRawIsland();
		return rawIsland.getOwner().equals(u) ? "OWNER" : rawIsland.getMembers().contains(u) ? "TEAM" : api.getCoopIslands(Bukkit.getPlayer(u)).contains(api.getIslandLocation(getIslandOwner(island))) ? "COOP" : "VISITOR";
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return null;
		}
		return ((Island) island.getRawIsland()).getOwner();
	}

	@Override
	public String getIslandIdentifier(Object rawIsland) {
		return U.locationToString( (rawIsland instanceof IslandWrapper) ?
				((Island) ((IslandWrapper) rawIsland).getRawIsland()).getCenter()
				: ((Island) rawIsland).getCenter());
	}
	
	@Override
	public IslandWrapper getIslandFromIdentifier(String identifier) {
		return getIslandWrapper(api.getIslandAt(U.locationFromString(identifier)));
	}
	
	@Override
	public boolean isIslandMember(UUID u, IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return false;
		}
		return ((Island) island.getRawIsland()).getMembers().contains(u);
	}

	@Override
	public IslandWrapper getTeamIsland(UUID u) {
		return getIslandAt(api.getHomeLocation(u));
	}

	@Override
	public double getIslandLevel(UUID u) {
		return api.getLongIslandLevel(u);
	}

	@Override
	public double getIslandLevel(IslandWrapper island) {
		return api.getLongIslandLevel(getIslandOwner(island));
	}



	@Override
	public Player[] getOnlineMembers(IslandWrapper island) {
		List<Player> online = new ArrayList<>();
		for (UUID u: ((Island) island.getRawIsland()).getMembers()) {
			Player p = Bukkit.getPlayer(u);
			if (p != null) {
				online.add(p);
			}
		}
		return online.toArray(new Player[online.size()]);
	}



	@Override
	public UUID[] getIslandMembers(IslandWrapper island) {
		List<UUID> members = ((Island) island.getRawIsland()).getMembers();
		return members.toArray(new UUID[members.size()]);
	}

	@Override
	public String[] getRoles() {
		return ROLES;
	}

	@Override
	public boolean isIslandWorld(Location loc) {
		return loc.getWorld().equals(api.getIslandWorld()) || loc.getWorld().equals(api.getNetherWorld());
	}

	@Override
	public boolean isInIsland(IslandWrapper island, Location loc) {
		return ((Island)island.getRawIsland()).inIslandSpace(loc);
	}
}
