package com.moneybags.tempfly;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;
import com.moneybags.tempfly.time.TimeHandle;

public class TempFlyAPI {
	
	/**
	 * @param player Player uuid
	 * @return The amount of flight in seconds a player has.
	 */
	public double getFlightTime(UUID player) {
		return TimeHandle.getTime(player);
	}
	
	/**
	 * Set the flight time of a player in seconds.
	 * @param player Player uuid
	 */
	public void setFlightTime(UUID player, double seconds) {
		TimeHandle.setTime(player, seconds);
	}
	
	/**
	 * Add flight time in seconds
	 * @param player Player uuid
	 * @param seconds Seconds to give the player
	 */
	public void addFlightTime(UUID player, double seconds) {
		TimeHandle.addTime(player, seconds);
	}
	
	/**
	 * Remove flight time from the player.
	 * @param player Player uuid
	 * @param seconds Seconds to remove from the player
	 */
	public void removeFlightTime(UUID player, double seconds) {
		TimeHandle.removeTime(player, seconds);
	}
	
	/**
	 * 
	 * @param loc The location to check
	 * @return True if a player is allowed to fly at the given location.
	 */
	public boolean canFlyAt(Location loc) {
		return FlyHandle.flyAllowed(loc);
	}
	
	/**
	 * 
	 * @return All flyer objects for players using TempFly.
	 */
	public Flyer[] getAllFlyers() {
		return FlyHandle.getFlyers();
	}
	
	/**
	 * 
	 * @param p
	 * @return The flyer object if one exists. Null if the player is not using TempFly.
	 */
	public Flyer getFlyer(Player p) {
		return FlyHandle.getFlyer(p);
	}
	
	/**
	 * 
	 * @param p Player to toggle
	 * @param enabled Toggle enabled true or false
	 * @param fallDamage If true the player will take fall damage when their flight is toggled off.
	 */
	public void toggleTempfly(Player p, boolean enabled, boolean fallDamage) {
		if (enabled) {
			FlyHandle.removeFlyer(p);
			if (!fallDamage) FlyHandle.addDamageProtection(p);
		} else {
			FlyHandle.removeDamageProtction(p);
			FlyHandle.addFlyer(p);
		}
	}
} 
