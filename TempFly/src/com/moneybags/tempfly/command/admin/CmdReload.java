package com.moneybags.tempfly.command.admin;

import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdReload {

	public CmdReload(TempFly tempfly, CommandSender s) {
		if (!U.hasPermission(s, "tempfly.reload")) {
			U.m(s, V.invalidPermission);
			return;
		}
		tempfly.reloadTempfly();
		U.m(s, V.reload);
	}
}
