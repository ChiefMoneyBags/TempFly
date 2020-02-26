package com.moneybags.tempfly.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FlightEnabledEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private Player p;
	private boolean cancelled;
	
	public FlightEnabledEvent(Player p) {
		this.p = p;
	}
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public Player getPlayer() {
		return p;
	}

}
