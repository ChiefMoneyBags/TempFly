package com.moneybags.tempfly.hook;

public interface TerritoryWrapper {

	/**
	 * Get the raw territory this object is safely wrapping.
	 * Example A Faction / Island object.
	 * @return The raw territory object from the plugin being hooked into. 
	 */
	abstract Object getRawTerritory();
	
}
