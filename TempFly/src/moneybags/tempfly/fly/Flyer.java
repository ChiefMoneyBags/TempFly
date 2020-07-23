package moneybags.tempfly.fly;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.aesthetic.ActionBarAPI;
import moneybags.tempfly.aesthetic.TitleAPI;
import moneybags.tempfly.aesthetic.particle.Particles;
import moneybags.tempfly.environment.RelativeTimeRegion;
import moneybags.tempfly.hook.HookManager;
import moneybags.tempfly.hook.region.CompatRegion;
import moneybags.tempfly.time.TimeHandle;
import moneybags.tempfly.util.Console;
import moneybags.tempfly.util.U;
import moneybags.tempfly.util.V;
import moneybags.tempfly.util.data.DataBridge.DataValue;

public class Flyer {
	
	private Player p;
	private int idle = 0;
	private BukkitTask timer;
	private List<RelativeTimeRegion> rtEncompassing = new ArrayList<>();
	
	private double
		time, 
		start;
	
	private String
		listName,
		tagName,
		particle;
	
	
	public Flyer(Player p) {
		this.p = p;
		this.time = TimeHandle.getTime(p.getUniqueId());
		this.start = time;
		this.listName = p.getPlayerListName();
		this.tagName = p.getDisplayName();
		this.particle = Particles.loadTrail(p.getUniqueId());
		p.setAllowFlight(true);
		p.setFlying(!p.isOnGround());
		
		applySpeedCorrect();
		
		asessRtRegions();
		asessRtWorlds();
		this.timer = new Timer().runTaskTimer(TempFly.getInstance(), 0, 20);
	}
	
