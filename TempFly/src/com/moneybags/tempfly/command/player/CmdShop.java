package com.moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.pages.PageShop;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdShop {

	public CmdShop(CommandSender s) {
		if (!V.shop || TempFly.eco == null) {
			U.m(s, V.invalidCommand);
			return;
		}
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		if (!U.hasPermission(s, "tempfly.shop")) {
			U.m(s, V.invalidPermission);
			return;
		}
		new PageShop(GuiSession.newGuiSession((Player)s), 0);
	}
}
