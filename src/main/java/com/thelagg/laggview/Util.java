package com.thelagg.laggview;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class Util {
	public static void print(String msg) {
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(msg));
	}
	public static void print(IChatComponent msg) {
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(msg);
	}
	
	public static UUID getUUID(String str) {
		if(str==null) {
			return null;
		}
		UUID uuid = null;
		if(str.length()==36) {
			try {
				uuid = UUID.fromString(str);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else if(str.length()==32) {
			uuid = UUID.fromString(str.substring(0, 8) + "-" + str.substring(8,12) + "-" + str.substring(12,16) + "-" + str.substring(16,20) + "-" + str.substring(20,32));
		}
		return uuid;
	}	
}
