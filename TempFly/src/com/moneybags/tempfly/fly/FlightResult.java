package com.moneybags.tempfly.fly;

import com.moneybags.tempfly.fly.RequirementProvider.InquiryType;
import com.moneybags.tempfly.util.V;

public class FlightResult {
	
	private boolean allowed;
	private DenyReason reason;
	private InquiryType type;
	private String message;
	private RequirementProvider requirement;
	private boolean fallSafely;
	
	
	public FlightResult(DenyReason reason, RequirementProvider requirement, InquiryType type, String message, boolean fallSafely) {
		this.allowed = false;
		this.reason = reason;
		this.message = message;
		this.fallSafely = fallSafely;
		this.requirement = requirement;
		this.type = type;
	}
	
	public FlightResult(boolean allowed, RequirementProvider requirement, InquiryType type, String message) {
		this.allowed = allowed;
		this.type = type;
		this.message = message;
		this.requirement = requirement;
	}

	public boolean isAllowed() {
		return allowed;
	}
	
	public DenyReason getDenyReason() {
		return reason;
	}
	
	public InquiryType getInquiryType() {
		return type;
	}
	
	public FlightResult setInquiryType(InquiryType type) {
		this.type = type;
		return this;
	}
	
	public String getMessage() {
		return message == null ? (allowed ? V.requirePassDefault : V.requireFailDefault) : message;
	}
	
	public RequirementProvider getRequirement() {
		return requirement;
	}
	
	public boolean hasDamageProtection() {
		return fallSafely;
	}
	
	public static enum DenyReason {
		COMBAT,
		DISABLED_WORLD,
		DISABLED_REGION,
		REQUIREMENT,
		OTHER;
	}

}
