package moneybags.tempfly.util;

import java.util.logging.Logger;

import org.bukkit.Bukkit;

import moneybags.tempfly.TempFly;

public class Console {
	
	public static void generateException(String message) {
		try {
			throw new Exception(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Logger getLogger() {
		return TempFly.getInstance().getLogger();
	}
	
	public static void info(String m){
		getLogger().info(m);
	}
	
	public static void warn(String m){
		getLogger().warning(m);
	}
	
	public static void severe(String m){
		getLogger().severe(m);
	}
	
	//lmao
	public static void extreme(String m) {
		for (int x = 0; x < 23; x++) severe((x > 10 && x < 13) ? m : (x == 11 || x == 13) ? "--------------" : "!!!");
	}

	public static void debug(Object obj) {
		if (V.debug) {
			getLogger().info("[DEBUG] " + String.valueOf(obj));
		}
	}
}
