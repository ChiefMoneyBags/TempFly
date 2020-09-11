package com.moneybags.tempfly.command.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.event.FlightEnabledEvent;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdFly extends TempFlyCommand {

	public CmdFly(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		Player p = null;
		boolean
			manual = false,
			toggleVal = false;
		if (args.length > 0) {
			manual = true;
			if (tempfly.getCommandManager().getEnable().contains(args[0])) {
				toggleVal = true;
			} else if (tempfly.getCommandManager().getDisable().contains(args[0])) {
				toggleVal = false;
			} else {
				U.m(s, U.cc("&c/tf [on/off]"));
				return;
			}
			if (args.length > 1) {
				p = Bukkit.getPlayer(args[1]);
				if (s.equals(p) && !U.hasPermission(s, "tempfly.toggle.self")
						|| !s.equals(p) && !U.hasPermission(s, "tempfly.toggle.other")) {
					U.m(s, V.invalidPermission);
					return;
				}
				if (p == null) {
					U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[1]));
					return;
				}
			} else {
				if (!U.isPlayer(s)) {
					U.m(s, V.invalidSender);
					return;
				}
				if (!U.hasPermission(s, "tempfly.toggle.self")) {
					U.m(s, V.invalidPermission);
					return;
				}
			}
		
			
		} else {
			if (!U.isPlayer(s)) {
				U.m(s, V.invalidSender);
				return;
			}
			if (!U.hasPermission(s, "tempfly.toggle.self")) {
				U.m(s, V.invalidPermission);
				return;
			}
		}
		if (p == null) {
			p = (Player)s;
		}
		
		
		FlightUser user = tempfly.getFlightManager().getUser(p);
		// if command is /fly on and player is not flying || command is base && player is not flying
		// try to enable flight
		if (manual && toggleVal || !manual && !toggleVal && !user.hasFlightEnabled()) {
			if (user.hasFlightEnabled()) {
				if (s == p) {U.m(s, V.flyAlreadyEnabled);}
				return;
			}
			// Time check 
			double time = tempfly.getTimeManager().getTime(p.getUniqueId());
			if ((time <= 0) && (!user.hasInfiniteFlight())) {
				U.m(s, s.equals(p)
						? V.invalidTimeSelf
						: V.invalidTimeOther.replaceAll("\\{PLAYER}", p.getName()));
				return;
			}
			
			// Requirements check 
			if (!user.evaluateFlightRequirements(p.getLocation(), true) && !user.hasRequirementBypass()) {
				if (!user.hasAutoFlyQueued()) {
					user.setAutoFly(true);
				}
				if (s != p) {U.m(s, V.requireFailOther.replaceAll("\\{PLAYER}", p.getName()));}
				return;
			}
			
			// Event check 
			FlightEnabledEvent e = new FlightEnabledEvent(p);
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				return;
			}
			
			user.enableFlight();
			U.m(p, V.flyEnabledSelf);
			if (!s.equals(p)) {
				U.m(s, V.flyEnabledOther.replaceAll("\\{PLAYER}", p.getName()));	
			}
		// if command is /fly off and player is flying || command is base && player is flying
		// disable flight
		} else if (manual && !toggleVal && user.hasFlightEnabled() || !manual && user.hasFlightEnabled()) {
			user.disableFlight(0, !V.damageCommand);
			U.m(p, V.flyDisabledSelf);
			if (!s.equals(p)) {
				U.m(s, V.flyDisabledOther.replaceAll("\\{PLAYER}", p.getName()));	
			}
		} else if (manual && !toggleVal && user.hasAutoFlyQueued()) {
			user.setAutoFly(false);
			U.m(p, V.flyDisabledSelf);
			if (!s.equals(p)) {
				U.m(s, V.flyDisabledOther.replaceAll("\\{PLAYER}", p.getName()));	
			}
		} else {
			U.m(p, V.flyAlreadyDisabled);
		}
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		if (args.length == 2 && U.hasPermission(s, "tempfly.toggle.other")) {
			return getPlayerArguments(args[1]);
		} else if (args.length == 2 && U.hasPermission(s, "tempfly.toggle.self")) {
			return Arrays.asList(((Player)s).getName());
		}
		return new ArrayList<>();
	}
}
