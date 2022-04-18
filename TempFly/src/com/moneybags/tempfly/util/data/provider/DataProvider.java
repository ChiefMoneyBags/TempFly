package com.moneybags.tempfly.util.data.provider;

import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.values.DataPointer;
import com.moneybags.tempfly.util.data.values.StagedChange;

public interface DataProvider {

	public default Object getOrDefault(DataPointer pointer, Object def) {
		Object object;
		try { object = getValue(pointer); } catch (Exception e) {
			e.printStackTrace();
			return def;
		}
		if (V.debug) {Console.debug("", "-----Data Bridge Get or Default Value-----", "--|> Got: " + object, "--|> Returning: " + String.valueOf(object == null ? def : object));}
		return object == null ? def : object;
	}
	
	public Object getValue(DataPointer pointer);
	
	public void setValue(StagedChange change);
	
}
