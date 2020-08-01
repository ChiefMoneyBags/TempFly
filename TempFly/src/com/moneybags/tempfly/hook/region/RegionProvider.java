package com.moneybags.tempfly.hook.region;

import org.bukkit.Location;

public abstract class RegionProvider {

	protected boolean enabled;
	
	public RegionProvider() {
		
	}

	public boolean isEnabled() {
		return enabled;
	}
	
	public abstract CompatRegion[] getApplicableRegions(Location loc);
}
