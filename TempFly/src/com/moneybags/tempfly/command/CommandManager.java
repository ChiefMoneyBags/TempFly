package com.moneybags.tempfly.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.ConfigurationSection;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.admin.CmdGive;
import com.moneybags.tempfly.command.admin.CmdGiveAll;
import com.moneybags.tempfly.command.admin.CmdMigrate;
import com.moneybags.tempfly.command.admin.CmdReload;
import com.moneybags.tempfly.command.admin.CmdRemove;
import com.moneybags.tempfly.command.admin.CmdSet;
import com.moneybags.tempfly.command.admin.CmdTrailRemove;
import com.moneybags.tempfly.command.admin.CmdTrailSet;
import com.moneybags.tempfly.command.player.CmdBypass;
import com.moneybags.tempfly.command.player.CmdFly;
import com.moneybags.tempfly.command.player.CmdHelp;
import com.moneybags.tempfly.command.player.CmdInfinite;
import com.moneybags.tempfly.command.player.CmdPay;
import com.moneybags.tempfly.command.player.CmdShop;
import com.moneybags.tempfly.command.player.CmdSpeed;
import com.moneybags.tempfly.command.player.CmdTime;
import com.moneybags.tempfly.command.player.CmdTrails;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.Files;

public class CommandManager {

	private TempFly tempfly;
	private TempFlyExecutor executor;
	private TempFlyTabCompleter tab;
	
	private List<String> enable = new ArrayList<>();
	private List<String> disable = new ArrayList<>();
	
	private Map<CommandType, List<String>> subCommands = new HashMap<>();
	private Map<TimeUnit, List<String>> timeArgs = new HashMap<>();
	private Map<TimeUnit, String> timeComplete = new HashMap<>();
	
	private Map<String, Class<? extends TempFlyCommand>> hookRegistry = new HashMap<>();
	
	public CommandManager(TempFly tempfly) {
		this.tempfly = tempfly;
		executor = new TempFlyExecutor(this);
		tab = new TempFlyTabCompleter(this);
		tempfly.getCommand("tempfly").setExecutor(executor);
		tempfly.getCommand("tempfly").setTabCompleter(tab);
		
		for (CommandType type: CommandType.values()) {
			if (!type.isEnabled(tempfly)) {
				continue;
			}
			List<String> subs = Files.lang.getStringList("command.base." + type.toString().toLowerCase());
			if (subs == null || subs.size() == 0) {
				subCommands.put(type, Arrays.asList(type.getBase()));
				continue;
			}
			subCommands.put(type, subs);
		}
		
		ConfigurationSection csUnits = Files.lang.getConfigurationSection("command.unit");
		if (csUnits != null) {
			String path = "command.unit";
			for (String key: csUnits.getKeys(false)) {
				try {
					TimeUnit unit = TimeUnit.valueOf(key.toUpperCase());
					List<String> recognized = Files.lang.getStringList(path + "." + key + ".recognized");
					String complete = Files.lang.getString(path + "." + key + ".tab_complete");
					
					if (recognized != null) timeArgs.put(unit, recognized);
					if (complete != null) timeComplete.put(unit, complete);
				}
				catch (Exception e) {}
			}
		}
		if (!timeArgs.containsKey(TimeUnit.SECONDS))
			timeArgs.put(TimeUnit.SECONDS, Arrays.asList(TimeType.SECONDS.getBase()));
		if (!timeArgs.containsKey(TimeUnit.MINUTES))
			timeArgs.put(TimeUnit.MINUTES, Arrays.asList(TimeType.MINUTES.getBase()));
		if (!timeArgs.containsKey(TimeUnit.HOURS))
			timeArgs.put(TimeUnit.HOURS, Arrays.asList(TimeType.HOURS.getBase()));
		if (!timeArgs.containsKey(TimeUnit.DAYS))
			timeArgs.put(TimeUnit.DAYS, Arrays.asList(TimeType.DAYS.getBase()));
		
		if (!timeComplete.containsKey(TimeUnit.SECONDS))
			timeComplete.put(TimeUnit.SECONDS, TimeType.SECONDS.getCompletion());
		if (!timeComplete.containsKey(TimeUnit.MINUTES))
			timeComplete.put(TimeUnit.MINUTES, TimeType.MINUTES.getCompletion());
		if (!timeComplete.containsKey(TimeUnit.HOURS))
			timeComplete.put(TimeUnit.HOURS, TimeType.HOURS.getCompletion());
		if (!timeComplete.containsKey(TimeUnit.DAYS))
			timeComplete.put(TimeUnit.DAYS, TimeType.DAYS.getCompletion());
		
		List<String> temp = null;
		
		enable.addAll((temp = Files.lang.getStringList("command.enable")) == null || temp.size() == 0 ? 
				Arrays.asList("on", "enable") : temp);
		disable.addAll((temp = Files.lang.getStringList("command.disable")) == null || temp.size() == 0 ? 
				Arrays.asList("off", "disable") : temp);
	}
	
