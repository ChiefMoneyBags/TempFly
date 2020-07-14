package com.moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.pages.PageTrails;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdTrails {

	public CmdTrails(CommandSender s) {
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		if (!U.hasPermission(s, "tempfly.trails")) {
			U.m(s, V.invalidPermission);
			return;
		}
		new PageTrails(GuiSession.newGuiSession((Player)s), 0, true);
	}
}
