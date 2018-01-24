package com.thelagg.laggview;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class MyPacketHandler extends NetHandlerPlayClient {
	
	public MyPacketHandler(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager p_i46300_3_, GameProfile p_i46300_4_) throws Exception {
		super(mcIn, p_i46300_2_, p_i46300_3_, p_i46300_4_);
		mcIn.playerController = new PlayerControllerMP(mcIn, this);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addToSendQueue(Packet p) {
		try {
			Field f = this.getClass().getSuperclass().getDeclaredField("netManager");
			f.setAccessible(true);
			NetworkManager netManager = (NetworkManager) f.get(this);
			netManager.sendPacket(p);
			Field[] fields = getFields(p.getClass()).toArray(new Field[getFields(p.getClass()).size()]);
			String[] values = new String[fields.length];
			for(int i = 0; i<fields.length; i++) {
				fields[i].setAccessible(true);
				values[i] = fields[i].getName() + ":";
				values[i] += fields[i].get(p)==null?"null":fields[i].get(p).toString();
			}
			if(p instanceof C03PacketPlayer) {
				return;
			}
			LogManager.getLogger(LaggView.MODID).log(Level.INFO, "packet - " + p.toString() + Arrays.toString(values));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
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
