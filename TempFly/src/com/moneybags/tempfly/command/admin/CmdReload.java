package com.moneybags.tempfly.command.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdReload extends TempFlyCommand {

	public CmdReload(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.reload")) {
			U.m(s, V.invalidPermission);
			return;
		}
		tempfly.reloadTempfly();
		U.m(s, V.reload);
	}
	
	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		return new ArrayList<>();
	}
}
