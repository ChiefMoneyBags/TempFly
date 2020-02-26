package com.moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;

public class ParticleTask extends BukkitRunnable {

	@Override
	public void run() {
		for (Flyer f: FlyHandle.getFlyers()) {
			if (f.isFlying()) {
				f.playEffect();
			}
		}
	}

}
