package com.thelagg.laggview.hud;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;

import com.thelagg.laggview.Game;
import com.thelagg.laggview.Game.GameType;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.games.MegaWallsGame;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
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
	private Minecraft mc;
	private LaggView laggView;
	
	public GameUpdater(Minecraft mc, LaggView laggView) {
		this.allGames = new ArrayList<Game>();
		this.mc = mc;
		this.laggView = laggView;
	}
	
	public Game getCurrentGame() {
		return currentGame;
	}
	
	private boolean isValid(ScoreObjective so) {
		if(so==null) {
			return false;
		}
		String newStr = so.getDisplayName().replaceAll("\u00A7.{1}", "").trim();
		return !newStr.equals("housing") && !newStr.equals("battleLobby") && !newStr.equals("PreScoreboard") && !newStr.equals("MegaScoreboard");
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		laggView.logger.log(Level.INFO, Minecraft.getMinecraft().getCurrentServerData().serverIP);
		if(System.currentTimeMillis()-lastWorldLoad<750 || !Minecraft.getMinecraft().getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net") || this.gettingGame) {
			return;
		}
		laggView.logger.log(Level.INFO, "loading world");
		if(currentGame!=null) {
			currentGame.exit();
		}
		lastWorldLoad = System.currentTimeMillis();
		gettingGame = true;
		new Thread() {
			public void run() {
				long timeout = 30000;
				long start = System.currentTimeMillis();
				while(!isValid(event.world.getScoreboard().getObjectiveInDisplaySlot(1)) && System.currentTimeMillis()-start<timeout) {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if(!isValid(event.world.getScoreboard().getObjectiveInDisplaySlot(1))) {
					laggView.logger.log(Level.INFO,"GAME: null " + System.currentTimeMillis());
				} else {
					String str = event.world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
					laggView.logger.log(Level.INFO,"GAME: " + str + " " + System.currentTimeMillis());
					for(Game.GameType type : Game.GameType.values()) {
						if(type.getNameOnScoreboard().equals(str.replaceAll("\u00A7.{1}", "").trim())) {
							laggView.logger.log(Level.INFO, "MATCHED WITH: " + type.name());
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
							} else if (id.toLowerCase().contains("lobby") || id.equals("limbo")) {
								
							} else {
								Game alreadyExisted = findGame(type,id);
								if(alreadyExisted==null) {
									currentGame = createGame(type,id);
									allGames.add(currentGame);
								} else {
									alreadyExisted.enter();
									currentGame = alreadyExisted;
								}
							}
							gettingGame = false;
							break;
						}
					}	
				}
				if(gettingGame) {
					currentGame = null;
				}
				gettingGame = false;
			}
		}.start();
	}
	
	public Game createGame(GameType type,String id) {
		switch(type) {
		case MEGA_WALLS:
			return new MegaWallsGame(id,mc,laggView);
		default:
			return new Game(type,id,mc,laggView);
		}
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
		Matcher m = Pattern.compile("\u00A7bYou are currently connected to server \u00A7r\u00A76(.*)\u00A7r").matcher(event.message.getFormattedText());
		if(event.message.getFormattedText().equals("\u00A7bYou are currently in limbo\u00A7r")) {
			serverId = "limbo";
			if(waitingForServerId) {
				event.setCanceled(true);
				waitingForServerId = false;
			}
		} else if(m.find()) {
			serverId = m.group(1);
			if(waitingForServerId) {
				event.setCanceled(true);
				waitingForServerId = false;
			}
		}
	}
	
}
