package com.moneybags.tempfly.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class TempFlyTabCompleter implements TabCompleter {
	
	private CommandManager manager;
	
	public TempFlyTabCompleter(CommandManager manager) {
		this.manager = manager;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			return manager.getAllCommandBases();
		} else if (args.length == 1) {
			return manager.getPartialCommandBases(args[0]);
		} else {
			TempFlyCommand command = manager.getCommand(args);
			return command == null ? null : command.getPotentialArguments(s);
		}
	}
}
