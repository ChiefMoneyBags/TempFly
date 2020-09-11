package com.moneybags.tempfly.aesthetic;

import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.time.TimeManager.Placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class ClipAPI {
	
	private static TempFly tempfly;
	private static PlaceHolders instance;
	
	public static void initialize(TempFly plugin) {
		tempfly = plugin;
		if (instance == null) {
			instance = new PlaceHolders();
			instance.register();
		}
	}
	
	public static class PlaceHolders extends PlaceholderExpansion {
		
		@Override
		public boolean persist() {
			return true;
		}
		
	    @Override
	    public boolean canRegister(){
	        return true;
	    }
		
		@Override
		public String getAuthor() {
			return "ChiefMoneyBags";
		}

		@Override
		public String getIdentifier() {
			return "tempfly";
		}

		@Override
		public String getVersion() {
			return tempfly.getDescription().getVersion();
		}
		
		@Override
		public String onPlaceholderRequest(Player p, String identifier) {
			if (p == null) {
				return null;
			}
			switch (identifier) {
			case "time-formatted":
				return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_FORMATTED);
			case "time-days":
				return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_DAYS);
			case "time-hours":
				return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_HOURS);
			case "time-minutes":
				return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_MINUTES);
			case "time-seconds":
				return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_SECONDS);
			case "time-seconds-total":
				return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_SECONDS_TOTAL);
			case "list-name":
				return p.isOnline() ? tempfly.getFlightManager().getUser(p).getListPlaceholder() : "";
			case "name-tag":
				return p.isOnline() ? tempfly.getFlightManager().getUser(p).getTagPlaceholder() : "";
			default:
				return "Invalid placeholder.. :( ";
			}
		}
	}
}
