package com.moneybags.tempfly.command.player;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdSpeed {

	public CmdSpeed(CommandSender s, String[] args) {
		
		Player p = null;
		float speed = 1;
		
		if (args.length > 2) {
			if (!U.hasPermission(s, "tempfly.speed.other")) {
				U.m(s, V.invalidPermission);
				return;
			}
			p = Bukkit.getPlayer(args[1]);
			if (p == null) {
				U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[1]));
				return;
			}
			
			try {
				speed = Float.parseFloat(args[2]);
			} catch (Exception e) {
				U.m(s, V.invalidNumber);
				return;
			}
			
			
		} else if (args.length > 1) {
			if (!U.isPlayer(s)) {
				U.m(s, V.invalidSender);
				return;
			}
			if (!U.hasPermission(s, "tempfly.speed.self")) {
				U.m(s, V.invalidPermission);
				return;
			}
			p = (Player)s;
			
			try {
				speed = Float.parseFloat(args[1]);
			} catch (Exception e) {
				U.m(s, V.invalidNumber);
				return;
			}
			
		} else {
			U.m(s, U.cc("/tf speed [speed]"));
			return;
		}
		
		float max = FlyHandle.getMaxSpeed(p);
		if (speed > max) {
			speed = max;
		}
		p.setFlySpeed((float) (speed * 0.1));
		U.m(p, V.flySpeedSelf
				.replaceAll("\\{SPEED}", String.valueOf(speed)));
		if (!s.equals(p)) {
			U.m(s, V.flySpeedOther
					.replaceAll("\\{SPEED}", String.valueOf(speed))
					.replaceAll("\\{PLAYER}", p.getName()));
		}
	}
	
}
