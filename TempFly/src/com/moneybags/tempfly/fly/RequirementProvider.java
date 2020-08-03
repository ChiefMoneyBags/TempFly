package com.moneybags.tempfly.fly;

import org.bukkit.Location;
import org.bukkit.World;

import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;

public interface RequirementProvider {
	
	/**
	 * Inquire whether a player can fly within a set of given regions.
	 * @param p
	 * @param regions
	 * @return
	 */
	public abstract FlightResult handleFlightInquiry(FlightUser user, CompatRegion[] regions);
	
	/**
	 * Inquire whether a player can fly within a specific regions.
	 * @param p
	 * @param regions
	 * @return
	 */
	public abstract FlightResult handleFlightInquiry(FlightUser user, CompatRegion r);

	/**
	 * Inquire whether a player can fly within a given world.
	 * @param p
	 * @param regions
	 * @return
	 */
	public abstract FlightResult handleFlightInquiry(FlightUser user, World world);
	
	/**
	 * Inquire whether a player can fly at a given location.
	 * @param p
	 * @param regions
	 * @return
	 */
	public abstract FlightResult handleFlightInquiry(FlightUser user, Location loc);

	/**
	 * Should tempfly inquire flight for the players location or will the provider handle it.
	 * Useful to save on unnecessary checks if the provider does not need it.
	 * 
	 * For example, why check if flight is allowed when a player enters a region in the combat manager,
	 * it has nothing to do with combat.
	 * @return true if tempfly should let the RequirementProvider handle its own location.
	 */
	public abstract boolean handles(InquiryType type);

	/**
	 * 
	 * Defines the types of inquiry. Mainly used to track where a FlightResult originated.
	 *
	 */
	public static enum InquiryType {
		/**
		 * Location Inquiry
		 */
		LOCATION,
		/**
		 * World Inquiry
		 */
		WORLD,
		/**
		 * Region Inquiry
		 */
		REGION,
		/**
		 * Inquiry is not within the scope of the base tempfly plugin, for instance
		 * island plots in the skyblock hook cannot be processed by the FlightManager, they are out_of_scope.
		 */
		OUT_OF_SCOPE;
	}
}
