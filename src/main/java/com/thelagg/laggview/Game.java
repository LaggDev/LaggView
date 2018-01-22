package com.thelagg.laggview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
	private ArrayList<ArrayList<String>> parties;
	private Map<Long,String> chatMessages;
	private GameType gameType;
	private String serverId;
	private long timeJoined;
	private Object[] lastPartyMessage;
	
	public Game(GameType type, String serverId) {
		this.gameType = type;
		this.serverId = serverId;
		timeJoined = System.currentTimeMillis();
		playerUUIDs = new ArrayList<UUID>();
		playerNames = new ArrayList<String>();
		chatMessages = new HashMap<Long,String>();
		parties = new ArrayList<ArrayList<String>>();
		MinecraftForge.EVENT_BUS.register(this);
		lastPartyMessage = new Object[] {0,"test"};
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
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		long time = System.currentTimeMillis();
		if(event.type!=2) {
			chatMessages.put(time, event.message.getFormattedText());
			processParty(time,event.message.getFormattedText());
		}
	}
	
	private void processParty(long time,String msg) {
		Pattern p = Pattern.compile("§r§.{1}(.*?)§r§e has joined \\(§r§b\\d+§r§e/§r§b\\d+§r§e\\)!§r");
		Matcher m = p.matcher(msg);
		long lastTime;
		try {
			lastTime = ((Long)lastPartyMessage[0]);
		} catch (ClassCastException e) {
			lastTime = ((Integer)lastPartyMessage[0]);
		}
		System.out.println(time + " " + lastTime);
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
	
	public static class Message {
		public long time;
		public String msg;
	}
	
}
