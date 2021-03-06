package com.thelagg.laggview.modules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.Level;

import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.GuildRequest;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.games.Game;
import com.thelagg.laggview.hud.MainHud.HudText;
import com.thelagg.laggview.quests.Quest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class QuestTracker {
	private PlayerRequest player;
	private Timer timer;
	private LaggView laggView;
	private Minecraft mc;
	
	public QuestTracker(Minecraft mc, LaggView laggView) {
		MinecraftForge.EVENT_BUS.register(this);
		this.mc = mc;
		this.laggView = laggView;
		
		new Thread() {
			public void run() {
				while(mc.thePlayer==null || mc.thePlayer.getUniqueID()==null) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				player = laggView.apiCache.getPlayerResult(mc.thePlayer.getUniqueID(), 0);
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						updatePlayer();
					}
				}, 0, 1000*60*2);
				
			}
		}.start();
	}
	
	public void updatePlayer() {
		new Thread() {
			public void run() {
				laggView.logger.log(Level.INFO, "Updating quests");
				player.updateRequest();
				update();
			}
		}.start();
	}
	
	public void update() {
		for(Game g : laggView.gameUpdater.getGames()) {
			for(Quest q : g.getQuests()) {
				q.update(player);
			}
		}
	}
	
}
