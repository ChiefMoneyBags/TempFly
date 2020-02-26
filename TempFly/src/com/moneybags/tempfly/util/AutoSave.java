package com.moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;

public class AutoSave extends BukkitRunnable {

	@Override
	public void run() {
		TempFly.save();
	}

}
