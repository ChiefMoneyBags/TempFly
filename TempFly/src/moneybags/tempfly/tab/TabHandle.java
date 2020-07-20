package moneybags.tempfly.tab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TabHandle implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
		if (args.length == 0 || args.length == 1) {
			return resolve(args[0], Arrays.asList(
					"help",
					"toggle",
					"time",
					"give",
					"remove",
					"pay",
					"set",
					"speed"));
		}
		if (args.length == 1 || args.length == 2) {
			List<String> names = new ArrayList<>();
			for (Player p: Bukkit.getOnlinePlayers()) {
				names.add(p.getName());
			}
			return resolve(args[1], names);
		}
		return null;
	}
	
	public List<String> resolve(String current, List<String> args) {
		char[] cChar = current.toCharArray();
		List<String> found = new ArrayList<>();
		
		matches:
		for (String s: args) {
			char[] aChar = s.toCharArray();
			for (int i = 0; i < cChar.length; i++) {
				if (aChar.length <= i) {
					continue matches;
				}
				if (aChar[i] == cChar[i]) {
					if (!found.contains(s)) {
						found.add(s);	
					}
				} else {
					if (found.contains(s)) {
						found.remove(s);
					}
					continue matches;
				}
			}
		}
		if (found.size() == 0) {
			return args;
		}
		return found;
	}
}
