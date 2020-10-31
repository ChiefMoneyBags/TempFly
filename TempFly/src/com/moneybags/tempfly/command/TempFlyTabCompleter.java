package com.moneybags.tempfly.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import com.moneybags.tempfly.util.U;

public class TempFlyTabCompleter implements TabCompleter, Listener {
	
	private CommandManager manager;
	
	public TempFlyTabCompleter(CommandManager manager) {
		this.manager = manager;
		Bukkit.getServer().getPluginManager().registerEvents(this, manager.getTempFly());
	}
	
	@Override
	public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			return manager.getAllCommandBases();
		} else if (args.length == 1) {
			return manager.getPartialCommandBases(args[0]);
		} else {
			TempFlyCommand command = manager.getCommand(args);
			return command == null ? new ArrayList<>() : command.getPotentialArguments(s);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(TabCompleteEvent e) {
		String[] args = e.getBuffer().split(" ");
		if (!args[0].equalsIgnoreCase("/fly")) {
			return;
		}
		
		List<String> completions = new ArrayList<>();
		Arrays.asList(U.skipArray(args, 1)).forEach(string -> completions.add(string));
		if (e.getBuffer().endsWith(" ")) {
			completions.add("");
		}
		e.setCompletions(onTabComplete(e.getSender(), null, "", completions.toArray(new String[completions.size()])));
	}
}
