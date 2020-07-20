package moneybags.tempfly.time;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.fly.FlyHandle;
import moneybags.tempfly.fly.Flyer;
import moneybags.tempfly.util.V;
import moneybags.tempfly.util.data.DataBridge;
import moneybags.tempfly.util.data.DataBridge.DataValue;

public class TimeHandle {

	public static double getTime(UUID u) {
		Flyer f = FlyHandle.getFlyer(Bukkit.getPlayer(u));
		if (f != null) {
			return f.getTime();
		}
		return (double) TempFly.getInstance().getDataBridge().getOrDefault(DataValue.PLAYER_TIME, 0d, new String[] {u.toString()});
	}
	
	public static void removeTime(UUID u, double seconds) {
		Flyer f = FlyHandle.getFlyer(Bukkit.getPlayer(u));
		DataBridge bridge = TempFly.getInstance().getDataBridge();
		double bal = f == null ? (double) bridge.getOrDefault(DataValue.PLAYER_TIME, 0d, new String[] {u.toString()}) : f.getTime();
		double remaining = (((bal-seconds) >= 0) ? (bal-seconds) : 0);
		
		if (f != null) {
			f.setTime(remaining);
			bridge.stageChange(DataValue.PLAYER_TIME, remaining, new String[] {u.toString()});
		} else {
			bridge.stageAndCommit(DataValue.PLAYER_TIME, remaining, new String[] {u.toString()});
		}
	}
	
	public static void addTime(UUID u, double seconds) {
		Flyer f = FlyHandle.getFlyer(Bukkit.getPlayer(u));
		DataBridge bridge = TempFly.getInstance().getDataBridge();
		double bal = f == null ? (double) bridge.getOrDefault(DataValue.PLAYER_TIME, 0d, new String[] {u.toString()}) : f.getTime();
		// This line prevents an overflow to -Double.MAX_VALUE.
		double remaining = (((bal+seconds) >= bal) ? (bal+seconds) : Double.MAX_VALUE);
		
		
		if (f != null) {
			f.setTime(remaining);
			bridge.stageChange(DataValue.PLAYER_TIME, remaining, new String[] {u.toString()});
		} else {
			bridge.stageAndCommit(DataValue.PLAYER_TIME, remaining, new String[] {u.toString()});
		}
	}
	
	public static void setTime(UUID u, double seconds) {
		Flyer f = FlyHandle.getFlyer(Bukkit.getPlayer(u));
		
		if (f != null) {
			f.setTime(seconds);
			TempFly.getInstance().getDataBridge().stageChange(DataValue.PLAYER_TIME, seconds, new String[] {u.toString()});
		} else {
			TempFly.getInstance().getDataBridge().stageAndCommit(DataValue.PLAYER_TIME, seconds, new String[] {u.toString()});
		}
	}
	
	// Absolute trash
	public static String regexString(String s, double seconds) {
		long days = formatTime(TimeUnit.DAYS, Math.ceil(seconds));
		long hours = formatTime(TimeUnit.HOURS, Math.ceil(seconds));
		long minutes = formatTime(TimeUnit.MINUTES, Math.ceil(seconds));
		long secs = formatTime(TimeUnit.SECONDS, Math.ceil(seconds));
		
		String fin = "";
		if (days > 0) fin = fin.concat(V.timeFormat
				.replaceAll("\\{QUANTITY}", String.valueOf(days))
				.replaceAll("\\{UNIT}", V.unitDays) + " ");
		if (hours > 0) fin = fin.concat(V.timeFormat
				.replaceAll("\\{QUANTITY}", String.valueOf(hours))
				.replaceAll("\\{UNIT}", V.unitHours) + " ");
		if (minutes > 0) fin = fin.concat(V.timeFormat
				.replaceAll("\\{QUANTITY}", String.valueOf(minutes))
				.replaceAll("\\{UNIT}", V.unitMinutes) + " ");
		if (secs > 0 || fin.length() == 0) fin = fin.concat(V.timeFormat
				.replaceAll("\\{QUANTITY}", String.valueOf(secs))
				.replaceAll("\\{UNIT}", V.unitSeconds) + " ");
		if ((fin.length() > 0) && (String.valueOf(fin.charAt(fin.length() - 1)).equals(" "))) {
			fin = fin.substring(0, fin.length()-1);
		}
		return s.replaceAll("\\{FORMATTED_TIME}", fin)
				.replaceAll("\\{DAYS}", String.valueOf(formatTime(TimeUnit.DAYS, seconds)))
				.replaceAll("\\{HOURS}", String.valueOf(formatTime(TimeUnit.HOURS, seconds)))
				.replaceAll("\\{MINUTES}", String.valueOf(formatTime(TimeUnit.MINUTES, seconds)))
				.replaceAll("\\{SECONDS}", String.valueOf(formatTime(TimeUnit.SECONDS, seconds)));
	}
	
	public static long formatTime(TimeUnit unit, double seconds) {
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
}
