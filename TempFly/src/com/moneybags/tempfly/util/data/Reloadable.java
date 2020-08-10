package com.moneybags.tempfly.util.data;

public interface Reloadable {
	
	public default void onTempflyReload() {
		return;
	}

}
