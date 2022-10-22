package com.moneybags.tempfly.aesthetic.actionbar;

import com.moneybags.tempfly.TempFly;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class ActionBar {
  protected final TempFly tempfly;
  
  public ActionBar(TempFly tempfly) {
    this.tempfly = tempfly;
  }
  
  public abstract void sendActionBar(final Player player, final String message);

  public void sendActionBar(final Player player, final String message, int duration) {
    sendActionBar(player, message);

    if (duration >= 0) {
      new BukkitRunnable() {
        @Override
        public void run() {
          sendActionBar(player, "");
        }
      }.runTaskLater(tempfly, duration + 1);
    }

    while (duration > 40) {
      duration -= 40;
      new BukkitRunnable() {
        @Override
        public void run() {
          sendActionBar(player, message);
        }
      }.runTaskLater(tempfly, (long) duration);
    }
  }
}
