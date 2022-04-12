package com.moneybags.tempfly.hook.factions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;

import com.moneybags.tempfly.hook.factions.FactionsHook.FactionRelation;
import com.moneybags.tempfly.util.Console;

public class FactionRoleSettings {

	private FactionsHook hook;
	private String role;
	private Map<FactionRelation, FactionRequirement> requirements = new HashMap<>();
	
	public FactionRoleSettings(FactionsHook hook, String role, ConfigurationSection section) {
		this.hook = hook;
		this.role = role;
		loadValues(section);
	}
	
	private void loadValues(ConfigurationSection section) {
		if (section == null) {
			return;
		}
		for (String key: section.getKeys(false)) {
			FactionRelation relation;
			try { relation = FactionRelation.valueOf(key.toUpperCase()); } catch (IllegalArgumentException e) {
				Console.warn("A faction relationship defined in the config does not exist: (" + key + ")");
				continue;
			}
			requirements.put(relation, new FactionRequirement(hook, relation, section.getConfigurationSection(key), false));
		}
	}
	
	public String getRole() {
		return role;
	}
	
	public FactionsHook getHook() {
		return hook;
	}
	
	public FactionRequirement getRequirement(FactionRelation relation) {
		return requirements.getOrDefault(relation, hook.getBaseRequirement(relation));
	}
}
