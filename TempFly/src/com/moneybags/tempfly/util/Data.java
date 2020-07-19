package com.moneybags.tempfly.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class Data {

	
	private static String
		host,
		port,
		name,
		user,
		pass;

	
	public static Connection getConnection() {
		try {
			String driver = "com.mysql.jdbc.Driver";
			String url = "jdbc:mysql://" + host + ":" + port + "/" + name;
			
			Connection con = DriverManager.getConnection(url, user, pass);
		} catch(Exception e) {
			
		}
		return null;
	}
	
}
