package com.moneybags.tempfly.command.player;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

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
		
		
		if (speed < 0) {
			speed = 0;
		} else if (speed > 10) {
			speed = 10;
		}
		
		if (!p.isOp()) {
			float max = 1;
			for (PermissionAttachmentInfo info: p.getEffectivePermissions()) {
				String perm = info.getPermission();
				if (perm.startsWith("tempfly.speed")) {
					String[] split = perm.split("\\.");
					try {
						float found = Float.parseFloat(split[2]);
						if (found > max) {
							max = found;
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
			if (speed > max) {
				speed = max;
			}
			if (speed > 10) {
				speed = 10;
			}
		}
		
		float fin = (float)(speed * 0.1);
		p.setFlySpeed(fin);
		U.m(p, V.flySpeedSelf
				.replaceAll("\\{SPEED}", String.valueOf(speed)));
		if (!s.equals(p)) {
			U.m(s, V.flySpeedOther
					.replaceAll("\\{SPEED}", String.valueOf(speed))
					.replaceAll("\\{PLAYER}", p.getName()));
		}
	}
	
}
