package com.moneybags.tempfly.util.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.moneybags.tempfly.hook.HookManager;
import com.moneybags.tempfly.hook.HookManager.Genre;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;


public class DataBridge implements DataFileHolder {

	private TempFly tempfly;
	private MysqlDataSource dataSource;
	
	private File dataf;
	private FileConfiguration data;
	
	private ExecutorService executor;
	
	// Staged changes are held in local memory until either the autosave runs, or they are forcefully committed.
	// The databridge will act like these changes are part of the database even though they are local. 
	// It will look to see if there is data here first before it queries the database or YAML file.

	private Map<DataPointer, StagedChange> changes = new ConcurrentHashMap<>();
	// A list of pointers that tell the asynchronous batch manager to save the data they point to
	// if it exists in the list of changes.

	private List<DataPointer> manualCommit = new ArrayList<>();
	
	public MysqlDataSource getDataSource() {
		return dataSource;
	}
	
	public boolean hasSqlEnabled() {
		return dataSource != null;
	}
	
	public boolean connectSql() throws SQLException {
		String
		host = Files.config.getString("system.mysql.host"),
		name = Files.config.getString("system.mysql.name"),
		user = Files.config.getString("system.mysql.user"),
		pass = Files.config.getString("system.mysql.pass");
		
		MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
		dataSource.setServerName(host);
		dataSource.setPortNumber(Files.config.getInt("system.mysql.port"));
		dataSource.setDatabaseName(name);
		dataSource.setUser(user);
		dataSource.setPassword(pass);
		
	    try (Connection conn = dataSource.getConnection()) {
	        if (!conn.isValid(1)) {
	        	Console.severe("Could not establish a connection to the database!");
	            return false;
	        }
	    } 
	    
	    this.dataSource = dataSource;
	    return true;
	}
	
	private void initDb() throws IOException, SQLException {
	    String setup;
	    try (InputStream in = tempfly.getResource("dbsetup.sql")) {
	        setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
	    } 
	    String[] queries = setup.split(";");
	    for (String query : queries) {
	        if (query.isBlank()) continue;
	        try (Connection conn = dataSource.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(query)) {
	            stmt.execute();
	        } 
	    }
	    Console.info("§2Database setup complete.");
	}
	
	public DataBridge(TempFly tempfly) throws IOException, SQLException {
		this.tempfly = tempfly;
		if (Files.config.getBoolean("system.mysql.enabled")) {
			connectSql();
			initDb();
		}
		
		// If connection is null we will default to yaml storage.
		if (!hasSqlEnabled()) {
			dataf = new File(tempfly.getDataFolder(), "data.yml");
		    if (!dataf.exists()){
		    	dataf.getParentFile().mkdirs();
		    	tempfly.saveResource("data.yml", false);
		    }
		    data = new YamlConfiguration();  
		    try { data.load(dataf); } catch (Exception e1) {
		    	Console.severe("There is a problem inside the data.yml, If you cannot fix the issue, please contact the developer.");
		        e1.printStackTrace();
		    }
		    formatYamlData(tempfly);
		}
		this.executor = Executors.newCachedThreadPool();
	}
	
	
	/**
	 * format the data file from legacy TempFly version.
	 * @param plugin
	 */
	private void formatYamlData(TempFly plugin) {
		double version = data.getDouble("version", 0.0);
		if (version < 2.0) {
			Console.warn("Your data file needs to update to support the current version. Updating to version 2.0 now...");
			if (!backupLegacyData("update_2_backup_")) {
				Bukkit.getPluginManager().disablePlugin(plugin);
				return;
			}
			
			data.set("version", 2.0);
			ConfigurationSection csPlayers = data.getConfigurationSection("players");
			if (csPlayers != null) {
				Map<String, Double> time = new HashMap<>();
				for (String key: csPlayers.getKeys(false)) {
					time.put(key, data.getDouble("players." + key));
				}
				for (Entry<String, Double> entry: time.entrySet()) {
					String uuid = entry.getKey();
					double value = entry.getValue();
					data.set("players." + uuid + ".time", value);
					data.set("players." + uuid + ".logged_in_flight", false);
					data.set("players." + uuid + ".trail", "");
				}	
			}
			List<String> disco = data.getStringList("flight_disconnect");
			if (disco != null) {
				for (String uuid: disco) {
					data.set("players." + uuid + ".logged_in_flight", true);
				}
			}
			data.set("flight_disconnect", null);
			saveData();
			
		} else if (version < 3.0) {
			Console.warn("", "This tempfly version has a new data management system, (data.yml) will be backed for your safety.", "");
			if (!backupLegacyData("update_3_backup_")) {
				Bukkit.getPluginManager().disablePlugin(plugin);
				return;
			}
			data.set("version", 3.0);
			saveData();
		} else if (version < 4.0) {
			if (!backupLegacyData("update_4_backup_")) {
				Bukkit.getPluginManager().disablePlugin(plugin);
				return;
			}
			data.set("version", 4.0);
			saveData();
		}
	}
	
