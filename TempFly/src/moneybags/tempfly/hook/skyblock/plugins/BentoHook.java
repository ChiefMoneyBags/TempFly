package moneybags.tempfly.hook.skyblock.plugins;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.hook.FlightResult;
import moneybags.tempfly.hook.HookManager.HookType;
import moneybags.tempfly.hook.TempFlyHook;

public class BentoHook extends TempFlyHook {

	public BentoHook(TempFly plugin) {
		super(HookType.BENTO_BOX, plugin);
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


}
