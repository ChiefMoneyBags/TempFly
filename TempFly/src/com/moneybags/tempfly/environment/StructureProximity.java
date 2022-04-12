package com.moneybags.tempfly.environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.StructureType;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
import com.moneybags.tempfly.fly.result.FlightResult.DenyReason;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.Files;

public class StructureProximity implements RequirementProvider {
	
	private FlightManager manager;
	
	private Map<StructureType, Integer> structs = new HashMap<>();
	
	public StructureProximity(FlightManager manager) throws NoSuchMethodException, SecurityException, ClassNotFoundException {
		this.manager = manager;
		Class.forName("org.bukkit.World").getMethod("locateNearestStructure", Location.class, StructureType.class, Integer.TYPE, Boolean.TYPE);
		onTempflyReload();
	}
	
	public FlightManager getFlightManager() {
		return this.manager;
	}
	
	@Override
	public FlightResult handleFlightInquiry(FlightUser user, Location loc) {
		World world = loc.getWorld();
		for (Entry<StructureType, Integer> entry: structs.entrySet()) {
			Location closest = world.locateNearestStructure(loc, entry.getKey(), entry.getValue(), false);
			if (closest != null) {
				Bukkit.broadcastMessage(String.valueOf(closest.distanceSquared(loc)));
				return new ResultDeny(DenyReason.OTHER, this, InquiryType.LOCATION, 
						V.requireFailStruct
						.replaceAll("\\{STRUCTURE}", entry.getKey().getName()), !V.damageStruct);
			}
		}
		return new ResultAllow(this, InquiryType.LOCATION, V.requirePassDefault);
	}

	@Override
	public boolean handles(InquiryType type) {
		return type != InquiryType.LOCATION;
	}
	
	@Override
	public void onTempflyReload() {
		ConfigurationSection csStruct = Files.config.getConfigurationSection("general.structure_proximity");
		if (csStruct == null) {
			return;
		}
		Map<String, StructureType> registered = StructureType.getStructureTypes();
		Console.debug(registered);
		for (String key: csStruct.getKeys(false)) {
			if (!registered.containsKey(key)) {
				Console.warn("An invalid structure type is defined in the config! (" + key + ")");
				continue;
			}
			int range = Files.config.getInt("general.structure_proximity." + key, -1);
			StructureType struct = registered.get(key);
			this.structs.put(struct, range);
		}
	}


}
