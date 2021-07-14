package com.moneybags.tempfly.aesthetic.actionbar;

import com.moneybags.tempfly.TempFly;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class ModernActionBar extends ActionBar {
	
	  public ModernActionBar(TempFly tempfly) {
	      super(tempfly);
    }

    public void sendActionBar(final Player player, final String message) {
        if (!player.isOnline()) {
        	  return;
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }
}
