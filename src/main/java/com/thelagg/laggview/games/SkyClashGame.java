package com.thelagg.laggview.games;

import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.hud.TabOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class SkyClashGame extends Game {
	
	public SkyClashGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.SKYCLASH, serverId, mc, laggView);
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "K/D", (PlayerRequest p) -> p.getSkyClashKDRStr());
	}
}