	/**
	 * Create a data backup from legacy TempFly version when updating.
	 * @return
	 */
	private boolean backupLegacyData(String file) {
		Console.info("Creating a backup of your data file...");
		File f = new File(tempfly.getDataFolder(), file + String.valueOf(new Random().nextInt(99999)) + ".yml");
		try {
			data.save(f);
		} catch (Exception e) {
			Console.severe("-----------------------------------", "There was an error while trying to backup the data file", "For your safety the plugin will disable. Please contact the tempfly developer.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public void stageChange(DataPointer pointer, Object data) {
		stageChange(pointer, data, null);
	}
	/**
	 * Stage a change to be sent to the database later.
	 * @param pointer The type and path of the data
	 * @param data the data.
	 */
	public void stageChange(DataPointer pointer, Object data, DataFileHolder fileHolder) {
		DataValue value = pointer.getValue();
		String[] path = pointer.getPath();
		if (V.debug) {//Console.debug(""); Console.debug("-----------Staging new change-----------"); Console.debug("--| Type: " + value.toString()); Console.debug("--| Path: " + U.arrayToString(pointer.getPath(), " | ")); Console.debug("--| Data: " + String.valueOf(data));
		}
		
		
		changes.put(pointer, new StagedChange(value, data, path, fileHolder));
	}
	
	public boolean isStaged(DataPointer pointer) {
		return changes.containsKey(pointer);
	}
	
	/**
	 * Commit all changes to the database or yaml if applicable.
	 * Adds all the staged changes to the manual batch and runs the async batch collector.
	 */
	public void commitAll() {
		Console.debug("", "--------> DataBridge Commit <--------", "--|>> Adding (ALL) changes to the commit queue");
		manualCommit.clear();
		manualCommit.addAll(changes.keySet());
		if (manualCommit.size() == 0) {
			return;
		}
		executor.submit(() -> {
			executeCommit();
		});
	}
	
	/**
	 * Collects StagedChanges using the pointers collected in the manual batch and sends data to the database.
	 */
	private void executeCommit() {
		List<StagedChange> commit = new ArrayList<>();
		
		if (V.debug) {Console.debug("", "-|>>>>> Preparing to execute the commit queue");}
		
		List<DataPointer> pl = new ArrayList<>();
		pl.addAll(manualCommit);
		manualCommit.clear();
		
		pointers:
		for (DataPointer pointer: pl) {
			if (V.debug) {Console.debug("", "--| Looking for data type:" + pointer.getValue().toString(), "--| Path:" + U.arrayToString(pointer.getPath(), " | "));}
			Console.debug("looking for: " + pointer.hashCode());
			StagedChange change = changes.get(pointer);
			if (change != null) {
				Console.debug("--|> Found a staged change that matches: data=(" + change.getData() + ")");
				commit.add(change);
				changes.remove(pointer);
				continue pointers;
			}
 			Console.debug("--|> No changes to save for this type...");
		}	
		
		
		if (commit.size() == 0 && V.debug) { Console.debug(">>>>> No changes to save...", "-----------End commit---------", "");
			return;
		}
		
		if (V.debug) { Console.debug("Preparing to set value for (" + String.valueOf(commit.size()) + ") change" + (commit.size() > 1 ? "s" : "") + " found...");}
		List<DataFileHolder> altered = new ArrayList<>();
		for (StagedChange change: commit) {
			DataFileHolder holder = change.getValue().getTable().getDataFileHolder(tempfly);
			if (!altered.contains(holder)) {
				altered.add(holder);
			}
			try {
				setValue(change, holder.forceYaml());
			} catch (SQLException e) {
				e.printStackTrace();
				continue;
			}
		}
		for (DataFileHolder holder: altered) {
			if (!hasSqlEnabled() || holder.forceYaml()) {
				holder.saveData();
			}
		}
		Console.debug("-----------End commit---------", "");
	}
	
	/**
	 * Manually add data pointers to the next manual commit and run the async batch collector.
	 * @param pointers
	 */
	public void manualCommit(DataPointer... pointers) {
		manualCommit.addAll(Arrays.asList(pointers));
		Console.debug(manualCommit.toString());
		executor.submit(() -> {
			executeCommit();
		});
	}
	
	/**
	 * Drop ALL changes, resets data back to the original state unless it has been commited. 
	 */
	public void dropChanges() {
		manualCommit.clear();
		changes.clear();
	}
	
	/**
	 * Get a value from the table
	 * @param value
	 * @param row
	 * @return
	 * @throws SQLException 
	 * @throws DataFormatException
	 */
	public Object getValue(DataPointer pointer) throws SQLException {
		DataValue value = pointer.getValue();
		String[] path = pointer.getPath();
		if (V.debug) {Console.debug("", "-----Data Bridge Get Value-----", "--| Type: " + value.toString(), "--| Path: " + U.arrayToString(pointer.getPath(), " | "));	}
		
		Console.debug("--| Checking local staged changes");
		
		StagedChange change = changes.get(pointer);
		if (change != null) {
			Console.debug("--|> found cached value... Returning local data!");
			return change.getData();
		}
		Console.debug("--|> No local data found, prepare for data retrieval!");
		
		if (!hasSqlEnabled()) {
			Console.debug("--| Using YAML");
			int index = 0;
			StringBuilder sb = new StringBuilder();
			for (String s: value.getYamlPath()) {
				sb.append((sb.length() > 0 ? "." : "") + s);
				if (path.length > index) {
					sb.append("." + path[index]);
				}
				index++;
			}
			return value.getTable().getDataFileHolder(tempfly).getDataConfiguration().get(sb.toString());
		} else {
			Console.debug("--| Using SQL");
			DataTable table = value.getTable();
			
			
			String statement = "SELECT " + value.getSqlColumn() + " FROM " + table.getSqlTable() + " WHERE " + table.getPrimaryKey() + " = ?";
			Console.debug(statement);
			try (PreparedStatement st = dataSource.getConnection().prepareStatement(statement)) {
				st.setString(1, path[0]);
				ResultSet result = st.executeQuery();
		        if (result.next()) {
		            return result.getObject(value.getSqlColumn());
		        }
			}
		}
		return null;
	}
	
	public PreparedStatement prepareStatement(String statement) {
		if (!hasSqlEnabled()) {
			return null;
		}
		try {
			return getDataSource().getConnection().prepareStatement(statement);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Object getOrDefault(DataPointer pointer, Object def) {
		Object object;
		try {
			object = getValue(pointer);
		} catch (SQLException e) {
			e.printStackTrace();
			return def;
		}
		if (V.debug) {Console.debug("", "-----Data Bridge Get or Default Value-----", "--|> Got: " + object, "--|> Returning: " + String.valueOf(object == null ? def : object));}
		return object == null ? def : object;
	}
	
	public Map<String, Object> getValues(DataTable table, String yamlPathTo, String row, String... extra) {
		return getValues(table, null, yamlPathTo, row, extra);
	}
	
	/**
	 * Get all values from the table for the given row.
	 * Assumes the row is path to the ConfigurationSection in yaml
	 * @param value
	 * @param row
	 * @return
	 */
	
	public Map<String, Object> getValues(DataTable table, DataFileHolder fileHolder, String yamlPathTo, String row, String... extra) {
		Map<String, Object> values = new HashMap<>();
		if (!hasSqlEnabled() || fileHolder.forceYaml()) {
			FileConfiguration df = fileHolder == null ?
					table.getDataFileHolder(tempfly).getDataConfiguration()
					: fileHolder.getDataConfiguration();
			String path = yamlPathTo + "." + row + "." + U.arrayToString(extra, ".");
			ConfigurationSection csValues = df.getConfigurationSection(path);
			if (csValues != null) {
				for (String key: csValues.getKeys(false)) {
					values.put(key, df.get(path + "." + key));
				}		
			}
		}
		for (StagedChange local: changes.values()) {
			if (local.comparePathPartial(row)) {
				values.put(local.getPath()[local.getPath().length-1], local.getData());
			}
		}	
		return values;
	}
	
	public void setValue(StagedChange change, boolean forceYaml) throws SQLException {
		DataValue value = change.getValue();
		String[] path = change.getPath();
		if (V.debug) {Console.debug("", "-----Data Bridge Set Value-----", "--| Type: " + value.toString(), "--| Path: " + U.arrayToString(path, " | "));	}
		if (!hasSqlEnabled() || forceYaml) {
			int index = 0;
			StringBuilder sb = new StringBuilder();
			for (String s: value.getYamlPath()) {
				sb.append((sb.length() > 0 ? "." : "") + s);
				if (path.length > index) {
					sb.append("." + path[index]);
				}
				index++;
			}
			if (V.debug) {Console.debug("--| Setting yaml value: " + sb.toString(), "--| New data: " + String.valueOf(change.getData()));}
			FileConfiguration yaml = change.getFileHolder() == null ?
					value.getTable().getDataFileHolder(tempfly).getDataConfiguration()
					: change.getFileHolder().getDataConfiguration();
			if (!yaml.contains(sb.toString())) {
				yaml.createSection(sb.toString());
			}
			yaml.set(sb.toString(), change.getData());
		} else {
			Console.debug("UPDATE " + value.getTable().getSqlTable() + " SET " + value.getSqlColumn()
					+ " = ? WHERE " + value.getTable().getPrimaryKey() + " = " + path[0]);
			PreparedStatement st = dataSource.getConnection().prepareStatement(
					"UPDATE " + value.getTable().getSqlTable() + " SET " + value.getSqlColumn()
					+ " = ? WHERE " + value.getTable().getPrimaryKey() + " = ?");
			Class<?> type = value.getType();
			if (type.equals(Boolean.TYPE)) {
				st.setBoolean(1, (boolean) change.getData());
			} else if (type.equals(Double.TYPE)) {
				st.setDouble(1, (double) change.getData());
			} else if (type.equals(String.class)) {
				st.setString(1, (String) change.getData());
			} else if (type.equals(Long.TYPE)) {
				st.setLong(1, (long) change.getData());
			}
			st.setString(2, path[0]);
			st.execute();
			st.close();
		}
	}

	
	public static enum DataTable {
		TEMPFLY_DATA("uuid"),
		ISLAND_SETTINGS;
		
		private DataTable() {}
		
		private String primary;
		
		private DataTable(String primary) {
			this.primary = primary;
		}
		
		public String getPrimaryKey() {
			return primary;
		}
		
		public DataFileHolder getDataFileHolder(TempFly tempfly) {
			switch (this) {
			case TEMPFLY_DATA:
				return tempfly.getDataBridge();
			case ISLAND_SETTINGS:
				HookManager hooks = tempfly.getHookManager();
				TempFlyHook[] hook;
				if ((hook = hooks.getGenre(Genre.SKYBLOCK)) != null && hook.length > 0) {
					return (DataFileHolder) hook[0];
				}
				break;
			default:
				return null;
			}
			return null;
		}
		
		public String getSqlTable() {
			switch (this) {
			case TEMPFLY_DATA:
				return "tempfly_data";
			case ISLAND_SETTINGS:
				//return tempfly.getHookManager().getGenre(Genre.SKYBLOCK)[0].getHookedPlugin() + "_island_settings";
			default:
				return null;
			}
		}
	}
	
	public static enum DataValue {
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
	
	protected class StagedChange {
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

	@Override
	public File getDataFile() {
		return dataf;
	}

	@Override
	public FileConfiguration getDataConfiguration() {
		return data;
	}

	@Override
	public void setDataFile(File file) {
		this.dataf = file;
	}

	@Override
	public void setDataConfiguration(FileConfiguration data) {
		this.data = data;
	}
	
	@Override
	public void saveData() {
		try { data.save(dataf); } catch (Exception e) {e.printStackTrace();}
	}

}
