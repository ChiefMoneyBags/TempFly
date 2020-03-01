package com.moneybags.tempfly.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class U {
	
	public static void command(String s) {
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s);
	}
	
	public static void logI(String m){
		Bukkit.getLogger().info("[TempFly] " + m);
	}
	
	public static void logW(String m){
		Bukkit.getLogger().warning("[TempFly] " + m);
	}
	
	public static void logS(String m){
		Bukkit.getLogger().severe("[TempFly] " + m);
	}
	
	public static boolean isPlayer(CommandSender s){
		if (s instanceof Player){
			return true;
		}
		return false;
	}
	
	public static String locationToString(Location loc) {
		if (loc != null) {
			return loc.getWorld().getName() + "~" + loc.getBlockX() + "~" + loc.getBlockY() + "~" + loc.getBlockZ();
		}
		return null;
	}
	
	public static Location locationFromString(String loc) {
		String[] s = loc.split("~");
		try {
			return new Location(Bukkit.getWorld(s[0]), Double.valueOf(s[1]), Double.valueOf(s[2]), Double.valueOf(s[3]));
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String cc(String m) {
		return ChatColor.translateAlternateColorCodes('&', m);
	}
	
	public static void m(Player p, String s) {
		if (s.equals(V.prefix) || s.equals("\\{PREFIX}") || s == null || s.length() == 0) {
			return;
		}
		p.sendMessage(s.replaceAll("\\{PREFIX}", V.prefix));
	}
	
	public static void m(CommandSender p, String s) {
		if (s == null || s.equals("\\{PREFIX}") || s.length() == 0) {
			return;
		}
		p.sendMessage(s.replaceAll("\\{PREFIX}", V.prefix));
	}
	
	public static boolean hasPermission(CommandSender s, String perm){
		if (s instanceof Player){
			Player p = (Player) s;
			if (p.hasPermission(perm)) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	public static String locToString(Location loc) {
		return loc.getWorld().getName()
				+ "~"+ String.valueOf(loc.getBlockX())
				+ "~" + String.valueOf(loc.getBlockY())
				+ "~" + String.valueOf(loc.getBlockZ());
	}
	
	public static Location locFromString(String loc) {
		String[] s = loc.split("~");
		return new Location(Bukkit.getWorld(s[0]),
				Double.parseDouble(s[1]), Double.parseDouble(s[2]), Double.parseDouble(s[3]));
	}
	
	public static ItemStack getConfigItem(FileConfiguration config, String path) {
		String item = config.getString(path + ".item", "1");
		String name = config.getString(path + ".name", "&cThis item is broken. :'(");
		int amount = config.getInt(path + ".amount", 1);
		List<String> lore = config.getStringList(path + ".lore");
		List<String> l = new ArrayList<>();
		
		ItemStack it = new ItemStack(Material.STONE, amount);
		ItemMeta meta = it.getItemMeta();
		
		meta.setDisplayName(cc("&r" + name));
		if (lore != null) {
			for (String line: lore) {
				l.add(cc("&r" + line));
			}
			meta.setLore(l);
		}
		it.setItemMeta(meta);
		it.setAmount(amount);
		return it;
	}
	
	
}
