package moneybags.tempfly.command.admin;

import org.bukkit.command.CommandSender;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.gui.GuiSession;
import moneybags.tempfly.gui.pages.PageShop;
import moneybags.tempfly.gui.pages.PageTrails;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;
import moneybags.tempfly.util.data.Files;

public class CmdReload {

	public CmdReload(CommandSender s) {
		if (!U.hasPermission(s, "tempfly.reload")) {
			U.m(s, V.invalidPermission);
			return;
		}
		TempFly.getInstance().reloadTempfly();
		U.m(s, V.reload);
	}
}
