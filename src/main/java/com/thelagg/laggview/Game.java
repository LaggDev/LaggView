package com.thelagg.laggview;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
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
		DUELS("DUELS");
		
		private String nameOnScoreboard;
		
		private GameType(String nameOnScoreboard) {
			this.nameOnScoreboard = nameOnScoreboard;
		}
		
		public String getNameOnScoreboard() {
			return this.nameOnScoreboard;
		}
		
	}

	
	public ArrayList<String> possibleParties = new ArrayList<String>();
	public ArrayList<String> playerNames = new ArrayList<String>();
	public ArrayList<UUID> playerUUIDs = new ArrayList<UUID>();
	public ArrayList<String> chatMessages = new ArrayList<String>();
	
	public Game() {
		
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		
	}
}
