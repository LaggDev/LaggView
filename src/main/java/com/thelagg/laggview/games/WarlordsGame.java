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
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class WarlordsGame extends Game {

	private int damage;
	private int healing;
	
	public WarlordsGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.WARLORDS, serverId, mc, laggView);
		this.updateHudText(new HudText(Priority.DAMAGE, ChatFormatting.LIGHT_PURPLE + "Damage: " + damage));
		this.updateHudText(new HudText(Priority.HEALING, ChatFormatting.LIGHT_PURPLE + "Healing: " + healing));
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		String msg = event.message.getFormattedText();
		countAttack(msg);
	}
	
	public void countAttack(String message) {
		Matcher m = Pattern.compile("\u00A7r\u00A7[a-z0-9](\u00BB|\u00AB) \u00A7r\u00A77(You|Your .*?|[a-zA-Z0-9_]+'s .*?) (critically |)(hit|healed) ([a-zA-Z0-9_]+) for \u00A7r\u00A7[a-z0-9](\u00A7l|)(\\d+)(!|)\u00A7r\u00A77 (critical |)(damage|melee damage|health).\u00A7r").matcher(message);
		if(m.find()) {
			if(m.group(1).equals("\u00BB")) {
				if(m.group(4).equals("hit")) {
					this.damage += Integer.parseInt(m.group(7));
					 this.updateHudText(new HudText(Priority.DAMAGE, ChatFormatting.LIGHT_PURPLE + "Damage: " + damage));
				} else if (m.group(4).equals("healed")) {
					this.healing += Integer.parseInt(m.group(7));
					this.updateHudText(new HudText(Priority.HEALING, ChatFormatting.LIGHT_PURPLE + "Healing: " + healing));
				}	 else {
					System.err.println(message);
				}
			} else if (m.group(1).equals("\u00AB")){
				if(m.group(4).equals("hit")) {
					
				} else if (m.group(4).equals("healed")) {
					
				} else {
					System.err.println(message);
				}
			} else {
				System.err.println(message);
			}
		}
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "W/L", (PlayerRequest p) -> p.getWarlordsWLRStr());
	}

}
