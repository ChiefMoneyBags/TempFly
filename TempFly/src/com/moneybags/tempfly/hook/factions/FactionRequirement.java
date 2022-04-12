package com.moneybags.tempfly.hook.factions;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.bukkit.configuration.ConfigurationSection;

import com.moneybags.tempfly.hook.factions.FactionsHook.FactionRelation;
import com.moneybags.tempfly.util.Console;

public class FactionRequirement {

	private FactionsHook hook;
	private FactionRelation relation;
	private boolean allowed;
	private Map<PowerContext, Double> powerRequirements = new HashMap<>();
	
	/**
	 * Loads a faction requirement from the config.
	 * @param hook
	 * @param relation
	 * @param section
	 */
	public FactionRequirement(FactionsHook hook, FactionRelation relation, ConfigurationSection section, boolean enforceDefaults) {
		this.hook = hook;
		this.relation = relation;
		loadValues(section, enforceDefaults);
	}
	
	/**
	 * Creates a default faction requirement with no restrictions.
	 * @param hook
	 * @param relation
	 */
	public FactionRequirement(FactionsHook hook, FactionRelation relation) {
		this.hook = hook;
		this.relation = relation;
		allowed = true;
		powerRequirements.put(PowerContext.HOME_POWER_PERCENTAGE, 0d);
		
		if (relation.getPowerContext() != PowerContext.SELF) {
			powerRequirements.put(PowerContext.FOREIGN_POWER_PERCENTAGE, 100d);
		} 
	}
	
	private void loadValues(ConfigurationSection section, boolean enforceDefaults) {
		Console.debug("--| Loading settings for faction relation: " + relation);
		if (section == null) {
			Console.debug("--| Configuration section is null, loading defaults if applicable.");
			if (enforceDefaults) {
				powerRequirements.put(PowerContext.HOME_POWER_PERCENTAGE, 0d);
				if (relation == FactionRelation.ALLIED) {
					powerRequirements.put(PowerContext.ALLIED_POWER_PERCENTAGE, 0d);	
				} else {
					powerRequirements.put(PowerContext.FOREIGN_POWER_PERCENTAGE, 100d);
				}
				allowed = true;
			}
			return;
		}
		
		allowed = section.getBoolean("allowed", enforceDefaults ? true : hook.getBaseRequirement(relation).isAllowed());
		
		boolean 
		self = false,
		foreign = false;
		
		for (String key: section.getKeys(false)) {
			PowerContext context;
			try { context = PowerContext.valueOf(key.toUpperCase()); } catch (IllegalArgumentException e) {
				continue;
			}
			if (powerRequirements.keySet().stream().anyMatch(definedContext -> definedContext.getContext() == context.getContext())) {
				Console.warn("You cannot set more than one power requirement under the context of (" + context.getContext() + ") power. | (" + context + ")");
				continue;
			}
			
			if (context.getContext() == PowerContext.SELF) {
				self = true;
			} else {
				foreign = true;
			}
			Console.debug("--| Loading " + key, "--| Value: " + String.valueOf(section.getDouble(key)));
			powerRequirements.put(context, section.getDouble(key));
		}
		
		if (enforceDefaults) {
			if (!self) {
				Console.debug("--| No self requirement defined, loading default.");
				powerRequirements.put(PowerContext.HOME_POWER_PERCENTAGE, 0d);
			}
			if (!foreign) {
				Console.debug("--| No foreign requirement defined, loading default.");
				if (relation == FactionRelation.ALLIED) {
					powerRequirements.put(PowerContext.ALLIED_POWER_PERCENTAGE, 0d);	
				} else {
					powerRequirements.put(PowerContext.FOREIGN_POWER_PERCENTAGE, 100d);
				}
			} 	
		}
		Console.debug(powerRequirements);
	}
	
	public boolean isThresholdMet(PowerContext context, double currentPower, double maxPower) {
		for (Entry<PowerContext, Double> entry: powerRequirements.entrySet()) {
			if (entry.getKey() != context && entry.getKey().getContext() != context) {
				continue;
			}
			Console.debug("--| Found a power requirement for the role...", "--| PowerContext: " + context, "--| DefinedContext: " + entry.getKey());
			PowerPackage pack = new PowerPackage();
			pack.defined = entry.getValue();
			pack.current = currentPower;
			pack.max = Math.max(maxPower, 1);
			Console.debug("--| value defined in config: " + pack.defined);
			return entry.getKey().meetsThreshold(pack);
		}
		Console.debug("--| power requirement does not exist for this role, using default requirement.");
		return hook.getBaseRequirement(relation).isThresholdMet(context, currentPower, maxPower);
	}
	
	public double getPowerThreshold(PowerContext context, double currentPower, double maxPower) {
		for (Entry<PowerContext, Double> entry: powerRequirements.entrySet()) {
			if (entry.getKey() != context && entry.getKey().getContext() != context) {
				continue;
			}
			return entry.getValue();
		}
		return hook.getBaseRequirement(relation).getPowerThreshold(context, currentPower, maxPower);
	}
	
	public String getPowerFormatted(PowerContext context, double currentPower, double maxPower) {
		for (Entry<PowerContext, Double> entry: powerRequirements.entrySet()) {
			if (entry.getKey() != context && entry.getKey().getContext() != context) {
				continue;
			}
			PowerPackage pack = new PowerPackage();
			pack.defined = entry.getValue();
			pack.current = currentPower;
			pack.max = Math.max(maxPower, 1);
			return entry.getKey().getFormatted(pack);
		}
		return hook.getBaseRequirement(relation).getPowerFormatted(context, currentPower, maxPower);
	}
	
	public boolean isAllowed() {
		return allowed;
	}
	
	
	
	
	private static class PowerPackage {
		double defined, current, max;
	}
	
	public static enum PowerContext {
		SELF(),
		HOME_POWER_PERCENTAGE(SELF, true,
				pack -> {
					return (pack.current / pack.max) * 100 >= pack.defined;
				}),
		HOME_POWER_ABSOLUTE(SELF, false,
				pack -> {
					return pack.current >= pack.defined;
				}),
		
		FOREIGN(),
		FOREIGN_POWER_PERCENTAGE(FOREIGN, true,
				pack -> {
					return (pack.current / pack.max) * 100 <= pack.defined;
				}),
		FOREIGN_POWER_ABSOLUTE(FOREIGN, false,
				pack -> {
					return pack.current <= pack.defined;
				}),
		
		
		ALLIED_POWER_PERCENTAGE(FOREIGN, true,
				pack -> {
					return (pack.current / pack.max) * 100 >= pack.defined;
				}),
		ALLIED_POWER_ABSOLUTE(FOREIGN, false,
				pack -> {
					return pack.current >= pack.defined;
				});
		
		private PowerContext context;
		private boolean percentage;
		private Predicate<PowerPackage> predicate;
		
		private PowerContext(PowerContext context, boolean percentage, Predicate<PowerPackage> predicate) {
			this.context = context;
			this.percentage = percentage;
			this.predicate = predicate;
		}
		
		private PowerContext() {
			this.context = null;
			this.predicate = null;
		}
		
		
		public PowerContext getContext() {
			return context;
		}
		
		private boolean meetsThreshold(PowerPackage pack) {
			return predicate.test(pack);
		}
		
		private String getFormatted(PowerPackage pack) {
			return percentage ? String.valueOf(pack.defined) + "%" : String.valueOf(pack.defined); 
		}
		
	}
	
}
