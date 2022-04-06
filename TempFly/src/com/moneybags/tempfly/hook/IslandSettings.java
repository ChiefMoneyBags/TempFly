package com.moneybags.tempfly.hook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.DataFormatException;

import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.DataBridge;
import com.moneybags.tempfly.util.data.DataBridge.DataTable;
import com.moneybags.tempfly.util.data.DataBridge.DataValue;
import com.moneybags.tempfly.util.data.DataPointer;

public class IslandSettings {

	private SkyblockHook hook;
	private IslandWrapper island;
	private Map<String, Boolean> settings = new HashMap<>();
	
	public IslandSettings(IslandWrapper island, SkyblockHook hook) {
		this.hook = hook;
		this.island = island;
		String id = hook.getIslandIdentifier(island.getRawIsland());
		DataBridge bridge = hook.getTempFly().getDataBridge();
		
		Map<String, Object> values = bridge.getValues(DataTable.ISLAND_SETTINGS, hook, "islands", id, "settings");
		for (Entry<String, Object> entry: values.entrySet()) {
			if (entry.getKey().equalsIgnoreCase("OWNER")) {
				continue;
			}
			if (!(entry.getValue() instanceof Boolean)) {
				try { throw new DataFormatException("A data value in Island Settings is not properly formatted, Expected boolean got (" + entry.getValue().getClass() + ")! island=(" + id + ") key=(" + entry.getKey() + ")");}
				catch (DataFormatException e) {
					e.printStackTrace();
					continue;
				}
			}
			settings.put(entry.getKey(), (Boolean)entry.getValue());
		}
	}
	
	public boolean canFly(String role) {
		role = role.toUpperCase();
		return role.equalsIgnoreCase("OWNER") ? true : settings.getOrDefault(role, hook.getDefaults().getOrDefault(role, false));
	}
	
	public void setCanFly(String role, boolean canFly) {
		role = role.toUpperCase();
		if (role.equals("OWNER")) {
			return;
		}
		Console.debug("-- Set flight for: " + role,
				"currently: " + canFly(role),
				"mapped settings contains: " + settings.containsKey(role));
		settings.put(role, canFly);
		hook.evaluate(island);
		hook.getTempFly().getDataBridge().stageChange(DataPointer.of(DataValue.ISLAND_SETTING, hook.getIslandIdentifier(island), role), canFly, hook);
	}
	
	public void toggleCanFly(String role) {
		role = role.toUpperCase();
		if (role.equals("OWNER")) {
			return;
		}
		if (V.debug) {Console.debug("-- Set flight for: " + role, "--| currently: " + canFly(role), "--| mapped settings contains: " + settings.containsKey(role));}
		boolean canFly = !canFly(role);
		settings.put(role, canFly);
		hook.evaluate(island);
		hook.getTempFly().getDataBridge().stageChange(DataPointer.of(DataValue.ISLAND_SETTING, hook.getIslandIdentifier(island.getRawIsland()), role), canFly, hook);
	}
	
	public List<Entry<String, Boolean>> getCurrentState() {
		Map<String, Boolean> state = new HashMap<>();
		state.putAll(settings);
		for (String role: hook.getRoles()) {
			role = role.toUpperCase();
			if (!role.equalsIgnoreCase("OWNER") && !state.containsKey(role)) {
				state.put(role, hook.getDefaults().getOrDefault(role, false));
			}
		}
		
		Comparator<Entry<String, Boolean>> comp = new Comparator<Entry<String, Boolean>>() {
			@Override
			public int compare(Entry<String, Boolean> e0, Entry<String, Boolean> e1) {
				return e0.getKey().compareTo(e1.getKey());
			}
		};
		List<Entry<String, Boolean>> sorted = new ArrayList<Entry<String, Boolean>>(state.entrySet());
		Collections.sort(sorted, comp);
		return sorted;
	}
	
	public IslandWrapper getIsland() {
		return island;
	}
	
}
