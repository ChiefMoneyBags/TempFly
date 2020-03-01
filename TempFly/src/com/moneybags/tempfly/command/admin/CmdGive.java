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

public class CmdGive {

	@SuppressWarnings("deprecation")
	public CmdGive(CommandSender s, String[] args) {
		if (!U.hasPermission(s, "tempfly.give")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (args.length < 3) {
			U.m(s, U.cc("&c/tf give [player] [amount-> {args=[-s][-m][-h][-d]}]"));
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
		amount = Math.floor(amount);
		if ((V.maxTime > -1) && (TimeHandle.getTime(p.getUniqueId()) + amount >= V.maxTime)) {
			U.m(s, TimeHandle.regexString(V.timeMaxOther, amount)
					.replaceAll("\\{PLAYER}", p.getName()));
			return;
		}
		TimeHandle.addTime(p.getUniqueId(), amount);
		U.m(s, TimeHandle.regexString(V.timeGivenOther, amount)
				.replaceAll("\\{PLAYER}", p.getName()));
		if (p.isOnline()) {
			U.m((Player)p, TimeHandle.regexString(V.timeGivenSelf, amount));	
		}
	}
	
	

}
