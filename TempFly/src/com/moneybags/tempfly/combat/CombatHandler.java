package com.moneybags.tempfly.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.fly.RequirementProvider;
import com.moneybags.tempfly.fly.result.FlightResult.DenyReason;
import com.moneybags.tempfly.fly.result.ResultAllow;
import com.moneybags.tempfly.fly.result.ResultDeny;
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
	
	public FlightManager getFlightManager() {
		return manager;
	}
	
	public void processCombat(Entity vic, Entity act) {
		if (act instanceof Projectile) {
			if (!(((Projectile)act).getShooter() instanceof Entity)) {
				return;
			}
			act = (Entity) ((Projectile)act).getShooter();
		}
		if (vic instanceof Player) {
			if (act instanceof Player) {
				if (act.equals(vic)) {
					onCombat(CombatType.FLYER_ATTACKS_SELF, vic, act);
				} else {
					onCombat(CombatType.PLAYER_ATTACKS_FLYER, vic, act);
					onCombat(CombatType.FLYER_ATTACKS_PLAYER, vic, act);	
				}
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
		
		if (!(act instanceof Player) && !(vic instanceof Player)) {
			return;
		}
		
		Player p = (type == CombatType.FLYER_ATTACKS_MOB || type == CombatType.FLYER_ATTACKS_PLAYER) ? (Player)act : (Player)vic;
		FlightUser user = manager.getUser(p);
		if (user == null) {
			return;
		}
		addTag(p.getUniqueId(), type.isPvp() ? V.combatTagPvp : V.combatTagPve);
		user.submitFlightResult(new ResultDeny(DenyReason.COMBAT, this, InquiryType.OUT_OF_SCOPE, V.requireFailCombat, !V.damageCombat));
	}
	
	public boolean combatDisable(CombatType type) {
		switch (type) {
		case FLYER_ATTACKS_MOB:
			return V.tagAttackMob;
		case FLYER_ATTACKS_PLAYER:
			return V.tagAttackPlayer;
		case MOB_ATTACKS_FLYER:
			return V.tagAttackedByMob;
		case PLAYER_ATTACKS_FLYER:
			return V.tagAttackedByPlayer;
		case FLYER_ATTACKS_SELF:
			return V.tagAttackedBySelf;
		}
		return false;
	}
	
	public enum CombatType {
		PLAYER_ATTACKS_FLYER(true),
		MOB_ATTACKS_FLYER(false),
		FLYER_ATTACKS_PLAYER(true),
		FLYER_ATTACKS_MOB(false),
		FLYER_ATTACKS_SELF(true);
		
		private boolean pvp;
		
		private CombatType(boolean pvp) {
			this.pvp = pvp;
		}
		
		public boolean isPvp() {
			return pvp;
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void on(PlayerDeathEvent e) {
		Player p = e.getEntity();
		if (isTagged(p.getUniqueId())) {
			cancelTag(p.getUniqueId());
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
			getTag(u).cancel();
			tags.remove(u);
		}
		
		Player p = Bukkit.getPlayer(u);
		if (p != null && p.isOnline()) {
			evaluate(manager.getUser(p));	
		}
	}
	
	public void addTag(UUID u, int time) {
		Console.debug("Adding combat tag for player, time=: " + time);
		if (isTagged(u)) {
			Console.debug("Player is already tagged!");
			CombatTag current = getTag(u);
			// If combat PvP is longer than PvE, we don't want users escaping PvP by attacking a sheep and having their combat time reset to the PvE timer.
			if (current.getRemainingTime() > time) {
				Console.debug("Current tag has more time remaining than the new tag! returning.");
				return;
			}
			Console.debug("Canceling players current tag!");
			current.cancel();
		}
		tags.put(u, new CombatTag(u, time, this));
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
