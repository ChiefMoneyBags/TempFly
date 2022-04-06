package com.moneybags.tempfly.hook.skyblock;

import com.moneybags.tempfly.hook.IslandSettings;
import com.moneybags.tempfly.hook.TerritoryWrapper;

public class IslandWrapper implements TerritoryWrapper {

	private SkyblockHook hook;
	private Object island;
	private IslandSettings settings;
	
	public IslandWrapper(Object island, SkyblockHook hook) {
		this.island = island;
		this.hook = hook;
		this.settings = new IslandSettings(this, hook);
	}
	
	@Override
	public Object getRawTerritory() {
		return island;
	}
	
	/**
	 * Retain compatibility with versions before TerritoryHook was added
	 * @return The raw island Object from the respective skyblock plugin.
	 */
	public Object getRawIsland() {
		return getRawTerritory();
	}
	
	public SkyblockHook getHook() {
		return hook;
	}
	
	public IslandSettings getSettings() {
		return settings;
	}
}
