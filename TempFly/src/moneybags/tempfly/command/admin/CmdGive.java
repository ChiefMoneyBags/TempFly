package moneybags.tempfly.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import moneybags.tempfly.command.CommandHandle;
import moneybags.tempfly.time.TimeHandle;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

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
		
		double amount = CommandHandle.quantifyArguments(s, args);
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
