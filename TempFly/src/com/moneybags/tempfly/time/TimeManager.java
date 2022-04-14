package com.moneybags.tempfly.time;

import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.event.FlightUserInitializedEvent;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.DailyDate;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.DataBridge;
import com.moneybags.tempfly.util.data.DataPointer;

import net.milkbowl.vault.permission.Permission;

import com.moneybags.tempfly.util.data.DataBridge.DataValue;

public class TimeManager implements Listener {

	private TempFly tempfly;
	
	public TimeManager(TempFly tempfly) {
		this.tempfly = tempfly;
		tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
	}
	
	/**
	 * Async
	 * 
	 * Get a players time.
	 * If offline The databridge will return the most recent staged change
	 * held in memory for the players time if one exists. Otherwise it will need to pull it from the
	 * database / yaml data.
	 * @param u
	 * @return
	 */
	private boolean alreadyThrown;
	public double getTime(UUID u) {
		FlightUser user = tempfly.getFlightManager().getUser(u);
		if (user == null && tempfly.getDataBridge().hasSqlEnabled() && Bukkit.getServer().isPrimaryThread() && !alreadyThrown) {
			alreadyThrown = true;
			try {throw new IllegalStateException("Invocation of getTime() for an offline player should be performed from an asychronous thread! It is not safe to access a database on the main server thread!");} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
		DataBridge bridge = tempfly.getDataBridge();
		// If user is not online the data needs pulled from the database. Otherwise get it from memory.
		return user == null ? (double) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), 0d) : user.getTime();
	}
	
	/**
	 * Set a users time.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The new seconds
	 */
	public void removeTime(UUID u, AsyncTimeParameters parameters) {
		double seconds = parameters.getAmount();
		if (seconds <= 0) {
			return;
		}
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
		double bal = user == null ? parameters.getCurrentTime() : user.getTime();
		double remaining = (((bal-seconds) >= 0) ? (bal-seconds) : 0);
		
		if (user != null) {
			user.setTime(remaining);
		} else {
			tempfly.getDataBridge().stageChange(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), remaining);
		}
	}
	
	/**
	 * Add time to a user.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The seconds to add
	 */
	public void addTime(UUID u, AsyncTimeParameters parameters) {
		double seconds = parameters.getAmount();
		if (seconds <= 0) {
			return;
		}
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
		double maxTime = parameters.getMaxTime();
		if (maxTime == -999) {
			return;
		}
		// If user is not online the data needs pulled from the database. Otherwise get it from memory.
		double bal = user == null ? parameters.getCurrentTime() : user.getTime();
		// This line prevents an overflow to -Double.MAX_VALUE.
		double remaining = (((bal+seconds) >= bal) ? (bal+seconds) : Double.MAX_VALUE);
		if (maxTime > -1 && remaining > maxTime) {
			remaining = maxTime;
		}
		
		if (user != null) {
			user.setTime(remaining);
		} else {
			tempfly.getDataBridge().stageChange(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), remaining);
		}
	}
	
	/**
	 * Set a users time.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The new seconds
	 */
	public void setTime(UUID u, AsyncTimeParameters parameters) {
		double seconds = parameters.getAmount();
		if (seconds < 0) {
			seconds = 0;
		}
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
		double maxTime = parameters.getMaxTime();
		if (maxTime == -999) {
			return;
		}
		if (maxTime > -1 && seconds > maxTime) {
			seconds = maxTime;
		}
		
		if (user != null) {
			user.setTime(seconds);
		} else {
			if (tempfly.getDataBridge().hasSqlEnabled()) {
				Console.warn("It is currently unsafe to alter player time for offline players while using MYSQL storage! The tempfly plugin currently does not sync player time between servers on a network. If this player is currently on a different server their time will not update and will be overwritten when they log off or switch servers.");
			}
			DataPointer pointer = DataPointer.of(DataValue.PLAYER_TIME, u.toString());
			tempfly.getDataBridge().stageChange(pointer, seconds);
			tempfly.getDataBridge().manualCommit(pointer);
		}
	}
	
	
	public double getMaxTime(UUID u) {
		Console.debug("-- Get max time --");
		Player p = Bukkit.getPlayer(u);
		double highest = 0;
		boolean hasGroup = false;
		if (p != null && p.isOnline()) {
			Console.debug("--| Player is online...");
			for (Entry<String, Double> group: V.maxTimeGroups.entrySet()) {
				double current = group.getValue();
				// If the group is less than the highest found so far continue.
				if (current < highest && current > -1) {
					continue;
				}
				if (p.hasPermission("tempfly.max." + group.getKey())) {
					Console.debug("--| Player has group: " + group.getKey() + " | " + group.getValue());
					hasGroup = true;
					if (current == -1) {
						return current;
					} else {
						highest = current;
					}
				}
			}
		} else {
			Console.debug("--| Player is offline...");
			if (!tempfly.getHookManager().hasPermissions()) {
				Console.debug("--|> No vault permissions, We cannot check max time!");
				// We are returning -999 to indicate something is wrong and we cannot check the players max balance.
				// In this case it is because the server does not have Vault and i can't check the offline players permissions.
				return -999;
			}
			OfflinePlayer op = Bukkit.getOfflinePlayer(u);
			Permission perms = tempfly.getHookManager().getPermissions();
			for (Entry<String, Double> group: V.maxTimeGroups.entrySet()) {
				double current = group.getValue();
				if (current < highest && current > -1) {
					continue;
				}
				if (op.isOp() || perms.playerHas(Bukkit.getWorlds().get(0).getName(), op, "tempfly.max." + group.getKey())) {
					Console.debug("--| Player has group: " + group.getKey() + " | " + group.getValue());
					hasGroup = true;
					if (current == -1) {
						return current;
					} else {
						highest = current;
					}
				}
			}
		}
		Console.debug("--|> Final value: " + (hasGroup ? highest : V.maxTimeBase));
		return hasGroup ? highest : V.maxTimeBase;
	}
	
	
	@EventHandler
	public void onUserInitialized(FlightUserInitializedEvent e) {
		Console.debug("", "--- Time manager will now process user initialization ---");
		Player p = e.getUser().getPlayer();
		if (V.timeDecay && p.hasPlayedBefore()) {
			Console.debug("--| Time decay is enabled...");
			long offline = (System.currentTimeMillis() - p.getLastPlayed()) / 1000;
			double lost = (offline / V.decayThresh) * V.decayAmount;
			double time = e.getUser().getTime();
			lost = lost > time ? time : lost;
			if (V.debug) Console.debug("--| Seconds offline: " + offline, "Threshold in seconds: " + V.decayThresh, "--| Seconds lost per threshold: " + V.decayAmount, "--| Seconds lost: " + lost, "");
			if (lost > 0) {
				new AsyncTimeParameters(tempfly, (AsyncTimeParameters parameters) -> {
					removeTime(p.getUniqueId(), parameters);
				}, p, lost).runAsync();
				U.m(p, regexString(V.timeDecayLost, lost));	
			}
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(tempfly, () -> {
			double maxTime = getMaxTime(p.getUniqueId());
			if (maxTime == -999) {
				return;
			}
			if (!p.hasPlayedBefore() && V.firstJoinTime > 0) {
				Console.debug("--| User has not played before, do first join bonus...");
				double currentTime = getTime(p.getUniqueId());
				double bonus = maxTime > -1 && ((currentTime + V.firstJoinTime) > maxTime) ? maxTime - currentTime : V.firstJoinTime;
				if (bonus > 0) {
					new AsyncTimeParameters(tempfly, (AsyncTimeParameters parameters) -> {
						addTime(p.getUniqueId(), parameters);
					}, p, bonus).run();
					U.m(p, regexString(V.firstJoin, bonus));
				}
			}
			loginBonus(p, maxTime);
		});

	}
	
	/**
	 * Run the daily login bonus on a player.
	 * @param p
	 */
	public void loginBonus(Player p, double maxTime) {
		Console.debug("--| Checking daily login bonus...");
		DataBridge bridge = tempfly.getDataBridge();
		long lastBonus = (long) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_DAILY_BONUS, p.getUniqueId().toString()), 0L);
		long sys = System.currentTimeMillis();
		
		if (new DailyDate(lastBonus).equals(new DailyDate(sys))) {
			Console.debug("--|> Same day no daily bonus :(");
			return;
		}
		double currentTime = getTime(p.getUniqueId());
		double bonus = 0;
		if (V.legacyBonus > 0) {
			Console.debug("--| Using legacy bonus...");
			bonus = maxTime > -1 && ((currentTime + V.legacyBonus) > maxTime) ? maxTime - currentTime : V.legacyBonus;
			if (bonus > 0) {
				new AsyncTimeParameters(tempfly, (AsyncTimeParameters parameters) -> {
					addTime(p.getUniqueId(), parameters);
				}, p, bonus).run();
				U.m(p, regexString(V.dailyLogin, bonus));
			}
			bridge.stageChange(DataPointer.of(DataValue.PLAYER_DAILY_BONUS, p.getUniqueId().toString()), sys);
		} else if (V.dailyBonus.size() > 0) {
			Console.debug("--| Using permission based bonus...");
			for (Entry<String, Double> entry: V.dailyBonus.entrySet()) {
				if (p.hasPermission("tempfly.bonus." + entry.getKey())) {
					bonus += entry.getValue();
				}
			}
			bonus = maxTime > -1 && ((currentTime + bonus) > maxTime) ? maxTime - currentTime : bonus;
			if (bonus > 0) {
				new AsyncTimeParameters(tempfly, (AsyncTimeParameters parameters) -> {
					addTime(p.getUniqueId(), parameters);
				}, p, bonus).run();
				U.m(p, regexString(V.dailyLogin, bonus));
			}
			bridge.stageChange(DataPointer.of(DataValue.PLAYER_DAILY_BONUS, p.getUniqueId().toString()), sys);
		}
	}
	
	public String regexString(String s, double seconds) {
		//We dont care about the decimal here, it is only used internally for relative time regions.
		long
		days = formatTime(TimeUnit.DAYS, Math.ceil(seconds)),
		hours = formatTime(TimeUnit.HOURS, Math.ceil(seconds)),
		minutes = formatTime(TimeUnit.MINUTES, Math.ceil(seconds)),
		secs = formatTime(TimeUnit.SECONDS, Math.ceil(seconds));
		
		StringBuilder sb = new StringBuilder();
		if (s.contains("{FORMATTED_TIME}")) {
			boolean addSpace = false;
			if (days > 0) {
				regexA(sb, days, V.unitDays, false);
				addSpace = true;
			} if (hours > 0) {
				regexA(sb, hours, V.unitHours, addSpace);
				addSpace = true;
			} if (minutes > 0) { 
				regexA(sb, minutes, V.unitMinutes, addSpace);
				addSpace = true;
			} if (secs > 0 || sb.length() == 0) {
				regexA(sb, secs, V.unitSeconds, addSpace);
			}
		}
		return s.replaceAll("\\{FORMATTED_TIME}", sb.toString())
				.replaceAll("\\{DAYS}", String.valueOf(days))
				.replaceAll("\\{HOURS}", String.valueOf(hours))
				.replaceAll("\\{MINUTES}", String.valueOf(minutes))
				.replaceAll("\\{SECONDS}", String.valueOf(secs));
	}
	
	private void regexA(StringBuilder sb, long quantity, String unit, boolean addSpace) {
		sb.append((addSpace ? " " : "") + V.timeFormat
				.replaceAll("\\{QUANTITY}", String.valueOf(quantity))
				.replaceAll("\\{UNIT}", unit));
	}
	
	public long formatTime(TimeUnit unit, double seconds) {
		switch (unit) {
		case DAYS:
			return (long)seconds / 86400;
		case HOURS:
			return (long)seconds % 86400 / 3600;
		case MINUTES:
			return (long)seconds % 3600 / 60;
		case SECONDS:
			return (long)seconds % 60;
		default:
			return 0;
		}
	}
	
	public String getPlaceHolder(Player p, Placeholder type) {
		double supply = getTime(p.getUniqueId());
		FlightUser user = tempfly.getFlightManager().getUser(p);
		if (user == null) {
			return "broken message";
		}
		switch (type) {
		case TIME_FORMATTED:
		{
			long
			days = formatTime(TimeUnit.DAYS, supply),
			hours = formatTime(TimeUnit.HOURS, supply),
			minutes = formatTime(TimeUnit.MINUTES, supply),
			seconds = formatTime(TimeUnit.SECONDS, supply);
			
			StringBuilder sb = new StringBuilder();
			if (user.hasInfiniteFlight()) {
				sb.append(V.infinity);
			} else {
				if (days > 0) 
					sb.append(V.fbDays.replaceAll("\\{DAYS}", String.valueOf(days)));
				if (hours > 0) 
					sb.append(V.fbHours.replaceAll("\\{HOURS}", String.valueOf(hours)));
				if (minutes > 0) 
					sb.append(V.fbMinutes.replaceAll("\\{MINUTES}", String.valueOf(minutes)));
				if (seconds > 0 || sb.length() == 0) 
					sb.append(V.fbSeconds.replaceAll("\\{SECONDS}", String.valueOf(seconds)));
			}
			return sb.toString();
		}
		case TIME_DAYS:
			long days = formatTime(TimeUnit.DAYS, supply);
			return String.valueOf(days);
		case TIME_HOURS:
			long hours = formatTime(TimeUnit.HOURS, supply);
			return String.valueOf(hours);
		case TIME_MINUTES:
			long minutes = formatTime(TimeUnit.MINUTES, supply);
			return String.valueOf(minutes);
		case TIME_SECONDS:
			long seconds = formatTime(TimeUnit.SECONDS, supply);
			return String.valueOf(seconds);
		case TIME_SECONDS_TOTAL:
			return String.valueOf((int)Math.floor(supply));
		default:
			break;
		}
		return "broken message";
	}
	
	
	public static enum Placeholder {
		TIME_FORMATTED,
		TIME_DAYS,
		TIME_HOURS,
		TIME_MINUTES,
		TIME_SECONDS,
		TIME_SECONDS_TOTAL;
	}
}
