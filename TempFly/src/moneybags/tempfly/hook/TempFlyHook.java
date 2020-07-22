package moneybags.tempfly.hook;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.hook.HookManager.HookType;
import moneybags.tempfly.util.Console;
import moneybags.tempfly.util.data.DataBridge.DataTable;
import moneybags.tempfly.util.data.Files;

public abstract class TempFlyHook {

	private HookType hookType;
	
	protected TempFly plugin;
	private String target;
	private boolean enabled;
	
	private FileConfiguration hookConfig;
	
	public TempFlyHook(HookType hookType, TempFly plugin) {
		this.target = hookType.getPluginName();
		if (Bukkit.getPluginManager().getPlugin(target) == null) {
			return;
		}
		
		Console.info("Attempting to initialize (" + target + ") hook...");
		this.hookType = hookType;
		this.plugin = plugin;
		try { initializeFiles(); } catch (Exception e) {
			Console.severe("An error occured while trying to initilize the (" + target + ") hook.");
			e.printStackTrace();
			return;
		}
	}
	
	private void initializeFiles() throws Exception {
		String configType = hookType.getGenre().toString().toLowerCase();
		File hookConfigf = new File(hookType.getGenre().getDirectory(),  configType + "_config.yml");
	    if (!hookConfigf.exists()) {
	    	hookConfigf.getParentFile().mkdirs();
	    	Files.createConfig(plugin.getResource(configType + "_config.yml"), hookConfigf);
	    }
	    
	    hookConfig = new YamlConfiguration();
    	hookConfig.load(hookConfigf);
		if (!hookConfig.getBoolean("enable_hook")) {
			Console.info("(" + target + ") hook is disabled, skipping...");
			return;
		}
		
		plugin.getDataBridge().initializeHookData(this, plugin, DataTable.ISLAND_SETTINGS);
	}
	
	public String getHookedPlugin() {
		return target;
	}
	
	public FileConfiguration getConfig() {
		return hookConfig;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public HookType getHookType() {
		return hookType;
	}
	
	public void saveData() {
		//TODO
	}

	public abstract FlightResult handleFlightInquiry(Player p, ApplicableRegionSet regions);
	
	public abstract FlightResult handleFlightInquiry(Player p, ProtectedRegion r);

	public abstract FlightResult handleFlightInquiry(Player p, World world);
	
	public abstract FlightResult handleFlightInquiry(Player p, Location loc);
}
