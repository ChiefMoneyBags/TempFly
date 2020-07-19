package com.moneybags.tempfly.hook.skyblock.b;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.FlightResult;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class BskyblockHook implements TempFlyHook {

	public BskyblockHook(TempFly plugin) {
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public FlightResult handleFlightInquiry(Player p, ApplicableRegionSet regions) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public FlightResult handleFlightInquiry(Player p, ProtectedRegion r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlightResult handleFlightInquiry(Player p, Location loc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlightResult handleFlightInquiry(Player p, World world) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileConfiguration getHookConfig() {
		// TODO Auto-generated method stub
		return null;
	}


}
