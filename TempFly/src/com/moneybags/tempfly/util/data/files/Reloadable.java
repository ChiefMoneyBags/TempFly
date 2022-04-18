package com.moneybags.tempfly.util.data.files;

public interface Reloadable {
	
	public default void onTempflyReload() {
		return;
	}

}
