package moneybags.tempfly;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import moneybags.tempfly.aesthetic.ActionBarAPI;
import moneybags.tempfly.aesthetic.ClipAPI;
import moneybags.tempfly.aesthetic.MvdWAPI;
import moneybags.tempfly.aesthetic.particle.Particles;
import moneybags.tempfly.command.CommandHandle;
import moneybags.tempfly.fly.FlyHandle;
import moneybags.tempfly.fly.Flyer;
import moneybags.tempfly.gui.GuiSession;
import moneybags.tempfly.gui.pages.PageShop;
import moneybags.tempfly.gui.pages.PageTrails;
import moneybags.tempfly.hook.HookManager;
import moneybags.tempfly.hook.TempFlyHook;
import moneybags.tempfly.hook.WorldGuardAPI;
import moneybags.tempfly.hook.skyblock.plugins.AskyblockHook;
import moneybags.tempfly.tab.TabHandle;
import moneybags.tempfly.util.AutoSave;
import moneybags.tempfly.util.F;
import moneybags.tempfly.util.ParticleTask;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;
import net.milkbowl.vault.economy.Economy;

public class TempFly extends JavaPlugin {
	
	public static TempFly plugin;
	public static TempFlyAPI tfApi;
	private static HookManager hooks;
	
	public static double version;
	
	
	public static TempFlyAPI getAPI() {
		return tfApi;
	}
	
	public static HookManager getHookManager() {
		return hooks;
	}

	@Override
	public void onEnable() {
		plugin = this;
		tfApi = new TempFlyAPI();
		
		F.createFiles(this);
		V.loadValues();
		Particles.initialize();
		PageTrails.initialize();
		PageShop.initialize();
		
		ActionBarAPI.initialize();
		
		hooks = new HookManager(this);
		
		FlyHandle.initialize();
		registerListeners();
		registerCommands();
		initializeAesthetics();
		
		try {
			Metrics metrics = new Metrics(this, 8196);
			
			// Hooks
	        metrics.addCustomChart(new Metrics.DrilldownPie("gamemode_hooks", () -> {
	        	
	        	//TODO this aint right...
	            Map<String, Map<String, Integer>> map = new HashMap<>();
	            //?
	            Map<String, Integer> entry = new HashMap<>();
	            
	            for (TempFlyHook hook: hooks.getEnabled()) {
	            	entry.put(hook.getHookedPlugin(), 1);
	            	map.put(hook.getHookedPlugin(), entry);
	            }
	            if (map.size() == 0) {
	            	entry.put("No Hooks", 1);
	            	map.put("No Hooks", entry);
	            }
	            return map;
	        }));
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		new AutoSave().runTaskTimer(this, 0, V.save * 20 * 60);
		if (V.particles) {
			new ParticleTask().runTaskTimer(this, 0, 5);	
		}
		
		for (Player p: Bukkit.getOnlinePlayers()) {
			if (FlyHandle.getFlyer(p) == null) {
				GameMode m = p.getGameMode();
				if ((m.equals(GameMode.CREATIVE)) || (m.equals(GameMode.SPECTATOR))) {
					continue;
				}
				
				
				new BukkitRunnable() {
					@Override
					public void run() {
						if (!FlyHandle.regainFlightDisconnect(p)) {
							FlyHandle.enforceDisabledFlight(p);
						}
					}
				}.runTaskLater(TempFly.plugin, 1);
			}	
		}
	}
	
	private static void initializeAesthetics() {
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			U.logI("Initizlizing MvdwAPI");
			MvdWAPI.initialize();	
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			U.logI("Initizlizing ClipAPI");
			ClipAPI.initialize();	
		}
	}
	
	@Override
	public void onDisable() {
		FlyHandle.onDisable();
	}
	
	public static boolean oldParticles() {
		String version = Bukkit.getVersion();
		return (version.contains("1.6")) || (version.contains("1.7")) || (version.contains("1.8")) || version.contains("1.9");
	}
	
	private void registerListeners() {
		PluginManager m = getServer().getPluginManager();
		m.registerEvents(new FlyHandle(), this);
		m.registerEvents(new CommandHandle(), this);
		m.registerEvents(new GuiSession.GuiListener(), this);
	}
	
	private void registerCommands() {
		CommandExecutor c = new CommandHandle();
		TabCompleter t = new TabHandle();
		getCommand("tempfly").setExecutor(c);
		getCommand("tempfly").setTabCompleter(t);
	}
	
}
