package com.moneybags.tempfly.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.FlightResult.DenyReason;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.config.ConfigSection;

public class FlightEnvironment implements RequirementProvider {

	private FlightManager manager;
	
	private Map<String, RelativeTimeRegion> rtRegions = new HashMap<>();
	private Map<String, RelativeTimeRegion> rtWorlds = new HashMap<>();
	
	private List<String> blackRegions = new ArrayList<>();
	private List<String> blackWorlds = new ArrayList<>();
	
	private List<String> whiteRegions = new ArrayList<>();
	private List<String> whiteWorlds = new ArrayList<>();
	
	private List<String> freeRegions = new ArrayList<>();
	private List<String> freeWorlds = new ArrayList<>();
	
	
	
	private float speedGlobal = 1;
	private boolean allowPreferredSpeed;
	private Map<String, Float> speedWorlds = new HashMap<>();
	private Map<String, Float> speedRegions = new HashMap<>();
	
	
	public FlightEnvironment(FlightManager manager) {
		this.manager = manager;
		onTempflyReload();
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
		return blackWorlds.contains(world.getName());
	}
	
	public boolean isDisabled(CompatRegion region) {
		return blackRegions.contains(region.getId());
	}
	
	
	
	/**
	 * 
	 * --=---------=--
	 *    whitelist
	 * --=---------=--
	 * 
	 */

	public boolean isWhitelisted(World world) {
		return whiteWorlds.size() == 0 || whiteWorlds.contains(world.getName());
	}
	
	public boolean isWhitelisted(CompatRegion region) {
		return whiteRegions.size() == 0 || whiteRegions.contains(region.getId());
	}
	
	
	
	/**
	 * 
	 * --=---------=--
	 *    infinite
	 * --=---------=--
	 * 
	 */
	
	public boolean isInfinite(World world) {
		return freeWorlds.contains(world.getName());
	}
	
	public boolean isInfinite(CompatRegion region) {
		return freeRegions.contains(region.getId());
	}
	
	
	/**
	 * 
	 * --=---------=--
	 *      Speed
	 * --=---------=--
	 * 
	 */
	
	
	
	public boolean hasMaxSpeed(World world) {
		return speedWorlds.containsKey(world.getName());
	}
	
	public boolean hasMaxSpeed(CompatRegion region) {
		//Console.debug(region.getId(), speedRegions.containsKey(region.getId()));
		return speedRegions.containsKey(region.getId());
	}
	
	public boolean hasMaxSpeed(CompatRegion[] regions) {
		for (CompatRegion region: regions) {
			if (hasMaxSpeed(region)) {
				return true;
			}
		}
		return false;
	}
	
	public float getDefaultSpeed() {
		return speedGlobal;
	}
	
	public float getMaxSpeed(World world) {
		return speedWorlds.getOrDefault(world.getName(), getDefaultSpeed());
	}
	
	public float getMaxSpeed(CompatRegion region) {
		//Console.debug(region.getId(), speedRegions.get(region.getId()));
		return speedRegions.getOrDefault(region.getId(), getDefaultSpeed());
	}
	
	public float getMaxSpeed(CompatRegion[] regions) {
		float highest = -999;
		for (CompatRegion region: regions) {
			if (hasMaxSpeed(region)) {
				float speed = getMaxSpeed(region);
				if (highest == -999 || highest < speed) {
					highest = speed;
				}
			}
		}
		return highest == -999 ? getDefaultSpeed() : highest;
	}
	
	public boolean allowSpeedPreference() {
		return allowPreferredSpeed;
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
	 * @param loc the location in question.
	 * @return
	 */
	@Deprecated
	public boolean flyAllowed(Location loc) {
		if (manager.getTempFly().getHookManager().hasRegionProvider()) {
			for (CompatRegion r: manager.getTempFly().getHookManager().getRegionProvider().getApplicableRegions(loc)) {
				if (blackRegions.contains(r.getId())) {
					return false;
				}
			}
		}
		return !V.disabledWorlds.contains(loc.getWorld().getName());
	}
	
	/**
	 * disabled regions
	 */
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, CompatRegion[] regions) {
		for (CompatRegion region: regions) {
			FlightResult result = handleFlightInquiry(user, region);
			if (!result.isAllowed()) {
				return result;
			}
		}
		return new ResultAllow(this, InquiryType.REGION, V.requirePassDefault);
	}

