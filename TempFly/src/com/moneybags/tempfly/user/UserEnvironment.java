package com.moneybags.tempfly.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import com.moneybags.tempfly.environment.FlightEnvironment;
import com.moneybags.tempfly.environment.RelativeTimeRegion;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.util.Console;

public class UserEnvironment {

	private final FlightUser user;
	private final FlightEnvironment environment;
	
	private boolean freeFlight;
	
	private final List<CompatRegion> encompassing = new LinkedList<>();
	
	private final List<RelativeTimeRegion> rtRegions = new ArrayList<>();
	private RelativeTimeRegion rtWorld;
	
	public UserEnvironment(FlightUser user, Player p) {
		Console.debug("--| Loading user environment...");
		this.user = user;
		this.environment = user.getFlightManager().getFlightEnvironment();
		
		encompassing.addAll(Arrays.asList(
				user.getFlightManager().getTempFly().getHookManager().hasRegionProvider()
				? user.getFlightManager().getTempFly().getHookManager().getRegionProvider().getApplicableRegions(user.getPlayer().getLocation())
				: new CompatRegion[0]));
		
		StringBuilder builder = new StringBuilder();
		encompassing.stream().forEach(rg -> builder.append(rg.getId() + ", "));
		Console.debug("--| Current regions: " + builder);
		asessRtRegions();
		asessRtWorld();
		asessInfiniteFlight();
	}
	
	public FlightUser getUser() {
		return user;
	}
	
	
	/**
	 * 
	 * --=-----------------=--
	 *    Real Time Regions
	 * --=-----------------=--
	 * 
	 */
	
	
	
	public CompatRegion[] getCurrentRegionSet() {
		return encompassing == null ? null : encompassing.toArray(new CompatRegion[encompassing.size()]);
	}
	
	public void updateCurrentRegionSet(CompatRegion[] regions) {
		this.encompassing.clear();
		this.encompassing.addAll(Arrays.asList(regions));
		asessRtRegions();
		asessInfiniteFlight();
	}
	
	public boolean isInside(CompatRegion region) {
		for (CompatRegion inside: encompassing) {
			if (inside.equals(region)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 
	 * --=------------=--
	 *    RelativeTime
	 * --=------------=--
	 * 
	 */
	
	
	public RelativeTimeRegion[] getRelativeTimeRegions() {
		List<RelativeTimeRegion> list = new LinkedList<>();
		list.addAll(rtRegions);
		if (rtWorld != null) list.add(rtWorld);
		return list.toArray(new RelativeTimeRegion[list.size()]);
	}

	public void asessRtWorld() {
		rtWorld = environment.getRelativeTime(user.getPlayer().getWorld());
	}
	
	public void asessRtRegions() {
		RelativeTimeRegion[] rtArray = user.getFlightManager().getFlightEnvironment().getRelativeTimeRegions();
		if (rtArray.length == 0) {
			return;
		}
		List<String> regions = new ArrayList<>();
		for(CompatRegion r : encompassing) {
			regions.add(r.getId());
		}
		for (RelativeTimeRegion rt : rtArray) {
			String rtName = rt.getName();
			if ((regions.contains(rtName)) && !(rtRegions.contains(rt))) {
				rtRegions.add(rt);
			} else if (!(regions.contains(rtName)) && (rtRegions.contains(rt))) {
				rtRegions.remove(rt);	
			}
		}
	}
	
	public void asessInfiniteFlight() {
		if (environment.isInfinite(user.getPlayer().getWorld())) {
			freeFlight = true;
			return;
		}
		
		for (CompatRegion r: encompassing) {
			if (environment.isInfinite(r)) {
				freeFlight = true;
				return;
			}
		}
		freeFlight = false;
	}
	
	public boolean hasInfiniteFlight() {
		return freeFlight;
	}
	

	/**
	 * @param regions The list of regions to check
	 * @return True if the list is the same.
	 */
	public boolean checkIdenticalRegions(List<CompatRegion> regions) {
		if (regions.size() != encompassing.size()) {
			return false;
		}
		check:
		for (CompatRegion check: regions) {
			for (CompatRegion current: encompassing) {
				if (current.equals(check)) {
					continue check;
				}
			}
			return false;
		}
		return true;
	}
}
