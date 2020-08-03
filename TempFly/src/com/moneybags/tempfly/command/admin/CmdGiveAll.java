package com.moneybags.tempfly.command.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TimeCommand;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdGiveAll extends TimeCommand {

	public CmdGiveAll(TempFly tempfly, CommandSender s, String[] args) {
		if (!U.hasPermission(s, "tempfly.giveall")) {
			U.m(s, V.invalidPermission);
			return;
		}
		//TODO TODO TODO
		List<String> ghettoQuickFix = new ArrayList<>();
		ghettoQuickFix.add("lazyRand");
		ghettoQuickFix.addAll(Arrays.asList(args));
		
		double amount = quantifyArguments(s, ghettoQuickFix.toArray(new String[ghettoQuickFix.size()]));
		if (amount == 0) {
			return;
		}
		amount = Math.floor(amount);
		TimeManager manager = tempfly.getTimeManager();
		for (Player p: Bukkit.getOnlinePlayers()) {
			if ((V.maxTime > -1) && (manager.getTime(p.getUniqueId()) + amount >= V.maxTime)) {
				continue;
			}
			manager.addTime(p.getUniqueId(), amount);
			U.m((Player)p, manager.regexString(V.timeGivenSelf, amount));
		}
	}
	
}