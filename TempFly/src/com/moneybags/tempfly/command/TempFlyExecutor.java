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

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.command.admin.CmdGive;
import com.moneybags.tempfly.command.admin.CmdGiveAll;
import com.moneybags.tempfly.command.admin.CmdReload;
import com.moneybags.tempfly.command.admin.CmdRemove;
import com.moneybags.tempfly.command.admin.CmdSet;
import com.moneybags.tempfly.command.admin.CmdTrailRemove;
import com.moneybags.tempfly.command.player.CmdFly;
import com.moneybags.tempfly.command.player.CmdHelp;
import com.moneybags.tempfly.command.player.CmdPay;
import com.moneybags.tempfly.command.player.CmdShop;
import com.moneybags.tempfly.command.player.CmdSpeed;
import com.moneybags.tempfly.command.player.CmdTime;
import com.moneybags.tempfly.command.player.CmdTrails;
import com.moneybags.tempfly.hook.skyblock.CmdIslandSettings;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.V;

public class TempFlyExecutor implements CommandExecutor, Listener {

	private TempFly tempfly;
	
	public TempFlyExecutor(TempFly tempfly) {
		this.tempfly = tempfly;
		tempfly.getServer().getPluginManager().registerEvents(this, tempfly);
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			new CmdFly(tempfly, s, args);
			return true;
		} else {
			base:
			switch (args[0]) {
			case "help":
			case "commands":
				new CmdHelp(s, args);
				break;
			case "toggle":
			case "on":
			case "enable":
			case "off":
			case "disable":
				new CmdFly(tempfly, s, args);
				break;
			case "give":
			case "add":
				new CmdGive(tempfly, s, args);
				break;
			case "giveall":
				new CmdGiveAll(tempfly, s, args);
				break;
			case "remove":
			case "take":
				new CmdRemove(tempfly, s, args);
				break;
			case "pay":
			case "send":
				new CmdPay(tempfly, s, args);
				break;
			case "time":
			case "info":
			case "remaining":
			case "balance":
			case "bal":
			case "seconds":
				new CmdTime(tempfly, s, args);
				break;
			case "set":
				new CmdSet(tempfly, s, args);
				break;
			case "speed":
			case "momentum":
				new CmdSpeed(tempfly, s, args);
				break;
			case "trail":
				if (args.length > 1) {
					switch (args[1]) {
					case "remove":
					case "delete":
						new CmdTrailRemove(s, args);
						break base;
					}
				}
			case "trails":
			case "particle":
				new CmdTrails(s);
				break;
			case "shop":
			case "buy":
			case "purchase":
				new CmdShop(tempfly, s);
				break;
			case "reload":
				new CmdReload(tempfly, s);
				break;
			case "island":
			case "settings":
			case "skyblock":
			case "sb":
			case "allow":
				new CmdIslandSettings(s, args);
				break;
			default:
				new CmdHelp(s, args);
				break;
			}
		}
		return true;
	}
	
	/**
	 * This handler catches the CommandPreprocessEvent and listens for command /fly.
	 * If the player has a permission listed under fly_override_permissions in the config
	 * such as essentials.fly, tempfly will not override the flight command, so it returns.
	 * otherwise we just steal the command base without registering it and run the tempfly executor.
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
					FlightUser user = tempfly.getFlightManager().getUser(p);
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
