package com.moneybags.tempfly.command.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.gui.pages.PageShop;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdShop extends TempFlyCommand {

	public CmdShop(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		if (!V.shop) {
			U.m(s, V.invalidCommand);
			return;
		}
		if (tempfly.getHookManager().getEconomy() == null) {
			U.m(s, V.invalidEconomy);
			return;
		}
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		if (!U.hasPermission(s, "tempfly.shop")) {
			U.m(s, V.invalidPermission);
			return;
		}
		new PageShop(tempfly.getGuiManager().createSession((Player)s), 0);
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		return new ArrayList<>();
	}
}
