package com.moneybags.tempfly.util.data.config;

import java.util.Collection;
import java.util.List;

public interface ConfigSection extends Config {
	
	Collection<String> getKeys(boolean b);
	
	boolean contains(String string);
	
	String getString(String path);
	
	String getString(String path, String def);

	boolean getBoolean(String string);
	
	public boolean getBoolean(String string, boolean b);

	public int getInt(String string);
	
	public int getInt(String string, int def);

	public List<String> getStringList(String string);

	public List<Long> getLongList(String string);

	public double getDouble(String string, double def);
	
	public double getDouble(String string);

	public double getLong(String string, long def);

}
