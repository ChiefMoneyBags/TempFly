package com.moneybags.tempfly.gui.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.aesthetic.particle.Particles;
import com.moneybags.tempfly.command.admin.CmdTrailRemove;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.abstraction.DynamicPage;
import com.moneybags.tempfly.util.CompatMaterial;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.data.Files;

public class PageTrails extends DynamicPage {
	
	private static TempFly tempfly;
	private static String title;
	private static ItemStack
		background,
		toolbar,
		next,
		prev,
		remove;
	
	public static void initialize(TempFly plugin) {
		tempfly = plugin;
		FileConfiguration config = Files.page;
		String path = "page.trails";
		title = U.cc(config.getString(path + ".title", "&dParticle Trails"));
		background = U.getConfigItem(config, path + ".background");
		toolbar = U.getConfigItem(config, path + ".toolbar");
		next = U.getConfigItem(config, path + ".next");
		prev = U.getConfigItem(config, path + ".prev");
		remove = U.getConfigItem(config, path + ".remove");
		
		CompatMaterial.setType(background, CompatMaterial.GRAY_STAINED_GLASS_PANE);
		CompatMaterial.setType(toolbar, CompatMaterial.BLACK_STAINED_GLASS_PANE);
		CompatMaterial.setType(next, CompatMaterial.REDSTONE_TORCH);
		CompatMaterial.setType(prev, CompatMaterial.REDSTONE_TORCH);
		CompatMaterial.setType(remove, CompatMaterial.LAVA_BUCKET);
		
	}
	
	private Inventory inv;
	private Map<Integer, String> layout = new HashMap<>();
	private List<String> allParticles;
	
	public PageTrails(GuiSession session, int num, boolean bookmark) {
		super(session);
		
		this.inv = Bukkit.createInventory(null, 54, title);
		
		for (int i = 0; i < 45; i++) {
			inv.setItem(i, background);
		}
		for (int i = 45; i < 54; i++) {
			inv.setItem(i, toolbar);
		}
		
		Player p = session.getPlayer();
		List<String> particles = new ArrayList<>();
		if (Particles.oldParticles()) {
			for (Effect e: Effect.values()) {
				if (e.toString().equalsIgnoreCase("ITEM_BREAK")) {
					continue;
				}
				if (p.hasPermission("tempfly.trail." + e.toString())) {
					particles.add(e.toString());
				}
			}
		} else {
			for (Particle particle: Particle.values()) {
				if (particle.toString().contains("LEGACY")) {
					continue;
				}
				if (p.hasPermission("tempfly.trail." + particle.toString())) {
					particles.add(particle.toString());
				}
			}
		}
		allParticles = particles;
		String current = Particles.loadTrail(session.getPlayer().getUniqueId());
		if (bookmark && particles.contains(current)) {
			num = (int) Math.floor(particles.indexOf(current) / 21);
		}
		super.calculateSlots(num, particles.size());
		if (particles.size() < 21*num) {
			num = 0;
		}
		
		particles = particles.subList(21 * num, particles.size());
		Iterator<String> itp = particles.iterator();
		Iterator<Integer> its = super.getOpenSlots().iterator();
		while (its.hasNext() && itp.hasNext()) {
			int slot = its.next();
			String particle = itp.next();
			layout.put(slot, particle);
			ItemStack display;
			if (particle.equalsIgnoreCase(current)) {
				display = CompatMaterial.get(CompatMaterial.LIME_STAINED_GLASS);
			} else {
				display = CompatMaterial.get(CompatMaterial.WHITE_STAINED_GLASS);
			}
			ItemMeta meta = display.getItemMeta();
			meta.setDisplayName(U.cc("&a" + particle.toLowerCase().replaceAll("\\_", " ")));
			if (p.isOp()) {
				meta.setLore(Arrays.asList(U.cc("&fPermission: &etempfly.trail." + particle)));
			}
			display.setItemMeta(meta);
			inv.setItem(slot, display);
		}
		if (allParticles.size() > (getPageNumber()+1)*21) {
			inv.setItem(53, next);
		} 
		if (getPageNumber() > 0) {
			inv.setItem(45, prev);
		}
		if (p.hasPermission("tempfly.trails.remove.self")) {
			inv.setItem(49, remove);
		}
		
		session.newPage(this, inv);
	}

	@Override
	public void runPage(int slot, InventoryClickEvent e) {
		if (layout.containsKey(slot)) {
			String s = layout.get(slot);
			Particles.setTrail(session.getPlayer().getUniqueId(), s);
			new PageTrails(session, getPageNumber(), false);
		} else if (slot == 53 && allParticles.size() > (getPageNumber()+1)*21) {
			new PageTrails(session, getPageNumber()+1, false);
		} else if (slot == 45 && getPageNumber() > 0) {
			new PageTrails(session, getPageNumber()-1, false);
		} else if (slot == 49 && session.getPlayer().hasPermission("tempfly.trails.remove.self")) {
			new CmdTrailRemove(tempfly, null).executeFromGui(session.getPlayer());
			new PageTrails(session, getPageNumber(), false);
		}
	}
}
