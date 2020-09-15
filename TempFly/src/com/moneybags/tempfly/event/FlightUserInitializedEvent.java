package com.moneybags.tempfly.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.moneybags.tempfly.user.FlightUser;

public class FlightUserInitializedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	
	private FlightUser user;
	
	public FlightUserInitializedEvent(FlightUser user) {
		this.user = user;
	}

	public FlightUser getUser() {
		return user;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
