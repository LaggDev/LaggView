package com.thelagg.laggview;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class HackerMonitor {
	List<String> hackerList;
	Minecraft mc;
	ArrayList<EntityPlayer> hackersWithinRadius;
	boolean currentlyRecording;
	public static float radius = 20f;
	private String startRecordingHotkey;
	private String stopRecordingHotkey;
	private LaggView laggView;
	
	public String getStartRecordingHotkey() {
		return this.startRecordingHotkey;
	}
	
	public void setStartRecordingHotkey(String s) {
		this.startRecordingHotkey = s;
	}
	
	public String getStopRecordingHotkey() {
		return this.stopRecordingHotkey;
	}
	
	public void setStopRecordingHotkey(String s) {
		this.stopRecordingHotkey = s;
	}
	
	public HackerMonitor(Minecraft mcIn, LaggView laggView) {
		this.mc = mcIn;
		this.hackerList = new ArrayList<String>();
		this.hackersWithinRadius = new ArrayList<EntityPlayer>();
		currentlyRecording = false;
		this.laggView = laggView;
		this.startRecordingHotkey = laggView.settings.getToggleRecordingOnHotkey();
		this.stopRecordingHotkey = laggView.settings.getToggleRecordingOffHotkey();
	}
	
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if(mc.theWorld!=null) {
			List<Entity> entities = mc.theWorld.getLoadedEntityList();
			EntityPlayerSP me = mc.thePlayer;
			for(Entity e : entities) {
				if(e instanceof EntityPlayer) {
					EntityPlayer p = (EntityPlayer)e;
					if(hackersWithinRadius.contains(p) && me.getDistanceToEntity(p) > radius) {
						hackersWithinRadius.remove(p);
					}
					if(hackerList.contains(p.getName().toLowerCase()) && me.getDistanceToEntity(p) < radius && !hackersWithinRadius.contains(p)) {
						hackersWithinRadius.add(p);
					}
				}
			}
			EntityPlayer[] hackerEntities = hackersWithinRadius.toArray(new EntityPlayer[hackersWithinRadius.size()]);
			for(EntityPlayer p : hackerEntities) {
				if(!entities.contains(p) || !hackerList.contains(p.getName().toLowerCase())) {
					hackersWithinRadius.remove(p);
				}
			}
			if(hackersWithinRadius.size()!=0) {
				startRecording();
			} else {
				stopRecording();
			}
		}
	}
	
	private void startRecording() {
		new Thread() {
			public void run() {
				if(currentlyRecording) {
					return;
				}
				currentlyRecording = true;
				try {
					enterKeyCode(startRecordingHotkey);
				} catch (Exception e) {
					e.printStackTrace();
					laggView.logger.log(Level.ERROR, "Could not run hotkey to start recording");
				}
			}
		}.start();
	}
	
	private static void enterKeyCode(String sequence) throws AWTException, InterruptedException {
		char c = sequence.charAt(sequence.length()-1);
		Robot r = new Robot();
		if(sequence.contains("CTRL")) {
			r.keyPress(KeyEvent.VK_CONTROL);
		}
		if(sequence.contains("SHIFT")) {
			r.keyPress(KeyEvent.VK_SHIFT);
		}
		if(sequence.contains("ALT")) {
			r.keyPress(KeyEvent.VK_ALT);
		}
		Thread.sleep(100);
		r.keyPress(((int)c));
		r.keyRelease(((int)c));
		Thread.sleep(100);
		if(sequence.contains("ALT")) {
			r.keyRelease(KeyEvent.VK_ALT);
		}
		if(sequence.contains("SHIFT")) {
			r.keyRelease(KeyEvent.VK_SHIFT);
		}
		if(sequence.contains("CTRL")) {
			r.keyRelease(KeyEvent.VK_CONTROL);
		}
	}
	
	private void stopRecording() {
		new Thread() {
			public void run() {
				if(!currentlyRecording) {
					return;
				}
				currentlyRecording = false;
				try {
					enterKeyCode(stopRecordingHotkey);
				} catch (Exception e) {
					e.printStackTrace();
					laggView.logger.log(Level.ERROR, "Could not run hotkey to stop recording");
				}
			}
		}.start();
	}
	
	public void printList() {
		Util.print("§r§9-----------------------------------------------------§r");
		Util.print("                              §bRecording List");
		for(String playerName : this.hackerList) {
			IChatComponent comp = new ChatComponentText(ChatFormatting.GOLD + playerName);
			ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/lagg record remove " + playerName));
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ChatComponentText(ChatFormatting.DARK_PURPLE + "remove " + playerName + " from list")));
			comp.setChatStyle(style);
			Util.print(comp);
		}
		Util.print(ChatFormatting.DARK_GREEN + "Total: " + hackerList.size());
		Util.print("§r§9-----------------------------------------------------§r");
	}
}
