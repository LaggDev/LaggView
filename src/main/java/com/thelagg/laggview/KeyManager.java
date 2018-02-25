package com.thelagg.laggview;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class KeyManager {

	boolean gettingKey = false;
	Minecraft mc;
	long timeCreated = System.currentTimeMillis();
	
	public KeyManager(Minecraft mc) {
		this.mc = mc;
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(System.currentTimeMillis()-timeCreated>1000*60*3) {
			MinecraftForge.EVENT_BUS.unregister(this);
		}
		File f = new File("./config/apiDone.txt");
		if(f.exists()) {
			MinecraftForge.EVENT_BUS.unregister(this);
		} else if(!gettingKey && mc.theWorld!=null && mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel")) {
			gettingKey = true;
			mc.thePlayer.sendChatMessage("/api");
		}
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		String msg = event.message.getFormattedText();
		if(gettingKey && msg.contains("\u00A7aYour new API key is")) {
			event.setCanceled(true);
			parseMsg(msg);
		} else if (gettingKey && msg.contains("\u00A7aYou already have an API Key, are you sure you want to regenerate it?")) {
			event.setCanceled(true);
		} else if (gettingKey && msg.contains("\u00A7aClick to run")) {
			event.setCanceled(true);
			mc.thePlayer.sendChatMessage("/api new");
		}
	}
	
	public void parseMsg(String s) {
		MinecraftForge.EVENT_BUS.unregister(this);
		gettingKey = false;
		Matcher m = Pattern.compile("([a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12})").matcher(s);
		if(!m.find()) return;
		UUID uuid = Util.getUUID(m.group(1));
		File f = new File("./config/apiDone.txt");
		try {
			URLConnectionReader.getText("http://thelagg.com/hypixel/addkey/" + uuid);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
