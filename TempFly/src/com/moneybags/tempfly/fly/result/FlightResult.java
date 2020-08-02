package com.moneybags.tempfly.fly.result;

import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.RequirementProvider.InquiryType;
import com.moneybags.tempfly.util.V;

public abstract class FlightResult {
	
	private boolean allowed;
	private DenyReason reason;
	private InquiryType type;
	private String message;
	private RequirementProvider requirement;
	private boolean fallSafely;

	public FlightResult(boolean allowed, DenyReason reason, InquiryType type, String message, RequirementProvider requirement, boolean fallSafely) {
		this.allowed = allowed;
		this.reason = reason;
		this.type = type;
		this.message = message;
		this.requirement = requirement;
		this.fallSafely = fallSafely;
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
