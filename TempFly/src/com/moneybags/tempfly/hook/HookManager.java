package com.moneybags.tempfly.hook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.plugin.RegisteredServiceProvider;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.factions.plugins.FactionsUUIDHook;
import com.moneybags.tempfly.hook.region.RegionProvider;
import com.moneybags.tempfly.hook.region.plugins.WorldGuardHook;
import com.moneybags.tempfly.hook.skyblock.plugins.AskyblockHook;
import com.moneybags.tempfly.hook.skyblock.plugins.BskyblockHook;
import com.moneybags.tempfly.hook.skyblock.plugins.IridiumHook;
import com.moneybags.tempfly.hook.skyblock.plugins.SuperiorHook;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.Reloadable;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class HookManager implements Reloadable {
	
	public static final Class<?>[] REGIONS = new Class<?>[] {WorldGuardHook.class};
	
	private TempFly plugin;
	private Economy eco = null;
	private Permission perms = null;
	private RegionProvider regions;
	
	private Map<Genre, List<TempFlyHook>> hooks = new HashMap<>();
	
	public HookManager(TempFly plugin) {
		this.plugin = plugin;
		
		loadRegionProvider();
		if (setupEconomy()) {
			setupPermissions();
		}
	}
	
	public boolean registerHook(TempFlyHook hook) throws IllegalArgumentException  {
		if (getHook(hook.getClass()) != null) {
			throw new IllegalArgumentException("You may only register a hook once within tempfly!");
		}
		List<TempFlyHook> loaded = hooks.getOrDefault(hook.getGenre(), new ArrayList<>());
		loaded.add(hook);
		hooks.put(hook.getGenre(), loaded);
		plugin.getFlightManager().registerRequirementProvider(hook);
		return true;
	}
	
	public void unregisterHook(TempFlyHook hook) {
		if (getHook(hook.getClass()) == null) {
			return;
		}
		List<TempFlyHook> loaded = hooks.get(hook.getGenre());
		loaded.remove(hook);
		plugin.getFlightManager().unregisterRequirementProvider(hook);
		if (loaded.size() < 1) {
			hooks.remove(hook.getGenre());
			return;
		}
		hooks.put(hook.getGenre(), loaded);
	}
	
	
	
	/**
	 *
	 * Initialization
	 * 
	 */
	
	private void loadRegionProvider() {
		RegionProvider hook;
		for (Class<?> clazz: REGIONS) {
			try {
				hook = (RegionProvider) clazz.getConstructor(TempFly.class).newInstance(plugin);
				if (hook.isEnabled()) {
					regions = hook;
					break;
				}
			} catch (Exception e) {e.printStackTrace();}
		}
	}
	
	/**
	 * Manually set tempfly's region provider.
	 * @param provider The regbion provider
	 */
	public void setRegionProvider(RegionProvider provider) {
		this.regions = provider;
	}
	
    public boolean hasRegionProvider() {
    	return !V.disableTracker && regions != null && regions.isEnabled();
    }
    
    public RegionProvider getRegionProvider() {
    	return regions;
    }
    
    public boolean hasPermissions() {
    	return perms != null;
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
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
	
	public void loadInternalGenres() {
		Console.debug("", "----------Loading Genre Hooks----------");
		TempFlyHook hook;
		for (Genre genre: Genre.values()) {
			Console.debug("", "--< Loading: " + genre.toString());
			for (Class<?> clazz: genre.getInternalClasses()) {
				Console.debug("", "----< Class: " + clazz.getName());
				try {
					hook = (TempFlyHook) clazz.getConstructor(TempFly.class).newInstance(plugin);
					Console.debug("----< Enabled: " + hook.isEnabled());
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Error e) {
					if (V.debug) {
						e.printStackTrace();
					}
				}
			}
		}
		Console.debug("--------Loading Genre Hooks End--------", "");
	}
	
	/**
	 *
	 * Getters
	 * 
	 */
	
    public Economy getEconomy() {
    	return eco;
    }
    
    public Permission getPermissions() {
    	return perms;
    }
	
	public TempFlyHook getHook(String plugin) {
		for (List<TempFlyHook> list: hooks.values()) {
			for (TempFlyHook hook: list) {
				if (hook.getPluginName().equals(plugin)) {
					return hook;
				}
			}
		}
		return null;
	}
	
	public TempFlyHook getHook(Class<? extends TempFlyHook> clazz) {
		for (List<TempFlyHook> list: hooks.values()) {
			for (TempFlyHook hook: list) {
				if (hook.getClass().equals(clazz)) {
					return hook;
				}
			}
		}
		return null;
	}
	
	public TempFlyHook[] getGenre(Genre genre) {
		List<TempFlyHook> list;
		return (list = hooks.getOrDefault(genre, new ArrayList<>())).toArray(new TempFlyHook[list.size()]);
	}
	
	public TempFlyHook[] getEnabled() {
		List<TempFlyHook> enabled = new ArrayList<>();
		for (List<TempFlyHook> genre: hooks.values()) {
			for (TempFlyHook hook: genre) {
				if (hook.isEnabled()) {
					enabled.add(hook);
				}
			}
		}
		return enabled.toArray(new TempFlyHook[enabled.size()]);
	}
	
	
	
	
	/*
	 * Represents the GameMode type of a hook  
	 */
	public static enum Genre {
		SKYBLOCK("SkyBlock", AskyblockHook.class, IridiumHook.class, BskyblockHook.class, SuperiorHook.class),
		LANDS("Lands"),
		FACTIONS("Factions", FactionsUUIDHook.class),
		OTHER("Other");
		
		private String folder;
		private final Class<?>[] classes; 
		
		private Genre(String folder, Class<?>... classes) {
			this.folder = folder;
			this.classes = classes;
		}
		
		public String getDirectory() {
			return "hooks" + File.separator + folder;
		}
		
		/**
		 * 
		 * @return An array of built in TempFly classes that represent this genre. You can still add your own that
		 * aren't on this list, i just use it internally for ease of access.
		 */
		public Class<?>[] getInternalClasses() {
			return classes;
		}
	}

	@Override
	public void onTempflyReload() {
		for (Entry<Genre, List<TempFlyHook>> entry: hooks.entrySet()) {
			for (TempFlyHook hook: entry.getValue()) {
				Console.debug("Preparing to reload hook: " + hook.getHookName());
				((Reloadable)hook).onTempflyReload();
			}
		}
		
	}

}
