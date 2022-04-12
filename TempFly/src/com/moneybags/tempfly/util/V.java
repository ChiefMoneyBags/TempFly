package com.moneybags.tempfly.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.moneybags.tempfly.util.data.Files;
import com.moneybags.tempfly.util.data.Files.C;

/**
 * TODO Most of these values should be relocated to their manager objects
 * @author Kevin
 *
 */
public class V {

	public static String
	prefix,
	reload,
	infinity,
	
	invalidParticle,
	invalidPermission,
	invalidPlayer,
	invalidNumber,
	invalidTimeSelf,
	invalidTimeOther,
	invalidSender,
	invalidCommand,
	invalidReciever,
	invalidFlyerSelf,
	invalidFunds,
	invalidEconomy,
	
	timeGivenOther,
	timeGivenSelf,
	timeRemovedOther,
	timeRemovedSelf,
	timeSentOther,
	timeSentSelf,
	timeSetOther,
	timeSetSelf,
	timeMaxOther,
	timeMaxSelf,
	timeDecayLost,
	timeFormat,
	timePurchased,
	firstJoin,
	dailyLogin,
	
	unitSeconds,
	unitMinutes,
	unitHours,
	unitDays,
	
	infoHeader,
	infoPlayer,
	infoDays,
	infoHours,
	infoMinutes,
	infoSeconds,
	infoFooter,
	infoInfinite,
	
	flyEnabledOther,
	flyEnabledSelf,
	flyDisabledOther,
	flyDisabledSelf,
	flySpeedOther,
	flySpeedSelf,
	flySpeedDenied,
	flySpeedLimitSelf,
	flySpeedLimitOther,
	flyAlreadyEnabled,
	flyAlreadyDisabled,
	flyInfiniteEnabled,
	flyInfiniteDisabled,
	flyBypassEnabled,
	flyBypassDisabled,
	
	disabledIdle,
	consideredIdle,

	requireFailOther,
	requireFailDefault,
	requirePassDefault,
	requireFailCombat,
	requirePassCombat,
	requireFailRegion,
	requireFailWorld,
	requireFailHeight,
	requireFailStruct,
	
	particleType,
	listName,
	listPlaceholderOn,
	listPlaceholderOff,
	tagName,
	tagPlaceholderOn,
	tagPlaceholderOff,
	
	fbDays,
	fbHours,
	fbMinutes,
	fbSeconds,
	
	warningTitle,
	warningSubtitle,
	
	actionText,
	
	trailRemovedSelf,
	trailRemovedOther,
	trailSetSelf,
	trailSetOther,
	
	vaultPermsRequired;

	public static boolean
	permaTimer,
	groundTimer,
	creativeTimer,
	spectatorTimer,
	idleTimer,
	idleDrop,
	payable,
	particles,
	particleDefault,
	list,
	tag,
	// Combat tag
	tagAttackPlayer,
	tagAttackMob,
	tagAttackedByPlayer,
	tagAttackedByMob,
	tagAttackedBySelf,
	// Fall damage
	damageCommand,
	damageTime,
	damageCombat,
	damageIdle,
	damageWorld,
	damageRegion,
	damageStruct,
	
	infiniteDisablePayment,
	infiniteDisableBonus,
	infiniteDisableDecay,
	
	autoFly,
	autoFlyTimeReceived,
	actionBar,
	timeDecay,
	flightToggle,
	hideVanish,
	shop,
	
	debug,
	disableTracker,
	disableTab,
	
	bugInfiniteA,
	bugInfiniteB;
	
	public static int
	idleThreshold,
	save,
	combatTagPvp,
	combatTagPve,
	maxY,
	decayThresh;
	
	public static double
	maxTimeBase,
	firstJoinTime,
	legacyBonus,
	decayAmount;
	
	public static List<String>
	help,
	helpExtended,
	disabledWorlds,
	disabledRegions,
	overrideFlightPermissions;
	
	public static List<Long> 
	warningTimes;
	
	public static Map<String, Double>
	dailyBonus,
	maxTimeGroups;

