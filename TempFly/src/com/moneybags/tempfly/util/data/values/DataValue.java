package com.moneybags.tempfly.util.data.values;

//TODO DataValue needs to be converted to some kind of abstract data structure to add expansions to data management
public enum DataValue {
	PLAYER_TIME(
			DataTable.TEMPFLY_DATA,
			Double.TYPE,
			"player_time",
			new String[] {"players", "time"},
			false),
	PLAYER_FLIGHT_LOG(
			DataTable.TEMPFLY_DATA,
			Boolean.TYPE,
			"logged_in_flight",
			new String[] {"players", "logged_in_flight"},
			false),
	PLAYER_COMPAT_FLIGHT_LOG(
			DataTable.TEMPFLY_DATA,
			Boolean.TYPE,
			"compat_logged_in_flight",
			new String[] {"players", "compat_logged_in_flight"},
			false),
	PLAYER_DAMAGE_PROTECTION(
			DataTable.TEMPFLY_DATA,
			Boolean.TYPE,
			"damage_protection",
			new String[] {"players", "damage_protection"},
			false),
	PLAYER_DAILY_BONUS(
			DataTable.TEMPFLY_DATA,
			Long.TYPE,
			"last_daily_bonus",
			new String[] {"players", "last_daily_bonus"},
			false),
	PLAYER_TRAIL(
			DataTable.TEMPFLY_DATA,
			String.class,
			"trail",
			new String[] {"players", "trail"},
			false),
	PLAYER_INFINITE(
			DataTable.TEMPFLY_DATA,
			Boolean.TYPE,
			"infinite",
			new String[] {"players", "infinite"},
			false),
	PLAYER_BYPASS(
			DataTable.TEMPFLY_DATA,
			Boolean.TYPE,
			"bypass",
			new String[] {"players", "bypass"},
			false),
	PLAYER_SPEED(
			DataTable.TEMPFLY_DATA,
			Double.TYPE,
			"speed",
			new String[] {"players", "speed"},
			false),
	
	
	
	
	ISLAND_SETTING(
			DataTable.ISLAND_SETTINGS,
			Boolean.TYPE,
			null,
			new String[] {"islands", "settings"},
			true);
	
	
	private DataTable table;
	private Class<?> type;
	private String
	sqlColumn;
	
	private String[]
	yamlPath;
	private boolean dynamic;
	
	private DataValue(DataTable table, Class<?> type, String sqlColumn, String[] yamlPath, boolean dynamic) {
		this.table = table;
		this.type = type;
		this.sqlColumn = sqlColumn;
		this.yamlPath = yamlPath;
		this.dynamic = dynamic;
	}
	
	public DataTable getTable() {
		return table;
	}
	
	public Class<?> getType() {
		return type;
	}
	
	public String getSqlColumn() {
		return sqlColumn;
	}
	
	public String[] getYamlPath() {
		return yamlPath;
	}
	
	public boolean hasDynamicPath() {
		return dynamic;
	}
}