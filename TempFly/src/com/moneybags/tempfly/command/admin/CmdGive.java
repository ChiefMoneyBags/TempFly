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

public class CmdGive extends TimeCommand {

	@SuppressWarnings("deprecation")
	public CmdGive(TempFly tempfly, CommandSender s, String[] args) {
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
		
		double amount = quantifyArguments(s, args, 2);
		if (amount <= 0) {
			return;
		}
		double maxTime = tempfly.getTimeManager().getMaxTime(p.getUniqueId());
		if (maxTime == -999) {
			U.m(s, s.isOp() ? V.vaultPermsRequired : V.invalidPlayer);
			return;
		}
		amount = Math.floor(amount);
		TimeManager manager = tempfly.getTimeManager();
		double currentTime = manager.getTime(p.getUniqueId());
		if (maxTime > -1 && (currentTime + amount > maxTime)) {
			U.m(s, manager.regexString(V.timeMaxOther, amount)
					.replaceAll("\\{PLAYER}", p.getName()));
			amount = maxTime - currentTime;
			if (amount <= 0) {
				return;
			}
		}
		manager.addTime(p.getUniqueId(), amount);
		if (p != s) {
			U.m(s, manager.regexString(V.timeGivenOther, amount)
					.replaceAll("\\{PLAYER}", p.getName()));
		}
		if (p.isOnline()) {
			U.m((Player)p, manager.regexString(V.timeGivenSelf, amount));	
		}
	}
	
	

}
