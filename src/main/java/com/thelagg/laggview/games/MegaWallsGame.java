package com.thelagg.laggview.games;

import java.util.HashMap;
import java.util.Map;

import com.thelagg.laggview.Game;

public class MegaWallsGame extends Game {

	Map<String,Integer[]> playerState = new HashMap<String,Integer[]>();
	
	
	public MegaWallsGame(GameType type, String serverId) {
		super(type, serverId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void drawGraphics() {
		
	}
}
