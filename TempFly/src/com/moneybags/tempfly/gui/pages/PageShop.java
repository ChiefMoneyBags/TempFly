package com.moneybags.tempfly.gui.pages;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.abstraction.DynamicPage;
import com.moneybags.tempfly.time.AsyncTimeParameters;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.CompatMaterial;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.Files;

import net.milkbowl.vault.economy.Economy;

public class PageShop extends DynamicPage {
	
	private static TempFly tempfly;
	private static List<ShopOption> allOptions = new ArrayList<>();
	
	private static String title;
	private static ItemStack background, toolbar, next, prev;
	
	public static void initialize(TempFly plugin) {
		FileConfiguration config = Files.page;
		String path = "page.shop";
		tempfly = plugin;
		title = U.cc(config.getString(path + ".title", "&dParticle Trails"));
		background = U.getConfigItem(config, path + ".background");
		toolbar = U.getConfigItem(config, path + ".toolbar");
		next = U.getConfigItem(config, path + ".next");
		prev = U.getConfigItem(config, path + ".prev");
		
		CompatMaterial.setType(background, CompatMaterial.GRAY_STAINED_GLASS_PANE);
		CompatMaterial.setType(toolbar, CompatMaterial.BLACK_STAINED_GLASS_PANE);
		CompatMaterial.setType(next, CompatMaterial.REDSTONE_TORCH);
		CompatMaterial.setType(prev, CompatMaterial.REDSTONE_TORCH);
		
		allOptions.clear();
		ConfigurationSection csOptons = Files.config.getConfigurationSection("shop.options");
		if (csOptons != null) {
			for (String s: csOptons.getKeys(false)) {
				path = "shop.options." + s;
				allOptions.add(new ShopOption(Files.config.getInt(path + ".time", 0), Files.config.getDouble(path + ".cost", 1000000)));
			}
		}
	}
	
	private Inventory inv;
	private Map<Integer, ShopOption> layout = new HashMap<>();
	
	public PageShop(GuiSession session, int num) {
		super(session);
		
		this.inv = Bukkit.createInventory(null, 54, title);
		
		for (int i = 0; i < 45; i++) {
			inv.setItem(i, background);
		}
		for (int i = 45; i < 54; i++) {
			inv.setItem(i, toolbar);
		}
		
		super.calculateSlots(num, allOptions.size());
		if (allOptions.size() < 21*num) {
			num = 0;
		}
		List<ShopOption> options = new ArrayList<>();
		options.addAll(allOptions);
		options = options.subList(21 * num, options.size());
		Iterator<ShopOption> ito = options.iterator();
		Iterator<Integer> its = super.getOpenSlots().iterator();
		while (its.hasNext() && ito.hasNext()) {
			int slot = its.next();
			ShopOption option = ito.next();
			layout.put(slot, option);
			inv.setItem(slot, option.getDisplay());
		}
		if (allOptions.size() > (getPageNumber()+1)*21) {
			inv.setItem(53, next);
		} 
		if (getPageNumber() > 0) {
			inv.setItem(45, prev);
		}
		
		session.newPage(this, inv);
	}
	
	@Override
	public void runPage(int slot, InventoryClickEvent e) {
		if (layout.containsKey(slot)) {
			Player p = session.getPlayer();
			ShopOption option = layout.get(slot);
			TimeManager manager = tempfly.getTimeManager();
			double maxTime = manager.getMaxTime(p.getUniqueId());
			if (maxTime == -999) {
				U.m(p, "&cAn internal error occured. please contact the developer!");
				return;
			}
			
			if (manager.getMaxTime(p.getUniqueId()) > -1 &&
					manager.getTime(p.getUniqueId()) + option.getTime() > maxTime) {
				U.m(p, V.timeMaxSelf);
				return;
			}
			Economy eco = tempfly.getHookManager().getEconomy();
			double balance = eco.getBalance(p);
			if (option.getCost() > balance) {
				U.m(p, manager.regexString(V.invalidFunds, option.getTime())
						.replaceAll("\\{COST}", String.valueOf(option.getCost())));
			} else {
				eco.withdrawPlayer(p, option.getCost());
				U.m(p, manager.regexString(V.timePurchased, option.getTime())
						.replaceAll("\\{COST}", String.valueOf(option.getCost())));
				new AsyncTimeParameters(tempfly, (AsyncTimeParameters parameters) -> {
					parameters.getTempfly().getTimeManager().addTime(p.getUniqueId(), parameters);
				}, p, p, option.getTime()).run();
			}
		} else if (slot == 53 && allOptions.size() > (getPageNumber()+1)*21) {
			new PageShop(session, getPageNumber()+1);
		} else if (slot == 45 && getPageNumber() > 0) {
			new PageShop(session, getPageNumber()-1);
		}
	}

	public static class ShopOption {
		
		private int time;
		private double cost;
		private ItemStack item;
		
		private ShopOption(int time, double cost) {
			this.time = time;
			this.cost = cost;
			item = CompatMaterial.get(CompatMaterial.FEATHER);
			ItemMeta meta = item.getItemMeta();
			
			String name = U.cc(
					tempfly.getTimeManager().regexString(
							Files.page.getString("page.shop.option.name", "{FORMATTED_TIME}"), time)
							.replaceAll("\\{COST}", String.valueOf(cost))
								);
			meta.setDisplayName(name);
			
			List<String> l = Files.page.getStringList("page.shop.option.lore");
			List<String> lore = new ArrayList<>();
			if (l != null) {
				DecimalFormat df = new DecimalFormat("##.##");
				for (String s: l) {
					lore.add(U.cc(
							tempfly.getTimeManager().regexString(s, time)
							.replaceAll("\\{COST}", df.format(cost))
							));
				}
			}
			meta.setLore(lore);
			item.setItemMeta(meta);
		}		
		public int getTime() {
			return time;
		}
		
		public double getCost() {
			return cost;
		}
		
		public ItemStack getDisplay() {
			return item;
		}
	}
	
}
