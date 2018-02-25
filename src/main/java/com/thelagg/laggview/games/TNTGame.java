package com.thelagg.laggview.games;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.games.Game.GameType;
import com.thelagg.laggview.hud.GuiOverlay;
import com.thelagg.laggview.hud.TabOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;

public class TNTGame extends Game {
	
	public static TNTGame getTNTGame(String serverId, Minecraft mc, LaggView laggView) {
		switch(mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName().replaceAll("\u00A7.", "").trim()) {
		case "THE TNT GAMES":
			if(mc.ingameGUI instanceof GuiOverlay) {
				for(String s : ((GuiOverlay)mc.ingameGUI).getScoreboard()) {
					s = s.replaceAll("[^\\x00-\\x7F]", "");
					System.out.println(s);
					if(s.contains("TNT Run")) {
						return new TNTRunGame(serverId, mc, laggView);
					} else if (s.contains("TNT Wizards")) {
						return new WizardsGame(serverId, mc, laggView);
					} else if (s.contains("PVP Run")) {
						return new PvPRunGame(serverId, mc, laggView);
					} else if (s.contains("Bow Spleef")) {
						return new BowSpleefGame(serverId, mc, laggView);
					} else if (s.contains("TNT Tag")) {
						return new TNTTagGame(serverId, mc, laggView);
					}
				}
			}
			return new TNTGame(serverId, mc, laggView);
		case "TNT RUN":
			return new TNTRunGame(serverId, mc, laggView);
		case "BOW SPLEEF":
			return new BowSpleefGame(serverId, mc, laggView);
		case "PVP RUN":
			return new PvPRunGame(serverId, mc, laggView);
		case "TNT TAG":
			return new TNTTagGame(serverId, mc, laggView);
		case "WIZARDS":
			return new WizardsGame(serverId, mc, laggView);
		default:
			return new TNTGame(serverId, mc, laggView);
		}
	}
	
	public TNTGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.THE_TNT_GAMES, serverId, mc, laggView);
	}
	
	public static class TNTRunGame extends TNTGame {

		public TNTRunGame(String serverId, Minecraft mc, LaggView laggView) {
			super(serverId, mc, laggView);
		}
		
		@Override
		public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
			return this.genericProcessPlayerTab(player, tabOverlay, "W/L", (PlayerRequest p) -> p.getTNTRunWinsStr());
		}
		
	}
	
	public static class BowSpleefGame extends TNTGame {
		
		public BowSpleefGame(String serverId, Minecraft mc, LaggView laggView) {
			super(serverId, mc, laggView);
		}
		
		@Override
		public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
			return this.genericProcessPlayerTab(player, tabOverlay, "W/L", (PlayerRequest p) -> p.getBowSpleefWinsStr());
		}
		
	}
	
	public static class WizardsGame extends TNTGame {
		
		public WizardsGame(String serverId, Minecraft mc, LaggView laggView) {
			super(serverId, mc, laggView);
		}
		
		@Override
		public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
			return this.genericProcessPlayerTab(player, tabOverlay, "K/D", (PlayerRequest p) -> p.getWizardsKDRStr());
		}
		
	}
	
	public static class PvPRunGame extends TNTGame {
		
		public PvPRunGame(String serverId, Minecraft mc, LaggView laggView) {
			super(serverId, mc, laggView);
		}
		
		@Override
		public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
			return this.genericProcessPlayerTab(player, tabOverlay, "W/L", (PlayerRequest p) -> p.getPVPRunWinsStr());
		}
		
	}
	
	public static class TNTTagGame extends TNTGame {
		
		public TNTTagGame(String serverId, Minecraft mc, LaggView laggView) {
			super(serverId, mc, laggView);
		}
		
		@Override
		public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
			return this.genericProcessPlayerTab(player, tabOverlay, "Wins", (PlayerRequest p) -> p.getTNTTagWinsStr());
		}
		
	}
}
