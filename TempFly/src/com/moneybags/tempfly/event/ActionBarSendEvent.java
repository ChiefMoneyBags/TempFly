package com.moneybags.tempfly.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event will not be fired as of tempfly 2.0.3 due to a complication with async threads and the EventApi
 * If you wish to cancel the action bar you can disable it in the tempfly config. Alteratively you can set V.actionBar false 
 * @author Kevin
 *
 */
@Deprecated
public class ActionBarSendEvent extends Event implements Cancellable {
	
	private final HandlerList handlers = new HandlerList();
	private final Player player;
	private String message;
	private boolean cancelled = false;

	public ActionBarSendEvent(Player player, String message) {
		this.player = player;
		this.message = message;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return player;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
