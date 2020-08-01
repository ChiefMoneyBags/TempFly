package com.moneybags.tempfly.gui.abstraction;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.moneybags.tempfly.gui.GuiSession;


public abstract class Page {

	private int num;
	protected GuiSession session;
	
	public Page(GuiSession session, int num) {
		this.session = session;
		this.num = num;
	}
	
	public Page(GuiSession session) {
		this.session = session;
	}
	
	public GuiSession getSession() {
		return session;
	}
	
	public int getPageNumber() {
		return num;
	}
	
	public void onClose(InventoryCloseEvent e) {
		return;
	}
	
	public abstract void runPage(int slot, InventoryClickEvent e);
	
}
