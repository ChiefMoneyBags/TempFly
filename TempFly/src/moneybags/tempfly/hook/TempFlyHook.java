package moneybags.tempfly.hook;

import java.io.File;
import java.io.IOException;
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
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.data.DataBridge.DataTable;
import moneybags.tempfly.util.data.Files;

public abstract class TempFlyHook {

	private HookType hookType;
	
	protected TempFly plugin;
	private String target;
	private boolean enabled;
	
	private File
	hookDataf,
	hookConfigf;
	
	private FileConfiguration hookConfig;
	
	public TempFlyHook(HookType hookType, TempFly plugin) {
		this.target = hookType.getPluginName();
		if (Bukkit.getPluginManager().getPlugin(target) == null) {
			return;
		}
		
		U.logI("Initializing (" + target + ") hook...");
		this.hookType = hookType;
		this.plugin = plugin;
		try { initializeFiles(); } catch (Exception e) {
			U.logS("An error occured while trying to initilize the (" + target + ") hook.");
			e.printStackTrace();
			return;
		}
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
	
	private void initializeFiles() throws Exception {
		File hookConfigf = new File(hookType.getGenre().getDirectory(), target + "_config.yml");
	    if (!hookConfigf.exists()) {
	    	hookConfigf.getParentFile().mkdirs();
	    	Files.createConfig(plugin.getResource("skyblock_config.yml"), hookConfigf);
	    }
	    
	   FileConfiguration hookConfig = new YamlConfiguration();
    	hookConfig.load(hookConfigf);
		if (!hookConfig.getBoolean("enable_hook")) {
			return;
		}
		
		plugin.getDataBridge().initializeHookData(this, plugin, DataTable.ISLAND_SETTINGS);
	}

	public abstract FlightResult handleFlightInquiry(Player p, ApplicableRegionSet regions);
	
	public abstract FlightResult handleFlightInquiry(Player p, ProtectedRegion r);

	public abstract FlightResult handleFlightInquiry(Player p, World world);
	
	public abstract FlightResult handleFlightInquiry(Player p, Location loc);
}
