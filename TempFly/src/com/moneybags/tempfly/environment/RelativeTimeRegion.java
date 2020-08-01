package com.moneybags.tempfly.environment;

public class RelativeTimeRegion {

	private double factor;
	private boolean isWorld;
	private String name;
	
	public RelativeTimeRegion(double factor, boolean isWorld, String name) {
		this.factor = factor;
		this.isWorld = isWorld;
		this.name = name;
	}
	
	public double getFactor() {
		return factor;
	}
	
	public boolean isWorld() {
		return isWorld;
	}
	
	public String getName() {
		return name;
	}
}
