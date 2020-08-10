package com.moneybags.tempfly.hook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.plugin.RegisteredServiceProvider;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.region.RegionProvider;
import com.moneybags.tempfly.hook.region.plugins.WorldGuardHook;
import com.moneybags.tempfly.hook.skyblock.plugins.AskyblockHook;
import com.moneybags.tempfly.hook.skyblock.plugins.BentoHook;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.data.Reloadable;

import net.milkbowl.vault.economy.Economy;

public class HookManager implements Reloadable {
	
	public static final Class<?>[] REGIONS = new Class<?>[] {WorldGuardHook.class};
	
	private TempFly plugin;
	private Economy eco;
	private RegionProvider regions;
	private Map<Genre, Map<HookType, TempFlyHook>> hooks = new HashMap<>();
	
	public HookManager(TempFly plugin) {
		this.plugin = plugin;
		
		loadRegionProvider();
		setupEconomy();
		loadGenres();
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
    	return regions != null && regions.isEnabled();
    }
    
    public RegionProvider getRegionProvider() {
    	return regions;
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
    
	
	private void loadGenres() {
		Console.debug("", "----------Loading Genre Hooks----------");
		TempFlyHook hook;
		for (Genre genre: Genre.values()) {
			Console.debug("", "--< Loading: " + genre.toString());
			Map<HookType, TempFlyHook> loaded = new HashMap<>();
			for (Class<?> clazz: genre.getClasses()) {
				Console.debug("", "----< Class: " + clazz.getName());
				try {
					hook = (TempFlyHook) clazz.getConstructor(TempFly.class).newInstance(plugin);
					Console.debug("----< Enabled: " + hook.isEnabled());
					if (hook.isEnabled()) {
						loaded.put(hook.getHookType(), hook);
						if (genre.isSolitary()) {
							Console.debug("----< Genre is solitary, breaking: " + genre);
							break;
						}
					}
				} catch (Exception e) {e.printStackTrace();}
			}
			if (loaded.size() > 0) {
				hooks.put(genre, loaded);
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
	
	public TempFlyHook getHook(HookType hook) {
		return hooks.getOrDefault(hook.getGenre(), new HashMap<>()).getOrDefault(hook, null);
	}
	
	public TempFlyHook[] getGenre(Genre genre) {
		Map<HookType, TempFlyHook> map;
		return (map = hooks.getOrDefault(genre, new HashMap<>())).values().toArray(new TempFlyHook[map.size()]);
	}
	
	public TempFlyHook[] getEnabled() {
		List<TempFlyHook> enabled = new ArrayList<>();
		for (Map<HookType, TempFlyHook> genre: hooks.values()) {
			for (TempFlyHook hook: genre.values()) {
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
		SKYBLOCK("SkyBlock", true, AskyblockHook.class, BentoHook.class),
		LANDS("Lands", true),
		FACTIONS("Factions", true);
		
		private String folder;
		private boolean solitary;
		private final Class<?>[] classes; 
		
		private Genre(String folder, boolean solitary, Class<?>... classes) {
			this.folder = folder;
			this.solitary = solitary;
			this.classes = classes;
		}
		
		public String getDirectory() {
			return "hooks" + File.separator + folder;
		}
		
		public boolean isSolitary() {
			return solitary;
		}
		
		public Class<?>[] getClasses() {
			return classes;
		}
	}
	
	/*
	 * Represents the target plugin of a hook. 
	 */
	public static enum HookType {
		ASKYBLOCK(
				Genre.SKYBLOCK,
				"ASkyBlock", "ASkyBlock", "skyblock_config"),
		BENTO_BOX(
				Genre.SKYBLOCK,
				"BentoBox", "BentoBox", "skyblock_config"),
		SUPERIOR_SKYBLOCK_2(
				Genre.SKYBLOCK,
				"???????", "SuperiorSkyblock", "skyblock_config");
		
		private Genre genre;
		private String plugin, config, embedded;
		
		private HookType(Genre genre, String plugin, String config, String embedded) {
			this.genre = genre;
			this.plugin = plugin;
			this.config = config;
			this.embedded = embedded;
		}
		
		public Genre getGenre() {
			return genre;
		}
		
		public String getPluginName() {
			return plugin;
		}
		
		public String getConfigName() {
			return config;
		}
		
		public String getEmbeddedConfigName() {
			return embedded;
		}
	}

	@Override
	public void onTempflyReload() {
		for (Entry<Genre, Map<HookType, TempFlyHook>> entry: hooks.entrySet()) {
			for (TempFlyHook hook: entry.getValue().values()) {
				((Reloadable)hook).onTempflyReload();
			}
		}
		
	}

}
