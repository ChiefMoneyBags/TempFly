package moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import moneybags.tempfly.TempFly;

public class AutoSave extends BukkitRunnable {

	@Override
	public void run() {
		TempFly.getInstance().getDataBridge().commit();
	}

}
