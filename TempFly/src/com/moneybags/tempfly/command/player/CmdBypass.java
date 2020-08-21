package com.moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdBypass {

	public CmdBypass(CommandSender s, String[] args, TempFly tempfly) {
		if (!U.hasPermission(s, "tempfly.bypass.toggle")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
		}
		FlightUser user = tempfly.getFlightManager().getUser((Player)s);
		boolean toggleVal = false;
		if (args.length > 1) {
			switch (args[1].toLowerCase()) {
			case "on": case "enable":
				toggleVal = true;
			case "off": case "disable":
				break;
			default:
				U.m(s, "&c/tempfly bypass [on/off]");
				return;
			}
		} else {
			toggleVal = !user.hasRequirementBypass();
		}
		U.m(s, toggleVal ? V.flyBypassEnabled : V.flyBypassDisabled);
		user.setRequirementBypass(toggleVal);
	}
}
