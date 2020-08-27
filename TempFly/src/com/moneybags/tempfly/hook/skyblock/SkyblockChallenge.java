package com.moneybags.tempfly.hook.skyblock;

public class SkyblockChallenge {

	private String name;
	private int progress;
	
	public SkyblockChallenge(String name, int progress) {
		this.name = name;
		this.progress = progress;
	}
	
	public String getName() {
		return name;
	}
	
	public int getRequiredProgress() {
		return progress;
	}

}
