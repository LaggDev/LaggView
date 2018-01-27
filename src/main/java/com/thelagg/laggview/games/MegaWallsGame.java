package com.thelagg.laggview.games;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.Game;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.TabOverlay;
import com.thelagg.laggview.Util;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.hud.Hud.HudText;
import com.thelagg.laggview.hud.Hud.Priority;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MegaWallsGame extends Game {

	private Map<String,Integer> playerFinalKills = new HashMap<String,Integer>();
	private int kills, assists, finalKills, finalAssists;
	private List<PotionEffect> activePotionEffects = new ArrayList<PotionEffect>();
	
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

	public void sendToServer() {
		
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
						System.out.println(name);
						Matcher m = Pattern.compile("\u00A7.{1}\u00A7(.{1})(\\[.{1}] |)(\u00A7.{1}|)(\\S+)\u00A77 \\[.{3}]").matcher(name);
						if(m.find()) {
							char team = m.group(1).charAt(0);
							String playerName = m.group(4);
							if(MegaWallsGame.this.playerFinalKills.containsKey(playerName)) {
								int numberOfFinals = MegaWallsGame.this.playerFinalKills.get(playerName);
								finalCounts.put(team, finalCounts.get(team)==null?numberOfFinals:(finalCounts.get(team)+numberOfFinals));
							}
						} else {
							System.out.println("could not match name for player: " + name);
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
		/*
		EntityPlayerSP me = mc.thePlayer;
		Collection<PotionEffect> potionEffects = me==null?null:me.getActivePotionEffects();
		if(potionEffects!=null) {
            for (PotionEffect potioneffect : potionEffects) {
                if (!activePotionEffects.contains(potioneffect)) {
                    onPotionAdded(potioneffect);
                    activePotionEffects.add(potioneffect);
                }
            }
		}*/
	}
	
	public void onPotionAdded(PotionEffect potionEffect) {
		
	}
	
	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		checkForMyKillsAndAssists(event.message.getFormattedText());
		countFinals(event.message.getFormattedText());
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
        String s1 = tabOverlay.getPlayerName(player);
        
        String name = player.getGameProfile().getName(); 
        PlayerRequest playerRequest = laggView.apiCache.getPlayerResult(name, 1);
        SessionRequest sessionRequest = laggView.apiCache.getSessionResult(mc.thePlayer.getUniqueID(), 1);
        if(sessionRequest!=null && sessionRequest.timeRequested-System.currentTimeMillis()>60*1000) {
        	laggView.apiCache.update(sessionRequest);
        }
        String realName = "";
        if(playerRequest==null && sessionRequest!=null) {
        	PlayerRequest realPlayer = sessionRequest.findByNick(name);
        	if(realPlayer!=null && realPlayer.getName()!=null) {
        		realName += EnumChatFormatting.LIGHT_PURPLE + " (" + realPlayer.getName() + ")";
        		playerRequest = realPlayer;
        	}
        }
        s1 += realName;
        String finalkdr = playerRequest==null?"?":playerRequest.getFinalKDRString();
        tabOverlay.getNamesInTab().put(player, s1);
        tabOverlay.getSuffixes().put(player, finalkdr);
		return true;
	}
}
