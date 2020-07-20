package moneybags.tempfly.util.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.DataFormatException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.hook.HookManager.Genre;
import moneybags.tempfly.hook.HookManager.HookType;
import moneybags.tempfly.hook.TempFlyHook;
import moneybags.tempfly.util.U;
import net.minecraft.server.v1_15_R1.GeneratorSettingsDefault;


public class DataBridge {

	private Connection connection;
	
	private File dataf;
	private static FileConfiguration data;
	
	private Map<HookType, File> hookFiles = new HashMap<>();
	private static Map<HookType, FileConfiguration> hookData = new HashMap<>();
	
	private List<StagedChange> changes = new CopyOnWriteArrayList<>();
	
	public DataBridge(TempFly plugin) {
		if (Files.config.getBoolean("system.sql.enabled")) {
			String
			host = Files.config.getString("system.sql.host"),
			port = Files.config.getString("system.sql.port"),
			name = Files.config.getString("system.sql.name"),
			user = Files.config.getString("system.sql.user"),
			pass = Files.config.getString("system.sql.pass");
			try {
				Class.forName("com.mysql.jdbc.Driver");
				connection = DriverManager.getConnection(
						"jdbc:mysql://" + host + ":"+ port + "/" + name, user, pass);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// If connection is null we will default to yaml storage.
		if (connection == null) {
			dataf = new File(plugin.getDataFolder(), "data.yml");
		    if (!dataf.exists()){
		    	dataf.getParentFile().mkdirs();
		        plugin.saveResource("data.yml", false);
		    }
		    data = new YamlConfiguration();
		    try { data.load(dataf); } catch (Exception e1) {
		    	U.logS("There is a problem inside the data.yml, If you cannot fix the issue, please contact the developer.");
		        e1.printStackTrace();
		    }
		    formatData(plugin);
		}
	}
	
	/**
	 * format the data file from legacy TempFly version.
	 * @param plugin
	 */
	private void formatData(TempFly plugin) {
		double version = data.getDouble("version", 0.0);
		if (version < 2.0) {
			U.logW("Your data file needs to update to support the current version. Updating to version 2.0 now...");
			if (!backupLegacyData()) {
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
		}
	}
	
	/**
	 * Create a data backup from old TempFly version when updating.
	 * @return
	 */
	private boolean backupLegacyData() {
		U.logI("Creating a backup of your data file...");
		File f = new File(TempFly.getInstance().getDataFolder(), "data_backup_" + UUID.randomUUID().toString() + ".yml");
		try {
			data.save(f);
		} catch (Exception e) {
			U.logS(U.cc("&c-----------------------------------"));
			U.logS("There was an error while trying to backup the data file");
			U.logS("For your safety the plugin will disable. Please contact the developer.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private void saveData() {
		try { data.save(dataf); } catch (Exception e) { e.printStackTrace(); };
	}
	
	public void initializeHookData(TempFlyHook hook, TempFly plugin, DataTable table) throws IOException, InvalidConfigurationException, SQLException {
		HookType hookType = hook.getHookType();
		String target = hook.getHookedPlugin();
		if (connection == null) {
			File hookDataf = new File(hookType.getGenre().getDirectory(), target + "_data.yml");
		    if (!hookDataf.exists()) {
		    	hookDataf.getParentFile().mkdirs();
		    	hookDataf.createNewFile();
		    }
		    FileConfiguration hookData = new YamlConfiguration();
		    hookData.load(hookDataf);	
		} else {
			DatabaseMetaData meta = connection.getMetaData();
			ResultSet results = meta.getTables(null, null, table.getSqlTable(), null);
			if (!results.next()) {
				PreparedStatement statement = connection.prepareStatement("create table if not exists " + table.getSqlTable());
				statement.executeQuery();
			}
			
		}
	}
	
	/**
	 * Stage a change to be sent to the database later.
	 * @param value the type of data value
	 * @param row the row to affect
	 * @param data the new value
	 * @throws DataFormatException
	 */
	public synchronized void stageChange(DataValue value, Object data, String... path) {
		for (StagedChange change: changes) {
			if (change.isDuplicate(value, path)) {
				changes.remove(change);
			}
		}
		changes.add(new StagedChange(value, data, path));
	}
	
	/**
	 * Stage a change to be sent to the database later.
	 * @param value the type of data value
	 * @param row the row to affect
	 * @param data the new value
	 * @throws DataFormatException
	 */
	public synchronized void stageAndCommit(DataValue value, Object data, String... path) {
		for (StagedChange change: changes) {
			if (change.isDuplicate(value, path)) {
				changes.remove(change);
				break;
			}
		}
		changes.add(new StagedChange(value, data, path));
		commit(value, path);
	}

	/**
	 * Commit all changes to the database or yaml if applicable.
	 * This method is usually called by async threads, it will wait for staged changes to be finished before committing
	 */
	public void commit() {
		List<StagedChange> commit = new ArrayList<>();
		synchronized(this) {
			commit.addAll(changes);
			changes.clear();
		}
		
		for (StagedChange change: commit) {
			setValue(change.getValue(), change.getData(), change.getPath());
		}
		
		if (connection == null) {
			synchronized (this) {
				for (Entry<HookType, FileConfiguration> dat: hookData.entrySet()) {
					try {dat.getValue().save(hookFiles.get(dat.getKey()));} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
				try {data.save(dataf);} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void commit(DataValue value, String... path) {
		List<StagedChange> commit = new ArrayList<>();
		synchronized (this) {
			for (StagedChange change: changes) {
				if (change.getPath().equals(path)) {
					commit.add(change);
					changes.remove(change);
				}
			}	
		}
		for (StagedChange change: commit) {
			setValue(value, change.getData(), change.getPath());
		}
		
		if (connection == null) {
			synchronized (this) {
				for (Entry<HookType, FileConfiguration> dat: hookData.entrySet()) {
					try {dat.getValue().save(hookFiles.get(dat.getKey()));} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
				try {data.save(dataf);} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void dropChanges() {
		changes.clear();
	}
	
	/**
	 * Get a value from the table
	 * @param value
	 * @param row
	 * @return
	 * @throws DataFormatException
	 */
	public Object getValue(DataValue value, String... path) {
		synchronized (this) {
			for (StagedChange change: changes) {
				if (change.isDuplicate(value, path)) {
					return change.getData();
				}
			}
		}
		
		if (connection == null) {
			int index = 0;
			StringBuilder sb = new StringBuilder();
			for (String s: value.getYamlPath()) {
				sb.append((sb.length() > 0 ? "." : "") + s);
				if (path.length > index) {
					sb.append("." + path[index]);
				}
				index++;
			}
			U.logS("-----------" + sb.toString());
			U.logS("---------" + String.valueOf(value.getTable().getYaml()));
			return value.getTable().getYaml().get(sb.toString());
		} else {
			//TODO sql
		}
		return null;
	}
	
	public Object getOrDefault(DataValue value, Object def, String... path) {
		Object object = getValue(value, path);
		return object == null ? def : object;
	}
	
	/**
	 * Get all values from the table for the given row.
	 * Assumes the row is path to the ConfigurationSection in yaml
	 * @param value
	 * @param row
	 * @return
	 */
	public Map<String, Object> getValues(DataTable table, String column, String row) {
		Map<String, Object> values = new HashMap<>();
		if (connection == null) {
			FileConfiguration df = table.getYaml();
			ConfigurationSection csValues = df.getConfigurationSection(column + "." + row);
			if (csValues != null) {
				for (String key: csValues.getKeys(false)) {
					values.put(key, df.getObject(row + "." + key, Object.class));
				}
			}
		} else {
			try {
				PreparedStatement statement = connection.prepareStatement("select * from " + table.getSqlTable() + " where " + column + " = " + row);
				ResultSet rs = statement.executeQuery();
				ResultSetMetaData meta = rs.getMetaData();
				Map<Integer, String> columns = new HashMap<>();
				for (int index = 1; index < meta.getColumnCount(); index++) {
					columns.put(index, meta.getColumnName(index));
				}
				
				int index = 0;
				while (rs.next()) {
					index++;
					values.put(columns.get(index), rs.getObject(index));
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}
		return values;
	}
	
	public void setValue(DataValue value, Object data, String... path) {
		if (connection == null) {
			int index = 0;
			StringBuilder sb = new StringBuilder();
			for (String s: value.getYamlPath()) {
				sb.append((sb.length() > 0 ? "." : "") + s);
				if (path.length > index) {
					sb.append("." + path[index]);
				}
				index++;
			}
			U.logS("setting " + sb.toString() + " to " + String.valueOf(data));
			FileConfiguration yaml = value.getTable().getYaml();
			if (!yaml.contains(sb.toString())) {
				U.logS("not exist");
				yaml.createSection(sb.toString());
			}
			yaml.set(sb.toString(), data);
		} else {
			//TODO sql
		}
	}

	
	public static enum DataTable {
		TEMPFLY_DATA,
		ISLAND_SETTINGS;
		
		public FileConfiguration getYaml() {
			switch (this) {
			case TEMPFLY_DATA:
				return data;
			case ISLAND_SETTINGS:
				for (Entry<HookType, FileConfiguration> entry: hookData.entrySet()) {
					if (entry.getKey().getGenre() == Genre.SKYBLOCK) {
						return entry.getValue();
					}
				}
			default:
				return null;
			}
		}
		
		public String getSqlTable() {
			switch (this) {
			case TEMPFLY_DATA:
				return "TEMPFLY_DATA";
			case ISLAND_SETTINGS:
				return TempFly.getInstance().getHookManager().getGenre(Genre.SKYBLOCK)[0].getHookedPlugin() + "_island_settings";
			default:
				return null;
			}
		}
	}
	
	public static enum DataValue {
		PLAYER_TIME(
				DataTable.TEMPFLY_DATA,
				Long.class,
				"BALANCE",
				new String[] {"players", "time"},
				false),
		PLAYER_FLIGHT_LOG(
				DataTable.TEMPFLY_DATA,
				Boolean.class,
				"LOGGED_IN_FLIGHT",
				new String[] {"players", "logged_in_flight"},
				false),
		PLAYER_DAILY_BONUS(
				DataTable.TEMPFLY_DATA,
				Long.class,
				"LAST_DAILY_BONUS",
				new String[] {"players", "last_daily_bonus"},
				false),
		PLAYER_TRAIL(
				DataTable.TEMPFLY_DATA,
				String.class,
				"TRAIL",
				new String[] {"players", "trail"},
				false),
		
		
		
		
		ISLAND_SETTING(
				DataTable.ISLAND_SETTINGS,
				Boolean.class,
				null,
				null,
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
		
		public StagedChange(DataValue value, Object data, String... path) {
			this.value = value;
			this.path = path;
			this.data = data;
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
		
		public boolean isDuplicate(DataValue value, String[] path) {
			return this.value.equals(value)
					&& this.path.equals(path);
		}
	}

}
