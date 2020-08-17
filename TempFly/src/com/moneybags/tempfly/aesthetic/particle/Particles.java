package com.moneybags.tempfly.aesthetic.particle;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.user.FlightUser;
import com.moneybags.tempfly.util.Console;
import com.moneybags.tempfly.util.V;
import com.moneybags.tempfly.util.data.DataBridge.DataValue;
import com.moneybags.tempfly.util.data.DataPointer;

public class Particles {

	private static Class<?>
	dustOptions = null,
	blockData;
	private static TempFly tempfly;
	private static boolean oldParticles;
	
	public static void initialize(TempFly plugin) {
		tempfly = plugin;
		try {dustOptions = Class.forName("org.bukkit.Particle$DustOptions");} catch (Exception e) {}
		try {blockData = Class.forName("org.bukkit.block.data.BlockData");} catch (Exception e) {}
		oldParticles = oldParticles();
	}
	
	public static boolean oldParticles() {
		String version = Bukkit.getVersion();
		return (version.contains("1.6")) || (version.contains("1.7")) || (version.contains("1.8")) || version.contains("1.9");
	}
	
	public static void play(Location loc, String s) {
		if (!oldParticles) {
			Particle particle = null;
			try {particle = Particle.valueOf(s.toUpperCase());} catch (Exception e1) {
				try {particle = Particle.valueOf(V.particleType.toUpperCase());} catch (Exception e2) {
					particle = Particle.VILLAGER_HAPPY;
				};
			}
			
			Class<?> c = particle.getDataType();
			try {
				if (dustOptions != null && dustOptions.equals(c)) {
					Random rand = new Random();
					loc.getWorld().spawnParticle(particle, loc, 1, new DustOptions(Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), 2f));	
				} else if (blockData != null && blockData.equals(c)) {
					loc.getWorld().spawnParticle(particle, loc, 1, Material.STONE.createBlockData());	
				} else {
					loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0.1);
				}
			} catch (Exception e) {
				loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0.1);
			}
		} else {
			Effect particle = null;
			// This effect value crashes clients and prevents them from joining the server again.
			if (s != null && s.equalsIgnoreCase("ITEM_BREAK")) {
				s = "HAPPY_VILLAGER";
			}
			try {particle = Effect.valueOf(s.toUpperCase());} catch (Exception e1) {
				try {particle = Effect.valueOf(V.particleType);} catch (Exception e2) {
					particle = Effect.valueOf("HAPPY_VILLAGER");	
				}
			}
			loc.getWorld().playEffect(loc, particle, 1);
		}
	}
	
	public static String loadTrail(UUID u) {
		String particle = (String) tempfly.getDataBridge().getOrDefault(DataPointer.of(DataValue.PLAYER_TRAIL, u.toString()), null);
		if (V.debug) {Console.debug("", "------Loading particle trail------", "Player: " + u.toString(), "Value from data: " + String.valueOf(particle), "Default trail enabled: " + V.particleDefault, "Default trail is: " + V.particleType, "Returning trail: " +  (particle != null ? particle: (V.particleDefault ? V.particleType : "")), "------End particle trail------", "");}
		return particle != null ? particle: (V.particleDefault ? V.particleType : "");
	}
	
	/**
	 * Set a players particle trail.
	 * If particle is set to null or if the trail specified does not exist TempFly will attempt to use the default trail if enabled in the config.
	 * If it is set to an empty string however the particle will be disabled, IE no trail. This is what the remove trail command does.
	 * @param u the player
	 * @param particle the particle
	 */
	public static void setTrail(UUID u, String particle) {
		FlightUser user = tempfly.getFlightManager().getUser(Bukkit.getPlayer(u));
		if (user != null) {
			user.setTrail(particle);
			return;
		}
		tempfly.getDataBridge().stageChange(DataPointer.of(DataValue.PLAYER_TRAIL, u.toString()), particle);
	}
	
}
