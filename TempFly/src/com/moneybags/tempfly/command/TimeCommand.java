package com.moneybags.tempfly.command;

import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.util.U;

public class TimeCommand {

	public double quantifyArguments(CommandSender s, String[] args) {
		double t = 0;
		double amount = 0;
		
		for (int i = 0; i < args.length; i++) {
			if (i < 2) {
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
}