	public static void loadValues() {
		dailyBonus 			= new HashMap<>();
		maxTimeGroups 		= new HashMap<>();
		
		help 				= new ArrayList<>();
		helpExtended 		= new ArrayList<>();
		disabledWorlds 		= new ArrayList<>();
		disabledRegions 	= new ArrayList<>();
		overrideFlightPermissions = new ArrayList<>();
		
		FileConfiguration config = Files.config;
		
		prefix 				= st(C.LANG, "system.prefix");
		reload 				= st(C.LANG, "system.reload");
		
		invalidParticle		= st(C.LANG, "general.invalid.particle");
		invalidPermission	= st(C.LANG, "general.invalid.permission");
		invalidPlayer		= st(C.LANG, "general.invalid.player");
		invalidNumber		= st(C.LANG, "general.invalid.number");
		invalidSender		= st(C.LANG, "general.invalid.sender");
		invalidCommand		= st(C.LANG, "general.invalid.command");
		invalidTimeOther	= st(C.LANG, "general.invalid.time_other");
		invalidTimeSelf		= st(C.LANG, "general.invalid.time_self");
		invalidReciever		= st(C.LANG, "general.invalid.reciever");
		invalidFlyerSelf	= st(C.LANG, "general.invalid.flyer_self");
		invalidFunds		= st(C.LANG, "general.invalid.funds");
		invalidEconomy		= st(C.LANG, "general.invalid.economy");
		vaultPermsRequired  = st(C.LANG, "general.invalid.vault_perms");
		
		timeGivenOther		= st(C.LANG, "general.time.given_other");
		timeGivenSelf		= st(C.LANG, "general.time.given_self");
		timeRemovedOther	= st(C.LANG, "general.time.removed_other");
		timeRemovedSelf		= st(C.LANG, "general.time.removed_self");
		timeSentOther		= st(C.LANG, "general.time.sent_other");
		timeSentSelf		= st(C.LANG, "general.time.sent_self");
		timeSetOther		= st(C.LANG, "general.time.set_other");
		timeSetSelf			= st(C.LANG, "general.time.set_self");
		timeMaxOther		= st(C.LANG, "general.time.max_other");
		timeMaxSelf			= st(C.LANG, "general.time.max_self");
		timeDecayLost		= st(C.LANG, "general.time.decay");
		timeFormat			= st(C.LANG, "general.time.format");
		timePurchased		= st(C.LANG, "general.time.purchased");
		firstJoin			= st(C.LANG, "general.time.first_join");
		dailyLogin			= st(C.LANG, "general.time.daily_login");
		
		unitSeconds			= st(C.LANG, "general.unit.seconds", "s");
		unitMinutes			= st(C.LANG, "general.unit.minutes", "m");
		unitHours			= st(C.LANG, "general.unit.hours", "h");
		unitDays			= st(C.LANG, "general.unit.days", "d");
		
		infoHeader			= st(C.LANG, "general.info.header");
		infoPlayer			= st(C.LANG, "general.info.player");
		infoDays			= st(C.LANG, "general.info.days");
		infoHours			= st(C.LANG, "general.info.hours");
		infoMinutes			= st(C.LANG, "general.info.minutes");
		infoSeconds			= st(C.LANG, "general.info.seconds");
		infoFooter			= st(C.LANG, "general.info.footer");
		infoInfinite		= st(C.LANG, "general.info.infinite");
		
		flyEnabledOther	    = st(C.LANG, "general.fly.enabled_other");
		flyEnabledSelf	    = st(C.LANG, "general.fly.enabled_self");
		flyDisabledOther	= st(C.LANG, "general.fly.disabled_other");
		flyDisabledSelf 	= st(C.LANG, "general.fly.disabled_self");
		flySpeedOther		= st(C.LANG, "general.fly.speed_other");
		flySpeedSelf		= st(C.LANG, "general.fly.speed_self");
		flySpeedLimitOther	= st(C.LANG, "general.fly.speed_limit_other");
		flySpeedLimitSelf	= st(C.LANG, "general.fly.speed_limit_self");
		flySpeedDenied		= st(C.LANG, "general.fly.speed_restricted");
		flyAlreadyEnabled	= st(C.LANG, "general.fly.already_enabled");
		flyAlreadyDisabled	= st(C.LANG, "general.fly.already_disabled");
		flyInfiniteEnabled	= st(C.LANG, "general.fly.infinite_enabled");
		flyInfiniteDisabled	= st(C.LANG, "general.fly.infinite_disabled");
		flyBypassEnabled	= st(C.LANG, "general.fly.bypass_enabled");
		flyBypassDisabled	= st(C.LANG, "general.fly.bypass_disabled");
		
		disabledIdle 		= st(C.LANG, "general.fly.idle_drop");
		consideredIdle 		= st(C.LANG, "general.fly.idle");
		
		requireFailOther	= st(C.LANG, "general.requirement.fail.default_other");
		requireFailDefault	= st(C.LANG, "general.requirement.fail.default");
		requirePassDefault	= st(C.LANG, "general.requirement.pass.default");
		requireFailCombat	= st(C.LANG, "general.requirement.fail.combat");
		requirePassCombat	= st(C.LANG, "general.requirement.pass.combat");
		requireFailRegion	= st(C.LANG, "general.requirement.fail.region");
		requireFailWorld	= st(C.LANG, "general.requirement.fail.world");
		requireFailHeight	= st(C.LANG, "general.requirement.fail.height");
		requireFailStruct   = st(C.LANG, "general.requirement.fail.structure");
		
		fbDays				= st(C.LANG, "aesthetic.featherboard.days");
		fbHours				= st(C.LANG, "aesthetic.featherboard.hours");
		fbMinutes			= st(C.LANG, "aesthetic.featherboard.minutes");
		fbSeconds			= st(C.LANG, "aesthetic.featherboard.seconds");
		infinity			= st(C.LANG, "aesthetic.symbols.infinity");
		
		warningTitle		= st(C.CONFIG, "aesthetic.warning.title");
		warningSubtitle		= st(C.CONFIG, "aesthetic.warning.subtitle");
		
		actionText			= st(C.CONFIG, "aesthetic.action_bar.text");
		
		trailRemovedSelf	= st(C.LANG, "aesthetic.trail.removed_self");
		trailRemovedOther	= st(C.LANG, "aesthetic.trail.removed_other");
		trailSetSelf		= st(C.LANG, "aesthetic.trail.set_self");
		trailSetOther		= st(C.LANG, "aesthetic.trail.set_other");
		
		List<String> h 		= Files.lang.getStringList("system.help");
		if (h != null) {
			for (String s: h) {
				help.add(U.cc(s));
			}
		}
		
		List<String> he 	= Files.lang.getStringList("system.help_extended");
		if (he != null) {
			for (String s: he) {
				helpExtended.add(U.cc(s));
			}
		}
		
		try {
			warningTimes    = Files.config.getLongList("aesthetic.warning.seconds");
		} catch (Exception e) {
			warningTimes = new ArrayList<>();
			Console.warn("You can only set numbers under (aesthetic.warning.seconds) in the config!");
		}
		
		
		disabledWorlds	 	= Files.config.getStringList("general.disabled.worlds");
		if (disabledWorlds == null) {
			disabledWorlds = new ArrayList<>();
		}
		
		disabledRegions	 	= Files.config.getStringList("general.disabled.regions");
		if (disabledRegions == null) {
			disabledRegions = new ArrayList<>();
		}
		
		overrideFlightPermissions = Files.config.getStringList("general.fly_override_permissions");
		if (overrideFlightPermissions == null) {
			overrideFlightPermissions = new ArrayList<>();
		}
		
		
		save 				= config.getInt("system.backup", 5);
		debug 				= config.getBoolean("system.debug");
		disableTracker		= config.getBoolean("system.disable_region_tracking");
		disableTab			= config.getBoolean("system.disable_tab");
		
		permaTimer			= config.getBoolean("general.timer.constant");
		groundTimer			= config.getBoolean("general.timer.ground");
		creativeTimer		= config.getBoolean("general.timer.creative");
		spectatorTimer		= config.getBoolean("general.timer.spectator");
		idleTimer 			= config.getBoolean("general.timer.idle");
		idleDrop			= config.getBoolean("general.idle.drop_player");
		idleThreshold 		= config.getInt("general.idle.threshold");
		payable				= config.getBoolean("general.time.payable");
		particles			= config.getBoolean("aesthetic.identifier.particles.enabled");
		particleType		= config.getString("aesthetic.identifier.particles.type", "VILLAGER_HAPPY");
		particleDefault		= config.getBoolean("aesthetic.identifier.particles.display_by_default");
		hideVanish			= config.getBoolean("aesthetic.identifier.particles.hide_vanish");
		list				= config.getBoolean("aesthetic.identifier.tab_list.enabled");
		listName			= st(C.CONFIG, "aesthetic.identifier.tab_list.name");
		listPlaceholderOn	= st(C.CONFIG, "aesthetic.identifier.tab_list.placeholder.enabled");
		listPlaceholderOff	= st(C.CONFIG, "aesthetic.identifier.tab_list.placeholder.disabled");
		tag					= config.getBoolean("aesthetic.identifier.name_tag.enabled");
		tagName				= st(C.CONFIG, "aesthetic.identifier.name_tag.name");
		tagPlaceholderOn	= st(C.CONFIG, "aesthetic.identifier.name_tag.placeholder.enabled");
		tagPlaceholderOff	= st(C.CONFIG, "aesthetic.identifier.name_tag.placeholder.disabled");
		tagAttackPlayer		= config.getBoolean("general.combat.attack_player");
		tagAttackMob		= config.getBoolean("general.combat.attack_mob");
		tagAttackedByPlayer	= config.getBoolean("general.combat.attacked_by_player");
		tagAttackedByMob	= config.getBoolean("general.combat.attacked_by_mob");
		tagAttackedBySelf	= config.getBoolean("general.combat.self_inflicted");
		combatTagPvp		= config.getInt("general.combat.pvp_tag", 5) * 20;
		combatTagPve		= config.getInt("general.combat.pve_tag", 10) * 20;
		timeDecay			= config.getBoolean("general.time_decay.enabled");
		decayThresh			= config.getInt("general.time_decay.threshold", 3600);
		decayAmount			= config.getDouble("general.time_decay.seconds_lost", 15);
		firstJoinTime		= config.getLong("general.bonus.first_join", 0);
		legacyBonus			= config.getLong("general.bonus.daily_login", 0);
		shop				= config.getBoolean("shop.general.enabled", false);
		
		bugInfiniteA		= config.getBoolean("workarounds.infinite_flight.fix_a");
		bugInfiniteB		= config.getBoolean("workarounds.infinite_flight.fix_b");
		
		maxY				= config.getInt("general.flight.maximum_height", 275);
		autoFly				= config.getBoolean("general.flight.auto_enable", true);
		autoFlyTimeReceived = config.getBoolean("general.flight.enable_on_time_received", false);
		
		
		damageCommand		= config.getBoolean("general.damage.on_command");
		damageTime			= config.getBoolean("general.damage.out_of_time");
		damageCombat		= config.getBoolean("general.damage.combat");
		damageIdle			= config.getBoolean("general.damage.idle");
		damageWorld			= config.getBoolean("general.damage.disabled_world");
		damageRegion		= config.getBoolean("general.damage.disabled_region");
		damageStruct		= config.getBoolean("general.damage.structure_proximity");
		
		actionBar			= config.getBoolean("aesthetic.action_bar.enabled");
		
		double legacyBonus 	= config.getDouble("general.bonus.daily_login");
		if (legacyBonus == 0) {
			ConfigurationSection csPerms = config.getConfigurationSection("general.bonus.daily_login");
			if (csPerms != null) {
				for (String key: csPerms.getKeys(false)) {
					double value = config.getDouble("general.bonus.daily_login." + key);
					if (value > 0 && !dailyBonus.containsKey(key)) {
						dailyBonus.put(key, value);
					}
				}
			}
		}
		maxTimeBase = config.getDouble("general.time.max.base", -1);
		ConfigurationSection csMax = config.getConfigurationSection("general.time.max.groups");
		if (csMax != null) {
			for (String s: csMax.getKeys(false)) {
				maxTimeGroups.put(s, config.getDouble("general.time.max.groups." + s));
			}
		}
	}
	
