package com.moneybags.tempfly.hook.askyblock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.Page;
import com.moneybags.tempfly.util.CompatMaterial;
import com.moneybags.tempfly.util.F;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

public class PageAskyblock implements Page {
	
	private GuiSession session;
	private Inventory inv;
	
	private static String title;
	private static ItemStack background;
	private static ItemStack toolbar;
	private static ItemStack team;
	private static ItemStack coop;
	private static ItemStack visitor;
	private static ItemStack allowed;
	private static ItemStack disallowed;
	
	public static void initialize() {
		FileConfiguration config = F.page;
		String path = "page.askyblock.settings";
		title = U.cc(config.getString(path + ".title", "&dFlight Settings"));
		background = U.getConfigItem(config, path + ".background");
		toolbar = U.getConfigItem(config, path + ".toolbar");
		team = U.getConfigItem(config, path + ".team");
		coop = U.getConfigItem(config, path + ".coop");
		visitor = U.getConfigItem(config, path + ".visitor");
		allowed = U.getConfigItem(config, path + ".allowed");
		disallowed = U.getConfigItem(config, path + ".disallowed");
		
		CompatMaterial.setType(background, CompatMaterial.LIGHT_GRAY_STAINED_GLASS_PANE);
		CompatMaterial.setType(toolbar, CompatMaterial.BLACK_STAINED_GLASS_PANE);
		CompatMaterial.setType(team, CompatMaterial.DIAMOND);
		CompatMaterial.setType(coop, CompatMaterial.EMERALD);
		CompatMaterial.setType(visitor, CompatMaterial.COAL);
		CompatMaterial.setType(allowed, CompatMaterial.LIME_WOOL);
		CompatMaterial.setType(disallowed, CompatMaterial.RED_WOOL);
	}
	
	public PageAskyblock(GuiSession session) {
		this.session = session;
		AskyblockHook hook = TempFly.getAskyblockHook();
		Island island = ASkyBlockAPI.getInstance().getIslandOwnedBy(session.getPlayer().getUniqueId());
		IslandSettings settings = hook.getIslandSettings(island);
		
		this.inv = Bukkit.createInventory(null, 45, U.cc(title));
		
		for (int i = 0; i < 36; i++) {
			inv.setItem(i, background);
		}
		for (int i = 36; i < 45; i++) {
			inv.setItem(i, toolbar);
		}
		inv.setItem(11, settings.getVisitorCanFly() ? allowed : disallowed);
		inv.setItem(13, settings.getCoopCanFly() ? allowed : disallowed);
		inv.setItem(15, settings.getTeamCanFly() ? allowed : disallowed);
		inv.setItem(20, visitor);
		inv.setItem(22, coop);
		inv.setItem(24, team);
		
		session.newPage(this, inv);
	}

	@Override
	public void runPage(int slot, InventoryClickEvent e) {
		Player p = session.getPlayer();
		ASkyBlockAPI api = ASkyBlockAPI.getInstance();
		Island island = api.getIslandOwnedBy(p.getUniqueId());
		if (island == null) {
			U.m(p, V.invalidIsland);
			session.endSession();
			return;
		}
		IslandSettings settings = TempFly.getAskyblockHook().getIslandSettings(island);
		switch(slot) {
		case 11:
		case 20:
			U.logS("click visitor");
			if (settings.getVisitorCanFly()) {
				U.logS("visitor true");
				settings.setVisitorCanFly(false);
			} else {
				settings.setVisitorCanFly(true);
			}
			U.logS(String.valueOf(settings.getVisitorCanFly()));
			new PageAskyblock(session);
			break;
		case 13:
		case 22:
			if (settings.getCoopCanFly()) {
				settings.setCoopCanFly(false);
			} else {
				settings.setCoopCanFly(true);
			}
			new PageAskyblock(session);
			break;
		case 15:
		case 24:
			if (settings.getTeamCanFly()) {
				settings.setTeamCanFly(false);
			} else {
				settings.setTeamCanFly(true);
			}
			new PageAskyblock(session);
			break;
		}
	}

	@Override
	public GuiSession getSession() {
		return session;
	}

	@Override
	public int getPageNumber() {
		return 1;
	}
}
