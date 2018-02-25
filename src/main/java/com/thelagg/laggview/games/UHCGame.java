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

public class UHCGame extends Game {
	
	public UHCGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.UHC_CHAMPIONS, serverId, mc, laggView);
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
        String s1 = tabOverlay.getPlayerName(player);
        tabOverlay.setFooter(new ChatComponentText(ChatFormatting.GREEN + "Displaying " + ChatFormatting.RED + "K/D" + ChatFormatting.GREEN + " in tab"));
        PlayerRequest playerRequest = laggView.apiCache.getPlayerResult(player.getGameProfile().getId(), 1);
        SessionRequest sessionRequest = laggView.apiCache.getSessionResult(mc.thePlayer.getUniqueID(), 1);
        if(sessionRequest!=null && sessionRequest.timeRequested-System.currentTimeMillis()>60*1000) {
        	laggView.apiCache.update(sessionRequest);
        }

        if(this.showRealNames && playerRequest!=null && playerRequest.getName()!=null && playersToReveal!=null && playersToReveal.contains(player.getGameProfile().getId())) {
        	if(!s1.contains(playerRequest.getName())) {
        		tabOverlay.getSecondNames().put(player, s1);
        	}
        	s1 = s1.replaceAll(player.getGameProfile().getName(), ChatFormatting.DARK_RED + playerRequest.getName());
        }
        
        String kdr = playerRequest==null?"?":playerRequest.getUHCKDRStr();
        tabOverlay.getNamesInTab().put(player, s1);
        tabOverlay.getSuffixes().put(player, kdr);
		return true;
	}
}
