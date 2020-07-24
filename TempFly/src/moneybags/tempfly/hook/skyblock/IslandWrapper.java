package moneybags.tempfly.hook.skyblock;

import moneybags.tempfly.hook.HookManager.HookType;

public class IslandWrapper {

	private SkyblockHook hook;
	private Object island;
	private IslandSettings settings;
	
	public IslandWrapper(HookType type, Object island, SkyblockHook hook) {
		this.island = island;
		this.hook = hook;
		this.settings = new IslandSettings(this, hook);
	}
	
	public Object getIsland() {
		return island;
	}
	
	public SkyblockHook getHook() {
		return hook;
	}
	
	public IslandSettings getSettings() {
		return settings;
	}
}
