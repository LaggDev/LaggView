package com.thelagg.laggview;

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
}
