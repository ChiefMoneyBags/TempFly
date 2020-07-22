package moneybags.tempfly.command.admin;

import org.bukkit.command.CommandSender;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

public class CmdReload {

	public CmdReload(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.reload")) {
			U.m(s, V.invalidPermission);
			return;
		}
		TempFly.getInstance().reloadTempfly();
		U.m(s, V.reload);
	}
}
