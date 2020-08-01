package com.moneybags.tempfly.combat;

import org.bukkit.scheduler.BukkitTask;

public class CombatTag {

	private long duration;
	private BukkitTask task;
	
	public CombatTag(long duration, BukkitTask task) {
		this.duration = duration;
		this.task = task;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public BukkitTask getTask() {
		return task;
	}

}
