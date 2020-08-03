package com.moneybags.tempfly.time;

import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.DailyDate;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.DataBridge;
import com.moneybags.tempfly.util.data.DataPointer;
import com.moneybags.tempfly.util.data.DataBridge.DataValue;

public class TimeManager implements Listener {

	private TempFly tempfly;
	
	public TimeManager(TempFly tempfly) {
		this.tempfly = tempfly;
		tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
	}
	
	/**
	 * Get a players time.
	 * If offline The databridge will return the most recent staged change
	 * held in memory for the players time if one exists. Otherwise it will need to pull it from the
	 * database / yaml data.
	 * @param u
	 * @return
	 */
	public double getTime(UUID u) {
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
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
	public void removeTime(UUID u, double seconds) {
		if (seconds <= 0) {
			return;
		}
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
		DataBridge bridge = tempfly.getDataBridge();
		// If user is not online the data needs pulled from the database. Otherwise get it from memory.
		double bal = user == null ? (double) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), 0d) : user.getTime();
		double remaining = (((bal-seconds) >= 0) ? (bal-seconds) : 0);
		
		if (user != null) {
			user.setTime(remaining);
		} else {
			bridge.stageChange(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), remaining);
		}
	}
	
	/**
	 * Add time to a user.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The seconds to add
	 */
	public void addTime(UUID u, double seconds) {
		if (seconds <= 0) {
			return;
		}
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
		DataBridge bridge = tempfly.getDataBridge();
		// If user is not online the data needs pulled from the database. Otherwise get it from memory.
		double bal = user == null ? (double) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), 0d) : user.getTime();
		// This line prevents an overflow to -Double.MAX_VALUE.
		double remaining = (((bal+seconds) >= bal) ? (bal+seconds) : Double.MAX_VALUE);
		if (V.maxTime > -1 && remaining > V.maxTime) {
			remaining = V.maxTime;
		}
		
		if (user != null) {
			user.setTime(remaining);
		} else {
			bridge.stageChange(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), remaining);
		}
	}
	
	/**
	 * Set a users time.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The new seconds
	 */
	public void setTime(UUID u, double seconds) {
		if (seconds <= 0) {
			return;
		}
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
		DataBridge bridge = tempfly.getDataBridge();
		if (V.maxTime > -1 && seconds > V.maxTime) {
			seconds = V.maxTime;
		}
		
		if (user != null) {
			user.setTime(seconds);
		} else {
			bridge.stageChange(DataPointer.of(DataValue.PLAYER_TIME, u.toString()), seconds);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void on(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (V.timeDecay && p.hasPlayedBefore()) {
			long offline = (System.currentTimeMillis() - p.getLastPlayed()) / 1000;
			double lost = (offline / V.decayThresh) * V.decayAmount;
			double time = getTime(p.getUniqueId());
			lost = lost > time ? time : lost;
			if (lost > 0) {
				removeTime(p.getUniqueId(), lost);
				U.m(p, regexString(V.timeDecayLost, lost));	
			}
		}
		
		if (!p.hasPlayedBefore() && V.firstJoinTime > 0) {
			double currentTime = getTime(p.getUniqueId());
			double bonus = V.maxTime > -1 && ((currentTime + V.firstJoinTime) > V.maxTime) ? V.maxTime - currentTime : V.firstJoinTime;
			if (bonus > 0) {
				addTime(p.getUniqueId(), bonus);
				U.m(p, regexString(V.firstJoin, bonus));
			}
		}
		loginBonus(p);
	}
	
	/**
	 * Run the daily login bonus on a player.
	 * @param p
	 */
	public void loginBonus(Player p) {
		DataBridge bridge = tempfly.getDataBridge();
		long lastBonus = (long) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_DAILY_BONUS, p.getUniqueId().toString()), 0L);
		long sys = System.currentTimeMillis();
		
		if (new DailyDate(lastBonus).equals(new DailyDate(sys))) {
			Console.debug("same day no daily bonus :(");
			return;
		}
		double currentTime = getTime(p.getUniqueId());
		double bonus = 0;
		if (V.legacyBonus > 0) {
			bonus = V.maxTime > -1 && ((currentTime + V.legacyBonus) > V.maxTime) ? V.maxTime - currentTime : V.legacyBonus;
			if (bonus > 0) {
				addTime(p.getUniqueId(), bonus);
				U.m(p, regexString(V.dailyLogin, bonus));
			}
			bridge.stageChange(DataPointer.of(DataValue.PLAYER_DAILY_BONUS, p.getUniqueId().toString()), sys);
		} else if (V.dailyBonus.size() > 0) {
			for (Entry<String, Double> entry: V.dailyBonus.entrySet()) {
				if (p.hasPermission("tempfly.bonus." + entry.getKey())) {
					bonus += entry.getValue();
				}
			}
			bonus = V.maxTime > -1 && ((currentTime + bonus) > V.maxTime) ? V.maxTime - currentTime : bonus;
			if (bonus > 0) {
				addTime(p.getUniqueId(), bonus);
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
		switch (type) {
		case TIME_FORMATTED:
		{
			long
			days = formatTime(TimeUnit.DAYS, supply),
			hours = formatTime(TimeUnit.HOURS, supply),
			minutes = formatTime(TimeUnit.MINUTES, supply),
			seconds = formatTime(TimeUnit.SECONDS, supply);
			
			StringBuilder sb = new StringBuilder();
			if (U.hasPermission(p, "tempfly.time.infinite")) {
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
		TIME_SECONDS;
	}
}
