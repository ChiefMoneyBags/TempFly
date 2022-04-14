package com.moneybags.tempfly.user;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
	compatLogged,
	ready;
	
	double
	selectedSpeed;
	
	@Override
	public void run() {
		final DataBridge bridge = manager.getTempFly().getDataBridge();
		final TimeManager timeManager = manager.getTempFly().getTimeManager();
		
		if (bridge.hasSqlEnabled()) {
			PreparedStatement st = bridge.prepareStatement("INSERT IGNORE INTO tempfly_data(uuid) VALUES(?)");
			try {
				st.setString(1, u.toString());
				st.execute();
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
				return;
			}
			
		}
		
		
		time = timeManager.getTime(u);
		particle = Particles.loadTrail(u);
		infinite = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_INFINITE, u.toString()), true); 
		bypass = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_BYPASS, u.toString()), true);
		logged = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_FLIGHT_LOG, u.toString()), false);
		compatLogged = (boolean) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_COMPAT_FLIGHT_LOG, u.toString()), false);
		selectedSpeed = (double) bridge.getOrDefault(DataPointer.of(DataValue.PLAYER_SPEED, u.toString()), -999D);
		ready = true;
		if (async) {
			manager.addUser(Bukkit.getPlayer(u));
		}
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public FlightUser buildUser() {
		return new FlightUser(Bukkit.getPlayer(u), manager, time, particle, infinite, bypass, logged, compatLogged, selectedSpeed);
	}
	
	public FlightUser buildUser(Player p) {
		return new FlightUser(p, manager, time, particle, infinite, bypass, logged, compatLogged, selectedSpeed);
	}
	

}
