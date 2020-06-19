package com.moneybags.tempfly.fly;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.WorldGuardAPI;
import com.moneybags.tempfly.time.RelativeTimeRegion;
import com.moneybags.tempfly.time.TimeHandle;
import com.moneybags.tempfly.util.F;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class FlyHandle implements Listener {

	private static Map<Player, Flyer> flyers = new HashMap<>();
	private static Map<UUID, Boolean> cooldown = new HashMap<>();
	private static Map<UUID, BukkitTask> prot = new HashMap<>();
	
	private static List<RelativeTimeRegion> rtRegions = new ArrayList<>();
	
	private static List<String> blackRegion = new ArrayList<>();
	
	private static BukkitTask dailyTask;
	
	public static void wipe() {
		flyers = new HashMap<>();
	}
	
	public static void initialize() {
		blackRegion = F.config.contains("general.disabled.regions") ? F.config.getStringList("general.disabled.regions") : new ArrayList<>();
		ConfigurationSection csRtW = F.config.getConfigurationSection("general.relative_time.worlds");
		if (csRtW != null) {
			for (String s : csRtW.getKeys(false)) {
				rtRegions.add(new RelativeTimeRegion(
						F.config.getDouble("general.relative_time.worlds." + s, 1), true, s));
			}
		}
		ConfigurationSection csRtR = F.config.getConfigurationSection("general.relative_time.regions");
		if (csRtW != null) {
			for (String s : csRtR.getKeys(false)) {
				rtRegions.add(new RelativeTimeRegion(
						F.config.getDouble("general.relative_time.regions." + s, 1), false, s));
			}
		}
		dailyTask = new DailyTask().runTaskTimer(TempFly.plugin, 0, 1);
	}
	
	public static List<RelativeTimeRegion> getRtRegions() {
		return rtRegions;
	}
	
	public static void save() {
		FileConfiguration data = F.data;
		for (Flyer f: flyers.values()) {
			String path = "players." + f.getPlayer().getUniqueId().toString() + ".time";
			double time = f.getTime();
			data.set(path, time);
		}
		F.saveData();
	}
	
	public static void save(Flyer f) {
		FileConfiguration data = F.data;
		String path = "players." + f.getPlayer().getUniqueId().toString() + ".time";
		double time = f.getTime();
		data.set(path, time);
		F.saveData();
	}
	
	public static void addDamageProtection(Player p) {
		UUID u = p.getUniqueId();
		prot.put(u,
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (prot.containsKey(u)) {
					prot.remove(u);
				}
				
			}
		}.runTaskLater(TempFly.plugin, 120));
	}
	
	public static void removeDamageProtction(Player p) {
		UUID u = p.getUniqueId();
		if (prot.containsKey(u)) {
			prot.get(u).cancel();
			prot.remove(u);
		}
	}
	
	public static void addFlyer(Player p) {
		flyers.put(p, new Flyer(p));
	}
	
	public static void removeFlyer(Player p) {
		if (flyers.containsKey(p)) {
			Flyer f = flyers.get(p);
			save(f);
			f.removeFlyer();
			flyers.remove(p);
		}
	}
	
	public static Flyer getFlyer(Player p) {
		return flyers.containsKey(p) ? flyers.get(p) : null;
	}
	
	public static Flyer[] getFlyers() {
		return flyers.values().toArray(new Flyer[flyers.size()]);
	}
	
	public static boolean flyAllowed(Location loc) {
		String world = loc.getWorld().getName();
		for (String w: V.disabledWorlds) {
			if (w.equals(world)) {
				return false;
			}
		}
		
		if (WorldGuardAPI.isEnabled()) {
			ApplicableRegionSet prot = WorldGuardAPI.getRegionSet(loc);
			if (prot != null) {
				for(ProtectedRegion r : prot) {
					if (blackRegion.contains(r.getId())) {
						return false;
					}
				}	
			}
		}			
		
		return true;
	}
	
	public static float getMaxSpeed(Player p) {
		float speed = (float) ((V.defaultSpeed < 0) ? 0f : (p.isOp() || V.defaultSpeed > 10) ? 10f : V.defaultSpeed);
		if (!p.isOp()) {
			float max = speed;
			for (PermissionAttachmentInfo info: p.getEffectivePermissions()) {
				String perm = info.getPermission();
				if (perm.startsWith("tempfly.speed")) {
					String[] split = perm.split("\\.");
					try {
						float found = Float.parseFloat(split[2]);
						if (found > max) {
							max = found;
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
			if (speed < max) {
				speed = max;
			}
		}
		return speed;
	}
	
	public static boolean onCooldown(Player p) {
		return cooldown.containsKey(p.getUniqueId());
	}
	
	public static void addCooldown(Player p, int time, boolean flying) {
		UUID u = p.getUniqueId();
		if (cooldown.containsKey(u)) {
			return;
		}
		cooldown.put(u, flying);
		new BukkitRunnable() {
			
			@Override
			public void run() {
				if (cooldown.containsKey(u)) {
					if (cooldown.get(u).equals(true)) {
						Player n = Bukkit.getPlayer(u);
						if (n != null) {
							addFlyer(p);
							U.m(n, V.flyCooldownOver);
						}
					}
					cooldown.remove(u);
				}
			}
		}.runTaskLater(TempFly.plugin, time);
	}
	
	public static void disableFlyer(Flyer f) {
		removeFlyer(f.getPlayer());
		f.removeFlyer();
		U.m(f.getPlayer(), V.flyDisabledSelf);
	}
	
	public static String getPlaceHolder(Player p, Placeholder type) {
		Flyer f = getFlyer(p);
		double supply;
		if (f != null) {
			supply = f.getTime();
		} else {
			supply = TimeHandle.getTime(p.getUniqueId());
		}
		switch (type) {
		case TIME_FORMATTED:
		{
			long days = TimeHandle.formatTime(TimeUnit.DAYS, supply);
			long hours = TimeHandle.formatTime(TimeUnit.HOURS, supply);
			long minutes = TimeHandle.formatTime(TimeUnit.MINUTES, supply);
			long seconds = TimeHandle.formatTime(TimeUnit.SECONDS, supply);
			String s = "";
			if (U.hasPermission(p, "tempfly.time.infinite")) {
				s = V.infinity;
			} else {
				if (days > 0) {
					s = s.concat(V.fbDays.replaceAll("\\{DAYS}", String.valueOf(days)));
				}
				if (hours > 0) {
					s = s.concat(V.fbHours.replaceAll("\\{HOURS}", String.valueOf(hours)));
				}
				if (minutes > 0) {
					s = s.concat(V.fbMinutes.replaceAll("\\{MINUTES}", String.valueOf(minutes)));
				}
				if (seconds > 0 || s.length() == 0) {
					s = s.concat(V.fbSeconds.replaceAll("\\{SECONDS}", String.valueOf(seconds)));
				}
			}
			return s;
		}
		case TIME_DAYS:
		{
			long days = TimeHandle.formatTime(TimeUnit.DAYS, supply);
			return String.valueOf(days);
		}
		case TIME_HOURS:
		{
			long hours = TimeHandle.formatTime(TimeUnit.HOURS, supply);
			return String.valueOf(hours);
		}
		case TIME_MINUTES:
		{
			long minutes = TimeHandle.formatTime(TimeUnit.MINUTES, supply);
			return String.valueOf(minutes);
		}
		case TIME_SECONDS:
		{
			long seconds = TimeHandle.formatTime(TimeUnit.SECONDS, supply);
			return String.valueOf(seconds);
		}
		default:
			break;
		}
		return "broken message";
	}
	
	
	public static enum Placeholder {
		TIME_FORMATTED,
		TIME_DAYS,
		TIME_HOURS,
		TIME_MINUTES,
		TIME_SECONDS;
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onMove(PlayerMoveEvent e) {
		Location from = e.getFrom();
		Location to = e.getTo();
		if (from.getBlock().equals(to.getBlock())) {
			return;
		}
		Player p = e.getPlayer();
		Flyer f = getFlyer(p);
		if (f != null) {
			f.asessRtRegions();
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void on(PlayerTeleportEvent e) {
		Location from = e.getFrom();
		Location to = e.getTo();
		if (from.getBlock().equals(to.getBlock())) {
			return;
		}
		Player p = e.getPlayer();
		Flyer f = getFlyer(p);
		if (f != null) {
			f.asessRtRegions();
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void on(PlayerRespawnEvent e) {
		Location from = e.getPlayer().getLocation();
		Location to = e.getRespawnLocation();
		Player p = e.getPlayer();
		Flyer f = getFlyer(p);
		if (f != null) {
			f.asessRtRegions();
			if (!from.getWorld().equals(to.getWorld())) {
				f.asessRtWorlds();
			}
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerChangedWorldEvent e) {
		Player p = e.getPlayer();
		if (!flyers.containsKey(p)) {
			return;
		}
		
		Flyer f = getFlyer(p);
		if (!flyAllowed(p.getLocation())) {
			disableFlyer(f);
		}
		f.asessRtWorlds();
		
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerGameModeChangeEvent e) {
		Player p = e.getPlayer();
		if (!flyers.containsKey(p)) {
			return;
		}
		new BukkitRunnable() {
			
			@Override
			public void run() {
				p.setAllowFlight(true);
				p.setFlying(true);
			}
		}.runTaskLater(TempFly.plugin, 1);
	}
	
	//TODO some rand is using methods here deprecated in 1997
	@SuppressWarnings("deprecation")
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		
		if (V.timeDecay && p.hasPlayedBefore()) {
			long offline = (System.currentTimeMillis() - p.getLastPlayed()) / 1000;
			double lost = (offline / V.decayThresh) * V.decayAmount;
			double time = TimeHandle.getTime(p.getUniqueId());
			if (lost > time) {
				lost = time;
			}
			if (lost > 0) {
				TimeHandle.removeTime(p.getUniqueId(), lost);
				U.m(p, TimeHandle.regexString(V.timeDecayLost, lost));	
			}
		}
		
		
		if (!p.hasPlayedBefore() && V.firstJoinTime > 0) {
			TimeHandle.addTime(p.getUniqueId(), V.firstJoinTime);
			U.m(p, TimeHandle.regexString(V.firstJoin, V.firstJoinTime));
		}
		
		
		Date lj = new Date(p.getLastPlayed());
		Date ct = new Date(System.currentTimeMillis());
		if ((lj.getDate() != ct.getDate())
				|| ((lj.getDate() == ct.getDate()) && (lj.getMonth() != ct.getMonth()))) {
			loginBonus(p);
		}
		
		
		GameMode m = p.getGameMode();
		if (!(m.equals(GameMode.CREATIVE)) && !(m.equals(GameMode.SPECTATOR))) {
			p.setFlying(false);
			p.setAllowFlight(false);
		}
		
		regainFlightDisconnect(p);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (getFlyer(p) != null) {
			addFlightDisconnect(p);
			removeFlyer(p);
		}
	}
	
	public static void addFlightDisconnect(Player p) {
		if (!flyers.containsKey(p)) 
			return;
		F.data.set("players." + p.getUniqueId() + ".logged_in_flight", true);
		F.saveData();
	}
	
	public static void regainFlightDisconnect(Player p) {
		if (!flyers.containsKey(p)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					GameMode m = p.getGameMode();
					if (!(m.equals(GameMode.CREATIVE)) && !(m.equals(GameMode.SPECTATOR))) {
						p.setFlying(false);
						p.setAllowFlight(false);
					}
					if (F.data.getBoolean("players." + p.getUniqueId() + ".logged_in_flight")) {
						if ((TimeHandle.getTime(p.getUniqueId()) > 0) && (!p.isOnGround())) {
							addFlyer(p);	
						}
						F.data.set("players." + p.getUniqueId() + ".logged_in_flight", false);
						F.saveData();
					}
				}
			}.runTaskLater(TempFly.plugin, 1);
		}
	}
	
	public static void loginBonus(Player p) {
		if (V.legacyBonus > 0) {
			TimeHandle.addTime(p.getUniqueId(), V.legacyBonus);
			U.m(p, TimeHandle.regexString(V.dailyLogin, V.legacyBonus));	
		} else if (V.dailyBonus.size() > 0) {
			double time = 0;
			
			for (Entry<String, Double> entry: V.dailyBonus.entrySet()) {
				if (p.hasPermission("tempfly.bonus." + entry.getKey())) {
					time += entry.getValue();
				}
			}
			
			if (time > 0) {
				TimeHandle.addTime(p.getUniqueId(), time);
				U.m(p, TimeHandle.regexString(V.dailyLogin, time));	
			}
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerMoveEvent e) {
		if (e.getFrom().getBlock().equals(e.getTo().getBlock())) {
			return;
		}
		Player p = e.getPlayer();
		if (!flyers.containsKey(p)) {
			return;
		}
		Flyer f = getFlyer(p);
		f.resetIdleTimer();
		if (p.getLocation().getBlockY() > V.maxY) {
			p.setFlying(false);
		}
		
		if (WorldGuardAPI.isEnabled()) {
			ApplicableRegionSet prot = WorldGuardAPI.getRegionSet(e.getTo());
			if (prot != null) {
				for(ProtectedRegion r : prot) {
					if (blackRegion.contains(r.getId())) {
						removeFlyer(p);
					}
					if (TempFly.getAskyblockHook() != null) {
						TempFly.getAskyblockHook().checkFlightRequirement(p, p.getLocation());
					}
				}	
			}
		}
	}

	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!flyers.containsKey(p)) {
			return;
		}
		flyers.get(p).resetIdleTimer();
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if (!flyers.containsKey(p)) {
			return;
		}
		flyers.get(p).resetIdleTimer();
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) {
			return;
		}
		Player p = (Player)e.getWhoClicked();
		if (!flyers.containsKey(p)) {
			return;
		}
		flyers.get(p).resetIdleTimer();
	}
	
	public static enum CombatType {
		PLAYER_ATTACKS_FLYER(true),
		MOB_ATTACKS_FLYER(false),
		FLYER_ATTACKS_PLAYER(true),
		FLYER_ATTACKS_MOB(false);
		
		private boolean pvp;
		
		private CombatType(boolean pvp) {
			this.pvp = pvp;
		}
		
		public boolean isPvp() {
			return pvp;
		}
	}
	
	public static void processCombat(Entity vic, Entity act) {
		if (act instanceof Arrow) {
			if (!(((Arrow)act).getShooter() instanceof Entity)) {
				return;
			}
			act = (Entity) ((Arrow)act).getShooter();
		}
		if (vic instanceof Player) {
			if (act instanceof Player) {
				onCombat(CombatType.PLAYER_ATTACKS_FLYER, vic, act);
				onCombat(CombatType.FLYER_ATTACKS_PLAYER, vic, act);
			} else if (act instanceof LivingEntity) {
				onCombat(CombatType.MOB_ATTACKS_FLYER, vic, act);
			}
		} else if (vic instanceof LivingEntity) {
			if (act instanceof Player) {
				onCombat(CombatType.FLYER_ATTACKS_MOB, vic, act);
			}
		}
	}		
	
	public static void onCombat(CombatType type, Entity vic, Entity act) {
		if (!combatDisable(type)) {
			return;
		}
		
		Player p = (type == CombatType.FLYER_ATTACKS_MOB || type == CombatType.FLYER_ATTACKS_PLAYER) ? (Player)act : (Player)vic;
		Flyer f = getFlyer(p);
		
		if (f != null) {
			if (!V.protCombat) {
				FlyHandle.addDamageProtection(p);
			}
			removeFlyer(p);
			f.removeFlyer();
			U.m(p, V.flyDisabledSelf);	
		}
		addCooldown(p, type.isPvp() ? V.cooldownPvp : V.cooldownPve, f != null);
	}
	
	public static boolean combatDisable(CombatType type) {
		switch (type) {
		case FLYER_ATTACKS_MOB:
			return V.attackM;
		case FLYER_ATTACKS_PLAYER:
			return V.attackP;
		case MOB_ATTACKS_FLYER:
			return V.attackedM;
		case PLAYER_ATTACKS_FLYER:
			return V.attackedP;
		}
		return false;
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(EntityDamageByEntityEvent e) {
		Entity vic = e.getEntity();
		Entity act = e.getDamager();
		processCombat(vic, act);
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void on(EntityDamageEvent e) {
		Entity vic = e.getEntity();
		if (!(vic instanceof Player)) {
			return;
		}
		Player p = (Player)vic;
		UUID u = p.getUniqueId();
		if (!prot.containsKey(u)) {
			return;
		}
		DamageCause cause = e.getCause();
		if (!cause.equals(DamageCause.FALL)) {
			return;
		}
		e.setCancelled(true);
		removeDamageProtction(p);
	}
	

	 
	//TODO If a player joins on the same tick that a new day starts they will receive the daily login bonus 2 times.
	// Very rare, almost impossible.
	
	// If the player is on through midnight and receive the bonus through the task, they cannot relog and get the
	// bonus again as their last played time will be the same day, not a new day.
	public static class DailyTask extends BukkitRunnable {

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			Date lj = new Date(System.currentTimeMillis() - 1000);
			Date ct = new Date(System.currentTimeMillis());
			if (lj.getDate() != ct.getDate()) {
				for (Player p: Bukkit.getOnlinePlayers()) {
					loginBonus(p);
				}
			}
		}
	}
	
	public static BukkitTask getDailyTask() {
		return dailyTask;
	}
}
