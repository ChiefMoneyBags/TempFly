package com.moneybags.tempfly.command.player;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.time.TimeHandle;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdTime {

	@SuppressWarnings("deprecation")
	public CmdTime(CommandSender s, String[] args) {
		if (args.length > 1) {
			if (!U.hasPermission(s, "tempfly.time.other")) {
				U.m(s, V.invalidPermission);
				return;
			}
			OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
			if (p == null || (p != null && !p.isOnline() && !p.hasPlayedBefore())) {
				U.m(s, V.invalidPlayer);
				return;
			}
			a(s, p);
		} else {
			if (!U.hasPermission(s, "tempfly.time.self")) {
				U.m(s, V.invalidPermission);
				return;
			}
			if (!U.isPlayer(s)) {
				U.m(s, V.invalidSender);
				return;
			}
			a(s, (Player)s);
		}
	}
	
	private void a(CommandSender s, OfflinePlayer p) {
		double time = TimeHandle.getTime(p.getUniqueId());
		U.m(s, TimeHandle.regexString(V.infoHeader, time));
		U.m(s, TimeHandle.regexString(V.infoPlayer, time).replaceAll("\\{PLAYER}", p.getName()));
		if (p.isOnline() && (((Player)p).hasPermission("tempfly.time.infinite"))) {
			U.m(s, V.infoInfinite);
			U.m(s, TimeHandle.regexString(V.infoFooter, time));
			return;
		}
		long days = TimeHandle.formatTime(TimeUnit.DAYS, time);
		if (days > 0) {
			U.m(s, TimeHandle.regexString(V.infoDays, time));	
		}
		
		double hours = TimeHandle.formatTime(TimeUnit.HOURS, time);
		if (hours > 0 || days > 0) {
			U.m(s, TimeHandle.regexString(V.infoHours, time));	
		}
		
		double minutes = TimeHandle.formatTime(TimeUnit.MINUTES, time);
		if (minutes > 0 || hours > 0 || days > 0) {
			U.m(s, TimeHandle.regexString(V.infoMinutes, time));	
		}
		
		U.m(s, TimeHandle.regexString(V.infoSeconds, time));
		U.m(s, TimeHandle.regexString(V.infoFooter, time));
	}
}
