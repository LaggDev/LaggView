package com.thelagg.laggview.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.GuildRequest;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuildMonitor {
	
	private LaggView laggView;
	private Minecraft mc;
	private List<UUID> guildMembers;
	private List<String> partyMembers;
	private Timer timer;
	private boolean checkingForParty = false;
	private boolean updatedParty = false;
	private boolean checkForPartiesNextMsg = false;
	
	public GuildMonitor(Minecraft mc, LaggView laggView) {
		this.mc = mc;
		this.laggView = laggView;
		guildMembers = new ArrayList<UUID>();
		partyMembers = new ArrayList<String>();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if(mc.thePlayer==null || mc.theWorld==null) {
					return;
				}
				GuildRequest r = laggView.apiCache.getGuildResult(mc.thePlayer.getUniqueID(), 0);
				guildMembers = r.getUUIDs();
				updateParty();
				updateNameTags();
			}
			
		}, 0, 1000*60*2);
	}
	
	public void updateNameTags() {
		for(Entity e : mc.theWorld.getLoadedEntityList()) {
			if(e instanceof EntityPlayer) {
				EntityPlayer p = (EntityPlayer)e;
				p.refreshDisplayName();
			}
		}
	}
	
	public void checkForPartiesNextMsg() {
		this.checkForPartiesNextMsg = true;
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		String str = event.message.getFormattedText();
		Matcher m = Pattern.compile("\u00A7aYou joined (.*?)[a-zA-Z0-9_]+\u00A7r\u00A7a's party!\u00A7r").matcher(str);
		Matcher m1 = Pattern.compile("\u00A7aParty members \\(\\d+\\): ").matcher(str);
		Matcher m2 = Pattern.compile("(\\S+ |\u00A7r\u00A77)([a-zA-Z0-9_]+)\u00A7r\u00A7a").matcher(str);
		Matcher m3 = Pattern.compile("(.*?)[a-zA-Z0-9_]+ \u00A7r\u00A7eleft the party.\u00A7r").matcher(str);
		Matcher m4 = Pattern.compile(".*?[a-zA-Z0-9_]+ \u00A7r\u00A7chas been offline for 5 minutes and was removed from your party.\u00A7r").matcher(str);
		Matcher m5 = Pattern.compile("\u00A7cThe party leader, .*?[a-zA-Z0-9_]+\u00A7r\u00A7c, has been offline for 5 minutes and the party has been disbanded.\u00A7r").matcher(str);
		Matcher m6 = Pattern.compile("(.*?)[a-zA-Z0-9_]+ \u00A7r\u00A7ajoined the party!\u00A7r").matcher(str);
		Matcher m7 = Pattern.compile("(.*?)[a-zA-Z0-9_]+ \u00A7r\u00A7ahas been removed from your party\u00A7r").matcher(str);
		Matcher m8 = Pattern.compile("(.*?)[a-zA-Z0-9_]+ \u00A7r\u00A7ehas disbanded the party!\u00A7r").matcher(str);
		Matcher m9 = Pattern.compile("\u00A7aYou left the party\u00A7r").matcher(str);
		Matcher m10 = Pattern.compile("\u00A7cYou must be in a party to use this command!\u00A7r").matcher(str);
		if(m.find() || m3.find() || m4.find() || m5.find() || m6.find() || m7.find() || m8.find() || m9.find()) {
			checkForPartiesNextMsg();
			return;
		}
		if(checkForPartiesNextMsg) {
			checkForPartiesNextMsg = false;
			new Thread(() -> updateParty()).start();
		}
		if(m1.find() || m10.find()) {
			if(!checkingForParty) {
				partyMembers = new ArrayList<String>();
			}
			while(m2.find()) {
				if(!partyMembers.contains(m2.group(2))) {
					partyMembers.add(m2.group(2));
				}
			}
			if(checkingForParty) {
				event.setCanceled(true);
				updatedParty = true;
			} else {
				this.updateNameTags();
			}
		}
		
		if(checkingForParty && str.equals("\u00A76-----------------------------------------------------\u00A7r")) {
			event.setCanceled(true);
			if(updatedParty) {
				checkingForParty = false;
				updatedParty = false;
			}
		} 
	}
	
	public void updateParty() {
		this.partyMembers = new ArrayList<String>();
		checkingForParty = true;
		mc.thePlayer.sendChatMessage("/p list");
		long timeSent = System.currentTimeMillis();
		long timeout = 3000;
		while(checkingForParty && System.currentTimeMillis()-timeSent<=timeout) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(checkingForParty) {
			checkingForParty = false;
			updatedParty = false;
		}
		updateNameTags();
	}
	
	@SubscribeEvent
	public void onPlayerName(PlayerEvent.NameFormat event) {
		UUID uuid = event.entityPlayer.getUniqueID();
		if(partyMembers.contains(event.username)) {
			event.displayname = event.displayname + ChatFormatting.DARK_BLUE + " [P]";
		}
		if(guildMembers.contains(uuid)) {
			event.displayname = event.displayname + ChatFormatting.DARK_GREEN + " [G]";
		}
	}
}
