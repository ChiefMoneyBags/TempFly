package com.moneybags.tempfly.hook.region;

import org.bukkit.Location;

public abstract class RegionProvider {

	
	/**
	 * 
	 * @return Whether or not this RegionProvider is currently active and accepting inquiries.
	 */
	public abstract boolean isEnabled();
	
	/**
	 * Called when tempfly wants to know about the regions that encompass this location.
	 * @param loc The location in question.
	 * @return
	 */
	public abstract CompatRegion[] getApplicableRegions(Location loc);
}
