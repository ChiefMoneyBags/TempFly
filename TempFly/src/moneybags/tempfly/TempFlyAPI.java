package moneybags.tempfly;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import moneybags.tempfly.fly.FlyHandle;
import moneybags.tempfly.fly.Flyer;
import moneybags.tempfly.hook.FlightResult;
import moneybags.tempfly.time.TimeHandle;

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
	 * @deprecated Since 3.0, checking if fly is allowed at a given location should now be done with the
	 * method that includes the player and location as parameters. This is due to the integration of the
	 * many hooks introduced in version 3.0 that contain conditional requirements based on many factors
	 * about the player other than just the location.
	 * 
	 * The method will still work as it used to by checking disabled regions and worlds, but may not function
	 * as intended with hooks enabled.
	 * 
	 * @param loc The location to check
	 * @return True if a player is allowed to fly at the given location.
	 */
	@Deprecated
	public boolean canFlyAt(Location loc) {
		return FlyHandle.flyAllowed(loc);
	}
	
	/**
	 * Check if a player is allowed to fly at the given location. This will check all worlds and regions,
	 * as well as conditions implemented by TempFlyHooks, such as island requirements in skyblock.
	 * 
	 * @param p The player
	 * @param loc The location to check.
	 * @param invokeHooks Do you want to check flight conditions implemented by internal gameplay hooks?
	 * @return
	 */
	public FlightResult canFlyAt(Player p, Location loc, boolean invokeHooks) {
		return FlyHandle.inquireFlight(p, loc, invokeHooks);
	}
	
	/**
	 * Check if a player is allowed to fly in the given world.
	 * Will check conditions implemented by TempFlyHooks, such as island requirements in skyblock.
	 * 
	 * @param p The player
	 * @param loc The location to check.
	 * @param invokeHooks Do you want to check flight conditions implemented by internal gameplay hooks?
	 * @return
	 */
	public FlightResult canFlyAy(Player p, World world, boolean invokeHooks) {
		return FlyHandle.inquireFlight(p, world, invokeHooks);
	}
	
	/**
	 * Check if a player is allowed to fly in the given region.
	 * Will check conditions implemented by TempFlyHooks, such as island requirements in skyblock.
	 * 
	 * @param p The player
	 * @param loc The location to check.
	 * @param invokeHooks Do you want to check flight conditions implemented by internal gameplay hooks?
	 * @return
	 */
	public FlightResult canFlyAt(Player p, ProtectedRegion region, boolean invokeHooks) {
		return FlyHandle.inquireFlight(p, region, invokeHooks);
	}
	
	/**
	 * Check if a player is allowed to fly in the given ApplicableRegionSet.
	 * Will check conditions implemented by TempFlyHooks, such as island requirements in skyblock.
	 * 
	 * @param p The player
	 * @param loc The location to check.
	 * @param invokeHooks Do you want to check flight conditions implemented by internal gameplay hooks?
	 * @return
	 */
	public FlightResult canFlyAt(Player p, ApplicableRegionSet regions, boolean invokeHooks) {
		return FlyHandle.inquireFlight(p, regions, invokeHooks);
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
	
	/**
	 * Force TempFly to process a combat tag.
	 * This will use all the settings in the config like normal combat.
	 * Useful for plugins that manually handle entity damage and do not
	 * allow entities to directly harm each other. For instance, if a plugin
	 * has a custom health system and deals damage on combat with .damage(), TempFly
	 * would have no way to know this combat occured unless you use this.
	 * 
	 * @param victim The entity that got attacked
	 * @param actor The attacking entity
	 */
	public void processCombat(Entity victim, Entity actor) {
		FlyHandle.processCombat(victim, actor);
	}
} 
