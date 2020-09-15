package com.moneybags.tempfly.time;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.U;
import com.moneybags.tempfly.util.V;

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
		Bukkit.getScheduler().runTaskAsynchronously(tempfly, this);
	}
	
	private double maxTime;
	private double currentTime;
	
	@Override
	public void run() {
		maxTime = tempfly.getTimeManager().getMaxTime(p.getUniqueId());
		currentTime = tempfly.getTimeManager().getTime(p.getUniqueId());
		Bukkit.getScheduler().runTask(tempfly, () -> executor.execute(this));
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
