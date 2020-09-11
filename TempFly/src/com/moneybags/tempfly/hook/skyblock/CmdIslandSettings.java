package com.moneybags.tempfly.hook.skyblock;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdIslandSettings extends TempFlyCommand {

	public CmdIslandSettings(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	public void executeAs(CommandSender s) {
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		Player p = (Player) s;
		if (!p.hasPermission("tempfly.skyblock.island.settings")) {
			U.m(s, V.invalidPermission);
			return;
		}
		new PageIslandSettings(tempfly.getGuiManager().createSession(p));
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
