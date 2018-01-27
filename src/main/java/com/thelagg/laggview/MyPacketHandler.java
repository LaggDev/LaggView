package com.thelagg.laggview;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;

public class MyPacketHandler extends NetHandlerPlayClient {
	
	/*
	if(mc.thePlayer!=null && mc.thePlayer.sendQueue!=null && !(mc.thePlayer.sendQueue instanceof MyPacketHandler)) {
		try {
			Field fsendQueue = mc.thePlayer.getClass().getDeclaredField("sendQueue");
			fsendQueue.setAccessible(true);
			NetHandlerPlayClient sendQueue = (NetHandlerPlayClient) fsendQueue.get(mc.thePlayer);
			Field fguiScreen = sendQueue.getClass().getDeclaredField("guiScreenServer");
			fguiScreen.setAccessible(true);
			Field fnetManager = sendQueue.getClass().getDeclaredField("netManager");
			fnetManager.setAccessible(true);
			Field fprofile = sendQueue.getClass().getDeclaredField("profile");
			fprofile.setAccessible(true);
			
			GuiScreen guiScreen = (GuiScreen) fguiScreen.get(sendQueue);
			NetworkManager networkManager = (NetworkManager) fnetManager.get(sendQueue);
			GameProfile gameProfile = (GameProfile) fprofile.get(sendQueue);
			fsendQueue.set(mc.thePlayer, new MyPacketHandler(mc,guiScreen,networkManager,gameProfile));
			
		} catch (Exception e) {
			System.err.println("error replacing PacketHandler");
			e.printStackTrace();
		}
	}*/
	
	public MyPacketHandler(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager p_i46300_3_, GameProfile p_i46300_4_) throws Exception {
		super(mcIn, p_i46300_2_, p_i46300_3_, p_i46300_4_);
		mcIn.playerController = new PlayerControllerMP(mcIn, this);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addToSendQueue(Packet p) {
		if(p instanceof C01PacketChatMessage && Pattern.compile("(^|\\s)L($|\\s)").matcher(((C01PacketChatMessage) p).getMessage()).find()) {
			Util.print(ChatFormatting.DARK_RED + "Please respect all users.");
			return;
		} else {
			super.addToSendQueue(p);
		}
	}
	
	private static ArrayList<Field> getFields(Class<?> myclass) {
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
	
	private void printPacket(Packet<?> packetIn) {
		try {
	        Field[] fields = getFields(packetIn.getClass()).toArray(new Field[getFields(packetIn.getClass()).size()]);
			String[] values = new String[fields.length];
			for(int i = 0; i<fields.length; i++) {
				fields[i].setAccessible(true);
				values[i] = fields[i].getName() + ":";
				values[i] += fields[i].get(packetIn)==null?"null":fields[i].get(packetIn).toString();
			}
			LogManager.getLogger(LaggView.MODID).log(Level.INFO, "packet - " + packetIn.toString() + Arrays.toString(values));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}
