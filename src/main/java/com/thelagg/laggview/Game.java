package com.thelagg.laggview;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

enum GameState {
	PREGAME,
	INGAME,
	POSTGAME
}

public class Game {
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
