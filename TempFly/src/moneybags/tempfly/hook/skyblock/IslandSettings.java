package moneybags.tempfly.hook.skyblock;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.DataFormatException;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.util.data.DataBridge;
import moneybags.tempfly.util.data.DataBridge.DataTable;
import moneybags.tempfly.util.data.DataBridge.DataValue;

public class IslandSettings {

	private SkyblockHook hook;
	private IslandWrapper island;
	private Map<String, Boolean> settings = new HashMap<>();
	
	public IslandSettings(IslandWrapper island, SkyblockHook hook) {
		this.hook = hook;
		String id = hook.getIslandIdentifier(island);
		DataBridge bridge = TempFly.getInstance().getDataBridge();
		
		Map<String, Object> values = bridge.getValues(DataTable.ISLAND_SETTINGS, "ISLANDS", id);
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
		return role.equalsIgnoreCase("OWNER") ? true : settings.get(role);
	}
	
	public void setCanFly(String role, boolean canFly) {
		if (role.equals("OWNER")) {
			return;
		}
		settings.put(role, canFly);
		TempFly.getInstance().getDataBridge().stageChange(DataValue.ISLAND_SETTING, canFly, new String[] {hook.getIslandIdentifier(island), role});
	}
	
	public void toggleCanFly(String rank) {
		settings.put(rank, !settings.get(rank));
	}
	
	public Set<Entry<String, Boolean>> getCurrentState() {
		return settings.entrySet();
	}
	
	public IslandWrapper getIsland() {
		return island;
	}
	
}
