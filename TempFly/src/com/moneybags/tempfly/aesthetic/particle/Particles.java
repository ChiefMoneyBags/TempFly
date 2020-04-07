package com.moneybags.tempfly.aesthetic.particle;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.util.F;

public class Particles {

	public static void play(Location loc, String s) {
		if (!TempFly.oldParticles()) {
			Particle particle = null;
			try {
				particle = Particle.valueOf(s.toUpperCase());
			} catch (Exception e) {
				particle = Particle.valueOf("VILLAGER_HAPPY");
			}
			
			Class<?> c = particle.getDataType();
			if (DustOptions.class.equals(c)) {
				Random rand = new Random();
				loc.getWorld().spawnParticle(particle, loc, 1, new DustOptions(Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), 2f));	
				
			} else if (BlockData.class.equals(c)) {
				loc.getWorld().spawnParticle(particle, loc, 1, Material.STONE.createBlockData());	
			} else {
				loc.getWorld().spawnParticle(particle, loc, 1, 0, 0, 0, 0.1);
			}
			
		} else {
			Effect particle = null;
			// This effect value crashes clients and prevents them from joining the server again.
			if (s != null && s.equalsIgnoreCase("ITEM_BREAK")) {
				s = "HAPPY_VILLAGER";
			}
			try {
				particle = Effect.valueOf(s.toUpperCase());
			} catch (Exception e) {
				particle = Effect.valueOf("HAPPY_VILLAGER");
			}
			loc.getWorld().playEffect(loc, particle, 1);
		}
	}
	
	public static String loadTrail(UUID u) {
		return F.data.getString("players." + u.toString() + ".trail", null);
	}
	
	public static void setTrail(UUID u, String s) {
		F.data.set("players." + u.toString() + ".trail", s);
		F.saveData();
	}
	
}
