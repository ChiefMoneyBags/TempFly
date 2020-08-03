package com.moneybags.tempfly.hook;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.hook.HookManager.HookType;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.data.Files;
import com.moneybags.tempfly.util.data.DataBridge.DataTable;

public abstract class TempFlyHook implements RequirementProvider {

	private HookType hookType;
	
	protected TempFly tempfly;
	private String target;
	private boolean enabled;
	
	private FileConfiguration hookConfig;
	
	public TempFlyHook(HookType hookType, TempFly tempfly) {
		this.target = hookType.getPluginName();
		if (Bukkit.getPluginManager().getPlugin(target) == null) {
			return;
		}
		
		Console.info("Attempting to initialize (" + target + ") hook...");
		this.hookType = hookType;
		this.tempfly = tempfly;
		try { initializeFiles(); } catch (Exception e) {
			Console.severe("An error occured while trying to initilize the (" + target + ") hook.");
			e.printStackTrace();
			return;
		}
	}
	
	private void initializeFiles() throws Exception {
		File hookConfigf = new File(tempfly.getDataFolder() + File.separator + hookType.getGenre().getDirectory(), hookType.getConfigName() + ".yml");
	    if (!hookConfigf.exists()) {
	    	hookConfigf.getParentFile().mkdirs();
	    	Files.createConfig(tempfly.getResource(hookType.getEmbeddedConfigName() + ".yml"), hookConfigf);
	    }
	    
	    hookConfig = new YamlConfiguration();
    	hookConfig.load(hookConfigf);
		if (!hookConfig.getBoolean("enable_hook")) {
			Console.info("(" + target + ") hook is disabled, skipping...");
			return;
		}
		
		tempfly.getDataBridge().initializeHookData(this, tempfly, DataTable.ISLAND_SETTINGS);
		enabled = true;
	}
	
	public TempFly getTempFly() {
		return tempfly;
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
}
