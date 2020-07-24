package moneybags.tempfly.hook.skyblock;

import java.util.List;

import javax.annotation.Nullable;

import moneybags.tempfly.hook.skyblock.SkyblockHook.SkyblockRequirementType;

public class SkyblockRequirement {

	private SkyblockRequirementType type;
	
	private long islandLevel;
	private long ownerLevel;
	@Nullable
	private String[] challenges;
	private String[] ownerChallenges;
	@Nullable
	private String name;
	
	public SkyblockRequirement(
			List<String> challenges, List<String> ownerChallenges,
			long islandLevel, long ownerLevel,
			String name, SkyblockRequirementType type) {
		this.type = type;
		this.name = name;
		
		this.challenges = challenges == null ? null : challenges.toArray(new String[challenges.size()]);
		this.ownerChallenges = ownerChallenges == null ? null : ownerChallenges.toArray(new String[challenges.size()]);
		this.islandLevel = islandLevel;
		this.ownerLevel = ownerLevel;
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
	public String[] getChallenges() {
		return challenges;
	}
	
	public String[] getOwnerChallenges() {
		return ownerChallenges;
	}
	
	public SkyblockRequirementType getType() {
		return type;
	}
	
}
