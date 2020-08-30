package com.moneybags.tempfly.gui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import com.moneybags.tempfly.gui.abstraction.Page;


public class GuiSession {
	
	private Page page;
	private Player p;
	private boolean safety;
	private boolean save;
	
	public GuiSession(Player p) {
		this.p = p;
	}
	
	public void setSaveSession(boolean save) {
		this.save = save;
	}
	
	public boolean saveSession() {
		return save;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public Page getPage() {
		return page;
	}
	
	public void newPage(Page page, Inventory inv) {
		setSaveSession(true);
		p.openInventory(inv);
		setSaveSession(false);
		this.page = page;
	}
	public void setPage(Page page) {
		this.page = page;
	}
	
	public boolean safetyLock() {
		return safety;
	}
	
	public void endSession() {
		p.closeInventory();
	}
}
