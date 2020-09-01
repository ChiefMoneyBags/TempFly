package com.moneybags.tempfly.hook.skyblock;

public class SkyblockChallenge {

	private String name;
	private int progress;
	private int completed;
	
	public SkyblockChallenge(String name, int progress, int completed) {
		this.name = name;
		this.progress = progress;
		this.completed = completed;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRequiredProgress() {
		return progress;
	}
	
	public int getRequiredCompletions() {
		return completed;
	}
	
	@Override
	public String toString() {
		return "[SkyblockChallenge] Name: (" + name + ") | RequiredProgress: (" + progress + ") | RequiredCompletions: (" + completed + ")";
	}

}
