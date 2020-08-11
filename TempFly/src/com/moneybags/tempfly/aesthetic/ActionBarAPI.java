package com.moneybags.tempfly.aesthetic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.moneybags.tempfly.TempFly;
import com.moneybags.tempfly.event.ActionBarSendEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class ActionBarAPI {
	
	private static TempFly tempfly;
	
    private static String nmsver;
    private static boolean
    useOldMethods = false,
    newConstructor = false;
    
    private static Class<?>
    craftPlayerClass,
    packetPlayOutChatClass,
    packetClass,
    chatSerializerClass,
    iChatBaseComponentClass,
    chatComponentTextClass,
    chatMessageTypeClass;
    
    private static Method
    m3,
    craftPlayerHandleMethod;
    
    private static Object
    chatMessageType;
    
    public static void initialize(TempFly plugin) {
    	tempfly = plugin;
        nmsver = Bukkit.getServer().getClass().getPackage().getName();
        nmsver = nmsver.substring(nmsver.lastIndexOf(".") + 1);

        if (nmsver.equalsIgnoreCase("v1_8_R1") || nmsver.startsWith("v1_7_")) {
            useOldMethods = true;
        } else if (nmsver.startsWith("v1_16_") || nmsver.startsWith("v1_17_")) {
        	newConstructor = true;
        }
        
        try {
        	craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsver + ".entity.CraftPlayer");
        	packetPlayOutChatClass = Class.forName("net.minecraft.server." + nmsver + ".PacketPlayOutChat");
        	packetClass = Class.forName("net.minecraft.server." + nmsver + ".Packet");
        	iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsver + ".IChatBaseComponent");
        	  if (useOldMethods) {
                  chatSerializerClass = Class.forName("net.minecraft.server." + nmsver + ".ChatSerializer");
                  m3 = chatSerializerClass.getDeclaredMethod("a", String.class);
        	  } else {
        		  try {
            		  chatComponentTextClass = Class.forName("net.minecraft.server." + nmsver + ".ChatComponentText");
            		  chatMessageTypeClass = Class.forName("net.minecraft.server." + nmsver + ".ChatMessageType");
                      chatMessageType = null;
                      for (Object obj : chatMessageTypeClass.getEnumConstants()) {
                          if (obj.toString().equals("GAME_INFO")) {
                              chatMessageType = obj;
                          }
                      }  
        		  } catch (Exception e) {
        			  
        		  }
        	  }
        	  craftPlayerHandleMethod = craftPlayerClass.getDeclaredMethod("getHandle");
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }

    public static void sendActionBar(Player player, String message) {
        if (!player.isOnline()) {
        	return;
        }

        ActionBarSendEvent actionBarMessageEvent = new ActionBarSendEvent(player, message);
        Bukkit.getPluginManager().callEvent(actionBarMessageEvent);
        if (actionBarMessageEvent.isCancelled()) {return;}
        
        try {
            Object craftPlayer = craftPlayerClass.cast(player);
            Object packet;
            if (useOldMethods) {
                Object cbc = iChatBaseComponentClass.cast(m3.invoke(chatSerializerClass, "{\"text\": \"" + message + "\"}"));
                packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(cbc, (byte) 2);
            } else {
                Object chatCompontentText = chatComponentTextClass.getConstructor(new Class<?>[]{String.class}).newInstance(message);
				if (newConstructor) {
					packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass, UUID.class}).newInstance(chatCompontentText, chatMessageType, UUID.randomUUID());
				} else {
					try {packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass}).newInstance(chatCompontentText, chatMessageType);} catch (Exception e) {
						packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(chatCompontentText, (byte) 2);
					}
				}
                
            }
            Object craftPlayerHandle = craftPlayerHandleMethod.invoke(craftPlayer);
            Field playerConnectionField = craftPlayerHandle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(craftPlayerHandle);
            Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", packetClass);
            sendPacketMethod.invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendActionBar(final Player player, final String message, int duration) {
        sendActionBar(player, message);

        if (duration >= 0) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendActionBar(player, "");
                }
            }.runTaskLater(tempfly, duration + 1);
        }

        while (duration > 40) {
            duration -= 40;
            new BukkitRunnable() {
                @Override
                public void run() {
                    sendActionBar(player, message);
                }
            }.runTaskLater(tempfly, (long) duration);
        }
    }
}
