package com.moneybags.tempfly.command.player;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.event.FlightEnabledEvent;
import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;
import com.moneybags.tempfly.time.TimeHandle;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdFly {

	public CmdFly(CommandSender s, String[] args) {
		if (args.length == 1) {
			U.m(s, U.cc("/tf toggle [player]"));
			return;
		}
		
		Player p = null;
		if (args.length > 1) {
			if (!U.hasPermission(s, "tempfly.toggle.other")) {
				U.m(s, V.invalidPermission);
				return;
			}
			p = Bukkit.getPlayerExact(args[1]);
			if (p == null) {
				U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[2]));
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
			p = (Player)s;
		}
		
		Flyer f = FlyHandle.getFlyer(p);
		if (f == null) {
			double time = TimeHandle.getTime(p.getUniqueId());
			if ((time <= 0) && (!p.hasPermission("tempfly.time.infinite"))) {
				if (args.length > 0) {
					U.m(s, V.invalidTimeOther.replaceAll("\\{PLAYER}", p.getName()));	
				} else {
					U.m(s, V.invalidTimeSelf);
				}
				return;
			}
			if (!FlyHandle.flyAllowed(p.getLocation())) {
				if (args.length > 0) {
					U.m(s, V.invalidZoneOther.replaceAll("\\{PLAYER}", p.getName()));
					return;
				} else {
					U.m(s, V.invalidZoneSelf);
					return;
				}	
			}
			if ((FlyHandle.onCooldown(p)) && (args.length < 1)) {
				U.m(s, V.flyCooldownDeny);
				return;
			}
			FlightEnabledEvent e = new FlightEnabledEvent(p);
			Bukkit.getPluginManager().callEvent(e);
			if (e.isCancelled()) {
				return;
			}
			FlyHandle.removeDamageProtction(p.getUniqueId());
			FlyHandle.addFlyer(p);
			U.m(p, V.flyEnabledSelf);
			if (args.length > 0) {
				U.m(s, V.flyEnabledOther.replaceAll("\\{PLAYER}", p.getName()));	
			}
		} else {
			if (!V.protCommand) {
				FlyHandle.addDamageProtection(p);	
			}
			FlyHandle.removeFlyer(p);
			U.m(p, V.flyDisabledSelf);
			if (args.length > 0) {
				U.m(s, V.flyDisabledOther.replaceAll("\\{PLAYER}", p.getName()));	
			}
		}
	}
}
