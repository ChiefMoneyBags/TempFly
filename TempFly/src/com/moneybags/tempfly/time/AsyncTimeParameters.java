package com.moneybags.tempfly.time;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.TempFly;

public class AsyncTimeParameters implements Runnable {

	private TempFly tempfly;
	private CommandSender s;
	private OfflinePlayer p;
	private double amount;
	private AsyncTimeExecutor executor;
	
	public AsyncTimeParameters(TempFly tempfly, AsyncTimeExecutor executor, CommandSender s, OfflinePlayer p, double amount) {
		this.tempfly = tempfly;
		this.executor = executor;
		this.s = s;
		this.p = p;
		this.amount = amount;
	}
	
	public AsyncTimeParameters(TempFly tempfly, AsyncTimeExecutor executor, OfflinePlayer p, double amount) {
		this.tempfly = tempfly;
		this.executor = executor;
		this.p = p;
		this.amount = amount;
	}
	
	private double maxTime;
	private double currentTime;
	
	
	public void runAsync() {
		Bukkit.getScheduler().runTaskAsynchronously(tempfly, this);
	}
	
	@Override
	public void run() {
		if (Bukkit.isPrimaryThread()) {
			this.runAsync();
			return;
		}
		maxTime = tempfly.getTimeManager().getMaxTime(p.getUniqueId());
		currentTime = tempfly.getTimeManager().getTime(p.getUniqueId());
		
		Bukkit.getScheduler().runTask(tempfly, () -> {
			executor.execute(this);
		});
	}
	
	public TempFly getTempfly() {
		return tempfly;
	}

	public double getMaxTime() {
		return maxTime;
	}

	public double getCurrentTime() {
		return currentTime;
	}
	
	public CommandSender getSender() {
		return s;
	}
	
	public OfflinePlayer getTarget() {
		return p;
	}
	
	public double getAmount() {
		return amount;
	}
} 
