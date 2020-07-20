package moneybags.tempfly.aesthetic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import moneybags.tempfly.TempFly;
import moneybags.tempfly.fly.FlyHandle;
import moneybags.tempfly.fly.FlyHandle.Placeholder;


public class MvdWAPI {

	public static void initialize() {
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			
			  PlaceholderAPI.registerPlaceholder(TempFly.plugin, "tempfly_time_formatted", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return FlyHandle.getPlaceHolder(p, Placeholder.TIME_FORMATTED);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(TempFly.plugin, "tempfly_time_days", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return FlyHandle.getPlaceHolder(p, Placeholder.TIME_DAYS);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(TempFly.plugin, "tempfly_time_hours", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return FlyHandle.getPlaceHolder(p, Placeholder.TIME_HOURS);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(TempFly.plugin, "tempfly_time_minutes", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return FlyHandle.getPlaceHolder(p, Placeholder.TIME_MINUTES);
					}
					return null;
				} 
			  });
			  
			  PlaceholderAPI.registerPlaceholder(TempFly.plugin, "tempfly_time_seconds", new PlaceholderReplacer() {
				@Override
				public String onPlaceholderReplace(PlaceholderReplaceEvent e) {
					Player p = e.getPlayer();
					if (p != null){
						return FlyHandle.getPlaceHolder(p, Placeholder.TIME_SECONDS);
					}
					return null;
				} 
			  });
		}
	}
}
