package com.thelagg.laggview.hud;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HUD {
	
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
		
		String nameOnScoreboard;
		
		private GameType(String nameOnScoreboard) {
			this.nameOnScoreboard = nameOnScoreboard;
		}
	}
	
	private GameType game;
	
	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		String str = event.world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
		Logger.getLogger("laggview").log(Level.INFO,"GAME: " + str);
		for(GameType type : GameType.values()) {
			if(type.nameOnScoreboard.equals(str)) {
				game = type;
				Logger.getLogger("laggview").log(Level.INFO, "MATCHED WITH: " + type.name());
			}
		}
	}
}
