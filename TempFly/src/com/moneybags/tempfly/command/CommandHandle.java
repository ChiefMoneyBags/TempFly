package com.moneybags.tempfly.command;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.moneybags.tempfly.command.admin.CmdGive;
import com.moneybags.tempfly.command.admin.CmdReload;
import com.moneybags.tempfly.command.admin.CmdRemove;
import com.moneybags.tempfly.command.admin.CmdSet;
import com.moneybags.tempfly.command.player.CmdAskyblock;
import com.moneybags.tempfly.command.player.CmdFly;
import com.moneybags.tempfly.command.player.CmdHelp;
import com.moneybags.tempfly.command.player.CmdPay;
import com.moneybags.tempfly.command.player.CmdSpeed;
import com.moneybags.tempfly.command.player.CmdTime;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CommandHandle implements CommandExecutor, Listener {

	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			new CmdFly(s, args);
			return true;
		} else {
			switch (args[0]) {
			case "help":
			case "commands":
				new CmdHelp(s, args);
				break;
			case "toggle":
				new CmdFly(s, args);
				break;
			case "give":
			case "add":
				new CmdGive(s, args);
				break;
			case "remove":
			case "take":
				new CmdRemove(s, args);
				break;
			case "pay":
			case "send":
				new CmdPay(s, args);
				break;
			case "time":
			case "info":
			case "remaining":
			case "balance":
			case "bal":
			case "seconds":
				new CmdTime(s, args);
				break;
			case "set":
				new CmdSet(s, args);
				break;
			case "speed":
			case "momentum":
				new CmdSpeed(s, args);
				break;
			case "reload":
				new CmdReload(s);
				break;
			case "island":
			case "settings":
			case "skyblock":
			case "sb":
			case "allow":
				new CmdAskyblock(s, args);
				break;
			default:
				new CmdHelp(s, args);
				break;
			}
		}
		return true;
	}
	
	public static double quantifyArguments(CommandSender s, String[] args) {
		double t = 0;
		double amount = 0;
		
		for (int i = 0; i < args.length; i++) {
			if (i < 3) {
				continue;
			}
			String arg = args[i];
			try {
				double parse = Double.parseDouble(arg);
				t += parse;
				continue;
			} catch (NumberFormatException e) {}
			char[] c = arg.toCharArray();
			if (String.valueOf(c[0]).equals("-")) {
				switch(arg.replaceAll("\\-", "").toLowerCase()) {
				case "s":
				case "sec":
				case "second":
				case "seconds":
					try {
						amount = Double.parseDouble(args[i+1]);
					} catch (Exception e) {
						U.m(s, U.cc("&c(" + arg + ") must be followed by a valid number."));
						return 0;
					}
					t += amount;
					i++;
					continue;
				case "m":
				case "min":
				case "minute":
				case "minutes":
					try {
						amount = Double.parseDouble(args[i+1]);
					} catch (Exception e) {
						U.m(s, U.cc("&c(" + arg + ") must be followed by a valid number."));
						return 0;
					}
					t += amount * 60;
					i++;
					continue;
				case "h":
				case "hour":
				case "hours":
					try {
						amount = Double.parseDouble(args[i+1]);
					} catch (Exception e) {
						U.m(s, U.cc("&c(" + arg + ") must be followed by a valid number."));
						return 0;
					}
					t += amount * 3600;
					i++;
					continue;
				case "d":
				case "day":
				case "days":
					try {
						amount = Double.parseDouble(args[i+1]);
					} catch (Exception e) {
						U.m(s, U.cc("&c(" + arg + ") must be followed by a valid number."));
						return 0;
					}
					t += amount * 86400;
					i++;
					continue;
				default:
					U.m(s, U.cc("&c(" + arg + ") is an unknown variable."));
					return 0;
				}
			}
		}
		return t;
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void on(PlayerCommandPreprocessEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		String s = e.getMessage();
		String[] ls = s.split(" ");
		Player p = e.getPlayer();
		if (ls[0].equals("/fly")) {
			for (String perm : V.overrideFlightPermissions) {
				if (p.hasPermission(perm)) {
					return;
				}
			}
			String[] args = Arrays.copyOfRange(ls, 1, ls.length);
			e.setCancelled(true);
			onCommand(e.getPlayer(), null, "", args);	
		}
	}
}
