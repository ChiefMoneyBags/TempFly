package com.moneybags.tempfly.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.fly.FlightResult;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.FlightResult.DenyReason;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.Files;

public class FlightEnvironment extends RequirementProvider {

	private FlightManager manager;
	
	private Map<String, RelativeTimeRegion> rtRegions = new HashMap<>();
	private Map<String, RelativeTimeRegion> rtWorlds = new HashMap<>();
	
	private List<String> blackRegion = new LinkedList<>();
	private List<String> blackWorld = new LinkedList<>();
	
	public FlightEnvironment(FlightManager manager) {
		this.manager = manager;
		blackRegion = Files.config.contains("general.disabled.regions") ? Files.config.getStringList("general.disabled.regions") : new ArrayList<>();
		blackWorld = Files.config.contains("general.disabled.worlds") ? Files.config.getStringList("general.disabled.worlds") : new ArrayList<>();
	
		ConfigurationSection csRtW = Files.config.getConfigurationSection("general.relative_time.worlds");
		if (csRtW != null) {
			for (String s : csRtW.getKeys(false)) {
				rtWorlds.put(s, new RelativeTimeRegion(
						Files.config.getDouble("general.relative_time.worlds." + s, 1), true, s));
			}
		}
		ConfigurationSection csRtR = Files.config.getConfigurationSection("general.relative_time.regions");
		if (csRtW != null) {
			for (String s : csRtR.getKeys(false)) {
				rtRegions.put(s, new RelativeTimeRegion(
						Files.config.getDouble("general.relative_time.regions." + s, 1), false, s));
			}
		}
	}

	public FlightManager getFlightManager() {
		return manager;
	}
	
	
	
	/**
	 * 
	 * --=-------------=--
	 *    Relative Time
	 * --=-------------=--
	 * 
	 */
	
	
	
	public RelativeTimeRegion[] getRelativeTimeRegions() {
		return rtRegions.values().toArray(new RelativeTimeRegion[rtRegions.size()]);
	}
	
	public RelativeTimeRegion[] getRelativeTimeWorlds() {
		return rtWorlds.values().toArray(new RelativeTimeRegion[rtWorlds.size()]);
	}
	
	public boolean hasRelativeTime(World world) {
		return rtWorlds.containsKey(world.getName());
	}
	
	public boolean hasRelativeTime(CompatRegion region) {
		return rtRegions.containsKey(region.getId());
	}
	
	public RelativeTimeRegion getRelativeTime(World world) {
		return rtWorlds.get(world.getName());
	}
	
	public RelativeTimeRegion getRelativeTime(CompatRegion region) {
		return rtRegions.get(region.getId());
	}
	
	
	
	/**
	 * 
	 * --=--------=--
	 *    Disabled
	 * --=--------=--
	 * 
	 */
	
	
	
	public boolean isDisabled(World world) {
		return blackWorld.contains(world.getName());
	}
	
	public boolean isDisabled(CompatRegion region) {
		return blackRegion.contains(region.getId());
	}
	
	
	
	/**
	 * 
	 * --=--------------------=--
	 *    Requirement Provider
	 * --=--------------------=--
	 * 
	 */
	
	
	
	/**
	 * Deprecated as of TempFly 3.0, This method will only check for blacklisted regions and worlds.
	 * I left it in because the api needs this method for legacy support.
	 * Check if flight is allowed at a given location.
	 * @param loc
	 * @return
	 */
	@Deprecated
	public boolean flyAllowed(Location loc) {
		if (manager.getTempFly().getHookManager().hasRegionProvider()) {
			for (CompatRegion r: manager.getTempFly().getHookManager().getRegionProvider().getApplicableRegions(loc)) {
				if (blackRegion.contains(r.getId())) {
					return false;
				}
			}
		}
		return !V.disabledWorlds.contains(loc.getWorld().getName());
	}
	
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, CompatRegion[] regions) {
		for (CompatRegion region: regions) {
			FlightResult result = handleFlightInquiry(user, region);
			if (!result.isAllowed()) {
				return result;
			}
		}
		return new FlightResult(true, this, InquiryType.REGION, V.requirePassDefault);
	}

	@Override
	public FlightResult handleFlightInquiry(FlightUser user, CompatRegion r) {
		return isDisabled(r) ? new FlightResult(DenyReason.DISABLED_REGION, this, InquiryType.REGION, V.requireFailRegion, V.protectRegion)
				: new FlightResult(true, this, InquiryType.REGION, V.requirePassDefault);
	}

	@Override
	public FlightResult handleFlightInquiry(FlightUser user, World world) {
		return isDisabled(world) ? new FlightResult(DenyReason.DISABLED_WORLD, this, InquiryType.WORLD, V.requireFailWorld, V.protectWorld)
				: new FlightResult(true, this, InquiryType.WORLD, V.requirePassDefault);
	}

	@Override
	public FlightResult handleFlightInquiry(FlightUser user, Location loc) {
		return null;
	}
	
	@Override
	public boolean handles(InquiryType type) {
		return type == InquiryType.LOCATION;
	}
	
	
}
