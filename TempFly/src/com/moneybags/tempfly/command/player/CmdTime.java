package com.moneybags.tempfly.command.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TimeCommand;
import com.moneybags.tempfly.time.AsyncTimeParameters;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdTime extends TimeCommand {

	public CmdTime(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@SuppressWarnings("deprecation")
	public void executeAs(CommandSender s) {
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
			new AsyncTimeParameters(tempfly, this, s, p, 0).runAsync();
		} else {
			if (!U.hasPermission(s, "tempfly.time.self")) {
				U.m(s, V.invalidPermission);
				return;
			}
			if (!U.isPlayer(s)) {
				U.m(s, V.invalidSender);
				return;
			}
			new AsyncTimeParameters(tempfly, this, s, (Player)s, 0).runAsync();
		}
	}
	
	public void execute(AsyncTimeParameters parameters) {
		CommandSender s = parameters.getSender();
		OfflinePlayer p = parameters.getTarget();
		
		TimeManager manager = tempfly.getTimeManager();
		double time = parameters.getCurrentTime();
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

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		if (args.length < 3 && U.hasPermission(s, "tempfly.time.other")) {
			return getPlayerArguments(args[1]);
		} else if (args.length < 3 && U.hasPermission(s, "tempfly.time.self")) {
			return Arrays.asList(((Player)s).getName());
		}
		return new ArrayList<>();
		
	}
}
