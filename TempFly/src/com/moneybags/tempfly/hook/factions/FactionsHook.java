package com.moneybags.tempfly.hook.factions;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.RequirementProvider.InquiryType;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.hook.HookManager.Genre;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.hook.TempFlyHook;

public abstract class FactionsHook extends TempFlyHook {

	public FactionsHook(TempFly tempfly) {
		super(tempfly);
	}
	

	
	
	@Override
	public Genre getGenre() {
		return Genre.FACTIONS;
	}

	@Override
	public String getConfigName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEmbeddedConfigName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Class<? extends TempFlyCommand>> getCommands() {
		return null;
	}

	@Override
	public boolean initializeFiles() throws Exception {
		if (!super.initializeFiles()) {
			return false;
		}
		//tempfly.getDataBridge().initializeHookData(this, DataTable.ISLAND_SETTINGS);
		return true;
	}
	
	@Override
	public boolean initializeHook() {
		loadValues();
		return true;
	}
	
	
	public void loadValues() {
		Console.debug("", "----Loading Factions Settings----");
		FileConfiguration config = getConfig();
	}
	
	public abstract double getCurrentPower(UUID playerId);
	
	public abstract double getMaxPower(UUID playerId);
	
	public abstract double getCurrentPower(FactionWrapper faction);

	public abstract double getMaxPower(FactionWrapper faction);
	
	public abstract FactionWrapper getFaction(UUID playerId);
	
	public abstract UUID getFactionOwner(FactionWrapper faction);
	
	public abstract String getRole(UUID playerId, FactionWrapper faction);
	
	public abstract FactionWrapper getFactionAt(Location loc);
	
	public abstract boolean isWithinTerritory(Location loc, FactionWrapper faction);
	
}
