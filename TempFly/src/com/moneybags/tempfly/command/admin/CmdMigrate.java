package com.moneybags.tempfly.command.admin;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.data.DataBridge.DataTable;
import com.moneybags.tempfly.util.data.DataBridge.DataValue;

public class CmdMigrate extends TempFlyCommand{

	public CmdMigrate(TempFly tempfly, String[] args) {
		super(tempfly, args);
		
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		return new ArrayList<>();
	}
	
	private static boolean sure = false;

	@Override
	public void executeAs(CommandSender s) {
		if (!(s instanceof ConsoleCommandSender)) {
			s.sendMessage("Only the console may use this command!");
			return;
		}
		
		if (!sure) {
			s.sendMessage("Warning, Using this command will take all data found in the local tempfly (data.yml) and migrate it to the MySql database defined in the config. If there is any TempFly data already in this database it has the possibility of being overwritten by the migrated data. Please type the command again within the next 5 seconds to continue.");
			sure = true;
			Bukkit.getScheduler().runTaskLater(tempfly, () -> {
				sure = false;
			}, 100);
			return;
		}
		
		if (!tempfly.getDataBridge().hasSqlEnabled()) {
			s.sendMessage("You must enable MySql in the config to migrate your tempfly data...");
			return;
		}
		
		File dataf = new File(tempfly.getDataFolder(), "data.yml");
	    if (!dataf.exists()){
	    	s.sendMessage("There is no datafile to migrate...");
	    	return;
	    }
	    FileConfiguration data = new YamlConfiguration();
	    try { data.load(dataf); } catch (Exception e1) {
	    	s.sendMessage("There is a problem inside the data.yml, If you cannot fix the issue, please contact the developer.");
	        e1.printStackTrace();
	        return;
	    }

	    ConfigurationSection csPlayers = data.getConfigurationSection("players");
	    if (csPlayers == null) {
	    	s.sendMessage("There is no data to migrate...");
	    	return;
	    }
	    for (String key: csPlayers.getKeys(false)) {
	    	String[] path = new String[] {key};
	    
			try (PreparedStatement stCreate = tempfly.getDataBridge().prepareStatement("INSERT IGNORE INTO tempfly_data(uuid) VALUES(?)")){
				stCreate.setString(1, key);
				stCreate.execute();
				stCreate.close();
			} catch (SQLException e) {
				s.sendMessage("Failed to create database entry for (" + key + ")");
				e.printStackTrace();
				continue;
			}
			
	    	 for (DataValue value: DataValue.values()) {
	 	    	if (value.getTable() != DataTable.TEMPFLY_DATA) {
	 	    		continue;
	 	    	}
	 	    	
	 	    	int index = 0;
	 			StringBuilder sb = new StringBuilder();
	 			for (String string: value.getYamlPath()) {
	 				sb.append((sb.length() > 0 ? "." : "") + string);
	 				if (path.length > index) {
	 					sb.append("." + path[index]);
	 				}
	 				index++;
	 			}
	 			Console.debug(sb.toString());
	 			Object obj = data.get(sb.toString());
	 			if (obj == null) {
	 				continue;
	 			}
	 			
	 			PreparedStatement st = tempfly.getDataBridge().prepareStatement(
						"UPDATE " + value.getTable().getSqlTable() + " SET " + value.getSqlColumn()
						+ " = ? WHERE " + value.getTable().getPrimaryKey() + " = ?");
				Class<?> type = value.getType();
				try {
					if (type.equals(Boolean.TYPE)) {
						st.setBoolean(1, (boolean) obj);
					} else if (type.equals(Double.TYPE)) {
						st.setDouble(1, (double) obj);
					}else if (type.equals(String.class)) {
						st.setString(1, (String) obj);
					} else if (type.equals(Long.TYPE)) {
						st.setLong(1, (long) obj);
					}
					st.setString(2, path[0]);
					st.execute();
					st.close();
				} catch (Exception e) {
					s.sendMessage("Error while setting data");
					e.printStackTrace();
					continue;
				}
	 	    }
	    }
	    
	    String statement = "U";
	  
	}

}
