package com.moneybags.tempfly.hook.skyblock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.abstraction.DynamicPage;
import com.moneybags.tempfly.hook.IslandSettings;
import com.moneybags.tempfly.util.CompatMaterial;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;

public class PageIslandSettings extends DynamicPage {
	
	/**
	 * 
	 * Static values
	 * 
	 */
	
	private static SkyblockHook hook;
	
	private static String
	title,
	allowLore,
	disallowLore;
	
	private static ItemStack
	background,
	toolbar,
	allowed,
	disallowed;
	
	public static void initialize(SkyblockHook skyblockHook) {
		hook = skyblockHook;
		
		FileConfiguration config = hook.getConfig();
		String path = "gui.page.settings";
		
		title = U.cc(config.getString(path + ".title", "&dFlight Settings"));
		title = U.cc(config.getString(path + ".title", "&dFlight Settings"));
		background = U.getConfigItem(config, path + ".background");
		toolbar = U.getConfigItem(config, path + ".toolbar");
		allowed = U.getConfigItem(config, path + ".allowed");
		disallowed = U.getConfigItem(config, path + ".disallowed");
		
		CompatMaterial.setType(background, CompatMaterial.GRAY_STAINED_GLASS_PANE);
		CompatMaterial.setType(toolbar, CompatMaterial.BLACK_STAINED_GLASS_PANE);
		CompatMaterial.setType(allowed, CompatMaterial.LIME_WOOL);
		CompatMaterial.setType(disallowed, CompatMaterial.RED_WOOL);
	}
	
	
	/**
	 * 
	 * 
	 * Object
	 * 
	 * 
	 */
	private Inventory inv;
	private Map<Integer, String> layout = new HashMap<>();
	
	public PageIslandSettings(GuiSession session) {
		super(session);
		IslandSettings settings = hook.getIslandOwnedBy(session.getPlayer().getUniqueId()).getSettings();
		
		this.inv = Bukkit.createInventory(null, 45, U.cc(title));
		
		for (int i = 0; i < 36; i++) {
			inv.setItem(i, background);
		}
		for (int i = 36; i < 45; i++) {
			inv.setItem(i, toolbar);
		}
		List<Entry<String, Boolean>> perms = settings.getCurrentState();
		calculateSlots(0, perms.size());
		Iterator<Integer> itInt = getOpenSlots().iterator();
		Iterator<Entry<String, Boolean>> itPerms = perms.iterator();
		while (itInt.hasNext() && itPerms.hasNext()) {
			int slot = itInt.next();
			Entry<String, Boolean> perm = itPerms.next();
			ItemStack item = perm.getValue() ? allowed.clone() : disallowed.clone();
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(meta.getDisplayName().replaceAll("\\{ROLE}", perm.getKey())
					.replaceAll("\\{STATUS}", perm.getValue() ? allowLore : disallowLore));
			if (meta.hasLore()) {
				List<String> lore = new ArrayList<>();
				for (String line: meta.getLore()) {
					lore.add(line.replaceAll("\\{ROLE}", perm.getKey())
						.replaceAll("\\{STATUS}", perm.getValue() ? allowLore : disallowLore));
				}	
				meta.setLore(lore);
			}
			item.setItemMeta(meta);
			inv.setItem(slot, item);
			Console.debug("putting: " + slot + " : " + perm.getKey());
			layout.put(slot, perm.getKey());
		}
		
		session.newPage(this, inv);
	}

	@Override
	public void runPage(int slot, InventoryClickEvent e) {
		if (!layout.containsKey(slot)) {
			return;
		}
		Player p = session.getPlayer();
		IslandWrapper island = hook.getIslandOwnedBy(p.getUniqueId());
		if (island == null) {
			U.m(p, hook.requireIsland);
			session.endSession();
			return;
		}
		Console.debug("clicked: " + slot);
		Console.debug("oi: " + layout.get(slot));
		IslandSettings settings = island.getSettings();
		String rank = layout.get(slot);
		settings.toggleCanFly(rank);
		new PageIslandSettings(session);
	}
}
