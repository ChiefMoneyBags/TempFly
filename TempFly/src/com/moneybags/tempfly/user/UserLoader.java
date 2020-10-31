package com.moneybags.tempfly.user;

import java.util.UUID;

import org.bukkit.Bukkit;
import com.moneybags.tempfly.aesthetic.particle.Particles;
import com.moneybags.tempfly.fly.FlightManager;
import com.moneybags.tempfly.time.TimeManager;
import com.moneybags.tempfly.util.data.DataBridge;
import com.moneybags.tempfly.util.data.DataPointer;
import com.moneybags.tempfly.util.data.DataBridge.DataValue;

public class UserLoader implements Runnable {

	private UUID u;
	private FlightManager manager;
	private boolean async;
	
	public UserLoader(UUID u, FlightManager manager, boolean async) {
		this.u = u;
		this.manager = manager;
		this.async = async;
	}
	
	double time;
	
	String particle;
	
	boolean
	infinite,
	bypass,
	logged,
	ready;
	
	@Override
	public void run() {
		final DataBridge bridge = manager.getTempFly().getDataBridge();
		final TimeManager timeManager = manager.getTempFly().getTimeManager();
		
		time = timeManager.getTime(u);
		particle = Particles.loadTrail(u);
		infinite = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_INFINITE, u.toString()), true); 
		bypass = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_BYPASS, u.toString()), true);
		logged = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_FLIGHT_LOG, u.toString()), false);
		ready = true;
		if (async) {
			manager.addUser(Bukkit.getPlayer(u));
		}
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public FlightUser buildUser() {
		return new FlightUser(Bukkit.getPlayer(u), manager, time, particle, infinite, bypass, logged);
	}
	

}
