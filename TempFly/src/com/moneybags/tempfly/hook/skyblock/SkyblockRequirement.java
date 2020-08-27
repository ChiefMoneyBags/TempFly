package com.moneybags.tempfly.hook.skyblock;

import java.util.List;

import javax.annotation.Nullable;

import com.moneybags.tempfly.hook.skyblock.SkyblockHook.SkyblockRequirementType;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class SkyblockRequirement {

	private SkyblockRequirementType type;
	
	private long islandLevel;
	private long ownerLevel;
	@Nullable
	private SkyblockChallenge[] challenges;
	private SkyblockChallenge[] ownerChallenges;
	@Nullable
	private String name;
	
	public SkyblockRequirement(
			SkyblockChallenge[] challenges, SkyblockChallenge[] ownerChallenges,
			long islandLevel, long ownerLevel,
			String name, SkyblockRequirementType type) {
		this.type = type;
		this.name = name;
		
		this.challenges = challenges == null ? new SkyblockChallenge[0] : challenges;
		this.ownerChallenges = ownerChallenges == null ? new SkyblockChallenge[0] : ownerChallenges;
		
		this.islandLevel = islandLevel;
		this.ownerLevel = ownerLevel;
		
		if (V.debug) {
			Console.debug("----< name: " + name,
			"----< challenges: " + U.arrayToString(this.challenges, ", "),
			"----< owner_challenges: " + U.arrayToString(this.ownerChallenges, ", "),
			"----< island_level: " + islandLevel,
			"----< owner_level: " + ownerLevel,
			"");
		}
	}
	
	@Nullable
	public String getName() {
		return name;
	}
	
	public long getIslandLevel() {
		return islandLevel;
	}
	
	public long getOwnerLevel() {
		return ownerLevel;
	}
	
	@Nullable
	public SkyblockChallenge[] getChallenges() {
		return challenges;
	}
	
	public SkyblockChallenge[] getOwnerChallenges() {
		return ownerChallenges;
	}
	
	public SkyblockRequirementType getType() {
		return type;
	}
	
}
