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

public class CmdSet {

	@SuppressWarnings("deprecation")
	public CmdSet(CommandSender s, String[] args) {
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
		List<String> a = Arrays.asList(args);
		a.remove(0);
		a.remove(0);
		double amount = CommandHandle.quantifyArguments(s, a.toArray(new String[a.size()]));
		if ((V.maxTime > -1) && (amount > V.maxTime)) {
			amount = V.maxTime;
		}
		amount = Math.floor(amount);
		TimeHandle.setTime(p.getUniqueId(), amount);
		U.m(s, TimeHandle.regexString(V.timeSetOther, amount)
				.replaceAll("\\{PLAYER}", p.getName()));
		if (p.isOnline()) {
			U.m((Player)p, TimeHandle.regexString(V.timeSetSelf, amount));	
		}
	}
	
}
