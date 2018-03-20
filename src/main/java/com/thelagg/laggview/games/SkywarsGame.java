package com.thelagg.laggview.games;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.apirequests.StatGetter;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.GuiOverlay;
import com.thelagg.laggview.hud.MainHud.HudText;
import com.thelagg.laggview.hud.MainHud.Priority;
import com.thelagg.laggview.quests.Quest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class SkywarsGame extends Game {

	private int kills = 0;
	private int assists = 0;
	private int souls = 0;
	private String mode = "solo";
	
	public SkywarsGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.SKYWARS, serverId, mc, laggView);
		this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
		this.updateHudText(new HudText(Priority.SOULS,ChatFormatting.LIGHT_PURPLE + "Souls: " + souls));
		quests = new Quest[] {
				new Quest("skywars_solo_win","Solo Win",1),
				new Quest("skywars_solo_kills","Solo Kills",15),
				new Quest("skywars_team_win","Team Win",1),
				new Quest("skywars_team_kills","Team Kills",15),
				new Quest("skywars_arcade_win","Lab Win",1),
				new Quest("skywars_weekly_kills","Weekly Kills",150),
				new Quest("skywars_weekly_arcade_win_all","Weekly Lab Wins",10),
				new Quest("skywars_daily_tokens","Tokens (S/T Kills)",10),
				new Quest("skywars_weekly_free_loot_chest","Weekly Easy Chest (S/T Wins)",1),
				new Quest("skywars_weekly_hard_chest","Weekly Hard Chest (S/T Wins)",7)
		};
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		checkForMyKillsAndAssists(event.message.getFormattedText());
	}
	
	public void updateMode() {
		if(mode.equals("solo") && mc.ingameGUI instanceof GuiOverlay) {
			for(String s : ((GuiOverlay)mc.ingameGUI).getScoreboard()) {
				if(s.contains("Lab:")) {
					mode = "lab";
				}
			}
			if(mode.equals("solo")) {
				for(String s : ((GuiOverlay)mc.ingameGUI).getScoreboard()) {
					if(s.contains("Teams Left:")) {
						mode = "teams";
					}
				}
			}
		}
	}
	
	/*
				new Quest("skywars_solo_win","Solo Win",1),
				new Quest("skywars_solo_kills","Solo Kills",15),
				new Quest("skywars_team_win","Team Win",1),
				new Quest("skywars_team_kills","Team Kills",15),
				new Quest("skywars_arcade_win","Lab Win",1),
				new Quest("skywars_weekly_kills","Weekly Kills",150),
				new Quest("skywars_weekly_arcade_win_all","Weekly Lab Wins",10),
				new Quest("skywars_daily_tokens","Tokens (S/T Kills)",10),
				new Quest("skywars_weekly_free_loot_chest","Weekly Easy Chest (S/T Wins)",1),
				new Quest("skywars_weekly_hard_chest","Weekly Hard Chest (S/T Wins)",7)
	 */
	
	public void checkForMyKillsAndAssists(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A76\\+\\d+ coins!.*  Kill\u00A7r").matcher(msg);
		Matcher m2 = Pattern.compile("\u00A7rYou have assisted killing \u00A7r\u00A7.\\S+ \u00A7r\u00A7e!\u00A7r").matcher(msg);
		Matcher m3 = Pattern.compile("\u00A7r\u00A7b\\+(\\d+) Soul").matcher(msg);
		if(m.find()) {
			kills++;
			this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
			switch(mode) {
				case "solo":
					getQuest("skywars_solo_kills").increaseValue(1);
					getQuest("skywars_daily_tokens").increaseValue(1);
					getQuest("skywars_weekly_kills").increaseValue(1);
					break;
				case "teams":
					getQuest("skywars_team_kills").increaseValue(1);
					getQuest("skywars_daily_tokens").increaseValue(1);
					getQuest("skywars_weekly_kills").increaseValue(1);
					break;
				case "lab":
					getQuest("skywars_weekly_kills").increaseValue(1);
					break;
			}
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
