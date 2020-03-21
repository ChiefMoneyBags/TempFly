package com.moneybags.tempfly.aesthetic.particle;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;

import com.moneybags.tempfly.util.U;

public class Particles {

	public static void play(Location loc, Particle particle) {
		loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0.1);
	}
	
}
