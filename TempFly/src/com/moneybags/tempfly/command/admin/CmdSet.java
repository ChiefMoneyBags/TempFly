package com.moneybags.tempfly.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TimeCommand;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdSet extends TimeCommand {

	@SuppressWarnings("deprecation")
	public CmdSet(TempFly tempfly, CommandSender s, String[] args) {
		if (!U.hasPermission(s, "tempfly.set")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (args.length < 3) {
			U.m(s, U.cc("&c/time set [player] [amount-> {args=[-s][-m][-h][-d]}]"));
			return;
		}
		
		OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
		if (p == null || (p != null && !p.isOnline() && !p.hasPlayedBefore())) {
			U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[1]));
			return;
		}
		
		double amount = quantifyArguments(s, args);
		if ((V.maxTime > -1) && (amount > V.maxTime)) {
			amount = V.maxTime;
		}
		amount = Math.floor(amount);
		TimeManager manager = tempfly.getTimeManager();
		manager.setTime(p.getUniqueId(), amount);
		U.m(s, manager.regexString(V.timeSetOther, amount)
				.replaceAll("\\{PLAYER}", p.getName()));
		if (p.isOnline()) {
			U.m((Player)p, manager.regexString(V.timeSetSelf, amount));	
		}
	}
	
}
