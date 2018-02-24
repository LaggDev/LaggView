package com.thelagg.laggview;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.ForgeHooks;

public class Util {
	public static void print(String msg) {
		if(Minecraft.getMinecraft().thePlayer==null) return;
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new ChatComponentText(msg));
	}
	
	public static void print(IChatComponent msg) {
		if(Minecraft.getMinecraft().thePlayer==null) return;
		Minecraft.getMinecraft().thePlayer.addChatComponentMessage(msg);
	}
	
	public static List<String> getPlayerNamesInTab() {
		List<String> list = new ArrayList<String>();
		if(Minecraft.getMinecraft().ingameGUI.getTabList() instanceof TabOverlay) {
			TabOverlay tab = (TabOverlay)Minecraft.getMinecraft().ingameGUI.getTabList();
			for(NetworkPlayerInfo i : tab.getCurrentlyDisplayedPlayers()) {
				list.add(i.getGameProfile().getName());
			}
		}
		return list;
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
	
	public static String toString(Object o) {
		String s = o.toString();
		for(Field f : getFields(o.getClass())) {
			f.setAccessible(true);
			try {
				s += f.getName() + ":" + f.get(o) + "--";
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return s;
	}
	
	public static String toString(Object[] oArr) {
		String bigS = "";
		for(Object o : oArr) {
			bigS += "__" + toString(o);
		}
		return bigS;
	}
	
	public static ArrayList<Field> getFields(Class<?> myclass) {
		if(myclass.getName().contains("Object")) {
			return new ArrayList<Field>();
		}
		ArrayList<Field> fields = new ArrayList<Field>();
		for(Field f : myclass.getDeclaredFields()) {
			fields.add(f);
		}
		if(myclass.getSuperclass()!=null) {
			fields.addAll(getFields(myclass.getSuperclass()));
		}
		return fields;
	}
	
}
