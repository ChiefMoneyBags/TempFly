package moneybags.tempfly.hook.skyblock;

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

import moneybags.tempfly.gui.GuiSession;
import moneybags.tempfly.gui.abstraction.DynamicPage;
import moneybags.tempfly.util.CompatMaterial;
import moneybags.tempfly.util.U;

public class PageIslandSettings extends DynamicPage {
	
	/**
	 * 
	 * Static values
	 * 
	 */
	
	private static String
	title,
	allowLore,
	disallowLore;
	
	private static ItemStack
	background,
	toolbar,
	team,
	coop,
	visitor,
	allowed,
	disallowed;
	
	public static void initialize(SkyblockHook hook) {
		FileConfiguration config = hook.getConfig();
		String path = "page.skyblock.settings";
		title = U.cc(config.getString(path + ".title", "&dFlight Settings"));
		title = U.cc(config.getString(path + ".title", "&dFlight Settings"));
		background = U.getConfigItem(config, path + ".background");
		toolbar = U.getConfigItem(config, path + ".toolbar");
		team = U.getConfigItem(config, path + ".team");
		coop = U.getConfigItem(config, path + ".coop");
		visitor = U.getConfigItem(config, path + ".visitor");
		allowed = U.getConfigItem(config, path + ".allowed");
		disallowed = U.getConfigItem(config, path + ".disallowed");
		
		CompatMaterial.setType(background, CompatMaterial.GRAY_STAINED_GLASS_PANE);
		CompatMaterial.setType(toolbar, CompatMaterial.BLACK_STAINED_GLASS_PANE);
		CompatMaterial.setType(team, CompatMaterial.DIAMOND);
		CompatMaterial.setType(coop, CompatMaterial.EMERALD);
		CompatMaterial.setType(visitor, CompatMaterial.COAL);
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
	
	private SkyblockHook hook;
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
		
		Iterator<Integer> itInt = getOpenSlots().iterator();
		Iterator<Entry<String, Boolean>> itPerms = settings.getCurrentState().iterator();
		while (itInt.hasNext() && itPerms.hasNext()) {
			int slot = itInt.next();
			Entry<String, Boolean> perm = itPerms.next();
			ItemStack item = perm.getValue() ? allowed : disallowed;
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(meta.getDisplayName().replaceAll("\\{RANK}", perm.getKey())
					.replaceAll("\\{STATUS}", perm.getValue() ? allowLore : disallowLore));
			List<String> lore = new ArrayList<>();
			for (String line: meta.getLore()) {
				lore.add(line.replaceAll("\\{RANK}", perm.getKey())
					.replaceAll("\\{STATUS}", perm.getValue() ? allowLore : disallowLore));
			}
			item.setItemMeta(meta);
			inv.setItem(slot, item);
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
		IslandSettings settings = island.getSettings();
		String rank = layout.get(slot);
		settings.toggleCanFly(rank);
		new PageIslandSettings(session);
	}
}
