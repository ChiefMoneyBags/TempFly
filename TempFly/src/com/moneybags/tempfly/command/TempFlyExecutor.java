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
import org.bukkit.event.server.TabCompleteEvent;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.admin.CmdGive;
import com.moneybags.tempfly.command.admin.CmdGiveAll;
import com.moneybags.tempfly.command.admin.CmdReload;
import com.moneybags.tempfly.command.admin.CmdRemove;
import com.moneybags.tempfly.command.admin.CmdSet;
import com.moneybags.tempfly.command.admin.CmdTrailRemove;
import com.moneybags.tempfly.command.player.CmdBypass;
import com.moneybags.tempfly.command.player.CmdFly;
import com.moneybags.tempfly.command.player.CmdHelp;
import com.moneybags.tempfly.command.player.CmdInfinite;
import com.moneybags.tempfly.command.player.CmdPay;
import com.moneybags.tempfly.command.player.CmdShop;
import com.moneybags.tempfly.command.player.CmdSpeed;
import com.moneybags.tempfly.command.player.CmdTime;
import com.moneybags.tempfly.command.player.CmdTrails;
import com.moneybags.tempfly.hook.skyblock.CmdIslandSettings;
import com.moneybags.tempfly.user.FlightUser;
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
		}
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
			for (String perm : V.overrideFlightPermissions) {
				if (p.hasPermission(perm)) {
					FlightUser user = manager.getTempFly().getFlightManager().getUser(p);
					user.disableFlight(-1, false);
					return;
				}
			}
			String[] args = Arrays.copyOfRange(ls, 1, ls.length);
			e.setCancelled(true);
			onCommand(e.getPlayer(), null, "", args);	
		}
	}
}
