package com.thelagg.laggview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.Timer;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.apirequests.GuildRequest;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuildMonitor {
	
	private LaggView laggView;
	private Minecraft mc;
	private List<UUID> guildMembers;
	private Timer t;
	
	public GuildMonitor(Minecraft mc, LaggView laggView) {
		this.mc = mc;
		this.laggView = laggView;
		guildMembers = new ArrayList<UUID>();
		t = new Timer(1000*60*3, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				GuildRequest r = laggView.apiCache.getCurrentGuild(mc.thePlayer.getUniqueID(),5);
				guildMembers = r.getUUIDs();
			}
			
		});
	}
	
	
	@SubscribeEvent
	public void onPlayerName(PlayerEvent.NameFormat event) {
		UUID uuid = event.entityPlayer.getUniqueID();
		if(guildMembers.contains(uuid)) {
			event.displayname = event.displayname + ChatFormatting.DARK_GREEN + " [G]";
		}
	}
}
