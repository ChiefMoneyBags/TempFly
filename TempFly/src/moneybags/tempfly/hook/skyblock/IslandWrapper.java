package moneybags.tempfly.hook.skyblock;

import java.util.UUID;

import com.wasteofplastic.askyblock.Island;

import moneybags.tempfly.hook.HookManager.HookType;
import moneybags.tempfly.util.U;

public class IslandWrapper {

	private HookType type;
	private Object island;
	private IslandSettings settings;
	
	public IslandWrapper(HookType type, Object island, SkyblockHook hook) {
		this.island = island;
		this.type = type;
		this.settings = new IslandSettings(this, hook);
	}
	
	public Object getIsland() {
		return island;
	}
	
	public HookType getType() {
		return type;
	}
	
	public IslandSettings getSettings() {
		return settings;
	}
	
	public String getIdentifier() {
		switch (type) {
		case ASKYBLOCK:
			return U.locationToString(((Island) island).getCenter());
		case BENTO_BOX:
			return null;
		case SUPERIOR_SKYBLOCK_2:
			return null;
		default:
			return null;
		}
	}
	
	public UUID getOwner() {
		switch (type) {
		case ASKYBLOCK:
			return ((Island) island).getOwner();
		case BENTO_BOX:
			break;
		case SUPERIOR_SKYBLOCK_2:
			break;
		default:
			break;
		}
		return null;
	}
	
	public boolean isMember(UUID id) {
		switch (type) {
		case ASKYBLOCK:
			return ((Island) island).getMembers().contains(id);
		case BENTO_BOX:
			break;
		case SUPERIOR_SKYBLOCK_2:
			break;
		default:
			break;
		}
		return false;
	}
}
