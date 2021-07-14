package com.moneybags.tempfly.aesthetic.title;

import org.bukkit.entity.Player;

public class ModernTitle implements Title {

	@Override
    public void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle) {
    	player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

	@Override
    public void clearTitle(Player player) {
        player.sendTitle("", "", 0, 0, 0);
    }
}
