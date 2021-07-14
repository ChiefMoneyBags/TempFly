package com.moneybags.tempfly.aesthetic.title;

import org.bukkit.entity.Player;

public interface Title {

    void sendTitle(Player player, Integer fadeIn, Integer stay, Integer fadeOut, String title, String subtitle);

    void clearTitle(Player player);
}
