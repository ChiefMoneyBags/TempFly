package com.moneybags.tempfly.aesthetic;

import com.moneybags.tempfly.aesthetic.actionbar.ActionBar;
import com.moneybags.tempfly.aesthetic.actionbar.LegacyActionBar;
import com.moneybags.tempfly.aesthetic.actionbar.ModernActionBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;

public class ActionBarAPI {
	
	private static ActionBar actionBar;
    
    public static void initialize(TempFly tempfly) {
        // Check if we're on 1.12 or above
    	  if (Bukkit.getServer().getVersion().matches(".*1\\.(?!10|11)\\d{2,}.*")) {
    	      actionBar = new ModernActionBar(tempfly);
        } else {
    	      actionBar = new LegacyActionBar(tempfly);
    	}
    }
    
    public static void sendActionBar(final Player player, final String message) {
    	actionBar.sendActionBar(player, message);
    }
    
    public static void sendActionBar(final Player player, final String message, int duration) {
    	actionBar.sendActionBar(player, message, duration);
    }
}
