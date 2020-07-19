package com.moneybags.tempfly;

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

import com.moneybags.tempfly.aesthetic.ActionBarAPI;
import com.moneybags.tempfly.aesthetic.ClipAPI;
import com.moneybags.tempfly.aesthetic.MvdWAPI;
import com.moneybags.tempfly.aesthetic.particle.Particles;
import com.moneybags.tempfly.command.CommandHandle;
import com.moneybags.tempfly.fly.FlyHandle;
import com.moneybags.tempfly.fly.Flyer;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.pages.PageShop;
import com.moneybags.tempfly.gui.pages.PageTrails;
import com.moneybags.tempfly.hook.TempFlyHooks;
import com.moneybags.tempfly.hook.WorldGuardAPI;
import com.moneybags.tempfly.hook.skyblock.a.AskyblockHook;
import com.moneybags.tempfly.tab.TabHandle;
import com.moneybags.tempfly.util.AutoSave;
import com.moneybags.tempfly.util.F;
import com.moneybags.tempfly.util.ParticleTask;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

import net.milkbowl.vault.economy.Economy;

public class TempFly extends JavaPlugin {
	
	public static TempFly plugin;
	public static TempFlyAPI tfApi;
	private static TempFlyHooks hooks;
	
	public static double version;
	
	
	public static TempFlyAPI getAPI() {
		return tfApi;
	}
	
	public static TempFlyHooks getHooks() {
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
		
		hooks = new TempFlyHooks(this);
		
		FlyHandle.initialize();
		registerListeners();
		registerCommands();
		initializeAesthetics();
		
		try {
			Metrics metrics = new Metrics(this, 8196);
			
			// Hooks
	        metrics.addCustomChart(new Metrics.DrilldownPie("gamemode_hooks", () -> {
	        	
	            Map<String, Map<String, Integer>> map = new HashMap<>();
	            Map<String, Integer> entry = new HashMap<>();
	            boolean hook = false;
	            /**
	            if (askyblockHook != null) {
	            	entry.put("ASkyBlock", 1);
	            	map.put("ASkyBlock Hook", entry);
	            	hook = true;
	            }
	            */
	            if (!hook) {
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