	/**
	 * disabled region
	 */
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, CompatRegion r) {
		return isDisabled(r) || !isWhitelisted(r) ? new ResultDeny(DenyReason.DISABLED_REGION, this, InquiryType.REGION, V.requireFailRegion, !V.damageRegion)
				: new ResultAllow(this, InquiryType.REGION, V.requirePassDefault);
	}

	/**
	 * disabled worlds
	 */
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, World world) {
		return isDisabled(world) || !isWhitelisted(world) ? new ResultDeny(DenyReason.DISABLED_WORLD, this, InquiryType.WORLD, V.requireFailWorld, !V.damageWorld)
				: new ResultAllow(this, InquiryType.WORLD, V.requirePassDefault);
	}

	/**
	 * max y
	 */
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, Location loc) {
		if (user.hasFlightRequirement(this, InquiryType.LOCATION)) {
			return loc.getBlockY() <= V.maxY-5 ? new ResultAllow(this, InquiryType.LOCATION, V.requirePassDefault) :
				new ResultDeny(DenyReason.OTHER, this, InquiryType.LOCATION, 
					V.requireFailHeight.replaceAll("\\{MAX_Y}", String.valueOf(V.maxY)), false);
		}
		return loc.getBlockY() > V.maxY ? new ResultDeny(DenyReason.OTHER, this, InquiryType.LOCATION, 
				V.requireFailHeight.replaceAll("\\{MAX_Y}", String.valueOf(V.maxY)), false) 
				: new ResultAllow(this, InquiryType.LOCATION, V.requirePassDefault);
	}
	
	@Override
	public boolean handles(InquiryType type) {
		return false;
	}

	@Override
	public void onTempflyReload() {
		ConfigSection config = getFlightManager().getTempFly().getConfigProvider().getDefaultConfig();
		
		blackRegions = config.contains("general.disabled.regions") ? config.getStringList("general.disabled.regions") : new ArrayList<>();
		blackWorlds = config.contains("general.disabled.worlds") ? config.getStringList("general.disabled.worlds") : new ArrayList<>();
		
		whiteRegions = config.contains("general.whitelist.regions") ? config.getStringList("general.whitelist.regions") : new ArrayList<>();
		whiteWorlds = config.contains("general.whitelist.worlds") ? config.getStringList("general.whitelist.worlds") : new ArrayList<>();
	
		whiteRegions = config.contains("general.whitelist.regions") ? config.getStringList("general.whitelist.regions") : new ArrayList<>();
		whiteWorlds = config.contains("general.whitelist.worlds") ? config.getStringList("general.whitelist.worlds") : new ArrayList<>();
		
		freeRegions = config.contains("general.time.infinite.regions") ? config.getStringList("general.time.infinite.regions") : new ArrayList<>();
		freeWorlds = config.contains("general.time.infinite.worlds") ? config.getStringList("general.time.infinite.worlds") : new ArrayList<>();
		
		ConfigSection csRtW = config.getConfigSection("other.relative_time.worlds");
		if (csRtW != null) {
			for (String s : csRtW.getKeys(false)) {
				rtWorlds.put(s, new RelativeTimeRegion(
						config.getDouble("other.relative_time.worlds." + s, 1), true, s));
			}
		}
		ConfigSection csRtR = config.getConfigSection("other.relative_time.regions");
		if (csRtW != null) {
			for (String s : csRtR.getKeys(false)) {
				rtRegions.put(s, new RelativeTimeRegion(
						config.getDouble("other.relative_time.regions." + s, 1), false, s));
			}
		}
		
		ConfigSection csSpeedW = config.getConfigSection("general.flight.speed.worlds");
		if (csSpeedW != null) {
			for (String s : csSpeedW.getKeys(false)) {
				speedWorlds.put(s, (float) config.getDouble("general.flight.speed.worlds." + s, 1));
			}
		}
		ConfigSection csSpeedR = config.getConfigSection("general.flight.speed.regions");
		if (csSpeedR != null) {
			for (String s : csSpeedR.getKeys(false)) {
				Console.debug(s, config.getDouble("general.flight.speed.regions." + s));
				
				speedRegions.put(s, (float) config.getDouble("general.flight.speed.regions." + s, 1));
			}
		}
		
		// legacy default speed.
		speedGlobal = (float) config.getDouble("general.flight.default_speed");
		// new default speed.
		if (speedGlobal == 0) {
			speedGlobal = (float) config.getDouble("general.flight.speed.default", 1);
		}
		
		allowPreferredSpeed = config.getBoolean("general.flight.speed.user_preference", true);
		
		
	}
	
	
}
