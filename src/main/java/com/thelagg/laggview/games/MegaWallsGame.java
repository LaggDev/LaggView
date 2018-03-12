package com.thelagg.laggview.games;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Timer;

import org.apache.logging.log4j.Level;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.hud.GuiOverlay;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.MainHud.HudText;
import com.thelagg.laggview.hud.MainHud.Priority;
import com.thelagg.laggview.utils.URLConnectionReader;
import com.thelagg.laggview.utils.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MegaWallsGame extends Game {

	private Map<String,Integer> playerFinalKills = new HashMap<String,Integer>();
	private int kills, assists, finalKills, finalAssists;
	private long countdownStart;
	
	public MegaWallsGame(String serverId, Minecraft mc, LaggView laggView) {
		super(GameType.MEGA_WALLS, serverId,mc, laggView);
		kills = 0;
		assists = 0;
		finalKills = 0;
		finalAssists = 0;
		this.updateHudText(new HudText(Priority.FINAL_KILLS,ChatFormatting.LIGHT_PURPLE + "Final Kills: " + finalKills));
		this.updateHudText(new HudText(Priority.FINAL_ASSISTS,ChatFormatting.LIGHT_PURPLE + "Final Assists: " + finalAssists));
		this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
	}
	
	public void printFinalKillsByTeam() {
		new Thread() {
			public void run() {
				String str = "";
				if(Minecraft.getMinecraft().ingameGUI.getTabList() instanceof TabOverlay) {
					TabOverlay tab = (TabOverlay)Minecraft.getMinecraft().ingameGUI.getTabList();
					NetworkPlayerInfo[] players = tab.currentlyDisplayedPlayers;
					Map<Character,Integer> finalCounts = new HashMap<Character,Integer>();
					for(NetworkPlayerInfo p : players) {
						String name = tab.getPlayerName(p);
						Matcher m = Pattern.compile("\u00A7.{1}\u00A7(.{1})(\\[.{1}] |)(\u00A7.{1}|)(\\S+)\u00A77 \\[.{3}]").matcher(name);
						if(m.find()) {
							char team = m.group(1).charAt(0);
							String playerName = m.group(4);
							if(MegaWallsGame.this.playerFinalKills.containsKey(playerName)) {
								int numberOfFinals = MegaWallsGame.this.playerFinalKills.get(playerName);
								finalCounts.put(team, finalCounts.get(team)==null?numberOfFinals:(finalCounts.get(team)+numberOfFinals));
							}
						} else {
							laggView.logger.log(Level.INFO, "could not match name for player: " + name);
						}
					}
					for(Character c : finalCounts.keySet()) {
						str += "\u00A7" + c.toString() + "" + finalCounts.get(c).toString() + " ";
					}
					Util.print(str);
				} else {
					Util.print("Error, tab overlay not loaded");
				}
			}
		}.start();
	}
	
	@Override
	public void onTick(ClientTickEvent event) {
		super.onTick(event);
		if(countdownStart!=0) {
			long time = System.currentTimeMillis();
			if(time-countdownStart>30000) {
				countdownStart = 0;
				this.removeHudText(Priority.WITHER_TIMER);
			} else {
				this.updateHudText(new HudText(Priority.WITHER_TIMER,ChatFormatting.LIGHT_PURPLE + "Wither: " + (((double)(time-countdownStart))/1000.0)));
			}
		}
	}
	
	public void onPotionAdded(PotionEffect potionEffect) {
		
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		checkForMyKillsAndAssists(event.message.getFormattedText());
		countFinals(event.message.getFormattedText());
	}
	
	public void checkForWitherDeath(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A7eThe \u00A7r\u00A7.\\S+ Wither \u00A7r\u00A7ehas died!\u00A7r").matcher(msg);
		if(m.find()) {
			long time = System.currentTimeMillis();
			new Thread() {
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Minecraft mc = Minecraft.getMinecraft();
					if(mc.ingameGUI instanceof GuiOverlay) {
						List<String> lines = GuiOverlay.getSidebarScores(mc.theWorld.getScoreboard());
						int notDead = 0;
						for(String s : lines) {
							if(s.replaceAll("\u00A7.", "").trim().matches("\\[.] \\S+ Wither: \\d+")) {
								notDead++;
							}
						}
						if(notDead==1) {
							MegaWallsGame.this.countdownStart = time;
						}
					}
				}
			}.start();
		}
	}
	
	public void checkForMyKillsAndAssists(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A76\\+(\\d+) coins .*?\u00A7r\u00A7b\u00A7lFINAL KILL\u00A7r").matcher(msg);
		Matcher m2 = Pattern.compile("\u00A7r\u00A76\\+(\\d+) coins .*?\u00A7r\u00A7b\u00A7lFINAL KILL \u00A7r\u00A7c\u00A7lASSIST \u00A7r\u00A76on \u00A7r\u00A7.{1}\\S+\u00A7r").matcher(msg);
		Matcher m3 = Pattern.compile("\u00A7r\u00A76\\+(\\d+) coins .*?\u00A7r\u00A76 \\(\\d+/18 Kills\\)\u00A7r").matcher(msg);
		Matcher m4 = Pattern.compile("\u00A7r\u00A76\\+(\\d+) coins .*?\u00A7r\u00A76 \\(\\d+/18 Assists\\) \u00A7r\u00A7c\u00A7lASSIST \u00A7r\u00A76on \u00A7r\u00A7.{1}\\S+\u00A7r").matcher(msg);
		if(m.find()) {
			finalKills++;
			this.updateHudText(new HudText(Priority.FINAL_KILLS,ChatFormatting.LIGHT_PURPLE + "Final Kills: " + finalKills));
		} else if (m2.find()) {
			finalAssists++;
			this.updateHudText(new HudText(Priority.FINAL_ASSISTS,ChatFormatting.LIGHT_PURPLE + "Final Assists: " + finalAssists));
		} else if (m3.find()) {
			kills++;
			this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		} else if (m4.find()) {
			assists++;
			this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
		}
	}
	
	public void countFinals(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A7.{1}\\S+\u00A7r\u00A7f \u00A7r\u00A7f.*? by \u00A7r\u00A7.{1}(\\S+)('s |)\u00A7r").matcher(msg);
		String lastMsg = chatMessages.size()<2?null:this.chatMessages.get(chatMessages.size()-2).getMsg();
		if(lastMsg==null) return;
		Matcher m3 = Pattern.compile("\u00A7r\u00A76\\+(\\d+) coins .*?\u00A7r\u00A76 \\(\\d+/18 Kills\\)\u00A7r").matcher(lastMsg);
		if(m.find() && !m3.find()) {
			addFinalKill(m.group(1));
		}
	}
	
	public void addFinalKill(String player) {
		playerFinalKills.put(player, playerFinalKills.get(player)==null?1:(playerFinalKills.get(player)+1));
	}
	
	@Override
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "Post-Update Final K/D", (PlayerRequest p) -> p.getMWFinalKDRStr(), (String s1) -> (s1.replaceAll("\u00A7.\\[.] ", "").replaceAll("\u00A77( |)\\[...]", "")), (String s) -> s.replaceAll("\u00A7k", ""));		
	}
}
