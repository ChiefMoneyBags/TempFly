package com.moneybags.tempfly.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult;
import com.moneybags.tempfly.fly.result.FlightResult.DenyReason;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;

public class CombatHandler implements RequirementProvider, Listener {

	private FlightManager manager;
	
	private Map<UUID, CombatTag> tags = new HashMap<>();
	
	public CombatHandler(FlightManager manager) {
		this.manager = manager;
		manager.getTempFly().getServer().getPluginManager().registerEvents(this, manager.getTempFly());
	}
	
	public void processCombat(Entity vic, Entity act) {
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
	
	public void onCombat(CombatType type, Entity vic, Entity act) {
		if (!combatDisable(type)) {
			return;
		}
		
		Player p = (type == CombatType.FLYER_ATTACKS_MOB || type == CombatType.FLYER_ATTACKS_PLAYER) ? (Player)act : (Player)vic;
		FlightUser user = manager.getUser(p);
		
		addTag(p.getUniqueId(), type.isPvp() ? V.combatTagPvp : V.combatTagPve);
		user.submitFlightResult(new ResultDeny(DenyReason.COMBAT, this, InquiryType.OUT_OF_SCOPE, V.requireFailCombat, !V.damageCombat));
	}
	
	public boolean combatDisable(CombatType type) {
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
	
	public enum CombatType {
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
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(EntityDamageByEntityEvent e) {
		Entity vic = e.getEntity();
		Entity act = e.getDamager();
		processCombat(vic, act);
	}
	
	public boolean isTagged(UUID u) {
		return tags.containsKey(u);
	}
	
	public CombatTag getTag(UUID u) {
		return tags.get(u);
	}
	
	public void cancelTag(UUID u) {
		if (isTagged(u)) {
			getTag(u).getTask().cancel();
			tags.remove(u);
		}
	}
	
	public void addTag(UUID u, int time) {
		Console.debug("Adding combat tag for player, time=: " + time);
		tags.put(u, new CombatTag(time, 
			new BukkitRunnable() {
			@Override
			public void run() {
				if (isTagged(u)) {
					tags.remove(u);
					Player p = Bukkit.getPlayer(u);
					if (p != null && p.isOnline()) {
						evaluate(manager.getUser(p));	
					}
				}
			}
		}.runTaskLater(manager.getTempFly(), time)));
	}
	
	private void evaluate(FlightUser user) {
		if (user == null) {
			return;
		}
		if (!isTagged(user.getPlayer().getUniqueId())) {
			if (user.hasFlightRequirement(this)) {
				Console.debug("", "--|> User has combat requirement but is no longer tagged!");
				user.submitFlightResult(new ResultAllow(this, InquiryType.OUT_OF_SCOPE, V.requirePassCombat));	
			}
			return;
		} 
		user.submitFlightResult(new ResultDeny(DenyReason.COMBAT, this, InquiryType.OUT_OF_SCOPE, V.requireFailCombat, !V.damageCombat));
	}
	
	@Override
	public boolean handles(InquiryType type) {
		return true;
	}

	@Override
	public void onUserInitialized(FlightUser user) {
		if (tags.containsKey(user.getPlayer().getUniqueId())) {
			user.submitFlightResult(new ResultDeny(DenyReason.COMBAT, this, InquiryType.OUT_OF_SCOPE, V.requireFailCombat, false));
		}
	}
}
