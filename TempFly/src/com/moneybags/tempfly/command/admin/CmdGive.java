package com.moneybags.tempfly.command.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TimeCommand;
import com.moneybags.tempfly.time.AsyncTimeParameters;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdGive extends TimeCommand {
	
	public CmdGive(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.give")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (args.length < 3) {
			U.m(s, U.cc("&c/tf give [player] [amount]"));
			return;
		}
		
		@SuppressWarnings("deprecation")
		OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
		if (p == null || (p != null && !p.isOnline() && !p.hasPlayedBefore())) {
			U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[1]));
			return;
		}
		double amount = quantifyArguments(s, 2);
		if (amount <= 0) {
			return;
		}
		new AsyncTimeParameters(tempfly, this, s, p, amount).runAsync();
	}

	@Override
	public void execute(AsyncTimeParameters parameters) {
		CommandSender s = parameters.getSender();
		double maxTime = parameters.getMaxTime();
		if (maxTime == -999) {
			U.m(s, s.isOp() ? V.vaultPermsRequired : V.invalidPlayer);
			return;
		}
		Console.debug(maxTime);
		
		TimeManager manager = tempfly.getTimeManager();
		OfflinePlayer p = parameters.getTarget();
		double currentTime = parameters.getCurrentTime();
		double amount = parameters.getAmount();
		if (maxTime > -1 && (currentTime + amount > maxTime)) {
			U.m(s, manager.regexString(V.timeMaxOther, amount)
					.replaceAll("\\{PLAYER}", p.getName()));
			U.m(p, V.timeMaxSelf);
			
			amount = maxTime - currentTime;
			if (amount <= 0) {
				return;
			}
		}
		manager.addTime(parameters.getTarget().getUniqueId(), parameters);
		if (p != s) {
			U.m(s, manager.regexString(V.timeGivenOther, amount)
					.replaceAll("\\{PLAYER}", p.getName()));
		}
		if (p.isOnline()) {
			U.m((Player)p, manager.regexString(V.timeGivenSelf, amount));	
		}
	}
	
	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.give")) {
			return new ArrayList<>();
		}
		if (args.length < 3) {
			return getPlayerArguments(args[1]);
		} else {
			return getTimeArguments(cleanArgs(args, 2));
		}
	}
}
