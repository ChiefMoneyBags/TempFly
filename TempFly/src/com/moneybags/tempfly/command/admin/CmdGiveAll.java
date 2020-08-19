package com.moneybags.tempfly.command.admin;

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
		
		final double amount = quantifyArguments(s, args, 1);
		if (amount == 0) {
			return;
		}
		TimeManager manager = tempfly.getTimeManager();
		for (Player p: Bukkit.getOnlinePlayers()) {
			double maxTime = tempfly.getTimeManager().getMaxTime(p.getUniqueId());
			double time = manager.getTime(p.getUniqueId());
			double amount2 = ((maxTime > -1) && (time + amount > maxTime))
					? maxTime - time : amount;
			manager.addTime(p.getUniqueId(), amount2);
			U.m((Player)p, manager.regexString(V.timeGivenSelf, amount2));
		}
		U.m(s, manager.regexString(V.timeGivenSelf, amount));
	}
	
}
