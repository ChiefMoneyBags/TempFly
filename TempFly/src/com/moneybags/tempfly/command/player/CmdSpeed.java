package com.moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdSpeed {

	public CmdSpeed(CommandSender s, String[] args) {
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		if (args.length < 2) {
			U.m(s, U.cc("&c/tf speed [speed]"));
			return;
		}
		Player p = (Player)s;
		Flyer f = FlyHandle.getFlyer(p);
		if (f == null) {
			U.m(s, V.invalidFlyerSelf);
			return;
		}
		int speed = 1;
		try {
			speed = Integer.parseInt(args[1]);
		} catch (Exception e) {
			U.m(s, V.invalidNumber);
			return;
		}
		
		if (speed < 1) {
			speed = 1;
		} else if (speed > 10) {
			speed = 10;
		}
		
		if (!U.hasPermission(s, "tempfly.speed." + args[1])) {
			U.m(s, V.invalidPermission);
			return;
		}
		float fin = (float)(speed * 0.1);
		p.setFlySpeed(fin);
		U.m(s, V.flySpeedSelf
				.replaceAll("\\{SPEED}", String.valueOf(speed)));
	}
	
}
