package com.moneybags.tempfly.command.admin;

import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.pages.PageShop;
import com.moneybags.tempfly.gui.pages.PageTrails;
import com.moneybags.tempfly.util.F;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

public class CmdReload {

	public CmdReload(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.reload")) {
			U.m(s, V.invalidPermission);
			return;
		}
		U.m(s, V.reload);
		F.createFiles(TempFly.plugin);
		V.loadValues();
		GuiSession.endAllSessions();
		PageTrails.initialize();
		PageShop.initialize();
	}
}
