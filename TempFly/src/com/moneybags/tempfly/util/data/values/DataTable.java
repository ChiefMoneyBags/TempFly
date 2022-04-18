package com.moneybags.tempfly.util.data.values;

//TODO DataTable needs to be converted to some kind of abstract data structure to add expansions to data management
public enum DataTable {
	TEMPFLY_DATA("uuid"),
	
	//This table needs removed or properly implemented for sql.
	ISLAND_SETTINGS;
	
	private DataTable() {}
	
	private String primary;
	
	private DataTable(String primary) {
		this.primary = primary;
	}
	
	public String getPrimaryKey() {
		return primary;
	}
	
	public String getSqlTable() {
		switch (this) {
		case TEMPFLY_DATA:
			return "tempfly_data";
		default:
			return null;
		}
	}
}