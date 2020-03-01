package com.moneybags.tempfly.command.admin;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.command.CommandHandle;
import com.moneybags.tempfly.time.TimeHandle;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdRemove {

	@SuppressWarnings("deprecation")
	public CmdRemove(CommandSender s, String[] args) {
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
		List<String> a = Arrays.asList(args);
		a.remove(0);
		a.remove(0);
		double amount = CommandHandle.quantifyArguments(s, a.toArray(new String[a.size()]));
		if (amount == 0) {
			return;
		}
		Math.floor(amount);
		if (p.isOnline() && TimeHandle.getTime(p.getUniqueId()) > 0) {
			U.m((Player)p, TimeHandle.regexString(V.timeRemovedSelf, amount));
		}
		TimeHandle.removeTime(p.getUniqueId(), amount);
		U.m(s, TimeHandle.regexString(V.timeRemovedOther, amount)
				.replaceAll("\\{PLAYER}", p.getName()));
	}
}
