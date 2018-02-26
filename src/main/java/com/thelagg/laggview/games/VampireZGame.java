package com.thelagg.laggview.games;

import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.games.Game.GameType;
import com.thelagg.laggview.hud.TabOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class VampireZGame extends Game {
	
	public VampireZGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.VAMPIREZ, serverId, mc, laggView);
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "Human K/D", (PlayerRequest p) -> p.getVampireZKDRStr());
	}
}
