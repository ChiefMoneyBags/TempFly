package com.moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.hook.TempFlyHooks.Hook;
import com.moneybags.tempfly.hook.skyblock.a.PageAskyblock;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdAskyblock {

	public CmdAskyblock(CommandSender s, String[] args) {
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		Player p = (Player) s;
		if (!TempFly.getHooks().getHook(Hook.ASKYBLOCK).isEnabled()) {
			U.m(s, V.invalidCommand);
			return;
		} else if (!p.hasPermission("tempfly.askyblock.panel")) {
			U.m(s, V.invalidPermission);
			return;
		}
		new PageAskyblock(GuiSession.newGuiSession(p));
	}
	
}
