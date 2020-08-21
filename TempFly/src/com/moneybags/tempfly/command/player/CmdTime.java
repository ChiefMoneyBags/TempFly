package com.moneybags.tempfly.command.player;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdTime {

	@SuppressWarnings("deprecation")
	public CmdTime(TempFly tempfly, CommandSender s, String[] args) {
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
			a(tempfly, s, p);
		} else {
			if (!U.hasPermission(s, "tempfly.time.self")) {
				U.m(s, V.invalidPermission);
				return;
			}
			if (!U.isPlayer(s)) {
				U.m(s, V.invalidSender);
				return;
			}
			a(tempfly, s, (Player)s);
		}
	}
	
	private void a(TempFly tempfly, CommandSender s, OfflinePlayer p) {
		TimeManager manager = tempfly.getTimeManager();
		double time = manager.getTime(p.getUniqueId());
		U.m(s, manager.regexString(V.infoHeader, time));
		U.m(s, manager.regexString(V.infoPlayer, time).replaceAll("\\{PLAYER}", p.getName()));
		final boolean infinite = p.isOnline() && tempfly.getFlightManager().getUser((Player)p).hasInfiniteFlight(); 
		if (infinite) {
			U.m(s, V.infoInfinite);
		}
		long days = manager.formatTime(TimeUnit.DAYS, time);
		if (days > 0) {
			U.m(s, manager.regexString(V.infoDays, time));	
		}
		
		double hours = manager.formatTime(TimeUnit.HOURS, time);
		if (hours > 0) {
			U.m(s, manager.regexString(V.infoHours, time));	
		}
		
		double minutes = manager.formatTime(TimeUnit.MINUTES, time);
		if (minutes > 0) {
			U.m(s, manager.regexString(V.infoMinutes, time));	
		}
		
		double seconds = manager.formatTime(TimeUnit.SECONDS, time);
		if (seconds > 0 || (seconds == 0 && days == 0 && hours == 0 && minutes == 0 && !infinite)) {
			U.m(s, manager.regexString(V.infoSeconds, time));	
		}
		U.m(s, manager.regexString(V.infoFooter, time));
	}
}
