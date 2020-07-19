package com.moneybags.tempfly.hook;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public interface TempFlyHook {

	abstract boolean isEnabled();

	abstract FlightResult handleFlightInquiry(Player p, ApplicableRegionSet regions);
	
	abstract FlightResult handleFlightInquiry(Player p, ProtectedRegion r);

	abstract FlightResult handleFlightInquiry(Player p, World world);
	
	abstract FlightResult handleFlightInquiry(Player p, Location loc);
	
	abstract FileConfiguration getHookConfig();
}
