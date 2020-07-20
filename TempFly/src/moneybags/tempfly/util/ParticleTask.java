package moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import moneybags.tempfly.fly.FlyHandle;
import moneybags.tempfly.fly.Flyer;

public class ParticleTask extends BukkitRunnable {

	@Override
	public void run() {
		for (Flyer f: FlyHandle.getFlyers()) {
			if (f.isFlying()) {
				f.playTrail();
			}
		}
	}

}
