package com.moneybags.tempfly.hook.region.plugins;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.hook.region.CompatRegion;
import com.moneybags.tempfly.hook.region.RegionProvider;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class WorldGuardHook implements RegionProvider {
	
	private boolean enabled;
	
    private static Object worldGuard = null;
    private static Object worldGuardPlugin = null;
    private static Object regionContainer = null;
    private static Method regionContainerGetMethod = null;
    private static Method worldAdaptMethod = null;
    private static Method regionManagerGetMethod = null;
    private static Constructor<?> vectorConstructor = null;
    private static Method vectorConstructorMethod = null;

    public WorldGuardHook(TempFly tempfly) {
        try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Method getInstanceMethod = worldGuardClass.getMethod("getInstance");
            worldGuard = getInstanceMethod.invoke(null);
        } catch (Exception ex) {
    		Plugin plugin = tempfly.getServer().getPluginManager().getPlugin("WorldGuard");
    		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
    			return;
    		}
    		worldGuardPlugin = (WorldGuardPlugin) plugin;
        }
        if (worldGuard != null) {
            try {
                Method getPlatFormMethod = worldGuard.getClass().getMethod("getPlatform");
                Object platform = getPlatFormMethod.invoke(worldGuard);
                Method getRegionContainerMethod = platform.getClass().getMethod("getRegionContainer");
                regionContainer = getRegionContainerMethod.invoke(platform);
                
                Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
                Class<?> worldEditAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                worldAdaptMethod = worldEditAdapterClass.getMethod("adapt", World.class);
                regionContainerGetMethod = regionContainer.getClass().getMethod("get", worldEditWorldClass);
            } catch (Exception ex) {
                regionContainer = null;
                return;
            }
        } else {
            try {
				regionContainer = ((WorldGuardPlugin) worldGuardPlugin).getClass().getMethod("getRegionContainer").invoke(((WorldGuardPlugin) worldGuardPlugin));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
				return;
			}
            try {
                regionContainerGetMethod = regionContainer.getClass().getMethod("get", World.class);
            } catch (Exception ex) {
                regionContainer = null;
                return;
            }
        }
        try {
            Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
            vectorConstructor = vectorClass.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE);
            regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
        } catch (Exception ex) {
            try {
                Class<?> vectorClass = Class.forName("com.sk89q.worldedit.math.BlockVector3");
                vectorConstructorMethod = vectorClass.getMethod("at", Double.TYPE, Double.TYPE, Double.TYPE);
                regionManagerGetMethod = RegionManager.class.getMethod("getApplicableRegions", vectorClass);
            } catch (Exception sodonewiththis) {
                regionContainer = null;
                return;
            }
        }
        enabled = worldGuardPlugin != null || worldGuard != null;
    }

    public RegionManager getRegionManager(World world) {
        if (regionContainer == null || regionContainerGetMethod == null) return null;
        RegionManager regionManager = null;
        try {
            if (worldAdaptMethod != null) {
                Object worldEditWorld = worldAdaptMethod.invoke(null, world);
                regionManager = (RegionManager)regionContainerGetMethod.invoke(regionContainer, worldEditWorld);
            } else {
                regionManager = (RegionManager)regionContainerGetMethod.invoke(regionContainer, world);
            }
        } catch (Exception e) {}
        return regionManager;
    }

    public ApplicableRegionSet getRegionSet(Location location) {
        RegionManager regionManager = getRegionManager(location.getWorld());
        if (regionManager == null) return null;
        try {
            Object vector = vectorConstructorMethod == null
                    ? vectorConstructor.newInstance(location.getX(), location.getY(), location.getZ())
                    : vectorConstructorMethod.invoke(null, location.getX(), location.getY(), location.getZ());
            return (ApplicableRegionSet)regionManagerGetMethod.invoke(regionManager, vector);
        } catch (Exception ex) {
           
        }
        return null;
    }
    
    @Override
    public CompatRegion[] getApplicableRegions(Location loc) {
    	List<CompatRegion> list = new ArrayList<>();
    	for (ProtectedRegion r: getRegionSet(loc)) {
    		list.add(new CompatRegion(r.getId()));
    	}
    	return list.toArray(new CompatRegion[list.size()]);
    }

	@Override
	public boolean isEnabled() {
		return enabled;
	}
}
