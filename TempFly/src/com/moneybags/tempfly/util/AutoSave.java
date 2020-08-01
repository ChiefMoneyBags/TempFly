package com.moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.util.data.DataBridge;

public class AutoSave extends BukkitRunnable {

	private DataBridge bridge;
	
	public AutoSave(DataBridge bridge) {
		this.bridge = bridge;
	}
	
	@Override
	public void run() {
		bridge.commitAll();
	}

}