	public TempFlyTabCompleter getTabCompleter() {
		return tab;
	}
	
	public TempFly getTempFly() {
		return tempfly;
	}
	
	public List<String> getEnable() {
		return enable;
	}
	
	public List<String> getDisable() {
		return disable;
	}
	
	public List<String> getAllTimeArguments() {
		List<String> args = new ArrayList<>();
		for (Entry<TimeUnit, List<String>> unit: timeArgs.entrySet()) {
			args.addAll(unit.getValue());
		}
		return args;
	}
	
	public List<String> getTimeArguments(TimeUnit unit) {
		return timeArgs.getOrDefault(unit, Arrays.asList("{unit}"));
	}
	
	public TimeUnit parseUnit(String s) {
		for (Entry<TimeUnit, List<String>> entry: timeArgs.entrySet()) {
			if (entry.getValue().contains(s)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public List<String> getAllTimeCompletions() {
		List<String> args = new ArrayList<>();
		for (Entry<TimeUnit, String> unit: timeComplete.entrySet()) {
			args.add(unit.getValue());
		}
		return args;
	}
	
	public List<String> getTimeCompletions(List<TimeUnit> exclude) {
		List<String> args = new ArrayList<>();
		for (Entry<TimeUnit, String> entry: timeComplete.entrySet()) {
			if (exclude.contains(entry.getKey())) continue;
			args.add(entry.getValue());
		}
		return args;
	}
	
	public String getTimeCompletion(TimeUnit unit) {
		return timeComplete.getOrDefault(unit, "{unit}");
	}
	
	public List<String> getToggleCompletions(boolean filter) {
		if (filter) return Arrays.asList(enable.get(0), disable.get(0));
		List<String> all = new ArrayList<>();
		all.addAll(enable); all.addAll(disable);
		return all;
	}
	
	public TempFlyCommand getCommand(String[] args) {
		if (args == null) {
			return null;
		}
		if (args.length == 0 || getEnable().contains(args[0]) || getDisable().contains(args[0])) {
			return new CmdFly(tempfly, args);
		} else {
			for (CommandType type: CommandType.values()) {
				for (String base: getCommandBases(type)) {
					if (base.equals(args[0])) {
						try {return type.getDeclaredClass().getConstructor(TempFly.class, String[].class).newInstance(tempfly, args);} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			for (Entry<String, Class<? extends TempFlyCommand>> entry: hookRegistry.entrySet()) {
				if (entry.getKey().equals(args[0])) {
					try {return entry.getValue().getConstructor(TempFly.class, String[].class).newInstance(tempfly, args);} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	public void registerHookCommand(String base, Class<? extends TempFlyCommand> command) throws IllegalArgumentException {
		if (hookRegistry.containsKey(base)) {
			throw new IllegalArgumentException("Sub command bases must be unique! This command is already taken: " + base);
		}
		
		try {command.getConstructor(TempFly.class, String[].class).newInstance(tempfly, new String[0]);} catch (Exception e) {
			throw new IllegalArgumentException("This sub command is not properly structured: " + base);
		}
		hookRegistry.put(base, command);
	}
	
	public void unregisterHookCommand(String base) {
		hookRegistry.remove(base);
	}
	
	public List<String> getAllCommandBases() {
		List<String> bases = new ArrayList<>();
		bases.addAll(getToggleCompletions(true));
		for (CommandType type: CommandType.values()) {
			for (String base: getCommandBases(type)) {
				bases.add(base);
			}
			bases.addAll(hookRegistry.keySet());
		}
		return bases;
	}
	
	public List<String> getPartialCommandBases(String partial) {
		List<String> matches = new ArrayList<>();
		for (CommandType type: CommandType.values()) {
			for (String base: getCommandBases(type)) {
				if (compare(base, partial)) {
					matches.add(base);
				}
			}
		}
		for (String base: getToggleCompletions(false)) {
			if (compare(base, partial)) {
				matches.add(base);
			}
		}
		
		for (String base: hookRegistry.keySet()) {
			if (compare(base, partial)) {
				matches.add(base);
			}
		}
		
		return matches;
	}
	
	private boolean compare(String base, String partial) {
		char[] baseChars = base.toCharArray();
		if (partial.length() > baseChars.length) {
			return false;
		}
		for (int i = 0; i < partial.length(); i++) {
			char partialChar = partial.charAt(i);
			if (partialChar != baseChars[i]) {
				return false;
			}
		}
		return true;
	}
	
	public List<String> getCommandBases(CommandType type) {
		return subCommands.getOrDefault(type, Arrays.asList(type.getBase()));
	}
	
	public static enum TimeType {
		SECONDS("s", "sec", "second", "seconds"),
		MINUTES("m", "min", "minute", "minutes"),
		HOURS("h", "hour", "hours"),
		DAYS("d", "day", "days");
		
		private String[] base;
		
		private TimeType(String... base) {
			this.base = base;
		}
		
		public String[] getBase() {
			return base;
		}
		
		public String getCompletion() {
			return base[base.length-1];
		}
		
		public TimeType valueOf(TimeUnit unit) {
			return TimeType.valueOf(unit.toString());
		}
	}
	
	public static enum CommandType {
		GIVE(CmdGive.class, "give"),
		GIVE_ALL(CmdGiveAll.class, "giveall"),
		RELOAD(CmdReload.class, "reload"),
		REMOVE(CmdRemove.class, "remove"),
		SET(CmdSet.class, "set"),
		TRAIL_REMOVE(CmdTrailRemove.class, "remove_trail"),
		TRAIL_SET(CmdTrailSet.class, "set_trail"),
		BYPASS(CmdBypass.class, "bypass"),
		FLY(CmdFly.class, "toggle"),
		HELP(CmdHelp.class, "help"),
		INFINITE(CmdInfinite.class, "infinite"),
		PAY(CmdPay.class, "pay"),
		SHOP(CmdShop.class, "shop"),
		SPEED(CmdSpeed.class, "speed"),
		TIME(CmdTime.class, "time"),
		TRAILS(CmdTrails.class, "trails"),
		MIGRATE(CmdMigrate.class, "migrate");
		
		private Class<? extends TempFlyCommand> clazz;
		private String base;
		
		private CommandType(Class<? extends TempFlyCommand> clazz, String base) {
			this.clazz = clazz;
			this.base = base;
		}
		
		public boolean isEnabled(TempFly tempfly) {
			switch (this) {
			case SHOP:
				return V.shop;
			default:
				return true;
			}
		}
		
		public String getBase() {
			return base;
		}
		
		public Class<? extends TempFlyCommand> getDeclaredClass() {
			return clazz;
		}
	}

}
