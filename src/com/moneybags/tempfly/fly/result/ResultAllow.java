package com.moneybags.tempfly.fly.result;

import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.RequirementProvider.InquiryType;

/**
 * I know it looks kinda stupid but I made these 2 flight result classes seperate to help avoid consfusion
 * when people are using the API. They used to just be 2 seperate constructors in FlightResult.
 * I could forsee people using the wrong constructor and causing nullpointers in the plugin
 */
public class ResultAllow extends FlightResult {

	public ResultAllow(RequirementProvider requirement, InquiryType type, String allowMessage) {
		super(true, null, type, allowMessage, requirement, true);
	}

}
