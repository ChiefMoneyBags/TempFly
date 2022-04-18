package com.moneybags.tempfly.util.data.values;

import com.moneybags.tempfly.util.data.files.DataFileHolder;

public class StagedChange {
	DataValue value;
	String[] path;
	Object data;
	DataFileHolder fileHolder;
	
	public StagedChange(DataValue value, Object data, String[] path, DataFileHolder fileHolder) {
		this.value = value;
		this.path = path;
		this.data = data;
		this.fileHolder = fileHolder;
	}

	public DataPointer getPointer() {
		return DataPointer.of(value, path);
	}

	public DataFileHolder getFileHolder() {
		return fileHolder;
	}
	
	public DataValue getValue() {
		return value;
	}
	
	public String[] getPath() {
		return path;
	}
	
	public Object getData() {
		return data;
	}
	
	public boolean comparePathPartial(String... path) {
		for (int index = 0; path.length > index; index++) {
			if (this.path.length <= index || !path[index].equals(this.path[index])) {
				return false;
			}
		}
		return true;
	}
	
}