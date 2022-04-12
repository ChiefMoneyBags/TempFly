package com.moneybags.tempfly.util.data;

import com.moneybags.tempfly.util.data.DataBridge.DataValue;

public class DataPointer {

	public static DataPointer of(DataValue value, String... path) {
		return new DataPointer(value, path);
	}
	
	private DataValue value;
	private String[] path;
	
	private DataPointer(DataValue value, String... path) {
		this.value = value;
		this.path = path;
	}

	public DataValue getValue() {
		return value;
	}
	
	public String[] getPath() {
		return path;
	}
	
	@Override
	public int hashCode() {
		int code = 0;
		for (String string: path) {
			code += string.hashCode();
		}
		return value.toString().hashCode() + code;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj.hashCode() == this.hashCode();
	}

}
