package com.thelagg.laggview.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.utils.Util;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Config {
	private static Configuration config;
	private static double textHudX;
	private static double textHudY;
	private static GuildPartyTagSetting guildPartyTagSetting;	
	private static double questHudX;
	private static double questHudY;
	private static boolean openStatsInBrowser;
	
	public static GuildPartyTagSetting getGuildPartyTagSetting() {
		return guildPartyTagSetting;
	}
	
	public static void init(File path) {
		if(config==null) {
			config = new Configuration(path);
			loadConfiguration();
		}
	}
	
	private static void loadConfiguration() {
		if(config==null) {
			Util.print("error loading configuration {config==null}");
			return;
		}
		textHudX = config.getFloat("textHudX", "general", 0.0f, 0.0f, 1.0f, "Relative X-position of the main text HUD");
		textHudY = config.getFloat("textHudY", "general", 0.0f, 0.0f, 1.0f, "Relative Y-position of the main text HUD");
		questHudX = config.getFloat("questHudX", "general", 0.0f, 0.0f, 1.0f, "Relative X-position of the quest HUD");
		questHudY = config.getFloat("questHudY", "general", 0.0f, 0.0f, 1.0f, "Relative Y-position of the quest HUD");
		guildPartyTagSetting = GuildPartyTagSetting.valueOf(config.getString("Guild/Party Tag Setting", "general", GuildPartyTagSetting.PARTY_TAKES_PRIORITY.toString(), "Displays [P] or [G] after someone's name if they're in your party/guild", GuildPartyTagSetting.getValues()));
		openStatsInBrowser = config.getBoolean("openStatsInBrowser","general",true,"When running /lagg stats or /lagg session, your default web browser will be opened automatically. If false, it will paste a link into chat.");
		
		if(config.hasChanged()) config.save();
	}
	
	@SubscribeEvent
	public void onConfigurationChangeEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.modID.equalsIgnoreCase(LaggView.MODID)) {
			loadConfiguration();
		}
	}
	
	public static Configuration getConfig() {
		return config;
	}
	
	public static boolean getOpenStatsInBrowser() {
		return openStatsInBrowser;
	}
	
	public static void setQuestHudX(double d) {
		questHudX = d;
		config.get("general", "questHudX", 0.0f, "Relative X-position of the quest HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public static void setQuestHudY(double d) {
		questHudY = d;
		config.get("general", "questHudY", 0.0f, "Relative Y-position of the quest HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public static double getQuestHudX() {
		return questHudX;
	}
	
	public static double getQuestHudY() {
		return questHudY;
	}
	
	public static void setTextHudX(double d) {
		textHudX = d;
		config.get("general", "textHudX", 0.0f, "Relative X-position of the main text HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public static void setTextHudY(double d) {
		textHudY = d;
		config.get("general", "textHudY", 0.0f, "Relative Y-position of the main text HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public static double getTextHudX() {
		return textHudX;
	}
	
	public static double getTextHudY() {
		return textHudY;
	}
	
	public enum GuildPartyTagSetting {
		PARTY_ONLY,
		GUILD_ONLY,
		PARTY_TAKES_PRIORITY,
		BOTH,
		NONE;
		
		public static String[] getValues() {
			List<String> strings = new ArrayList<String>();
			for(GuildPartyTagSetting g : GuildPartyTagSetting.values()) {
				strings.add(g.toString());
			}
			return strings.toArray(new String[strings.size()]);
		}
	}
}
