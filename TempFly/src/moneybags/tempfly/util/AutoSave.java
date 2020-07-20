package moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.fly.FlyHandle;

public class AutoSave extends BukkitRunnable {

	@Override
	public void run() {
		TempFly.getInstance().getDataBridge().commit();
	}

}
