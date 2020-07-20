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
import moneybags.tempfly.util.F;
import moneybags.tempfly.util.U;

public abstract class TempFlyHook {

	private HookType hookType;
	
	protected TempFly plugin;
	private String target;
	private boolean enabled;
	
	private File
	hookDataf,
	hookConfigf;
	
	private FileConfiguration
	hookData,
	hookConfig;
	
	public TempFlyHook(HookType type, TempFly plugin) {
		this.target = type.getPluginName();
		if (Bukkit.getPluginManager().getPlugin(target) == null) {
			return;
		}
		U.logI("Initializing (" + target + ") hook...");
		try { initializeFiles(); } catch (Exception e) {
			U.logS("An error occured while trying to initilize the (" + target + ") hook.");
			e.printStackTrace();
			return;
		}
		this.plugin = plugin;
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
	
	public FileConfiguration getData() {
		return hookData;
	}
	
	public HookType getHookType() {
		return hookType;
	}
	
	public void saveData() {
		try { hookData.save(hookDataf); } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initializeFiles() throws Exception {
		hookConfigf = new File(hookType.getGenre().getDirectory(), target + "_config.yml");
	    if (!hookConfigf.exists()) {
	    	hookConfigf.getParentFile().mkdirs();
	    	F.createConfig(plugin.getResource("skyblock_config.yml"), hookConfigf);
	    }
	    
	    hookConfig = new YamlConfiguration();
    	hookConfig.load(hookConfigf);
		if (!hookConfig.getBoolean("enable_hook")) return;
		
		hookDataf = new File(hookType.getGenre().getDirectory(), target + "_data.yml");
	    if (!hookDataf.exists()) {
	    	hookDataf.getParentFile().mkdirs();
	    	hookDataf.createNewFile();
	    }
	    hookData = new YamlConfiguration();
	    hookData.load(hookDataf);
	}

	public abstract FlightResult handleFlightInquiry(Player p, ApplicableRegionSet regions);
	
	public abstract FlightResult handleFlightInquiry(Player p, ProtectedRegion r);

	public abstract FlightResult handleFlightInquiry(Player p, World world);
	
	public abstract FlightResult handleFlightInquiry(Player p, Location loc);
}
