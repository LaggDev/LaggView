package com.thelagg.laggview.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.games.Game.GameType;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.Hud.HudText;
import com.thelagg.laggview.hud.Hud.Priority;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class BedwarsGame extends Game {
	
	private int kills = 0;
	private int assists = 0;
	private int souls = 0;
	
	public BedwarsGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.BED_WARS, serverId, mc, laggView);
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
        String s1 = tabOverlay.getPlayerName(player);
        tabOverlay.setFooter(new ChatComponentText(ChatFormatting.GREEN + "Displaying " + ChatFormatting.RED + "Bedwars Level" + ChatFormatting.GREEN + " in tab"));
        PlayerRequest playerRequest = laggView.apiCache.getPlayerResult(player.getGameProfile().getId(), 1);
        SessionRequest sessionRequest = laggView.apiCache.getSessionResult(mc.thePlayer.getUniqueID(), 1);
        if(sessionRequest!=null && sessionRequest.timeRequested-System.currentTimeMillis()>60*1000) {
        	laggView.apiCache.update(sessionRequest);
        }

        if(this.showRealNames && playerRequest!=null && playerRequest.getName()!=null && playersToReveal!=null && playersToReveal.contains(player.getGameProfile().getId())) {
        	tabOverlay.getSecondNames().put(player, s1);
        	s1 = s1.replaceAll(player.getGameProfile().getName(), ChatFormatting.DARK_RED + playerRequest.getName());
        }
        
        String lvl = playerRequest==null?"?":playerRequest.getSkywarsKDRStr();
        tabOverlay.getNamesInTab().put(player, s1);
        tabOverlay.getSuffixes().put(player, lvl);
		return true;
	}
	
}
