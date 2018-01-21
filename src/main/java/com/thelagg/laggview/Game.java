package com.thelagg.laggview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
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
		DUELS("DUELS");
		
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
	private Map<Long,String> chatMessages;
	private GameType gameType;
	private String serverId;
	private long timeJoined;
	
	public Game(GameType type, String serverId) {
		this.gameType = type;
		this.serverId = serverId;
		timeJoined = System.currentTimeMillis();
		playerUUIDs = new ArrayList<UUID>();
		playerNames = new ArrayList<String>();
		chatMessages = new HashMap<Long,String>();
		MinecraftForge.EVENT_BUS.register(this);
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
		chatMessages.put(System.currentTimeMillis(), event.message.getFormattedText());
	}
}
