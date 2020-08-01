package com.moneybags.tempfly.util;

import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.user.FlightUser;

public class ParticleTask extends BukkitRunnable {

	private TempFly tempfly;
	
	public ParticleTask(TempFly tempfly) {
		this.tempfly = tempfly;
	}
	
	@Override
	public void run() {
		for (FlightUser user: tempfly.getFlightManager().getUsers()) {
			if (user.hasFlightEnabled() && user.getPlayer().isFlying()) {
				user.playTrail();
			}
		}
	}

}
