package com.moneybags.tempfly.command.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.TimeCommand;
import com.moneybags.tempfly.time.AsyncTimeParameters;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdGiveAll extends TimeCommand {

	public CmdGiveAll(TempFly tempfly, String[] args) {
		super(tempfly, args);
	}
	
	@Override
	public void executeAs(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.giveall")) {
			U.m(s, V.invalidPermission);
			return;
		}
		
		final double amount = quantifyArguments(s, 1);
		if (amount == 0) {
			U.m(s, V.invalidNumber.replaceAll("\\{NUMBER}", String.valueOf(amount)));
			return;
		}
		TimeManager manager = tempfly.getTimeManager();
		for (Player p: Bukkit.getOnlinePlayers()) {
			//If we were going to check offline player permissions it would need to be an AsyncTimeCommand
			double maxTime = tempfly.getTimeManager().getMaxTime(p.getUniqueId());
			double time = manager.getTime(p.getUniqueId());
			double amount2 = amount;
			if (maxTime > -1 && (time + amount > maxTime)) {
				U.m(s, manager.regexString(V.timeMaxOther, amount)
						.replaceAll("\\{PLAYER}", p.getName()));
				U.m(p, V.timeMaxSelf);
				
				amount2 = maxTime - time;
				if (amount <= 0) {
					continue;
				}
			}
			new AsyncTimeParameters(tempfly, this, s, p, amount2).runAsync();
			U.m((Player)p, manager.regexString(V.timeGivenSelf, amount2));
		}
		U.m(s, manager.regexString(V.timeGivenSelf, amount));
	}

	@Override
	public List<String> getPotentialArguments(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.giveall")) {
			return new ArrayList<>();
		}
		return getTimeArguments(cleanArgs(args, 1));
	}

	// Im not going to pay all offline players so it doen't need to be async.
	@Override
	public void execute(AsyncTimeParameters parameters) {
		parameters.getTempfly().getTimeManager().addTime(parameters.getTarget().getUniqueId(), parameters);
		return;
	}
}
