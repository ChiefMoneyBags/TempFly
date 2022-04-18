package com.moneybags.tempfly.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.data.config.ConfigProvider;
import com.moneybags.tempfly.util.data.config.ConfigSection;

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
		
		ConfigProvider provider = TempFly.getPlugin(TempFly.class).getConfigProvider();
		
		prefix 				= st(C.LANG, "system.prefix", provider);
		reload 				= st(C.LANG, "system.reload", provider);
		
		invalidParticle		= st(C.LANG, "general.invalid.particle", provider);
		invalidPermission	= st(C.LANG, "general.invalid.permission", provider);
		invalidPlayer		= st(C.LANG, "general.invalid.player", provider);
		invalidNumber		= st(C.LANG, "general.invalid.number", provider);
		invalidSender		= st(C.LANG, "general.invalid.sender", provider);
		invalidCommand		= st(C.LANG, "general.invalid.command", provider);
		invalidTimeOther	= st(C.LANG, "general.invalid.time_other", provider);
		invalidTimeSelf		= st(C.LANG, "general.invalid.time_self", provider);
		invalidReciever		= st(C.LANG, "general.invalid.reciever", provider);
		invalidFlyerSelf	= st(C.LANG, "general.invalid.flyer_self", provider);
		invalidFunds		= st(C.LANG, "general.invalid.funds", provider);
		invalidEconomy		= st(C.LANG, "general.invalid.economy", provider);
		vaultPermsRequired  = st(C.LANG, "general.invalid.vault_perms", provider);
		
		timeGivenOther		= st(C.LANG, "general.time.given_other", provider);
		timeGivenSelf		= st(C.LANG, "general.time.given_self", provider);
		timeRemovedOther	= st(C.LANG, "general.time.removed_other", provider);
		timeRemovedSelf		= st(C.LANG, "general.time.removed_self", provider);
		timeSentOther		= st(C.LANG, "general.time.sent_other", provider);
		timeSentSelf		= st(C.LANG, "general.time.sent_self", provider);
		timeSetOther		= st(C.LANG, "general.time.set_other", provider);
		timeSetSelf			= st(C.LANG, "general.time.set_self", provider);
		timeMaxOther		= st(C.LANG, "general.time.max_other", provider);
		timeMaxSelf			= st(C.LANG, "general.time.max_self", provider);
		timeDecayLost		= st(C.LANG, "general.time.decay", provider);
		timeFormat			= st(C.LANG, "general.time.format", provider);
		timePurchased		= st(C.LANG, "general.time.purchased", provider);
		firstJoin			= st(C.LANG, "general.time.first_join", provider);
		dailyLogin			= st(C.LANG, "general.time.daily_login", provider);
		
		unitSeconds			= st(C.LANG, "general.unit.seconds", "s", provider);
		unitMinutes			= st(C.LANG, "general.unit.minutes", "m", provider);
		unitHours			= st(C.LANG, "general.unit.hours", "h", provider);
		unitDays			= st(C.LANG, "general.unit.days", "d", provider);
		
		infoHeader			= st(C.LANG, "general.info.header", provider);
		infoPlayer			= st(C.LANG, "general.info.player", provider);
		infoDays			= st(C.LANG, "general.info.days", provider);
		infoHours			= st(C.LANG, "general.info.hours", provider);
		infoMinutes			= st(C.LANG, "general.info.minutes", provider);
		infoSeconds			= st(C.LANG, "general.info.seconds", provider);
		infoFooter			= st(C.LANG, "general.info.footer", provider);
		infoInfinite		= st(C.LANG, "general.info.infinite", provider);
		
		flyEnabledOther	    = st(C.LANG, "general.fly.enabled_other", provider);
		flyEnabledSelf	    = st(C.LANG, "general.fly.enabled_self", provider);
		flyDisabledOther	= st(C.LANG, "general.fly.disabled_other", provider);
		flyDisabledSelf 	= st(C.LANG, "general.fly.disabled_self", provider);
		flySpeedOther		= st(C.LANG, "general.fly.speed_other", provider);
		flySpeedSelf		= st(C.LANG, "general.fly.speed_self", provider);
		flySpeedLimitOther	= st(C.LANG, "general.fly.speed_limit_other", provider);
		flySpeedLimitSelf	= st(C.LANG, "general.fly.speed_limit_self", provider);
		flySpeedDenied		= st(C.LANG, "general.fly.speed_restricted", provider);
		flyAlreadyEnabled	= st(C.LANG, "general.fly.already_enabled", provider);
		flyAlreadyDisabled	= st(C.LANG, "general.fly.already_disabled", provider);
		flyInfiniteEnabled	= st(C.LANG, "general.fly.infinite_enabled", provider);
		flyInfiniteDisabled	= st(C.LANG, "general.fly.infinite_disabled", provider);
		flyBypassEnabled	= st(C.LANG, "general.fly.bypass_enabled", provider);
		flyBypassDisabled	= st(C.LANG, "general.fly.bypass_disabled", provider);
		
		disabledIdle 		= st(C.LANG, "general.fly.idle_drop", provider);
		consideredIdle 		= st(C.LANG, "general.fly.idle", provider);
		
		requireFailOther	= st(C.LANG, "general.requirement.fail.default_other", provider);
		requireFailDefault	= st(C.LANG, "general.requirement.fail.default", provider);
		requirePassDefault	= st(C.LANG, "general.requirement.pass.default", provider);
		requireFailCombat	= st(C.LANG, "general.requirement.fail.combat", provider);
		requirePassCombat	= st(C.LANG, "general.requirement.pass.combat", provider);
		requireFailRegion	= st(C.LANG, "general.requirement.fail.region", provider);
		requireFailWorld	= st(C.LANG, "general.requirement.fail.world", provider);
		requireFailHeight	= st(C.LANG, "general.requirement.fail.height", provider);
		requireFailStruct   = st(C.LANG, "general.requirement.fail.structure", provider);
		
		fbDays				= st(C.LANG, "aesthetic.featherboard.days", provider);
		fbHours				= st(C.LANG, "aesthetic.featherboard.hours", provider);
		fbMinutes			= st(C.LANG, "aesthetic.featherboard.minutes", provider);
		fbSeconds			= st(C.LANG, "aesthetic.featherboard.seconds", provider);
		infinity			= st(C.LANG, "aesthetic.symbols.infinity", provider);
		
		warningTitle		= st(C.CONFIG, "aesthetic.warning.title", provider);
		warningSubtitle		= st(C.CONFIG, "aesthetic.warning.subtitle", provider);
		
		actionText			= st(C.CONFIG, "aesthetic.action_bar.text", provider);
		
		trailRemovedSelf	= st(C.LANG, "aesthetic.trail.removed_self", provider);
		trailRemovedOther	= st(C.LANG, "aesthetic.trail.removed_other", provider);
		trailSetSelf		= st(C.LANG, "aesthetic.trail.set_self", provider);
		trailSetOther		= st(C.LANG, "aesthetic.trail.set_other", provider);
		
		ConfigSection config = provider.getDefaultConfig();
		ConfigSection lang = provider.getConfig("lang.yml");
		
		List<String> h 		= lang.getStringList("system.help");
		if (h != null) {
			for (String s: h) {
				help.add(U.cc(s));
			}
		}
		
		List<String> he 	= lang.getStringList("system.help_extended");
		if (he != null) {
			for (String s: he) {
				helpExtended.add(U.cc(s));
			}
		}
		
		try {
			warningTimes    = config.getLongList("aesthetic.warning.seconds");
		} catch (Exception e) {
			warningTimes = new ArrayList<>();
			Console.warn("You can only set numbers under (aesthetic.warning.seconds) in the config!");
		}
		
		
		disabledWorlds	 	= config.getStringList("general.disabled.worlds");
		if (disabledWorlds == null) {
			disabledWorlds = new ArrayList<>();
		}
		
		disabledRegions	 	= config.getStringList("general.disabled.regions");
		if (disabledRegions == null) {
			disabledRegions = new ArrayList<>();
		}
		
		overrideFlightPermissions = config.getStringList("general.fly_override_permissions");
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
		listName			= st(C.CONFIG, "aesthetic.identifier.tab_list.name", provider);
		listPlaceholderOn	= st(C.CONFIG, "aesthetic.identifier.tab_list.placeholder.enabled", provider);
		listPlaceholderOff	= st(C.CONFIG, "aesthetic.identifier.tab_list.placeholder.disabled", provider);
		tag					= config.getBoolean("aesthetic.identifier.name_tag.enabled");
		tagName				= st(C.CONFIG, "aesthetic.identifier.name_tag.name", provider);
		tagPlaceholderOn	= st(C.CONFIG, "aesthetic.identifier.name_tag.placeholder.enabled", provider);
		tagPlaceholderOff	= st(C.CONFIG, "aesthetic.identifier.name_tag.placeholder.disabled", provider);
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
			ConfigSection csPerms = config.getConfigSection("general.bonus.daily_login");
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
		ConfigSection csMax = config.getConfigSection("general.time.max.groups");
		if (csMax != null) {
			for (String s: csMax.getKeys(false)) {
				maxTimeGroups.put(s, config.getDouble("general.time.max.groups." + s));
			}
		}
	}
	
	public static enum C {
		CONFIG("config.yml"),
		LANG("lang.yml");
		
		private String file;
		
		private C(String file) {
			this.file = file;
		}
		
		public String getFileName() {
			return file;
		}
	}
	
	private static String st(C file, String key, ConfigProvider provider) {
		try { return U.cc(provider.getConfig(file.getFileName()).getString(key)).replaceAll("\\{PREFIX}", prefix); } catch (Exception e) {
			Console.warn("There is a missing message in the file: (" + file.getFileName() + ") | Path: (" + key + ")");
			return U.cc("&cThis message is broken! :(");
		}
	}
	
	public static String st(FileConfiguration config, String key, String fileName){
		try{ return U.cc(config.getString(key)).replaceAll("\\{PREFIX}", prefix); } catch (Exception e) {
			Console.warn("There is a missing message in the file: (" + config.getName() + ") | Path: (" + key + ")");
			return U.cc("&cThis message is broken! :(");
		}
	}
	
	private static String st(C file, String key, String def, ConfigProvider provider){
		try { return U.cc(provider.getConfig(file.getFileName()).getString(key)).replaceAll("\\{PREFIX}", prefix); } catch (Exception e) {
			Console.warn("There is a missing message in the file: (" + file.getFileName() + ") | Path: (" + key + ")");
			return U.cc(def);
		}
	}
}
