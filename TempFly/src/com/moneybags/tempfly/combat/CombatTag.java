package com.moneybags.tempfly.combat;

import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

public class CombatTag extends BukkitRunnable {

	private CombatHandler combat;
	private long duration;
	private long progress;
	private UUID u;
	
	public CombatTag(UUID u, long duration, CombatHandler combat) {
		this.duration = duration;
		this.u = u;
		this.combat = combat;
		this.runTaskTimer(combat.getFlightManager().getTempFly(), 0, 1);
	}
	
	public UUID getPlayer() {
		return u;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public long getProgress() {
		return progress;
	}
	
	public long getRemainingTime() {
		return duration-progress;
	}

	@Override
	public void run() {
		progress++;
		if (progress >= duration) {
			combat.cancelTag(u);
		}
	}

}
