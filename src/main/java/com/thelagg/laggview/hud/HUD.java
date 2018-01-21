package com.thelagg.laggview.hud;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.thelagg.laggview.Game;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
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
	
	private long lastWorldLoad;
	private GameType game;
	boolean waitingForServerId;
	String serverId;
	
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
				for(GameType type : GameType.values()) {
					if(type.nameOnScoreboard.equals(str.replaceAll("\u00A7.{1}", "").trim())) {
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
