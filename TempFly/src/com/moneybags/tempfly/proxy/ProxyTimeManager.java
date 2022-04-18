package com.moneybags.tempfly.proxy;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import com.moneybags.tempfly.time.AsyncTimeParameters;

public class ProxyTimeManager {

	private TempFlyProxy proxy;
	private ExecutorService executor;
	
	public ProxyTimeManager(TempFlyProxy proxy) {
		this.proxy = proxy;
		this.executor = Executors.newCachedThreadPool();
	}
	
	public <T> void getTime(UUID u, Consumer<T> c) {
		executor.submit(() -> {
			if (!proxy.isPlayerOnline(u)) {
				
			}
			
		});
	}
	
	/**
	 * Set a users time.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The new seconds
	 */
	public void removeTime(UUID u, AsyncTimeParameters parameters) {

	}
	
	/**
	 * Add time to a user.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The seconds to add
	 */
	public void addTime(UUID u, AsyncTimeParameters parameters) {

	}
	
	/**
	 * Set a users time.
	 * If the user is online it will also update their FlightUser object with the new time
	 * stages the new time to the DataBridge.
	 * @param u the uuid of the player
	 * @param seconds The new seconds
	 */
	public void setTime(UUID u, AsyncTimeParameters parameters) {

	}
	
}
