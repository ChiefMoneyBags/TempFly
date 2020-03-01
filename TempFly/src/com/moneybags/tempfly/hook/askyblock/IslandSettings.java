package com.moneybags.tempfly.hook.askyblock;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.U;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import com.wasteofplastic.askyblock.Island;

public class IslandSettings {

	private boolean team;
	private boolean coop;
	private boolean visitor;
	private Island island;
	
	public IslandSettings(Location loc) {
		this.island = ASkyBlockAPI.getInstance().getIslandAt(loc);
		String l = U.locationToString(loc);
		AskyblockHook hook = TempFly.getAskyblockHook();
		FileConfiguration data = hook.getIslandData();
		if (!data.contains("islands." + l)) {
			team = hook.getDefaultTeam();
			coop = hook.getDefaultCoop();
			visitor = hook.getDefaultVisitor();
			
			data.createSection("islands." + l);
			data.set("islands." + l + ".team", team);
			data.set("islands." + l + ".coop", coop);
			data.set("islands." + l + ".visitor", visitor);
			hook.saveIslandData();
		} else {
			team = data.getBoolean("islands." + l + ".team");
			coop = data.getBoolean("islands." + l + ".coop");
			visitor = data.getBoolean("islands." + l + ".visitor");
		}
	}
	
	public boolean getTeamCanFly() {
		return team;

	}
	
	public boolean getCoopCanFly() {
		return coop;
	}
	
	public boolean getVisitorCanFly() {
		return visitor;
	}
	
	public Island getIsland() {
		return island;
	}
	
	public void setTeamCanFly(boolean b) {
		this.team = b;
		saveSettings();
	}
	
	public void setCoopCanFly(boolean b) {
		this.coop = b;
		saveSettings();
	}
	
	public void setVisitorCanFly(boolean b) {
		this.visitor = b;
		U.logS("set: " + String.valueOf(b));
		saveSettings();
	}
	
	public void saveSettings() {
		AskyblockHook hook = TempFly.getAskyblockHook();
		Location loc = island.getCenter();
		String l = U.locationToString(loc);
		FileConfiguration data = hook.getIslandData();
		data.set("islands." + l + ".team", team);
		data.set("islands." + l + ".coop", coop);
		data.set("islands." + l + ".visitor", visitor);
		hook.saveIslandData();
	}
	
}
