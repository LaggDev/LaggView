package com.thelagg.laggview.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.apirequests.StatGetter;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.MainHud.HudText;
import com.thelagg.laggview.hud.MainHud.Priority;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class SkywarsGame extends Game {

	private int kills = 0;
	private int assists = 0;
	private int souls = 0;
	
	public SkywarsGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.SKYWARS, serverId, mc, laggView);
		this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
		this.updateHudText(new HudText(Priority.SOULS,ChatFormatting.LIGHT_PURPLE + "Souls: " + souls));
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		checkForMyKillsAndAssists(event.message.getFormattedText());
	}
	
	public void checkForMyKillsAndAssists(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A76\\+\\d+ coins!.*  Kill\u00A7r").matcher(msg);
		Matcher m2 = Pattern.compile("\u00A7rYou have assisted killing \u00A7r\u00A7.\\S+ \u00A7r\u00A7e!\u00A7r").matcher(msg);
		Matcher m3 = Pattern.compile("\u00A7r\u00A7b\\+(\\d+) Soul").matcher(msg);
		if(m.find()) {
			kills++;
			this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		} else if (m2.find()) {
			assists++;
			this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
		} else if (m3.find()) {
			souls+=Integer.parseInt(m3.group(1));
			this.updateHudText(new HudText(Priority.SOULS,ChatFormatting.LIGHT_PURPLE + "Souls: " + souls));
		}
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "K/D", (PlayerRequest p) -> p.getSkywarsKDRStr());
	}
	
}
