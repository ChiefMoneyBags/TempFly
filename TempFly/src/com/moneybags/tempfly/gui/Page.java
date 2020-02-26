package com.moneybags.tempfly.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;


public interface Page {

	abstract void runPage(int slot, InventoryClickEvent e);
	
	abstract GuiSession getSession();
	
	abstract int getPageNumber();
	
	default void onClose(InventoryCloseEvent e) {
		return;
	}
	
}
