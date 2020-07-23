package moneybags.tempfly;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
import moneybags.tempfly.hook.FlightResult;
import moneybags.tempfly.hook.HookManager;
import moneybags.tempfly.hook.TempFlyHook;
import moneybags.tempfly.tab.TabHandle;
import moneybags.tempfly.util.AutoSave;
import moneybags.tempfly.util.Console;
import moneybags.tempfly.util.ParticleTask;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;
import moneybags.tempfly.util.data.DataBridge;
import moneybags.tempfly.util.data.Files;

public class TempFly extends JavaPlugin {
	
	// static abusers unite
	private static TempFly plugin;
	private static TempFlyAPI tfApi;
	public static TempFly getInstance() {
		return plugin;
	}
	
	public static TempFlyAPI getAPI() {
		return tfApi;
	}
	
	

	private HookManager hooks;
	private DataBridge bridge;
	private BukkitTask autosave;
	
	public HookManager getHookManager() {
		return hooks;
	}
	
	public DataBridge getDataBridge() {
		return bridge;
	}

	@Override
	public void onEnable() {
		plugin = this;
		tfApi = new TempFlyAPI();
		
		Files.createFiles(this);
		V.loadValues();
		this.bridge = new DataBridge(this);
		
		Particles.initialize();
		PageTrails.initialize();
		PageShop.initialize();
		
		ActionBarAPI.initialize();
		
		this.hooks = new HookManager(this);
		
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
		} catch (Exception e) {e.printStackTrace();}
		
		autosave = new AutoSave().runTaskTimerAsynchronously(this, 0, V.save * 20 * 60);
		
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
						FlyHandle.regainFlightDisconnect(p);
					}
				}.runTaskLater(TempFly.plugin, 1);
			}	
		}
	}
	
	private void initializeAesthetics() {
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			Console.info("Initializing MvdwAPI");
			MvdWAPI.initialize();
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			Console.info("Initializing ClipAPI");
			ClipAPI.initialize();
		}
	}
	
	@Override
	public void onDisable() {
		FlyHandle.onDisable();
		GuiSession.endAllSessions();
		bridge.commit();
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
	
	/*
	 * Reload the plugin, this is the method called upon command /tempfly reload
	 */
	//TODO reload hooks
	public void reloadTempfly() {
		GuiSession.endAllSessions();
		bridge.commit();
		Files.createFiles(this);
		V.loadValues();
		PageTrails.initialize();
		PageShop.initialize();
		if (autosave != null) {
			autosave.cancel();
			autosave = new AutoSave().runTaskTimerAsynchronously(this, 0, V.save * 20 * 60);
		}
		for (Flyer f: FlyHandle.getFlyers()) {
			FlyHandle.evaluateFlightRequirements(f, true);
		}
	}
	
}
