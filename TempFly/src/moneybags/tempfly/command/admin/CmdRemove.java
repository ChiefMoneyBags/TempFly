package moneybags.tempfly.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import moneybags.tempfly.command.CommandHandle;
import moneybags.tempfly.time.TimeHandle;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

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
		double amount = CommandHandle.quantifyArguments(s, args);
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
