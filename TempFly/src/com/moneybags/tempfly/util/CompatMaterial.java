package com.moneybags.tempfly.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum CompatMaterial {

	GRAY_STAINED_GLASS_PANE(
			"STAINED_GLASS_PANE", (short) 7),
	
	BLACK_STAINED_GLASS_PANE(
			"STAINED_GLASS_PANE", (short) 15),
	
	LIME_STAINED_GLASS(
			"STAINED_GLASS", (short) 5),
	
	WHITE_STAINED_GLASS(
			"STAINED_GLASS", (short) 0),
	
	RED_WOOL(
			"WOOL", (short) 14),
	
	LIME_WOOL(
			"WOOL", (short) 5),
	
	EMERALD,
	
	DIAMOND,
	
	COAL,
	
	REDSTONE_TORCH(
			"REDSTONE_TORCH_ON", (short)0),
	
	FEATHER,
	
	LAVA_BUCKET;
	
	private String oldMaterial;
	private short oldData;
	
	private CompatMaterial(String oldMaterial, short oldData) {
		this.oldMaterial = oldMaterial;
		this.oldData = oldData;
	}
	
	private CompatMaterial() {
		
	}
	
	@SuppressWarnings("deprecation")
	public static ItemStack get(CompatMaterial mat) {
		try {
			return new ItemStack(Material.valueOf(mat.toString()));
		} catch (Exception e) {}
		try {
			return new ItemStack(Material.valueOf(mat.oldMaterial), 1, mat.oldData);
		} catch (Exception e) {
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static void setType(ItemStack item, CompatMaterial mat) {
		try {
			item.setType(Material.valueOf(mat.toString()));
		} catch (Exception e) {}
		try {
			item.setType(Material.valueOf(mat.oldMaterial));
			item.setDurability(mat.oldData);
		} catch (Exception e) {}
	}
}
