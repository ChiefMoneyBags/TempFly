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

public class CmdRemove extends TimeCommand {

	@SuppressWarnings("deprecation")
	public CmdRemove(TempFly tempfly, CommandSender s, String[] args) {
		if (!U.hasPermission(s, "tempfly.remove")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (args.length < 3) {
			U.m(s, U.cc("&c/tf remove [player] [amount-> {args=[-s][-m][-h][-d]}]"));
			return;
		}
		
		OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
		if (p == null || (p != null && !p.isOnline() && !p.hasPlayedBefore())) {
			U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[1]));
			return;
		}
		double amount = quantifyArguments(s, args);
		if (amount == 0) {
			return;
		}
		amount = Math.floor(amount);
		TimeManager manager = tempfly.getTimeManager();
		if (p.isOnline() && manager.getTime(p.getUniqueId()) > 0) {
			U.m((Player)p, manager.regexString(V.timeRemovedSelf, amount));
		}
		manager.removeTime(p.getUniqueId(), amount);
		U.m(s, manager.regexString(V.timeRemovedOther, amount)
				.replaceAll("\\{PLAYER}", p.getName()));
	}
}
