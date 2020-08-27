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
import org.bukkit.scheduler.BukkitRunnable;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockChallenge;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.U;
import com.wasteofplastic.askyblock.ASkyBlock;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;
import com.wasteofplastic.askyblock.events.ChallengeCompleteEvent;
import com.wasteofplastic.askyblock.events.IslandEnterEvent;
import com.wasteofplastic.askyblock.events.IslandExitEvent;
import com.wasteofplastic.askyblock.events.IslandPostLevelEvent;

public class AskyblockHook extends SkyblockHook implements Listener {
	
	public static final String[] ROLES = new String[] {"OWNER", "TEAM", "COOP", "VISITOR"};
	
	private ASkyBlockAPI api;
	private ASkyBlock asky;
	
	public AskyblockHook(TempFly plugin) {
		super(plugin);
	}
	
	@Override
	public boolean initializeHook() {
		super.initializeHook();
		this.api = ASkyBlockAPI.getInstance();
		this.asky = (ASkyBlock) Bukkit.getPluginManager().getPlugin("ASkyBlock");
		tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
		return true;
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
		Island island = e.getIsland();
		onIslandEnter(p, island, e.getLocation());
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(IslandExitEvent e) {
		Player p = Bukkit.getPlayer(e.getPlayer());
		Island rawIsland = api.getIslandAt(p.getLocation());
		if (rawIsland != null) {
			onIslandExit(p, rawIsland);
		}
	}
	
	/**
	 * Askyblock does not call the island exit event on player respawn...
	 * @param e
	 */
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Island rawIsland = api.getIslandAt(p.getLocation());
		if (rawIsland != null) {
			onIslandExit(p, rawIsland);
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
	 * Fix askyblocks world loader breaking flight user island tracking on /reload.
	 */
	@Override
	public void onUserInitialized(FlightUser user) {
		try {
			super.onUserInitialized(user); 
		} catch (Exception e) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (isCurrentlyTracking(user.getPlayer())) {
						return;
					}
					IslandWrapper island = getIslandAt(user.getPlayer().getLocation());
					if (island != null) {
						onIslandEnter(user.getPlayer(), island.getIsland(), user.getPlayer().getLocation());
					}
				}
			}.runTaskLater(getTempFly(), 6);	
		}
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
		return challenge.getRequiredProgress() == 0 || api.getChallengeStatus(id).getOrDefault(challenge.getName(), false);
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
		Island rawIsland = (Island) island.getIsland();
		return rawIsland.getOwner().equals(u) ? "OWNER" : rawIsland.getMembers().contains(u) ? "TEAM" : api.getCoopIslands(Bukkit.getPlayer(u)).contains(api.getIslandLocation(getIslandOwner(island))) ? "COOP" : "VISITOR";
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return null;
		}
		return ((Island) island.getIsland()).getOwner();
	}

	@Override
	public String getIslandIdentifier(Object rawIsland) {
		return U.locationToString( (rawIsland instanceof IslandWrapper) ?
				((Island) ((IslandWrapper) rawIsland).getIsland()).getCenter()
				: ((Island) rawIsland).getCenter());
	}
	
	@Override
	public boolean isIslandMember(UUID u, IslandWrapper island) {
		if (!island.getHook().equals(this)) {
			return false;
		}
		return ((Island) island.getIsland()).getMembers().contains(u);
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
		for (UUID u: ((Island) island.getIsland()).getMembers()) {
			Player p = Bukkit.getPlayer(u);
			if (p != null) {
				online.add(p);
			}
		}
		return online.toArray(new Player[online.size()]);
	}



	@Override
	public UUID[] getIslandMembers(IslandWrapper island) {
		List<UUID> members = ((Island) island.getIsland()).getMembers();
		return members.toArray(new UUID[members.size()]);
	}

	@Override
	public String[] getRoles() {
		return ROLES;
	}
}
