package com.moneybags.tempfly.hook;

public class FlightResult {

	private boolean allowed;
	private DenyReason reason;
	private String message;
	private int delay;
	
	public FlightResult(boolean allowed, DenyReason reason, String message) {
		this.allowed = allowed;
		this.reason = reason;
		this.message = message;
	}
	
	public FlightResult(boolean allowed, DenyReason reason, String message, int delay) {
		this.allowed = allowed;
		this.reason = reason;
		this.message = message;
		this.delay = delay;
	}
	
	public FlightResult(boolean allowed) {
		this.allowed = true;
		this.message = "";
		this.reason = DenyReason.OTHER;
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	public DenyReason getDenyReason() {
		return reason;
	}
	
	public String getMessage() {
		return message;
	}
	
	public int getDelay() {
		return delay;
	}
	
	public static enum DenyReason {
		REQUIREMENT,
		DISABLED_WORLD,
		DISABLED_REGION,
		OTHER;
	}

}
