package com.moneybags.tempfly.hook.skyblock;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.moneybags.tempfly.hook.skyblock.a.AskyblockHook;
import com.moneybags.tempfly.hook.skyblock.a.AskyblockHook.RequirementType;
import com.moneybags.tempfly.util.F;

public class AskyblockRequirement {

	private RequirementType type;
	private long islandLevel;
	@Nullable
	private String[] challenges;
	@Nullable
	private String name;
	
	public AskyblockRequirement(List<String> challenges, long islandLevel, String name, RequirementType type) {
		this.type = type;
		this.name = name;
		this.challenges = challenges == null ? null : challenges.toArray(new String[challenges.size()]);
		this.islandLevel = islandLevel;
	}
	
	public AskyblockRequirement(List<String> challenges, long islandLevel, RequirementType type) {
		this.type = type;
	}
	
	@Nullable
	public String getName() {
		return name;
	}
	
	public long getRequiredLevel() {
		return islandLevel;
	}
	
	@Nullable
	public String[] getRequiredChallenges() {
		return challenges;
	}
	
	public RequirementType getType() {
		return type;
	}
	
}
