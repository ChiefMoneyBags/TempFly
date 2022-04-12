package com.moneybags.tempfly.hook.factions;

import com.moneybags.tempfly.hook.TerritoryWrapper;

public class FactionWrapper implements TerritoryWrapper {

	private Object rawFaction;
	
	public FactionWrapper(Object rawFaction) {
		this.rawFaction = rawFaction;
	}
	
	@Override
	public Object getRawTerritory() {
		return rawFaction;
	}

}
