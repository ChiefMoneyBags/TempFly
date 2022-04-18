package com.moneybags.tempfly.util.data.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.data.files.ResourceProvider;

public class SpigotConfigProvider extends AbstractConfigProvider {

	private Plugin plugin;
	
	public SpigotConfigProvider(ResourceProvider resources, Plugin plugin) {
		super(resources);
		this.plugin = plugin;
	}

	@Override
	public void loadConfig(String name) {
		File file = new File(plugin.getDataFolder(), name);
	    if (!file.exists()){
	    	file.getParentFile().mkdirs();
	        plugin.saveResource(name, false);
	    }
	    FileConfiguration config = new YamlConfiguration();
	    try { config.load(file); } catch (IOException | InvalidConfigurationException e1){
	    	Console.severe("There is a problem inside the config.yml, If you cannot fix the issue, please contact the developer.");
	    	e1.printStackTrace();
	    }
	    configs.put(name, new SpigotConfig(file, config));
	}
	
	private static class SpigotConfig implements Config {

		private File file;
		private FileConfiguration config;
		
		public SpigotConfig(File file, FileConfiguration config) {
			this.file = file;
			this.config = config;
		}

		@Override
		public void reloadConfig() throws FileNotFoundException, IOException, InvalidConfigurationException {
			config = new YamlConfiguration();
			config.load(file);
		}
		
		@Override
		public ConfigSection getConfigSection(String path) {
			ConfigurationSection section = config.getConfigurationSection(path);
			return section == null ? null : new SpigotConfigSection(this, section);
		}
		
		@Override
		public ConfigSection getRootSection() {
			ConfigurationSection section = config.getDefaultSection();
			return section == null ? null : new SpigotConfigSection(this, section);
		}

		@Override
		public void saveConfig() throws IOException {
			config.save(file);
		}
		
	}
	
	public static class SpigotConfigSection implements ConfigSection {

		private SpigotConfig config;
		private ConfigurationSection cs;
		
		public SpigotConfigSection(SpigotConfig config, ConfigurationSection cs) {
			this.config = config;
			this.cs = cs;
		}
		
		public FileConfiguration getFileConfiguration() {
			return config.config;
		}
		
		public File getFile() {
			return config.file;
		}
		
		@Override
		public ConfigSection getConfigSection(String path) {
			ConfigurationSection section = cs.getConfigurationSection(path);
			return section == null ? null : new SpigotConfigSection(config, section);
		}
		
		@Override
		public ConfigSection getRootSection() {
			return config.getRootSection();
		}

		@Override
		public void reloadConfig() throws FileNotFoundException, IOException, InvalidConfigurationException {
			config.reloadConfig();
		}
		
		@Override
		public void saveConfig() throws IOException {
			config.saveConfig();
		}
		
		
		
		/**
		 * -------------
		 * ConfigSection
		 * -------------
		 */
		
		@Override
		public Set<String> getKeys(boolean b) {
			return cs.getKeys(b);
		}
		
		@Override
		public boolean contains(String path) {
			return cs.contains(path);
		}


		@Override
		public String getString(String path) {
			return cs.getString(path);
		}

		@Override
		public String getString(String path, String def) {
			return cs.getString(path, def);
		}

		@Override
		public boolean getBoolean(String path) {
			return cs.getBoolean(path);
		}

		@Override
		public boolean getBoolean(String path, boolean b) {
			return cs.getBoolean(path, b);
		}

		@Override
		public int getInt(String path) {
			return cs.getInt(path);
		}

		@Override
		public int getInt(String path, int def) {
			return cs.getInt(path, def);
		}

		@Override
		public List<String> getStringList(String path) {
			return cs.getStringList(path);
		}

		@Override
		public List<Long> getLongList(String path) {
			return cs.getLongList(path);
		}

		@Override
		public double getDouble(String path, double def) {
			return cs.getDouble(path, def);
		}

		@Override
		public double getDouble(String path) {
			return cs.getDouble(path);
		}

		@Override
		public double getLong(String path, long def) {
			return cs.getLong(path, def);
		}
		
	}

}
