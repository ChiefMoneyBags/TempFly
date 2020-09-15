package com.moneybags.tempfly.user;

import org.bukkit.entity.Player;

import com.moneybags.tempfly.aesthetic.particle.Particles;
import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.data.DataBridge;
import com.moneybags.tempfly.util.data.DataPointer;
import com.moneybags.tempfly.util.data.DataBridge.DataValue;

public class UserLoader implements Runnable {

	private Player p;
	private FlightManager manager;
	private FlightUser user;
	
	public UserLoader(Player p, FlightManager manager) {
		this.p = p;
		this.manager = manager;
	}

	@Override
	public void run() {
		final DataBridge bridge = manager.getTempFly().getDataBridge();
		final TimeManager timeManager = manager.getTempFly().getTimeManager();
		
		double time = timeManager.getTime(p.getUniqueId());
		String particle = Particles.loadTrail(p.getUniqueId());
		boolean infinite = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_INFINITE, p.getUniqueId().toString()), true); 
		boolean bypass = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_BYPASS, p.getUniqueId().toString()), true);
		boolean logged = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_FLIGHT_LOG, p.getUniqueId().toString()), false);
		
		user = new FlightUser(p, manager, time, particle, infinite, bypass, logged);
		manager.addUser(this);
	}
	
	public FlightUser getResult() {
		return user;
	}

}
