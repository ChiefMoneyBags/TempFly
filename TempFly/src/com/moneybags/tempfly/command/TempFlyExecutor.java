package com.moneybags.tempfly.command;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class TempFlyExecutor implements CommandExecutor, Listener {

	private CommandManager manager;
	
	public TempFlyExecutor(CommandManager manager) {
		this.manager = manager;
		manager.getTempFly().getServer().getPluginManager().registerEvents(this, manager.getTempFly());
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		TempFlyCommand command = manager.getCommand(args);
		if (command != null) {
			command.executeAs(s);
			return true;
		}
		U.m(s, V.invalidCommand);
		return true;
	}
	
	/**
	 * This handler catches the CommandPreprocessEvent and listens for command /fly.
	 * If the player has a permission listed under fly_override_permissions in the config
	 * such as essentials.fly, manager.getTempFly() will not override the flight command, so it returns.
	 * otherwise we just steal the command base without registering it and run the manager.getTempFly() executor.
	 * @param e
	 */
	//TODO test change ignoreCanlled true for AuthMe fly fix.
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void on(PlayerCommandPreprocessEvent e) {
		if (!(e.getPlayer() instanceof Player)) {
			return;
		}
		String[] ls = e.getMessage().split(" ");
		Player p = e.getPlayer();
		if (ls[0].equals("/fly")) {
			if (ls.length == 1) {
				for (String perm : V.overrideFlightPermissions) {
					if (p.hasPermission(perm)) {
						FlightUser user = manager.getTempFly().getFlightManager().getUser(p);
						user.disableFlight(-1, false);
						return;
					}
				}	
			}
			String[] args = Arrays.copyOfRange(ls, 1, ls.length);
			e.setCancelled(true);
			onCommand(e.getPlayer(), null, "", args);	
		}
	}
}
