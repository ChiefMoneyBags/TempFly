package moneybags.tempfly.hook;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.RegisteredServiceProvider;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.hook.skyblock.plugins.AskyblockHook;
import moneybags.tempfly.hook.skyblock.plugins.BentoHook;
import net.milkbowl.vault.economy.Economy;

public class HookManager {
	
	public static final Class<?>[] SKYBLOCK = new Class<?>[] {AskyblockHook.class, BentoHook.class};
	
	private TempFly plugin;
	private Economy eco = null;
	private WorldGuardAPI worldGuard = null;
	private Map<Genre, Map<HookType, TempFlyHook>> hooks = new HashMap<>();
	
	public HookManager(TempFly plugin) {
		this.plugin = plugin;
		setupEconomy();
		initializeHooks();
	}
	
	/**
	 *
	 * Initialization
	 * 
	 */
	
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
    
	private void initializeHooks() {
		worldGuard = new WorldGuardAPI();
		loadGenre(Genre.SKYBLOCK, SKYBLOCK, true);
	}
	
	private void loadGenre(Genre genre, Class<?>[] clazzes, boolean solitary) {
		TempFlyHook hook;
		Map<HookType, TempFlyHook> loaded = new HashMap<>();
		for (Class<?> clazz: clazzes) {
			try {
				hook = (TempFlyHook) clazz.getConstructor(TempFly.class).newInstance(plugin);
				if (hook.isEnabled()) {
					loaded.put(hook.getHookType(), hook);
					break;
				}
			} catch (Exception e) {e.printStackTrace();}
		}
		if (loaded.size() > 0) {
			hooks.put(genre, loaded);
		}
	}
	
	
	
	/**
	 *
	 * Getters
	 * 
	 */
	
    public Economy getEconomy() {
    	return eco;
    }
    
    public WorldGuardAPI getWorldGuard() {
    	return worldGuard;
    }
	
	public TempFlyHook getHook(HookType hook) {
		return hooks.getOrDefault(hook.getGenre(), new HashMap<>()).getOrDefault(hook, null);
	}
	
	public TempFlyHook[] getGenre(Genre genre) {
		Map<HookType, TempFlyHook> map;
		return (map = hooks.getOrDefault(genre, new HashMap<>())).values().toArray(new TempFlyHook[map.size()]);
	}
	
	public TempFlyHook[] getEnabled() {
		return hooks.values().toArray(new TempFlyHook[hooks.size()]);
	}
	
	
	
	
	/*
	 * Represents the GameMode type of a hook  
	 */
	public static enum Genre {
		SKYBLOCK("SkyBlock"),
		LANDS("Lands"),
		FACTIONS("Factions");
		
		private String folder;
		
		private Genre(String folder) {
			this.folder = folder;
		}
		
		public String getDirectory() {
			return TempFly.getInstance().getDataFolder() + File.separator + "hooks" + File.separator + folder;
		}
	}
	
	/*
	 * Represents the target plugin of a hook. 
	 */
	public static enum HookType {
		ASKYBLOCK(Genre.SKYBLOCK, "ASkyBlock"),
		BENTO_BOX(Genre.SKYBLOCK, "BentoBox"),
		SUPERIOR_SKYBLOCK_2(Genre.SKYBLOCK, "");
		
		private Genre genre;
		private String plugin;
		
		private HookType(Genre genre, String plugin) {
			this.genre = genre;
			this.plugin = plugin;
		}
		
		public Genre getGenre() {
			return genre;
		}
		
		public String getPluginName() {
			return plugin;
		}
	}

}
