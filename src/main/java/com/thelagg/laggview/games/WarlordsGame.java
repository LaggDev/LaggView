package com.thelagg.laggview.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.Hud.HudText;
import com.thelagg.laggview.hud.Hud.Priority;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class WarlordsGame extends Game {

	int damage;
	int healing;
	int kills;
	int assists;
	
	public WarlordsGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.WARLORDS, serverId, mc, laggView);
		this.updateHudText(new HudText(Priority.KILLS, ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		this.updateHudText(new HudText(Priority.ASSISTS, ChatFormatting.LIGHT_PURPLE + "Deaths: " + assists));
		this.updateHudText(new HudText(Priority.DAMAGE, ChatFormatting.LIGHT_PURPLE + "Damage: " + damage));
		this.updateHudText(new HudText(Priority.HEALING, ChatFormatting.LIGHT_PURPLE + "Healing: " + healing));
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		String msg = event.message.getFormattedText();
		Matcher m = Pattern.compile("").matcher(msg);
		Matcher m2 = Pattern.compile("").matcher(msg);
		Matcher m3 = Pattern.compile("").matcher(msg);
		Matcher m4 = Pattern.compile("").matcher(msg);
		if(m.find()) {
			this.kills++;
			this.updateHudText(new HudText(Priority.KILLS, ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		} else if (m2.find()) {
			this.assists++;
			this.updateHudText(new HudText(Priority.ASSISTS, ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
		} else if (m3.find()) {
			 this.damage += Integer.parseInt(m.group(1));
			 this.updateHudText(new HudText(Priority.DAMAGE, ChatFormatting.LIGHT_PURPLE + "Damage: " + damage));
		} else if (m4.find()) {
			this.healing += Integer.parseInt(m.group(1));
			this.updateHudText(new HudText(Priority.HEALING, ChatFormatting.LIGHT_PURPLE + "Healing: " + healing));
		}
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "W/L", (PlayerRequest p) -> p.getWarlordsWLRStr());
	}

}
