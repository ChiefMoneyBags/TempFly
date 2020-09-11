package com.moneybags.tempfly.hook.skyblock.plugins;

import java.util.Optional;
import org.bukkit.World;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.Console;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.Addon;

public class BskyblockHook extends BentoHook {

	public static final String BSKYBLOCK = "BSkyBlock"; 
	
	
	
	public BskyblockHook(TempFly plugin) {
		super(plugin);
	}
	
	@Override
	public String getHookName() {
		return BSKYBLOCK;
	}
	
	@Override
	public String getConfigName() {
		return BSKYBLOCK;
	}
	
	@Override
	public String getDataName() {
		return BSKYBLOCK;
	}
	
	@Override
	public World getMainIslandWorld() {
		Object bsky = getBSkyblockAddon();
		if (bsky == null) {
			Console.warn("The BskyBlock hook encountered a problem. It will now disable!");
			setEnabled(false);
			return null;
		}
		Class<?> clazz = bsky.getClass();
		try {return (World) (clazz.getMethod("getOverWorld").invoke(bsky) != null ? clazz.getMethod("getOverWorld").invoke(bsky)
					: clazz.getMethod("getNetherWorld").invoke(bsky) != null ? clazz.getMethod("getNetherWorld").invoke(bsky)
							: clazz.getMethod("getEndWorld").invoke(bsky));} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Object getBSkyblockAddon() {
		Optional<Addon> addon = BentoBox.getInstance().getAddonsManager().getAddonByName("BSkyBlock");
		return addon.isPresent() ? addon.get() : null;
	}
	


}
