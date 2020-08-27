package com.moneybags.tempfly.command.player;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TimeCommand;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdPay extends TimeCommand {

	public CmdPay(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override @SuppressWarnings("deprecation")
	public void executeAs(CommandSender s) {
		if (!V.payable) {
			U.m(s, V.invalidCommand);
			return;
		}
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		if (!U.hasPermission(s, "tempfly.pay")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (args.length < 3) {
			U.m(s, U.cc("&c/tf pay [player] [amount-> {args=[-s][-m][-h][-d]}]"));
			return;
		}
		OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
		if (p == null || (p != null && !p.isOnline() && !p.hasPlayedBefore())) {
			U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[1]));
			return;
		}
		
		if ((Player)s == p) {
			U.m(s, V.invalidReciever);
			return;
		}
		double amount = 0;
		amount = quantifyArguments(s, 2);
		if (amount <= 0) {
			U.m(s, V.invalidNumber.replaceAll("\\{NUMBER}", String.valueOf(amount)));
			return;
		}
		amount = Math.floor(amount);
		TimeManager manager = tempfly.getTimeManager();
		Player sender = (Player)s;
		double bal = manager.getTime(sender.getUniqueId());
		if (bal < amount) {
			U.m(s, V.invalidTimeSelf);
			return;
		}
		
		double maxTime = tempfly.getTimeManager().getMaxTime(p.getUniqueId());
		if (maxTime == -999) {
			U.m(s, s.isOp() ? V.vaultPermsRequired : V.invalidPlayer);
			return;
		}
		
		if ((maxTime > -1) && (manager.getTime(p.getUniqueId()) + amount >= maxTime)) {
			U.m(s, manager.regexString(V.timeMaxOther, amount)
					.replaceAll("\\{PLAYER}", p.getName()));
			return;
		}
		
		manager.removeTime(sender.getUniqueId(), amount);
		manager.addTime(p.getUniqueId(), amount);
		U.m(s, manager.regexString(V.timeSentOther, amount)
				.replaceAll("\\{PLAYER}", p.getName()));
		if (p.isOnline()) {
			U.m((Player)p, manager.regexString(V.timeSentSelf, amount)
					.replaceAll("\\{PLAYER}", s.getName()));	
		}
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.pay") || !V.payable) {
			return new ArrayList<>();
		}
		if (args.length < 3) {
			return getPlayerArguments(args[1]);
		} else {
			return getTimeArguments(cleanArgs(args, 2));
		}
	}


}
