package com.moneybags.tempfly.hook.skyblock.plugins;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.skyblock.IslandWrapper;
import com.moneybags.tempfly.hook.skyblock.SkyblockChallenge;
import com.moneybags.tempfly.hook.skyblock.SkyblockHook;

public class BentoHook extends SkyblockHook {

	public BentoHook(TempFly plugin) {
		super(plugin);
	}

	@Override
	public IslandWrapper getIslandOwnedBy(UUID p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IslandWrapper getTeamIsland(UUID p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IslandWrapper getIslandAt(Location loc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isChallengeCompleted(UUID p, SkyblockChallenge challenge) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean islandRoleExists(IslandWrapper island, String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean islandRoleExists(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getIslandRole(UUID p, IslandWrapper island) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getIslandOwner(IslandWrapper island) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIslandIdentifier(Object rawIsland) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isIslandMember(UUID p, IslandWrapper island) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getIslandLevel(UUID p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getIslandLevel(IslandWrapper island) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Player[] getOnlineMembers(IslandWrapper island) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID[] getIslandMembers(IslandWrapper island) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPluginName() {
		// TODO Auto-generated method stub
		return "f";
	}

	@Override
	public IslandWrapper getIslandFromIdentifier(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isIslandWorld(Location loc) {
		// TODO Auto-generated method stub
		return false;
	}



}