	private static int missingMessages = 0;
	
	private static String st(C file, String key){
		try{
			switch (file)
			{
			case CONFIG:
				return U.cc(Files.config.getString(key)).replaceAll("\\{PREFIX}", prefix);
			case LANG:
				return U.cc(Files.lang.getString(key)).replaceAll("\\{PREFIX}", prefix);
			default:
				return "";
			}
		} catch (Exception e) {
			Console.warn("There is a missing message in the file: (" + file.toString().toLowerCase() + ".yml) | Path: (" + key + ")");
			if (missingMessages++ < 3) {Console.warn("THIS IS NOT AN ERROR! You simply need to add the missing message.");}
			return U.cc("&cThis message is broken! :(");
		}
	}
	
	public static String st(FileConfiguration config, String key, String fileName){
		try{
			return U.cc(config.getString(key)).replaceAll("\\{PREFIX}", prefix);
		} catch (Exception e) {
			Console.warn("There is a missing message in the file: (" + fileName + ".yml) | Path: (" + key + ")");
			if (missingMessages++ < 3) {Console.warn("THIS IS NOT AN ERROR! You simply need to add the missing message.");}
			return U.cc("&cThis message is broken! :(");
		}
	}
	
	private static String st(C file, String key, String def){
		try{
			switch (file)
			{
			case CONFIG:
				return U.cc(Files.config.getString(key)).replaceAll("\\{PREFIX}", prefix);
			case LANG:
				return U.cc(Files.lang.getString(key)).replaceAll("\\{PREFIX}", prefix);
			default:
				return "";
			}
		} catch (Exception e) {
			Console.warn("There is a missing message in the file: (" + file.toString().toLowerCase() + ") | Path: (" + key + ")");
			if (missingMessages++ < 3) {Console.warn("THIS IS NOT AN ERROR! You simply need to add the missing messagge.");}
			return U.cc(def);
		}
	}
}
