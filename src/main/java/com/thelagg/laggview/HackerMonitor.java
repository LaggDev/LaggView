package com.thelagg.laggview;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Mouse;

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
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class HackerMonitor {
	private List<String> hackerList;
	Minecraft mc;
	ArrayList<EntityPlayer> hackersWithinRadius;
	boolean currentlyRecording;
	public static float radius = 20f;
	private String startRecordingHotkey;
	private String stopRecordingHotkey;
	private boolean toggleRecording;
	private LaggView laggView;
	
	public void toggleRecording() {
		this.toggleRecording = !toggleRecording;
		if(!this.toggleRecording) {
			Mouse.setGrabbed(true);
			this.stopRecording();
			Util.print(ChatFormatting.DARK_PURPLE + "Auto-recording toggled " + ChatFormatting.RED + "OFF");
		} else {
			Util.print(ChatFormatting.DARK_PURPLE + "Auto-recording toggled " + ChatFormatting.GREEN + "ON");
		}
		laggView.settings.setToggleRecording(toggleRecording);
	}
	
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
	
	public void remove(String player) {
		if(hackerList.contains(player.toLowerCase())) {
			hackerList.remove(player.toLowerCase());
			Util.print(EnumChatFormatting.DARK_PURPLE + "Removed " + EnumChatFormatting.GOLD + player + EnumChatFormatting.DARK_PURPLE + " from hacker list");
			laggView.settings.setHackerList(hackerList);
		} else {
			Util.print(ChatFormatting.GOLD + player + EnumChatFormatting.DARK_RED + " not on your hacker list");
		}
	}
	
	public void add(String player) {
		if(!hackerList.contains(player.toLowerCase())) {
			hackerList.add(player.toLowerCase());
			Util.print(EnumChatFormatting.DARK_PURPLE + "Added " + EnumChatFormatting.GOLD + player + EnumChatFormatting.DARK_PURPLE + " to hacker list");
			laggView.settings.setHackerList(hackerList);
		}
	}
	
	public void addOrRemove(String player) {
		if(hackerList.contains(player.toLowerCase())) {
			remove(player);
		} else {
			add(player);
		}
	}
	
	public HackerMonitor(Minecraft mcIn, LaggView laggView) {
		this.mc = mcIn;
		this.hackersWithinRadius = new ArrayList<EntityPlayer>();
		currentlyRecording = false;
		this.laggView = laggView;
		this.startRecordingHotkey = laggView.settings.getToggleRecordingOnHotkey();
		this.stopRecordingHotkey = laggView.settings.getToggleRecordingOffHotkey();
		this.hackerList = laggView.settings.getHackerList();
		this.toggleRecording = laggView.settings.getToggleRecording();
	}
	
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if(mc.theWorld!=null && toggleRecording) {
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
				if(!Mouse.isGrabbed()) {
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
		boolean holdingCtrl = Mouse.isButtonDown(KeyEvent.VK_CONTROL);
		boolean holdingShift = Mouse.isButtonDown(KeyEvent.VK_SHIFT);
		boolean holdingAlt = Mouse.isButtonDown(KeyEvent.VK_ALT);
		if(sequence.contains("CTRL") && !holdingCtrl) {
			r.keyPress(KeyEvent.VK_CONTROL);
		}
		if(sequence.contains("SHIFT") && !holdingShift) {
			r.keyPress(KeyEvent.VK_SHIFT);
		}
		if(sequence.contains("ALT") && !holdingAlt) {
			r.keyPress(KeyEvent.VK_ALT);
		}
		Thread.sleep(100);
		r.keyPress(((int)c));
		Thread.sleep(50);
		r.keyRelease(((int)c));
		Thread.sleep(100);
		if(sequence.contains("ALT") && !holdingAlt) {
			r.keyRelease(KeyEvent.VK_ALT);
		}
		if(sequence.contains("SHIFT") && !holdingShift) {
			r.keyRelease(KeyEvent.VK_SHIFT);
		}
		if(sequence.contains("CTRL") && !holdingCtrl) {
			r.keyRelease(KeyEvent.VK_CONTROL);
		}
	}
	
	private void stopRecording() {
		new Thread() {
			public void run() {
				if(!currentlyRecording) {
					return;
				}
				if(!Mouse.isGrabbed()) {
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
		Util.print("                             \u00A7bRecording List");
		for(String playerName : this.hackerList) {
			IChatComponent comp = new ChatComponentText(ChatFormatting.GOLD + playerName);
			ChatStyle style = new ChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/lagg record remove " + playerName));
			style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ChatComponentText(ChatFormatting.DARK_PURPLE + "remove " + playerName + " from list")));
			comp.setChatStyle(style);
			Util.print(comp);
		}
		Util.print(ChatFormatting.DARK_GREEN + "Total: " + hackerList.size());
	}
}
