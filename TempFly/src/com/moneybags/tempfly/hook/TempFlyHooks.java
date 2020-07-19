package com.moneybags.tempfly.hook;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.RegisteredServiceProvider;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.factions.MassiveCraftHook;
import com.moneybags.tempfly.hook.lands.LandsHook;
import com.moneybags.tempfly.hook.skyblock.a.AskyblockHook;
import com.moneybags.tempfly.hook.skyblock.b.BskyblockHook;
import com.moneybags.tempfly.util.F;

import net.milkbowl.vault.economy.Economy;

public class TempFlyHooks {
	
	private TempFly plugin;
	private Economy eco = null;
	private WorldGuardAPI worldGuard = null;
	private Map<Hook, TempFlyHook> hooks = new HashMap();
	
	public TempFlyHooks(TempFly plugin) {
		this.plugin = plugin;
		setupEconomy();
		initializeHooks();
	}
	
    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }
	
    public Economy getEconomy() {
    	return eco;
    }
    
    public WorldGuardAPI getWorldGuard() {
    	return worldGuard;
    }
    
	private void initializeHooks() {
		worldGuard = new WorldGuardAPI();
		
		TempFlyHook hook;
		hook = new AskyblockHook(plugin);
		if (hook.isEnabled()) hooks.put(Hook.ASKYBLOCK, hook);
		hook = new BskyblockHook(plugin);
		if (hook.isEnabled()) hooks.put(Hook.BSKYBLOCK, hook);
		hook = new LandsHook(plugin);
		if (hook.isEnabled()) hooks.put(Hook.LANDS, hook);
		hook = new MassiveCraftHook(plugin);
		if (hook.isEnabled()) hooks.put(Hook.MASSIVE_CRAFT_FACTIONS, hook);
	}
	
	public TempFlyHook getHook(Hook hook) {
		return hooks.get(hook);
	}
	
	public TempFlyHook[] getEnabled() {
		return hooks.values().toArray(new TempFlyHook[hooks.size()]);
	}
	
	
	
	
	
	
	public static enum Hook {
		ASKYBLOCK,
		BSKYBLOCK,
		LANDS,
		MASSIVE_CRAFT_FACTIONS,
		
	}

}
