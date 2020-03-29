package com.moneybags.tempfly.hook.askyblock;

import java.util.ArrayList;
import java.util.List;

import com.moneybags.tempfly.util.F;

public class IslandRequirements {

	private List<String> challenges = new ArrayList<>();
	private long islandLevel;
	private String name;
	
	public IslandRequirements(String path, String name) {
		challenges = F.config.getStringList(path + ".challenges");
		islandLevel = F.config.getLong(path + ".island_level", 0);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public long getRequiredLevel() {
		return islandLevel;
	}
	
	public List<String> getRequiredChallenges() {
		return challenges;
	}
	
}
