package com.moneybags.tempfly.util;

import java.util.logging.Logger;

public final class Console {
	
	private static Logger logger;
	
	private Console() {}
	
	public static void setLogger(Logger logger) {
		Console.logger = logger;
	}
	
	public static void generateException(String message) {
		try {throw new Exception(message);} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void info(String... m){
		for (String s: m) {info(s);}
 	}
	
	public static void info(String m){
		logger.info(m);
	}
	
	public static void warn(String... m){
		for (String s: m) {warn(s);}
	}
	
	public static void warn(String m){
		logger.warning(m);
	}
	
	public static void severe(String... m){
		for (String s: m) {severe(s);}
	}
	
	public static void severe(String m){
		logger.severe(m);
	}
	
	/**
	 * lmao
	 */
	public static void extreme(String m) {
		for (int x = 0; x < 23; x++) severe((x > 10 && x < 13) ? m : (x == 11 || x == 13) ? "--------------" : "!!!");
	}

	public static void debug(Object obj) {
		if (V.debug) {logger.info("[DEBUG] " + String.valueOf(obj));}
	}
	
	public static void debug(Object... objects) {
		if (V.debug) {
			for (Object obj: objects) {logger.info("[DEBUG] " + String.valueOf(obj));}
		}
	}
}
