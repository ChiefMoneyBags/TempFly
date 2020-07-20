package moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.gui.GuiSession;
import moneybags.tempfly.gui.pages.PageShop;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

public class CmdShop {

	public CmdShop(CommandSender s) {
		if (!V.shop || TempFly.getInstance().getHookManager().getEconomy() == null) {
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
