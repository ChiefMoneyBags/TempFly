package moneybags.tempfly.aesthetic.particle;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.block.data.BlockData;

import moneybags.tempfly.TempFly;
import moneybags.tempfly.util.data.DataBridge.DataValue;

public class Particles {

	private static Class<?> dustOptions = null;
	
	public static void initialize() {
		try {
			dustOptions = Class.forName("org.bukkit.Particle$DustOptions");
		} catch (Exception e) {}
	}
	
	public static void play(Location loc, String s) {
		if (!TempFly.oldParticles()) {
			Particle particle = null;
			try {
				particle = Particle.valueOf(s.toUpperCase());
			} catch (Exception e) {
				particle = Particle.VILLAGER_HAPPY;
			}
			
			Class<?> c = particle.getDataType();
			try {
				if (dustOptions != null && dustOptions.equals(c)) {
					Random rand = new Random();
					loc.getWorld().spawnParticle(particle, loc, 1, new DustOptions(Color.fromRGB(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255)), 2f));	
				} else if (BlockData.class.equals(c)) {
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
			try {
				particle = Effect.valueOf(s.toUpperCase());
			} catch (Exception e) {
				particle = Effect.valueOf("HAPPY_VILLAGER");
			}
			loc.getWorld().playEffect(loc, particle, 1);
		}
	}
	
	public static String loadTrail(UUID u) {
		return (String) TempFly.getInstance().getDataBridge().getValue(DataValue.PLAYER_TRAIL, u.toString());
	}
	
	public static void setTrail(UUID u, String s) {
		TempFly.getInstance().getDataBridge().setValue(DataValue.PLAYER_TRAIL, u.toString(), s);
	}
	
}
