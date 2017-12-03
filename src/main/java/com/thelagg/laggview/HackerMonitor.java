package com.thelagg.laggview;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class HackerMonitor {
	List<String> hackerList;
	Minecraft mc;
	ArrayList<EntityPlayer> hackersWithinRadius;
	boolean currentlyRecording;
	public static float radius = 20f;
	
	public HackerMonitor(Minecraft mcIn) {
		this.mc = mcIn;
		this.hackerList = new ArrayList<String>();
		this.hackersWithinRadius = new ArrayList<EntityPlayer>();
		currentlyRecording = false;
	}
	
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if(mc.theWorld!=null) {
			List<Entity> entities = mc.theWorld.getLoadedEntityList();
			EntityPlayerSP me = mc.thePlayer;
			for(Entity e : entities) {
				if(e instanceof EntityPlayer) {
					EntityPlayer p = (EntityPlayer)e;
					if(hackersWithinRadius.contains(p.getName().toLowerCase()) && me.getDistanceToEntity(p) > radius) {
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
		if(currentlyRecording) {
			return;
		}
		currentlyRecording = true;
		try {
			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_CONTROL);
			Thread.sleep(100);
			r.keyPress(KeyEvent.VK_J);
			r.keyRelease(KeyEvent.VK_J);
			Thread.sleep(100);
			r.keyRelease(KeyEvent.VK_CONTROL);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not start recording");
		}
	}
	
	private void stopRecording() {
		if(!currentlyRecording) {
			return;
		}
		currentlyRecording = false;
		try {
			Robot r = new Robot();
			r.keyPress(KeyEvent.VK_CONTROL);
			Thread.sleep(100);
			r.keyPress(KeyEvent.VK_I);
			r.keyRelease(KeyEvent.VK_I);
			Thread.sleep(100);
			r.keyRelease(KeyEvent.VK_CONTROL);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not start recording");
		}
	}
}
