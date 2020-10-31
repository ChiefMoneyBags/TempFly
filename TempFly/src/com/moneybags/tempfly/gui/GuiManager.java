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

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.gui.abstraction.Page;

public class GuiManager implements Listener {

	private TempFly tempfly;
	
	public GuiManager(TempFly tempfly) {
		this.tempfly = tempfly;
		tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
	}
	
	private Map<Player, GuiSession> sessions = new HashMap<>();
	
	public TempFly getTempFly() {
		return tempfly;
	}
	
	public Collection<GuiSession> getSessions() {
		return sessions.values();
	}
	
	public void endAllSessions() {
		for (GuiSession session: sessions.values()) {
			session.endSession();
		}
	}
	
	public GuiSession getSession(Player p) {
		return sessions.containsKey(p) ? sessions.get(p) : null;
	}
	
	public GuiSession createSession(Player p) {
		if (sessions.containsKey(p)) {
			sessions.get(p).endSession();
		}
		GuiSession session = new GuiSession(p);
		sessions.put(p, session);
		return session;
	}
	
	
	
	/**
	 * --------------
	 * Event Handling
	 * --------------
	 */
	
	
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		if (sessions.containsKey(e.getPlayer())) {
			GuiSession session = sessions.get(e.getPlayer()); 
			Page page = session.getPage();
			if (page != null) {
				page.onClose(e);	
			}
			if (!session.saveSession()) {
				sessions.remove(e.getPlayer());	
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player)e.getWhoClicked();
		if (!sessions.containsKey(p)) {
			return;
		}
		GuiSession session = sessions.get(p);
		e.setCancelled(true);
		if (e.getClickedInventory() == null) {
			return;
		}
		int slot = e.getRawSlot();
		session.getPage().runPage(slot, e);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(InventoryDragEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player)e.getWhoClicked();
		if (!sessions.containsKey(p)) {
			return;
		}
		e.setCancelled(true);
	}	

}
