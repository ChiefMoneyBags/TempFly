package com.moneybags.tempfly.util.data.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.data.config.ConfigSection;
import com.moneybags.tempfly.util.data.files.ResourceProvider;
import com.moneybags.tempfly.util.data.values.DataPointer;
import com.moneybags.tempfly.util.data.values.DataTable;
import com.moneybags.tempfly.util.data.values.DataValue;
import com.moneybags.tempfly.util.data.values.StagedChange;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;

public class SqlProvider implements DataProvider {

	private MysqlDataSource dataSource;
	
	public SqlProvider(ResourceProvider resources) throws SQLException, IOException {
		connectSql(resources);
		initDb(resources);
	}
	
	
	
	/**
	 * -----------
	 * SqlProvider
	 * -----------
	 */
	
	/**
	 * Setup the database for use with tempfly.
	 * @param resources
	 * @throws IOException
	 * @throws SQLException
	 */
	private void initDb(ResourceProvider resources) throws IOException, SQLException {
	    String setup;
	    try (InputStream in = resources.getResourceStream("dbsetup.sql")) {
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
	    Console.info("Database setup complete.");
	}
	
	public boolean connectSql(ResourceProvider resources) throws SQLException {
		ConfigSection config = resources.getConfigProvider().getDefaultConfig();
		String
		host = config.getString("system.mysql.host"),
		name = config.getString("system.mysql.name"),
		user = config.getString("system.mysql.user"),
		pass = config.getString("system.mysql.pass");
		int
		port = config.getInt("system.mysql.port");
		
		dataSource = new MysqlConnectionPoolDataSource();
		dataSource.setServerName(host);
		dataSource.setPortNumber(port);
		dataSource.setDatabaseName(name);
		dataSource.setUser(user);
		dataSource.setPassword(pass);
		
	    try (Connection conn = dataSource.getConnection()) {
	        if (!conn.isValid(1)) {
	        	Console.severe("Could not establish a connection to the database!");
	            return false;
	        }
	    } 

	    return true;
	}
	
	public PreparedStatement prepareStatement(String statement) {
		try { return getDataSource().getConnection().prepareStatement(statement); } catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public MysqlDataSource getDataSource() {
		return dataSource;
	}
	
	
	
	/**
	 * ------------
	 * DataProvider
	 * ------------
	 */
	
	/**
	 * 
	 */
	@Override
	public Object getValue(DataPointer pointer) {
		Console.debug("--| Sql provider fetching value...");
		
		DataValue value = pointer.getValue();
		String[] path = pointer.getPath();
		DataTable table = value.getTable();
		try (PreparedStatement st = dataSource.getConnection().prepareStatement(
				"SELECT " + value.getSqlColumn() + " FROM " + table.getSqlTable()
				+ " WHERE " + table.getPrimaryKey() + " = ?")) {
			st.setString(1, path[0]);
			ResultSet result = st.executeQuery();
	        if (result.next()) {
	            return result.getObject(value.getSqlColumn());
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void setValue(StagedChange change) {
		Console.debug("--| Sql provider setting value...");
		DataValue value = change.getValue();
		String[] path = change.getPath();
		try (PreparedStatement st = dataSource.getConnection().prepareStatement(
				"UPDATE " + value.getTable().getSqlTable() + " SET " + value.getSqlColumn()
				+ " = ? WHERE " + value.getTable().getPrimaryKey() + " = ?")) {
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
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
