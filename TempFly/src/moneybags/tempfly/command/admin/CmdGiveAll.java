package moneybags.tempfly.command.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import moneybags.tempfly.command.CommandHandle;
import moneybags.tempfly.time.TimeHandle;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

public class CmdGiveAll {

	public CmdGiveAll(CommandSender s, String[] args) {
		if (!U.hasPermission(s, "tempfly.giveall")) {
			U.m(s, V.invalidPermission);
			return;
		}
		//TODO TODO TODO
		List<String> ghettoQuickFix = new ArrayList<>();
		ghettoQuickFix.add("lazyRand");
		ghettoQuickFix.addAll(Arrays.asList(args));
		
		double amount = CommandHandle.quantifyArguments(s, ghettoQuickFix.toArray(new String[ghettoQuickFix.size()]));
		if (amount == 0) {
			return;
		}
		amount = Math.floor(amount);
		for (Player p: Bukkit.getOnlinePlayers()) {
			if ((V.maxTime > -1) && (TimeHandle.getTime(p.getUniqueId()) + amount >= V.maxTime)) {
				continue;
			}
			TimeHandle.addTime(p.getUniqueId(), amount);
			U.m((Player)p, TimeHandle.regexString(V.timeGivenSelf, amount));
		}
	}
	
}
