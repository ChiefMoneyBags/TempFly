package com.moneybags.tempfly.command.admin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.aesthetic.particle.Particles;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdTrailRemove {

	
	// Invoked from command
	public CmdTrailRemove(CommandSender s, String[] args) {
		Player target = null;
		if (args.length == 2
				|| ((args.length > 2 && (target = Bukkit.getPlayerExact(args[3])) != null && target == s))) {
			if (!(s instanceof Player)) {
				U.m(s, V.invalidSender);
				return;
			}
			target = (Player) s;
			if (!target.hasPermission("tempfly.trails.remove.self")) {
				U.m(target, V.invalidPermission);
				return;
			}
			removeTrail(target);
			return;
		}
		if (!U.hasPermission(s, "tempfly.trails.remove.other")) {
			U.m(s, V.invalidPermission);
			return;
		}
		if (args.length < 3) {
			U.m(s, U.cc("&c/tf trail remove [player]"));
			return;
		}
		
		if (target == null) {
			U.m(s, V.invalidPlayer.replaceAll("\\{PLAYER}", args[3]));
			return;
		}
		removeTrail(target);
		if (s != target) {
			U.m(s, V.trailRemovedOther.replaceAll("\\{PLAYER}", target.getName()));
		}
	}
	
	// Invoked from the gui
	public CmdTrailRemove(Player target) {
		removeTrail(target);
	}

	private void removeTrail(Player target) {
		Particles.setTrail(target.getUniqueId(), "");
		U.m(target, V.trailRemovedSelf);
	}
	
}
