package com.thelagg.laggview.hud;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thelagg.laggview.Game;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GameUpdater {
	
	private ArrayList<Game> allGames;
	private Game currentGame;
	private long lastWorldLoad;
	boolean waitingForServerId;
	
	public GameUpdater() {
		this.allGames = new ArrayList<Game>();
		
	}
	
	public static void getGame() {
		
	}
	

	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(System.currentTimeMillis()-lastWorldLoad<750 || !Minecraft.getMinecraft().getCurrentServerData().serverIP.contains("hypixel.net")) {
			return;
		}
		lastWorldLoad = System.currentTimeMillis();
		new Thread() {
			public void run() {
				long timeout = 30000;
				long start = System.currentTimeMillis();
				while(event.world.getScoreboard().getObjectiveInDisplaySlot(1)==null && System.currentTimeMillis()-start<timeout) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(null==event.world.getScoreboard().getObjectiveInDisplaySlot(1)) {
					game = null;
					Logger.getLogger("laggview").log(Level.INFO,"GAME: null " + System.currentTimeMillis());
					return;
				}
				String str = event.world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
				Logger.getLogger("laggview").log(Level.INFO,"GAME: " + str + " " + System.currentTimeMillis());
				for(Game.GameType type : Game.GameType.values()) {
					if(type.getNameOnScoreboard().equals(str.replaceAll("\u00A7.{1}", "").trim())) {
						game = type;
						Logger.getLogger("laggview").log(Level.INFO, "MATCHED WITH: " + type.name());
						Minecraft.getMinecraft().thePlayer.sendChatMessage("/whereami");
						waitingForServerId = true;
					}
				}		
			}
		}.start();
		
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		Matcher m = Pattern.compile("§bYou are currently connected to server §r§6(.*)§r").matcher(event.message.getFormattedText());
		if(m.find()) {
			if(waitingForServerId) {
				event.setCanceled(true);
				waitingForServerId = false;
			}
			serverId = m.group(1);
		}
	}
	
}
