package moneybags.tempfly.gui;

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

import moneybags.tempfly.gui.abstraction.Page;


public class GuiSession {

	private static Map<Player, GuiSession> sessions = new HashMap<>();
	
	
	public static Collection<GuiSession> getSessions() {
		return sessions.values();
	}
	
	public static void endAllSessions() {
		for (GuiSession session: sessions.values()) {
			session.endSession();
		}
	}
	
	public static GuiSession getSession(Player p) {
		return sessions.containsKey(p) ? sessions.get(p) : null;
	}
	
	public static GuiSession newGuiSession(Player p) {
		if (sessions.containsKey(p)) {
			sessions.get(p).endSession();
		}
		GuiSession session = new GuiSession(p);
		sessions.put(p, session);
		return session;
	}
	
	public static class GuiListener implements Listener {
		
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
