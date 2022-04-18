package com.moneybags.tempfly.util.data.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import com.moneybags.tempfly.util.data.files.ResourceProvider;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeConfigProvider extends AbstractConfigProvider {


	private Plugin plugin;
	
	public BungeeConfigProvider(ResourceProvider resources, Plugin plugin) {
		super(resources);
		this.plugin = plugin;
	}

	@Override
	public void loadConfig(String name) throws IOException {;
		File file = new File(plugin.getDataFolder(), name);
	    if (!file.exists()){
	    	file.getParentFile().mkdirs();
	        createConfigFile(getResources().getResourceStream(name), file);
	    }
	    Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
	    configs.put(name, new BungeeConfig(file, config));
	}
	
	private static class BungeeConfig implements Config {

		private File file;
		private Configuration config;
		
		public BungeeConfig(File file, Configuration config) {
			this.file = file;
			this.config = config;
		}

		@Override
		public void reloadConfig() throws FileNotFoundException, IOException {
			config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
		}
		
		@Override
		public ConfigSection getConfigSection(String path) {
			Configuration section = config.getSection(path);
			return section == null ? null : new BungeeConfigSection(this, section);
		}
		
		@Override
		public ConfigSection getRootSection() {
			Configuration section = config.getSection("");
			return section == null ? null : new BungeeConfigSection(this, section);
		}

		@Override
		public void saveConfig() throws IOException {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
		}
		
	}
	
	public static class BungeeConfigSection implements ConfigSection {

		private BungeeConfig config;
		private Configuration cs;
		
		public BungeeConfigSection(BungeeConfig config, Configuration cs) {
			this.config = config;
			this.cs = cs;
		}
		
		@Override
		public ConfigSection getConfigSection(String path) {
			Configuration section = cs.getSection(path);
			return section == null ? null : new BungeeConfigSection(config, section);
		}
		
		@Override
		public ConfigSection getRootSection() {
			return config.getRootSection();
		}

		@Override
		public void reloadConfig() throws FileNotFoundException, IOException {
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
		public Collection<String> getKeys(boolean b) {
			return cs.getKeys();
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
