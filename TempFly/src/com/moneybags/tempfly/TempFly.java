package com.moneybags.tempfly;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.moneybags.tempfly.aesthetic.ActionBarAPI;
import com.moneybags.tempfly.aesthetic.ClipAPI;
import com.moneybags.tempfly.aesthetic.MvdWAPI;
import com.moneybags.tempfly.aesthetic.particle.Particles;
import com.moneybags.tempfly.command.TempFlyExecutor;
import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.gui.GuiSession;
import com.moneybags.tempfly.gui.pages.PageShop;
import com.moneybags.tempfly.gui.pages.PageTrails;
import com.moneybags.tempfly.hook.HookManager;
import com.moneybags.tempfly.hook.TempFlyHook;
import com.moneybags.tempfly.tab.TabHandle;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.AutoSave;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.ParticleTask;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.DataBridge;
import com.moneybags.tempfly.util.data.Files;

public class TempFly extends JavaPlugin {
	
	// static abusers unite
	private static TempFlyAPI tfApi;
	public static TempFlyAPI getAPI() {
		return tfApi;
	}

	
	private HookManager hooks;
	private DataBridge bridge;
	private FlightManager flight;
	private TimeManager time;
	private BukkitTask autosave;
	
	public HookManager getHookManager() {
		return hooks;
	}
	
	public DataBridge getDataBridge() {
		return bridge;
	}
	
	public FlightManager getFlightManager() {
		return flight;
	}
	
	public TimeManager getTimeManager() {
		return time;
	}

	
	
	@Override
	public void onEnable() {
		tfApi = new TempFlyAPI(this);
		new Console(this);
		
		Files.createFiles(this);
		V.loadValues();
		this.bridge = new DataBridge(this);
		this.hooks 	= new HookManager(this);
		this.flight = new FlightManager(this);
		this.time 	= new TimeManager(this);
		
		registerListeners();
		registerCommands();
		initializeGui();
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
		
		autosave = new AutoSave(bridge).runTaskTimerAsynchronously(this, V.save * 20 * 60, V.save * 20 * 60);
		
		// Support "/reload"
		for (Player p: Bukkit.getOnlinePlayers()) {
			flight.addUser(p);
		}
	}
	
	private void initializeAesthetics() {
		Particles.initialize(this);
		if (V.particles) {
			new ParticleTask(this).runTaskTimer(this, 0, 5);
		}
		
		if (V.actionBar) {ActionBarAPI.initialize(this);}
		
		if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
			Console.info("Initializing MvdwAPI");
			MvdWAPI.initialize(this);
		}
		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			Console.info("Initializing ClipAPI");
			ClipAPI.initialize(this);
		}
	}
	
	private void initializeGui() {
		PageTrails.initialize();
		PageShop.initialize(this);
	}
	
	@Override
	public void onDisable() {
		flight.onDisable();
		GuiSession.endAllSessions();
		bridge.commitAll();
	}
	
	@Deprecated
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new GuiSession.GuiListener(), this);
	}
	
	private void registerCommands() {
		CommandExecutor c = new TempFlyExecutor(this);
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
		bridge.commitAll();
		Files.createFiles(this);
		V.loadValues();
		PageTrails.initialize();
		PageShop.initialize(this);
		if (autosave != null) {
			autosave.cancel();
			autosave = new AutoSave(bridge).runTaskTimerAsynchronously(this, 0, V.save * 20 * 60);
		}
		for (FlightUser user: flight.getUsers()) {
			user.evaluateFlightRequirements(user.getPlayer().getLocation(), user.hasFlightEnabled());
		}
	}
	
}
