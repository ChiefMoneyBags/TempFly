package com.moneybags.tempfly.command.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdInfinite extends TempFlyCommand {

	public CmdInfinite(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.infinite.toggle")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
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
				U.m(s, "&c/tempfly infinite [on/off]");
				return;
			}
		} else {
			toggleVal = !user.hasInfiniteFlight();
		}
		user.setInfiniteFlight(toggleVal);
		// If the player has auto fly queued we will auto enable their flight when they toggle infinite time. 
		if (toggleVal && user.hasAutoFlyQueued()) {
			user.enableFlight();
		}
		U.m(s, toggleVal ? V.flyInfiniteEnabled : V.flyInfiniteDisabled);
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		if (args.length < 3 && U.hasPermission(s, "tempfly.infinite.toggle")) {
			return tempfly.getCommandManager().getToggleCompletions(true);
		}
		return new ArrayList<>();
	}

}
