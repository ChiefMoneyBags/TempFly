package moneybags.tempfly.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import moneybags.tempfly.util.data.Files;
import moneybags.tempfly.util.data.Files.C;

public class V {

	public static String
	prefix,
	reload,
	infinity,
	
	invalidPermission,
	invalidPlayer,
	invalidNumber,
	invalidTimeSelf,
	invalidTimeOther,
	invalidSender,
	invalidCommand,
	invalidReciever,
	invalidFlyerSelf,
	invalidZoneOther,
	invalidZoneSelf,
	invalidFunds,
	
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
	flyCooldownDeny,
	flyCooldownOver,
	flyRequirementFail,
	
	particleType,
	listName,
	tagName,
	
	fbDays,
	fbHours,
	fbMinutes,
	fbSeconds,
	
	warningTitle,
	warningSubtitle,
	
	actionText,
	
	trailRemovedSelf,
	trailRemovedOther;

	public static boolean
	debug,
	groundTimer,
	idleTimer,
	idleDrop,
	payable,
	particles,
	particleDefault,
	list,
	tag,
	attackP,
	attackM,
	attackedP,
	attackedM,
	protCommand,
	protTime,
	protCombat,
	actionBar,
	actionProgress,
	permaTimer,
	timeDecay,
	flightToggle,
	hideVanish,
	shop;
	
	public static int
	idleThreshold,
	save,
	cooldownPvp,
	cooldownPve,
	maxY,
	decayThresh;
	
	public static double
	maxTime,
	firstJoinTime,
	legacyBonus,
	decayAmount,
	defaultSpeed;
	
	public static List<String>
	help = new ArrayList<>(),
	helpExtended = new ArrayList<>(),
	disabledWorlds = new ArrayList<>(),
	disabledRegions = new ArrayList<>(),
	overrideFlightPermissions = new ArrayList<>();
	
	public static List<Long> 
	warningTimes;
	
	public static Map<String, Double>
	dailyBonus = new HashMap<>();

	public static void loadValues() {
		FileConfiguration config = Files.config;
		
		prefix 				= st(C.LANG, "system.prefix");
		reload 				= st(C.LANG, "system.reload");
		
		invalidPermission	= st(C.LANG, "general.invalid.permission");
		invalidPlayer		= st(C.LANG, "general.invalid.player");
		invalidNumber		= st(C.LANG, "general.invalid.number");
		invalidSender		= st(C.LANG, "general.invalid.sender");
		invalidCommand		= st(C.LANG, "general.invalid.command");
		invalidTimeOther	= st(C.LANG, "general.invalid.time_other");
		invalidTimeSelf		= st(C.LANG, "general.invalid.time_self");
		invalidReciever		= st(C.LANG, "general.invalid.reciever");
		invalidFlyerSelf	= st(C.LANG, "general.invalid.flyer_self");
		invalidZoneOther	= st(C.LANG, "general.invalid.zone_other");
		invalidZoneSelf		= st(C.LANG, "general.invalid.zone_self");
		invalidFunds		= st(C.LANG, "general.invalid.funds");
		
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
		flyCooldownDeny		= st(C.LANG, "general.fly.cooldown_deny");
		flyCooldownOver		= st(C.LANG, "general.fly.cooldown_over");
		flyRequirementFail  = st(C.LANG, "general.fly.requirement_fail");
		
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
		
		
		debug 				= config.getBoolean("system.debug");
		groundTimer			= config.getBoolean("general.timer.ground");
		idleTimer 			= config.getBoolean("general.timer.idle");
		idleDrop			= config.getBoolean("general.idle.drop_player");
		idleThreshold 		= config.getInt("general.idle.threshold");
		payable				= config.getBoolean("general.time.payable");
		maxTime				= config.getDouble("general.time.max_time");
		save 				= config.getInt("system.backup", 5);
		particles			= config.getBoolean("aesthetic.identifier.particles.enabled");
		particleType		= config.getString("aesthetic.identifier.particles.type", "VILLAGER_HAPPY");
		particleDefault		= config.getBoolean("aesthetic.identifier.particles.display_by_default");
		hideVanish			= config.getBoolean("aesthetic.identifier.particles.hide_vanish");
		list				= config.getBoolean("aesthetic.identifier.tab_list.enabled");
		listName			= st(C.CONFIG, "aesthetic.identifier.tab_list.name");
		tag					= config.getBoolean("aesthetic.identifier.name_tag.enabled");
		tagName				= st(C.CONFIG, "aesthetic.identifier.name_tag.name");
		attackP				= config.getBoolean("general.disable_flight.attack_player");
		attackM				= config.getBoolean("general.disable_flight.attack_mob");
		attackedP			= config.getBoolean("general.disable_flight.attacked_by_player");
		attackedM			= config.getBoolean("general.disable_flight.attacked_by_mob");
		cooldownPvp			= config.getInt("general.disable_flight.cooldown_pvp", 1) * 20;
		cooldownPve			= config.getInt("general.disable_flight.cooldown_pve", 1) * 20;
		timeDecay			= config.getBoolean("general.time_decay.enabled");
		decayThresh			= config.getInt("general.time_decay.threshold", 3600);
		decayAmount			= config.getDouble("general.time_decay.seconds_lost", 15);
		firstJoinTime		= config.getLong("general.bonus.first_join", 0);
		legacyBonus			= config.getLong("general.bonus.daily_login", 0);
		shop				= config.getBoolean("shop.general.enabled", false);
		defaultSpeed		= config.getDouble("general.flight.default_speed", 1);
		
		
		protCommand			= config.getBoolean("general.damage.flight_disabled");
		protTime			= config.getBoolean("general.damage.out_of_time");
		protCombat			= config.getBoolean("general.damage.combat");
		//permaTimer			= config.getBoolean("general.damage.combat");
		
		actionBar			= config.getBoolean("aesthetic.action_bar.enabled");
		actionProgress		= config.getBoolean("aesthetic.action_bar.progress_bar");
		
		maxY				= config.getInt("general.maximum_height");
		
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
			Console.warn("There is a missing message in the file: (" + file.toString().toLowerCase() + ") | Path: (" + key + ")");
			if (missingMessages++ < 4) {Console.warn("THIS IS NOT AN ERROR, You simply need to add the missing message to the yaml file. Look on the tempfly page for an example config.");}
			return U.cc("&cThis message is broken! :(");
		}
	}
	
	public static String st(FileConfiguration config, String key){
		try{
			return U.cc(config.getString(key)).replaceAll("\\{PREFIX}", prefix);
		} catch (Exception e) {
			Console.warn("There is a missing message in the file: (" + config.getName() + ") | Path: (" + key + ")");
			if (missingMessages++ < 4) {Console.warn("THIS IS NOT AN ERROR, You simply need to add the missing message to the yaml file. Look on the tempfly page for an example config.");}
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
			if (missingMessages++ < 4) {Console.warn("THIS IS NOT AN ERROR, You simply need to add the missing message to the yaml file. Look on the tempfly page for an example config.");}
			return U.cc(def);
		}
	}
}
