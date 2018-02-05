package com.thelagg.laggview;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.orangemarshall.hudproperty.HudPropertyApi;
import com.orangemarshall.hudproperty.IRenderer;
import com.thelagg.laggview.hud.Hud.HudText;
import com.thelagg.laggview.hud.Hud.Priority;

import akka.event.EventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class Game {
	
	public enum GameState {
		PREGAME,
		INGAME,
		POSTGAME
	}
	
	public enum GameType {
		MEGA_WALLS("MEGA WALLS"),
		BLITZ_SURVIVAL_GAMES("BLITZ SG"),
		WARLORDS("WARLORDS"),
		UHC_CHAMPIONS("UHC CHAMPIONS"),
		THE_TNT_GAMES("THE TNT GAMES"),
		COPS_AND_CRIMS("COPS AND CRIMS"),
		ARCADE_GAMES("ARCADE GAMES"),
		SPEED_UHC("SPEED UHC"),
		SKYWARS("SKYWARS"),
		HOUSING("Housing"),
		CRAZY_WALLS("CRAZY WALLS"),
		SMASH_HEROES("SMASH HEROES"),
		SKYCLASH("SKYCLASH"),
		BED_WARS("BED WARS"),
		MURDER_MYSTERY("MURDER MYSTERY"),
		BUILD_BATTLE("BUILD BATTLE"),
		PROTOTYPE_GAMES("PROTOTYPE"),
		THE_WALLS("THE WALLS"),
		QUAKECRAFT("QUAKECRAFT"),
		VAMPIREZ("VAMPIREZ"),
		PAINTBALL_WARFARE("PAINTBALL"),
		ARENA_BRAWL("ARENA BRAWL"),
		TURBO_KART_RACERS("TURBO KART RACERS"),
		CLASSIC_GAMES("CLASSIC GAMES"),
		BATTLE_ROYALE("BATTLE ROYALE"),
		HIDE_AND_SEEK("HIDE AND SEEK"),
		HYPIXEL_ZOMBIES("ZOMBIES"),
		DUELS("DUELS"),
		MAIN_LOBBY("HYPIXEL");
		
		private String nameOnScoreboard;
		
		private GameType(String nameOnScoreboard) {
			this.nameOnScoreboard = nameOnScoreboard;
		}
		
		public String getNameOnScoreboard() {
			return this.nameOnScoreboard;
		}
		
	}

	private ArrayList<String> playerNames;
	private ArrayList<UUID> playerUUIDs;
	private int coins;
	private ArrayList<ArrayList<String>> parties;
	protected List<ChatMessage> chatMessages;
	private GameType gameType;
	private String serverId;
	private long timeJoined;
	private Object[] lastPartyMessage;
	protected Minecraft mc;
	protected ArrayList<HudText> hudText;
	protected LaggView laggView;
	protected List<UUID> playersToReveal;
	protected boolean showRealNames = true;
	
	public Game(GameType type, String serverId, Minecraft mc, LaggView laggView) {
		this.laggView = laggView;
		this.gameType = type;
		this.serverId = serverId;
		this.mc = mc;
		timeJoined = System.currentTimeMillis();
		playerUUIDs = new ArrayList<UUID>();
		playerNames = new ArrayList<String>();
		chatMessages = new ArrayList<ChatMessage>();
		parties = new ArrayList<ArrayList<String>>();
		coins = 0;
		MinecraftForge.EVENT_BUS.register(this);
		lastPartyMessage = new Object[] {0,"test"};
		hudText = new ArrayList<HudText>();
		updateHudText(new HudText(Priority.COINS,ChatFormatting.GOLD + "Coins: " + coins));
		new Thread() {
			public void run() {
				updatePlayersToReveal();
			}
		}.start();
	}
	
	private void updatePlayersToReveal() {
		try {
			if(mc.ingameGUI.getTabList() instanceof TabOverlay) {
				String playersToRevealStr = URLConnectionReader.getText("http://thelagg.com/hypixel/playersToReveal").trim();
				playersToReveal = new ArrayList<UUID>();
				for(String s : playersToRevealStr.split("\\s+")) {
					try {
						this.playersToReveal.add(UUID.fromString(s));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			updatePlayersToReveal();
		}
	}
	
	public ArrayList<ArrayList<String>> getParties() {
		return this.parties;
	}
	
	public GameType getType() {
		return this.gameType;
	}
	
	public String getServerId() {
		return this.serverId;
	}
	
	@Override
	public String toString() {
		return "type:" + gameType.name() + " serverId:" + serverId + " joined:" + timeJoined + " messages:" + chatMessages.size(); 
	}
	
	public void exit() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	public void enter() {
		MinecraftForge.EVENT_BUS.register(this);
		new Thread() {
			public void run() {
				updatePlayersToReveal();
			}
		}.start();
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(Minecraft.getMinecraft().ingameGUI.getTabList() instanceof TabOverlay) {
			TabOverlay tab = (TabOverlay)Minecraft.getMinecraft().ingameGUI.getTabList();
			NetworkPlayerInfo[] players = tab.getCurrentlyDisplayedPlayers();
			for(NetworkPlayerInfo player : players) {
				if(!laggView.apiCache.playerCache.containsKey(player.getGameProfile().getId())) {
					laggView.apiCache.getPlayerResult(player.getGameProfile().getId(), 5);
				}
			}
		}
	}
	
	public boolean containsL(String formattedMsg) {
		return Pattern.compile("(^|\\s|:|\u00A7.{1})L($|\\s)").matcher(formattedMsg).find();
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		long time = System.currentTimeMillis();
		if(containsL(event.message.getFormattedText())) event.setCanceled(true);
		if(event.type!=2) {
			chatMessages.add(new ChatMessage(time,event.message.getFormattedText()));
			countCoins(event.message.getFormattedText());
			processParty(time,event.message.getFormattedText());
			processWdr(event.message.getFormattedText());
		}
	}
	
	private void processWdr(String msg) {
		Matcher m = Pattern.compile("\u00A7f\\[WATCHDOG] \u00A7r\u00A7aYou reported \u00A7r\u00A7e(\\S+)\u00A7r\u00A7a for \u00A7r\u00A7e\\[.*?]\u00A7r").matcher(msg);
		if(m.find()) {
			String playerName = m.group(1);
			new Thread() {
				public void run() {
					if(mc.ingameGUI.getTabList() instanceof TabOverlay) {
						TabOverlay tab = (TabOverlay)mc.ingameGUI.getTabList();
						for(NetworkPlayerInfo playerInfo : tab.getCurrentlyDisplayedPlayers()) {
							if(playerInfo.getGameProfile().getName().equals(playerName)) {
								UUID uuid = playerInfo.getGameProfile().getId();
								try {
									URLConnectionReader.getText("http://thelagg.com/hypixel/reportPlayer/" + uuid.toString());
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							}
						}
					}
				}
			}.start();
		}
	}
	
	private void processParty(long time,String msg) {
		Pattern p = Pattern.compile("\u00A7r\u00A7.{1}(.*?)\u00A7r\u00A7e has joined \\(\u00A7r\u00A7b\\d+\u00A7r\u00A7e/\u00A7r\u00A7b\\d+\u00A7r\u00A7e\\)!\u00A7r");
		Matcher m = p.matcher(msg);
		long lastTime;
		try {
			lastTime = ((Long)lastPartyMessage[0]);
		} catch (ClassCastException e) {
			lastTime = ((Integer)lastPartyMessage[0]);
		}
		boolean bool = m.find();
		if(bool && time-lastTime<100) {
			String thisPlayer = m.group(1);
			String otherPlayer = (String)lastPartyMessage[1];
			boolean found = false;
			for(ArrayList<String> party : parties) {
				if(party.contains(otherPlayer)) {
					party.add(thisPlayer);
					found = true;
					break;
				}
			}
			if(!found) {
				ArrayList<String> newParty = new ArrayList<String>();
				newParty.add(thisPlayer);
				newParty.add(otherPlayer);
				parties.add(newParty);
			}
		}
		if(bool) {
			lastPartyMessage[0] = time;
			lastPartyMessage[1] = m.group(1);
		}
	}
	
	protected void updateHudText(HudText hudText) {
		for(int i = 0; i<this.hudText.size(); i++) {
			HudText entry = this.hudText.get(i);
			if(hudText.samePriority(entry)) {
				this.hudText.remove(entry);
			}
		}
		this.hudText.add(hudText);
	}
	
	public ArrayList<HudText> getHudText() {
		return this.hudText;
	}
	
	public void countCoins(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A76\\+(\\d+) coins").matcher(msg);
		if(m.find()) {
			try {
				coins += Integer.parseInt(m.group(1));
				updateHudText(new HudText(Priority.COINS,ChatFormatting.GOLD + "Coins: " + coins));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return false;
	}
	
	public static class ChatMessage {
		private long time;
		private String msg;
		
		public ChatMessage(long time, String msg) {
			this.time = time;
			this.msg = msg;
		}
		
		public void setTime(long l) {
			time = l;
		}
		
		public void setMsg(String s) {
			this.msg = s;
		}
		
		public long getTime() {
			return this.time;
		}
		
		public String getMsg() {
			return this.msg;
		}
	}
	
}
