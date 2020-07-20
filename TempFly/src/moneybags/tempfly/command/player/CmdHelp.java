package moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;

import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

public class CmdHelp {

	public CmdHelp(CommandSender s, String[] args) {
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

}
