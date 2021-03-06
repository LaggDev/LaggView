package com.thelagg.laggview.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;

import com.ibm.icu.impl.duration.impl.Utils;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.games.BedwarsGame;
import com.thelagg.laggview.games.BlitzGame;
import com.thelagg.laggview.games.CrazyWallsGame;
import com.thelagg.laggview.games.Game;
import com.thelagg.laggview.games.Lobby;
import com.thelagg.laggview.games.MegaWallsGame;
import com.thelagg.laggview.games.PaintballGame;
import com.thelagg.laggview.games.QuakeCraftGame;
import com.thelagg.laggview.games.SkyClashGame;
import com.thelagg.laggview.games.SkywarsGame;
import com.thelagg.laggview.games.SmashHeroesGame;
import com.thelagg.laggview.games.TNTGame;
import com.thelagg.laggview.games.UHCGame;
import com.thelagg.laggview.games.VampireZGame;
import com.thelagg.laggview.games.WarlordsGame;
import com.thelagg.laggview.games.Game.GameType;
import com.thelagg.laggview.utils.Util;

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
	
	public List<Game> getGames() {
		return allGames;
	}
	
	private boolean isValid(ScoreObjective so) {
		if(so==null) {
			return false;
		}
		String newStr = so.getDisplayName().replaceAll("\u00A7.{1}", "").trim();
		return !newStr.equals("housing") && !newStr.equals("battleLobby") && !newStr.equals("PreScoreboard") && !newStr.equals("MegaScoreboard")
				&& !newStr.equals("HYPIXEL UHC");
	}
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		if(Minecraft.getMinecraft().getCurrentServerData()==null || Minecraft.getMinecraft().getCurrentServerData().serverIP==null) {
			return;
		}
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
					
					Game.GameType match = Game.GameType.UNKNOWN;
					for(Game.GameType type : Game.GameType.values()) {
						if(type.getNameOnScoreboard().equals(str.replaceAll("\u00A7.{1}", "").trim())) {
							match = type;
						}
					}
					
					laggView.logger.log(Level.INFO, "MATCHED WITH: " + match.name());
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
						Game alreadyExisted = findGame(Game.GameType.LOBBY,id);
						if(alreadyExisted==null) {
							currentGame = createGame(Game.GameType.LOBBY,id);
							allGames.add(currentGame);
						} else {
							alreadyExisted.enter();
							currentGame = alreadyExisted;
						}
					} else {
						Game alreadyExisted = findGame(match,id);
						if(alreadyExisted==null) {
							currentGame = createGame(match,id);
							allGames.add(currentGame);
						} else {
							if(alreadyExisted.shouldDelete()) {
								alreadyExisted.setOld();
								currentGame = createGame(match,id);
								allGames.add(currentGame);
							} else {
								alreadyExisted.enter();
								currentGame = alreadyExisted;
							}
						}
					}
					gettingGame = false;
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
		case LOBBY:
			return new Lobby(id,mc,laggView);
		case SKYWARS:
			return new SkywarsGame(id,mc,laggView);
		case BED_WARS:
			return new BedwarsGame(id,mc,laggView);
		case BLITZ_SURVIVAL_GAMES:
			return new BlitzGame(id,mc,laggView);
		case THE_TNT_GAMES:
		case BOW_SPLEEF:
		case TNT_RUN:
		case TNT_TAG:
		case WIZARDS:
		case PVP_RUN:
			return TNTGame.getTNTGame(id, mc, laggView);
		case UHC_CHAMPIONS:
			return new UHCGame(id,mc,laggView);
		case WARLORDS:
			return new WarlordsGame(id,mc,laggView);
		case CRAZY_WALLS:
			return new CrazyWallsGame(id,mc,laggView);
		case PAINTBALL_WARFARE:
			return new PaintballGame(id,mc,laggView);
		case QUAKECRAFT:
			return new QuakeCraftGame(id,mc,laggView);
		case SKYCLASH:
			return new SkyClashGame(id,mc,laggView);
		case SMASH_HEROES:
			return new SmashHeroesGame(id,mc,laggView);
		case VAMPIREZ:
			return new VampireZGame(id,mc,laggView);
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
