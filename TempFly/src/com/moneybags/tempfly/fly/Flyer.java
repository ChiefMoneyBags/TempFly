package com.moneybags.tempfly.fly;

import org.bukkit.entity.Player;
import com.moneybags.tempfly.environment.RelativeTimeRegion;
import com.moneybags.tempfly.user.FlightUser;

@Deprecated
public class Flyer {
	
	private FlightUser user;
	
	@Deprecated
	public Flyer(FlightUser user) {
		this.user = user;
	}
	
	@Deprecated
	public boolean isFlying() {
		return user.getPlayer().isFlying();
	}
	@Deprecated
	public boolean isIdle() {
		return user.isIdle();
	}
	@Deprecated
	public void resetIdleTimer() {
		user.resetIdleTimer();
	}
	@Deprecated
	public RelativeTimeRegion[] getRtEncompassing() {
		return user.getEnvironment().getRelativeTimeRegions();
	}
	@Deprecated
	public Player getPlayer() {
		return user.getPlayer();
	}
	@Deprecated
	public double getTime() {
		return user.getTime();
	}
	@Deprecated
	public void setTime(double time) {
		user.setTime(time);
	}
	
	/*
	 * 
	 * 
	 */
	
	@Deprecated
	public void asessRtWorlds() {
		return;
	}
	
	@Deprecated
	public void asessRtRegions() {
		return;
	}
	
	@Deprecated
	public void removeFlyer() {
		//TODO
		user.disableFlight(1, true);
	}
	
	/**
	 * This method returns a string to keep the plugin compatible through versions.
	 * @return The enum string representation of the particle
	 */
	@Deprecated
	public String getTrail() {
		return user.getTrail();
	}
	
	/**
	 *  This method requires a string to keep the plugin compatible through versions.
	 *  The enum value of the particle as a string
	 * @param particle
	 */
	@Deprecated
	public void setTrail(String particle) {
		user.setTrail(particle);
	}
	
	@Deprecated
	public void playTrail() {
		user.playTrail();
	}
}
