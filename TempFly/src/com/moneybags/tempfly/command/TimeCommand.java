package com.moneybags.tempfly.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class TimeCommand {
	
	public TimeUnit parseUnit(String s) {
		switch (s) {
		case "s": case "sec": case "second": case "seconds":
			return TimeUnit.SECONDS;
		case "m": case "min": case "minute": case "minutes":
			return TimeUnit.MINUTES;
		case "h": case "hour": case "hours":
			return TimeUnit.HOURS;
		case "d": case "day": case "days":
			return TimeUnit.DAYS;
		default:
			return null;
		}
	}
	
	private int lastNumericIndex(String s) {
		int fin = 0;
		for (int i = 0; i < s.length(); i++) {
			if (!String.valueOf(s.charAt(i)).matches("[0-9]")) {
				return i > 0 ? fin-1 : fin;
			}
			fin++;
		}
		return s.length()-1;
	}

	public String[] cleanArgs(String[] arguments, int skip) {
		List<String> temp = new ArrayList<>();
		for (int i = 0; i < arguments.length; i++) {
			if (i <= skip-1) {
				continue;
			}
			temp.add(arguments[i]);
		}
		return temp.toArray(new String[temp.size()]);
	}
	
	public double quantifyArguments(CommandSender s, String[] arguments, int skip) {
		double seconds = 0;
		String[] args = skip > 0 ? cleanArgs(arguments, skip) : arguments;
		for (int i = 0; i < args.length; i++) {
			TimeUnit unit;
			String parse;
			// /tf give -{unit} 1
			if (String.valueOf(args[i].charAt(0)).equals("-")) {
				if ((unit = parseUnit(args[i].toLowerCase().replaceAll("\\-", ""))) == null) {
					U.m(s, U.cc("&c(" + args[i] + ") is an unknown time argument!"));
					return 0;
				}
				if (args.length >= i+1) {
					parse = args[i+1];
					i++;
				} else {
					U.m(s, U.cc("&c(" + args[i] + ") must be followed by a valid number!"));
					return 0;
				}
				
			// /tf give 1{unit}
			} else if (lastNumericIndex(args[i]) < args[i].length()-1) {
				if ((unit = parseUnit(args[i].replaceAll("[0-9]", "").toLowerCase())) == null) {
					U.m(s, U.cc("&c(" + args[i].replaceAll("[0-9]", "") + ") is an unknown time argument!"));
					return 0;
				}
				parse = args[i].replaceAll("[a-zA-Z]+", "");
				
			// /tf give 60 {unit} | /tf give 60
			} else {
				parse = args[i];
				if (args.length-1 > i && (unit = parseUnit(args[i+1])) != null) {
					i++;
				} else {
					unit = TimeUnit.SECONDS;
				}
			}
			try {
				Console.debug(parse);
				double fin = Double.parseDouble(parse);
				switch (unit) {
				case DAYS:		fin *= 24;
				case HOURS:		fin *= 60;
				case MINUTES: 	fin *= 60;
				case SECONDS: 	seconds += fin;
				default:
					break;
				}
			} catch (NumberFormatException e) {
				U.m(s, V.invalidNumber.replaceAll("\\{NUMBER}", parse));
				return 0;
			}
		}
		return seconds;
	}
}
