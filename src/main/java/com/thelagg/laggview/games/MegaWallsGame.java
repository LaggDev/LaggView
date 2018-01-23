package com.thelagg.laggview.games;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.Game;
import com.thelagg.laggview.hud.Hud.HudText;
import com.thelagg.laggview.hud.Hud.Priority;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class MegaWallsGame extends Game {

	Map<String,Integer> playerFinalKills = new HashMap<String,Integer>();
	private int kills, assists, finalKills, finalAssists;
	
	public MegaWallsGame(String serverId, Minecraft mc) {
		super(GameType.MEGA_WALLS, serverId,mc);
		kills = 0;
		assists = 0;
		finalKills = 0;
		finalAssists = 0;
		this.updateHudText(new HudText(Priority.FINAL_KILLS,ChatFormatting.LIGHT_PURPLE + "Final Kills: " + finalKills));
		this.updateHudText(new HudText(Priority.FINAL_ASSISTS,ChatFormatting.LIGHT_PURPLE + "Final Assists: " + finalAssists));
		this.updateHudText(new HudText(Priority.KILLS,ChatFormatting.LIGHT_PURPLE + "Kills: " + kills));
		this.updateHudText(new HudText(Priority.ASSISTS,ChatFormatting.LIGHT_PURPLE + "Assists: " + assists));
	}

	@Override
	public void onChat(ClientChatReceivedEvent event) {
		super.onChat(event);
		checkForMyKillsAndAssists(event.message.getFormattedText());
		countFinals(event.message.getFormattedText());
	}
	
	public void checkForMyKillsAndAssists(String msg) {
		Matcher m = Pattern.compile("§r§6\\+(\\d+) coins .*?§r§b§lFINAL KILL§r").matcher(msg);
		Matcher m2 = Pattern.compile("§r§6\\+(\\d+) coins .*?§r§b§lFINAL KILL §r§c§lASSIST §r§6on §r§.{1}\\S+§r").matcher(msg);
		Matcher m3 = Pattern.compile("§r§6\\+(\\d+) coins .*?§r§6 \\(\\d+/18 Kills\\)§r").matcher(msg);
		Matcher m4 = Pattern.compile("§r§6\\+(\\d+) coins .*?§r§6 \\(\\d+/18 Assists\\) §r§c§lASSIST §r§6on §r§.{1}\\S+§r").matcher(msg);
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
		Matcher m = Pattern.compile("§r§.{1}\\S+§r§f §r§f.*? by §r§.{1}(\\S+)('s |)§r").matcher(msg);
		if(m.find()) {
			addFinalKill(m.group(1));
		}		
	}
	
	public void addFinalKill(String player) {
		if(playerFinalKills.containsKey(player)) {
			playerFinalKills.put(player, playerFinalKills.get(player)+1);
		} else {
			playerFinalKills.put(player, 1);
		}
	}
}
