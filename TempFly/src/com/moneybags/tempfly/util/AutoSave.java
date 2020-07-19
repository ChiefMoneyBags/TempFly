package com.moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.FlyHandle;

public class AutoSave extends BukkitRunnable {

	@Override
	public void run() {
		FlyHandle.save();
	}

}