	public void applySpeedCorrect() {
		float maxSpeed = FlyHandle.getMaxSpeed(p);
		if (p.getFlySpeed() >= (maxSpeed * 0.1f)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					if (p.isOnline()) {
						p.setFlySpeed(maxSpeed * 0.1f);	
					}
				}
			}.runTaskLater(TempFly.getInstance(), 10);
		}
	}
	
	
	public boolean isFlying() {
		return p.isFlying();
	}
	
	public boolean isIdle() {
		return idle >= V.idleThreshold;
	}
	
	public void resetIdleTimer() {
		this.idle = 0;
	}
	
	public RelativeTimeRegion[] getRtEncompassing() {
		return rtEncompassing.toArray(new RelativeTimeRegion[rtEncompassing.size()]);
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public double getTime() {
		return time;
	}
	
	public void setTime(double time) {
		this.time = time;
		this.start = time;
	}
	
	
	public void asessRtWorlds() {
		for (RelativeTimeRegion rt : FlyHandle.getRtRegions()) {
			String world = p.getWorld().getName();
			if (rt.isWorld()) {
				String rtName = rt.getName();
				if ((rtName.equals(world)) && !(rtEncompassing.contains(rt))) {
					rtEncompassing.add(rt);
				} else if (!(rtName.equals(world)) && (rtEncompassing.contains(rt))) {
					rtEncompassing.remove(rt);
				}
			}
		}
	}
	
	public void asessRtRegions() {
		List<String> regions = new ArrayList<>();
		HookManager hooks = TempFly.getInstance().getHookManager();
		if (hooks.hasRegionProvider()) {
			for(CompatRegion r : hooks.getRegionProvider().getApplicableRegions(p.getLocation())) {
				regions.add(r.getId());
			}
		}	
		for (RelativeTimeRegion rt : FlyHandle.getRtRegions()) {
			String rtName = rt.getName();
			if (rt.isWorld()) {
				continue;
			}
			if ((regions.contains(rtName)) && !(rtEncompassing.contains(rt))) {
				rtEncompassing.add(rt);
			} else if (!(regions.contains(rtName)) && (rtEncompassing.contains(rt))) {
				rtEncompassing.remove(rt);	
			}
		}
	}
	
	/**
	 * Internal method, used to tell the flyer object when when it is being removed so it can clean itself up.
	 * @param drop
	 */
	public void onFlightDisabled(boolean drop) {
		timer.cancel();
		GameMode m = p.getGameMode();
		updateList(true);
		updateName(true);
		if (drop && (m != GameMode.CREATIVE || m != GameMode.SPECTATOR)) {
			p.setFlying(false);
			p.setAllowFlight(false);
		}
		
	}
	
	/**
	 * This method returns a string to keep the plugin compatible through versions.
	 * @return The enum string representation of the particle
	 */
	public String getTrail() {
		return particle;
	}
	
	/**
	 *  This method requires a string to keep the plugin compatible through versions.
	 *  The enum value of the particle as a string
	 * @param particle
	 */
	public void setTrail(String particle) {
		this.particle = particle;
		TempFly.getInstance().getDataBridge().stageChange(DataValue.PLAYER_TRAIL, particle, new String[] {p.getUniqueId().toString()});
	}
	
	public void playTrail() {
		if (particle == null || particle.length() == 0) {
			return;
		}
		if (V.hideVanish) {
			for (MetadataValue meta : p.getMetadata("vanished")) {
				if (meta.asBoolean()) {
					return;
				}
			}
		}
		Particles.play(p.getLocation(), particle);
	}
	
	private void updateList(boolean reset) {
		if (!V.list) {
			return;
		}
		if (!isFlying() || reset) {
			p.setPlayerListName(listName);
		} else {
			p.setPlayerListName(TimeHandle.regexString(V.listName
					.replaceAll("\\{PLAYER}", p.getName())
					.replaceAll("\\{OLD_TAG}", listName), time));
		}
	}
	
	private void updateName(boolean reset) {
		if (!V.tag) {
			return;
		}
		if (!isFlying() || reset) {
			p.setDisplayName(tagName);
		} else {
			p.setDisplayName(TimeHandle.regexString(V.tagName
					.replaceAll("\\{PLAYER}", p.getName())
					.replaceAll("\\{OLD_TAG}", tagName), time));
		}
	}
	
	public class Timer extends BukkitRunnable {
		
		@Override
		public void run() {
			p.setAllowFlight(true);
			if (p.hasPermission("tempfly.time.infinite")) {
				return;
			}
			idle++;
			updateList(false);
			updateName(false);
			
			if (checkIdle() || (!isFlying() && !V.groundTimer)) {
				return;
			}
			
			if (time > 0) {
				double cost = 1;
				for (RelativeTimeRegion rtr : rtEncompassing) {
					cost *= rtr.getFactor();
				}
			
				time = time-cost <= 0 ? 0 : time-cost;
				TempFly.getInstance().getDataBridge().stageChange(DataValue.PLAYER_TIME, time, new String[]{p.getUniqueId().toString()});
				
				if (time == 0) {
					if (!V.protTime) {
						FlyHandle.addDamageProtection(p);	
					}
					FlyHandle.removeFlyer(p);
					U.m(p, V.invalidTimeSelf);
				}
				
				if (V.warningTimes.contains((long)time)) {
					TitleAPI.sendTitle(p, 15, 30, 15,
							TimeHandle.regexString(V.warningTitle, time),
							TimeHandle.regexString(V.warningSubtitle, time));
				}
				if (V.actionBar) {
					doActionBar();
				}
			} else {
				if (!V.protTime) {
					FlyHandle.addDamageProtection(p);	
				}
				FlyHandle.removeFlyer(p);
				U.m(p, V.invalidTimeSelf);
			}
		}
		
		private boolean checkIdle() {
			if (isIdle()) {
				if (V.idleDrop) {
					FlyHandle.removeFlyer(p);
					return true;
				}
				if (!V.idleTimer) {
					return true;
				}
			}
			return false;
		}
		
		private void doActionBar() {
			if (V.actionProgress) {
				double percent = (((float)time/start)*100);
				StringBuilder bar = new StringBuilder();
				bar.append("&8[&a");
				boolean neg = true;
				for (double i = 0; i < 100; i += 7.69) {
					if ((percent <= i) && (neg)) {
						bar.append("&c");
						neg = false;
					}
					bar.append("=");
				}
				bar.append("&8]");
				ActionBarAPI.sendActionBar(p, U.cc(bar.toString()));
			} else {
				ActionBarAPI.sendActionBar(p, TimeHandle.regexString(V.actionText, getTime()));
			}
		}
	}
}
