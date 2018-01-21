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
	boolean gettingGame;
	String serverId;
	
	public GameUpdater() {
		this.allGames = new ArrayList<Game>();
	}
	
	public Game getCurrentGame() {
		return currentGame;
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(System.currentTimeMillis()-lastWorldLoad<750 || !Minecraft.getMinecraft().getCurrentServerData().serverIP.contains("hypixel.net") || this.gettingGame) {
			return;
		}
		if(currentGame!=null) {
			currentGame.exit();
		}
		lastWorldLoad = System.currentTimeMillis();
		gettingGame = true;
		new Thread() {
			public void run() {
				long timeout = 30000;
				long start = System.currentTimeMillis();
				while(event.world.getScoreboard().getObjectiveInDisplaySlot(1)==null && System.currentTimeMillis()-start<timeout) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(null==event.world.getScoreboard().getObjectiveInDisplaySlot(1)) {
					currentGame = null;
					Logger.getLogger("laggview").log(Level.INFO,"GAME: null " + System.currentTimeMillis());
					return;
				}
				String str = event.world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
				Logger.getLogger("laggview").log(Level.INFO,"GAME: " + str + " " + System.currentTimeMillis());
				for(Game.GameType type : Game.GameType.values()) {
					if(type.getNameOnScoreboard().equals(str.replaceAll("\u00A7.{1}", "").trim())) {
						Logger.getLogger("laggview").log(Level.INFO, "MATCHED WITH: " + type.name());
						long timeOut = 5000;
						long start2 = System.currentTimeMillis();
						waitingForServerId = true;
						Minecraft.getMinecraft().thePlayer.sendChatMessage("/whereami");
						long time = System.currentTimeMillis();
						while(waitingForServerId && (time = System.currentTimeMillis())-start2<timeOut) {
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						String id = serverId;
						if(time-start2>=timeOut) {
							waitingForServerId = false;
							currentGame = null;
						} else {
							Game alreadyExisted = findGame(type,id);
							if(alreadyExisted==null) {
								currentGame = new Game(type,id);
								allGames.add(currentGame);
							} else {
								alreadyExisted.enter();
							}
						}
						break;
					}
				}
				gettingGame = false;
			}
		}.start();
	}
	
	public Game findGame(Game.GameType type,String id) {
		for(Game g : this.allGames) {
			if(g.getType()==type && g.getServerId().equals(id)) {
				return g;
			}
		}
		return null;
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
