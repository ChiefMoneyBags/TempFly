package moneybags.tempfly.command.player;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import moneybags.tempfly.gui.GuiSession;
import moneybags.tempfly.hook.skyblock.PageIslandSettings;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;

public class CmdIslandSettings {

	public CmdIslandSettings(CommandSender s, String[] args) {
		if (!U.isPlayer(s)) {
			U.m(s, V.invalidSender);
			return;
		}
		Player p = (Player) s;
		if (!p.hasPermission("tempfly.skyblock.island.settings")) {
			U.m(s, V.invalidPermission);
			return;
		}
		new PageIslandSettings(GuiSession.newGuiSession(p));
	}
	
}
