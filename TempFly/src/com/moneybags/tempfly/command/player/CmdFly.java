package com.moneybags.tempfly.command.player;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.event.FlightEnabledEvent;
import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;
import com.moneybags.tempfly.hook.FlightResult;
import com.moneybags.tempfly.time.TimeHandle;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdFly {

	public CmdFly(CommandSender s, String[] args) {
		Player p = null;
		boolean
			toggle = false,
			toggleVal = false;
		
		
		
		if (args.length > 1) {
			
			p = Bukkit.getPlayer(args[1]);
			if (p != null) {
				if (!U.hasPermission(s, "tempfly.toggle.other")) {
					U.m(s, V.invalidPermission);
					return;
				}
				
				if (args.length > 2) {
					toggle = true;
					if (args[2].equalsIgnoreCase("on")) {
						toggleVal = true;
					} else if (args[2].equalsIgnoreCase("off")) {
						toggleVal = false;
					} else {
						U.m(s, U.cc("&c/tf toggle [player] [on / off]"));
						return;
					}
				}
				
			} else if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("off")) {
				if (!U.isPlayer(s)) {
					U.m(s, V.invalidSender);
					return;
				}
				if (!U.hasPermission(s, "tempfly.toggle.self")) {
					U.m(s, V.invalidPermission);
					return;
				}
				
				toggle = true;
				if (args[1].equalsIgnoreCase("on")) {
					toggleVal = true;
				} else if (args[1].equalsIgnoreCase("off")) {
					toggleVal = false;
				}
				
			} else if (args.length > 2) {
				U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[1]));
				return;
			} else {
				U.m(s, U.cc("&c/tf toggle [on / off]"));
				return;
			}
		
			
		} else {
			if (!U.isPlayer(s)) {
				U.m(s, V.invalidSender);
				return;
			}
			if (!U.hasPermission(s, "tempfly.toggle.self")) {
				U.m(s, V.invalidPermission);
				return;
			}
		}
		if (p == null) {
			p = (Player)s;
		}
		
		
		Flyer f = FlyHandle.getFlyer(p);                 //lmao what ?? TODO
		if (toggle && toggleVal || !toggle && !toggleVal && f == null) {
			if (f != null) { //<----
				return;
			}
			if ((FlyHandle.onCooldown(p)) && (args.length < 1)) {
				U.m(s, V.flyCooldownDeny);
				return;
			}
			
			/*
			 * Time check 
			 */
			double time = TimeHandle.getTime(p.getUniqueId());
			if ((time <= 0) && (!p.hasPermission("tempfly.time.infinite"))) {
				if (!s.equals(p)) {
					U.m(s, V.invalidTimeOther.replaceAll("\\{PLAYER}", p.getName()));	
				} else {
					U.m(s, V.invalidTimeSelf);
				}
				return;
			}
			
			/*
			 * Region, world and requirements check 
			 */
			FlightResult result = FlyHandle.inquireFlight(p, p.getLocation(), true);
			if (!result.isAllowed()) {
				if (!s.equals(p)) {
					U.m(s, V.invalidZoneOther.replaceAll("\\{PLAYER}", p.getName()));
					return;
				} else {
					U.m(s, result.getMessage());
					return;
				}	
			}
			
			/*
			 * Event check 
			 */
			FlightEnabledEvent e = new FlightEnabledEvent(p);
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				return;
			}
			
			
			FlyHandle.removeDamageProtction(p);
			FlyHandle.addFlyer(p);
			U.m(p, V.flyEnabledSelf);
			if (!s.equals(p)) {
				U.m(s, V.flyEnabledOther.replaceAll("\\{PLAYER}", p.getName()));	
			}
		} else if (toggle && !toggleVal && f != null || !toggle && f != null) {
			if (!V.protCommand) {
				FlyHandle.addDamageProtection(p);	
			}
			FlyHandle.removeFlyer(f);
			if (!s.equals(p)) {
				U.m(s, V.flyDisabledOther.replaceAll("\\{PLAYER}", p.getName()));	
			}
		}
	}
}
