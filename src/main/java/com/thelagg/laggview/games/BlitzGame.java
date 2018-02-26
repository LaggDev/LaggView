package com.thelagg.laggview.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.Hud.HudText;
import com.thelagg.laggview.hud.Hud.Priority;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class BlitzGame extends Game {

	private int kills = 0;
	private int assists = 0;
	
	public BlitzGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.BLITZ_SURVIVAL_GAMES, serverId, mc, laggView);
		this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		checkForMyKillsAndAssists(event.message.getFormattedText());
	}
	
	public void checkForMyKillsAndAssists(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A7eYou damaged \u00A7r\u00A7.[a-zA-Z0-9_]+\u00A7r\u00A7e for \u00A7r\u00A7b\\d+%\u00A7r\u00A7e before killing them!\u00A7r").matcher(msg);
		Matcher m2 = Pattern.compile("\u00A7r\u00A7eYou damaged \u00A7r\u00A7.[a-zA-Z0-9_]+\u00A7r\u00A7e for \u00A7r\u00A7b\\d+%\u00A7r\u00A7e before \u00A7r\u00A7.[a-zA-Z0-9_]+\u00A7r\u00A7e killed them!\u00A7r").matcher(msg);
		if(m.find()) {
			kills++;
			this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		} else if (m2.find()) {
			assists++;
			this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
		}
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "K/D", (PlayerRequest p) -> p.getBsgKDRStr(), (String s) -> s, (String s) -> s.replaceFirst("XXXXX", "").replaceAll("\u00A7k", ""));
	}

}
