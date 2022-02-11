package com.moneybags.tempfly.command.player;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TempFlyCommand;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdSpeed extends TempFlyCommand {

	public CmdSpeed(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		Player p = null;
		float speed = 1;
		
		if (args.length > 2) {
			if (!U.hasPermission(s, "tempfly.speed.other")) {
				U.m(s, V.invalidPermission);
				return;
			}
			p = Bukkit.getPlayer(args[2]);
			if (p == null) {
				U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[2]));
				return;
			}
		}
		
		if (args.length > 1) {
			if (p == null && !U.isPlayer(s)) {
				U.m(s, V.invalidSender);
				return;
			}
			if (!U.hasPermission(s, "tempfly.speed.self")) {
				U.m(s, V.invalidPermission);
				return;
			}
			if (p == null) {
				p = (Player) s;
			}
			
			if (args[1].equalsIgnoreCase("reset")) {
				speed = -999;
			} else {
				try {
					speed = Float.parseFloat(args[1]);
				} catch (Exception e) {
					U.m(s, V.invalidNumber);
					return;
				}	
			}
		} else {
			U.m(s, U.cc("/tf speed [speed / reset]"));
			return;
		}
		
		FlightUser user = tempfly.getFlightManager().getUser(p);
		user.setSpeedPreference(speed);
		float old = user.getPlayer().getFlySpeed();
		float fin = user.applySpeedCorrect(false, 0);
		String result = new DecimalFormat("#.##").format(fin * 10);

		if (fin < (speed / 10) && p.equals(s)) {
			U.m(p, V.flySpeedLimitSelf.replaceAll("\\{SPEED}", result));
			if (old == (speed / 10)) {
				return;
			}
		}
		U.m(p, V.flySpeedSelf
				.replaceAll("\\{SPEED}", speed == -999 ? "DEFAULT" : result));
		if (!s.equals(p)) {
			if (fin < speed) {
				U.m(s, V.flySpeedLimitOther
						.replaceAll("\\{SPEED}", result)
						.replaceAll("\\{PLAYER}", p.getName()));
				if (old == (speed / 10)) {
					return;
				}
			}
			U.m(s, V.flySpeedOther
					.replaceAll("\\{SPEED}", speed == -999 ? "DEFAULT" : result)
					.replaceAll("\\{PLAYER}", p.getName()));
		}
		
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		if (args.length > 3) {
			return new ArrayList<>();
		}
		if (args.length == 3) {
			return U.hasPermission(s, "tempfly.speed.other") ? getPlayerArguments(args[2]) 
					: U.hasPermission(s, "tempfly.speed.self") ? Arrays.asList(((Player)s).getName()) : new ArrayList<>();
		}
		if (args.length >= 2 && !U.hasPermission(s, "tempfly.speed.self") || (s instanceof Player)) {
			return new ArrayList<>();
		}
		if (s instanceof Player) {
			FlightUser user = tempfly.getFlightManager().getUser((Player)s);
			Console.debug(user.getMaxSpeed());
			return getRange(1, (int)Math.floor(user.getMaxSpeed()));
		} else {
			return getRange(0, 10);
		}
	}
}
