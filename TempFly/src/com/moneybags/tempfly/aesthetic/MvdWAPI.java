package com.moneybags.tempfly.aesthetic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.time.TimeManager.Placeholder;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;


public class MvdWAPI {

	public static void initialize(TempFly tempfly) {
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_time_formatted", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_FORMATTED);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_time_days", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_DAYS);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_time_hours", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_HOURS);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_time_minutes", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_MINUTES);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_time_seconds", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_SECONDS);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_time_seconds_total", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return tempfly.getTimeManager().getPlaceHolder(p, Placeholder.TIME_SECONDS_TOTAL);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_list_name", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null && p.isOnline()) {
						return tempfly.getFlightManager().getUser(p).getListPlaceholder();
					}
					return null;
				} 
			  });
			  PlaceholderAPI.registerPlaceholder(tempfly, "tempfly_name_tag", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null && p.isOnline()){
						return tempfly.getFlightManager().getUser(p).getTagPlaceholder();
					}
					return null;
				} 
			  });
		}
	}
}
