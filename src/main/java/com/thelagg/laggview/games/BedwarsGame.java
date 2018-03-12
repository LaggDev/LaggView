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

public class BedwarsGame extends Game {
	
	private int kills = 0;
	private int bedsDestroyed = 0;
	private int finalKills = 0;
	
	public BedwarsGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.BED_WARS, serverId, mc, laggView);
		this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		this.updateHudText(new HudText(Priority.FINAL_KILLS,ChatFormatting.LIGHT_PURPLE + "Final Kills: " + finalKills));
		this.updateHudText(new HudText(Priority.BEDS_DESTROYED,ChatFormatting.LIGHT_PURPLE + "Beds Destroyed: " + bedsDestroyed));
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		checkForMyKills(event.message.getFormattedText());
	}
	
	public void checkForMyKills(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A7.[a-zA-Z0-9_]+ \u00A7r\u00A77.*? \u00A7r\u00A7." + mc.thePlayer.getName() + "\u00A7r\u00A77.\u00A7r").matcher(msg);
		Matcher m2 = Pattern.compile("\u00A7r\u00A76\\+\\d+ coins\\!.*? \\(Bed Destroyed\\)\u00A7r").matcher(msg);
		Matcher m3 = Pattern.compile("\u00A7r\u00A76\\+\\d+ coins\\!.*? \\(Final Kill\\)\u00A7r").matcher(msg);
		Matcher m4 = Pattern.compile("\u00A7r\u00A76\\+\\d+ coins\\!.*? \\(Win\\)\u00A7r").matcher(msg);
		
		if(m.find()) {
			kills++;
			this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		} else if (m2.find()) {
			bedsDestroyed++;
			this.updateHudText(new HudText(Priority.BEDS_DESTROYED,ChatFormatting.LIGHT_PURPLE + "Beds Destroyed: " + bedsDestroyed));
		} else if (m3.find() || m4.find()) {
			finalKills++;
			this.updateHudText(new HudText(Priority.FINAL_KILLS,ChatFormatting.LIGHT_PURPLE + "Final Kills: " + finalKills));
		}
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "Final K/D", (PlayerRequest p) -> p.getMWFinalKDRStr());
	}
	
}
