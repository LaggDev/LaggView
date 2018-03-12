package com.thelagg.laggview.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.games.Game.GameType;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.MainHud.HudText;
import com.thelagg.laggview.hud.MainHud.Priority;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class UHCGame extends Game {
	
	public UHCGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.UHC_CHAMPIONS, serverId, mc, laggView);
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "K/D", (PlayerRequest p) -> p.getUHCKDRStr());
	}
}
