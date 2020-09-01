package com.moneybags.tempfly.hook.skyblock;

public class IslandWrapper {

	private SkyblockHook hook;
	private Object island;
	private IslandSettings settings;
	
	public IslandWrapper(Object island, SkyblockHook hook) {
		this.island = island;
		this.hook = hook;
		this.settings = new IslandSettings(this, hook);
	}
	
	public Object getRawIsland() {
		return island;
	}
	
	public SkyblockHook getHook() {
		return hook;
	}
	
	public IslandSettings getSettings() {
		return settings;
	}
}
