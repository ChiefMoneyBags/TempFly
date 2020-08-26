package com.moneybags.tempfly.command.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdHelp extends TempFlyCommand {

	public CmdHelp(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.help")) {
			U.m(s, V.invalidPermission);
			return;
		}
		for (String line: V.help) {
			U.m(s, line);
		}
		if (U.hasPermission(s, "tempfly.help.admin")) {
			for (String line: V.helpExtended) {
				U.m(s, line);
			}
		}
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		return new ArrayList<>();
	}

}
