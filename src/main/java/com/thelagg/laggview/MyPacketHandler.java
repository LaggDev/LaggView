package com.thelagg.laggview;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import com.mojang.authlib.GameProfile;
import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;

public class MyPacketHandler extends NetHandlerPlayClient {
	
	public static void replacePacketHandler() {
		try {
			Minecraft mc = Minecraft.getMinecraft();
			
			Field fsendQueue, fguiScreen, fnetManager, fprofile, fclientWorldController, fdoneLoadingTerrain, fplayerInfoMap, fcurrentServerMaxPlayers;
			try {
				fsendQueue = mc.thePlayer.getClass().getDeclaredField("sendQueue");
				fguiScreen = NetHandlerPlayClient.class.getDeclaredField("guiScreenServer");
				fnetManager = NetHandlerPlayClient.class.getDeclaredField("netManager");
				fprofile = NetHandlerPlayClient.class.getDeclaredField("profile");
				fclientWorldController = NetHandlerPlayClient.class.getDeclaredField("clientWorldController");
				fdoneLoadingTerrain = NetHandlerPlayClient.class.getDeclaredField("doneLoadingTerrain");
				fplayerInfoMap = NetHandlerPlayClient.class.getDeclaredField("playerInfoMap");
				fcurrentServerMaxPlayers = NetHandlerPlayClient.class.getDeclaredField("currentServerMaxPlayers");
			} catch (NoSuchFieldException e) {
				fsendQueue = mc.thePlayer.getClass().getDeclaredField("field_71174_a");
				fguiScreen = NetHandlerPlayClient.class.getDeclaredField("field_147307_j");
				fnetManager = NetHandlerPlayClient.class.getDeclaredField("field_147302_e");
				fprofile = NetHandlerPlayClient.class.getDeclaredField("field_175107_d");
				fclientWorldController = NetHandlerPlayClient.class.getDeclaredField("field_147300_g");
				fdoneLoadingTerrain = NetHandlerPlayClient.class.getDeclaredField("field_147309_h");
				fplayerInfoMap = NetHandlerPlayClient.class.getDeclaredField("field_147310_i");
				fcurrentServerMaxPlayers = NetHandlerPlayClient.class.getDeclaredField("field_147304_c");
			}
			
			fsendQueue.setAccessible(true);
			fguiScreen.setAccessible(true);
			fnetManager.setAccessible(true);
			fprofile.setAccessible(true);
			fclientWorldController.setAccessible(true);
			fdoneLoadingTerrain.setAccessible(true);
			fplayerInfoMap.setAccessible(true);
			fcurrentServerMaxPlayers.setAccessible(true);
			
			NetHandlerPlayClient sendQueue = (NetHandlerPlayClient) fsendQueue.get(mc.thePlayer);
			GuiScreen guiScreen = (GuiScreen) fguiScreen.get(sendQueue);
			NetworkManager networkManager = (NetworkManager) fnetManager.get(sendQueue);
			GameProfile gameProfile = (GameProfile) fprofile.get(sendQueue);
			WorldClient clientWorldController = (WorldClient)fclientWorldController.get(sendQueue);
			Map<UUID,NetworkPlayerInfo> playerInfoMap = (Map<UUID, NetworkPlayerInfo>) fplayerInfoMap.get(sendQueue);
			int currentServerMaxPlayers = fcurrentServerMaxPlayers.getInt(sendQueue);
			boolean doneLoadingTerrain = fdoneLoadingTerrain.getBoolean(sendQueue);
			
			MyPacketHandler myPacketHandler = new MyPacketHandler(mc,guiScreen,networkManager,gameProfile); 
			fclientWorldController.set(myPacketHandler, clientWorldController);
			fplayerInfoMap.set(myPacketHandler, playerInfoMap);
			fcurrentServerMaxPlayers.set(myPacketHandler, currentServerMaxPlayers);
			fdoneLoadingTerrain.set(myPacketHandler, doneLoadingTerrain);
			
			fsendQueue.set(mc.thePlayer, myPacketHandler);
			
			System.out.println("replacing packet handler");
		} catch (Exception e) {
			System.err.println("error replacing PacketHandler");
			e.printStackTrace();
		}
	}
	
	public static void replaceNetworkManagerPacketHandler() {
		try {
			Minecraft mc = Minecraft.getMinecraft();
			if(mc.thePlayer!=null && mc.thePlayer.sendQueue!=null && mc.thePlayer.sendQueue instanceof MyPacketHandler) {
				Field fnetManager, fPacketListener;
				try {
					fnetManager = NetHandlerPlayClient.class.getDeclaredField("netManager");
					fPacketListener = NetworkManager.class.getDeclaredField("packetListener");
				} catch (NoSuchFieldException e) {
					fnetManager = NetHandlerPlayClient.class.getDeclaredField("field_147302_e");
					fPacketListener = NetworkManager.class.getDeclaredField("field_150744_m");
				}
				fnetManager.setAccessible(true);
				fPacketListener.setAccessible(true);
				NetworkManager networkManager = (NetworkManager) fnetManager.get(mc.thePlayer.sendQueue);
				INetHandler netHandler = (INetHandler) fPacketListener.get(networkManager);
				if(!(netHandler instanceof MyPacketHandler) && netHandler instanceof NetHandlerPlayClient)
					fPacketListener.set(networkManager, mc.thePlayer.sendQueue);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public MyPacketHandler(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager p_i46300_3_, GameProfile p_i46300_4_) throws Exception {
		super(mcIn, p_i46300_2_, p_i46300_3_, p_i46300_4_);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addToSendQueue(Packet p) {
		if(p instanceof C01PacketChatMessage) {
			String msg = ((C01PacketChatMessage) p).getMessage();
			if(Pattern.compile("(^|\\s)L($|\\s)").matcher(msg).find()) {
				Util.print(ChatFormatting.DARK_RED + "Please respect all users.");
			} else {
				super.addToSendQueue(p);
			}
			
		} else {
			super.addToSendQueue(p);
		}
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
