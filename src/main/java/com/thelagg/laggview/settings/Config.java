package com.thelagg.laggview.settings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.thelagg.laggview.LaggView;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Config {
	private Configuration config;
	private double textHudX;
	private double textHudY;
	private GuildPartyTagSetting guildPartyTagSetting;	
	private double questHudX;
	private double questHudY;
	
	public GuildPartyTagSetting getGuildPartyTagSetting() {
		return guildPartyTagSetting;
	}
	
	public void init(File path) {
		if(config==null) {
			config = new Configuration(path);
			loadConfiguration();
		}
	}
	
	private void loadConfiguration() {
		textHudX = config.getFloat("textHudX", "general", 0.0f, 0.0f, 1.0f, "Relative X-position of the main text HUD");
		textHudY = config.getFloat("textHudY", "general", 0.0f, 0.0f, 1.0f, "Relative Y-position of the main text HUD");
		questHudX = config.getFloat("questHudX", "general", 0.0f, 0.0f, 1.0f, "Relative X-position of the quest HUD");
		questHudY = config.getFloat("questHudY", "general", 0.0f, 0.0f, 1.0f, "Relative Y-position of the quest HUD");
		guildPartyTagSetting = GuildPartyTagSetting.valueOf(config.getString("Guild/Party Tag Setting", "general", GuildPartyTagSetting.PARTY_TAKES_PRIORITY.toString(), "Displays [P] or [G] after someone's name if they're in your party/guild", GuildPartyTagSetting.getValues()));
				
		if(config.hasChanged()) config.save();
	}
	
	public static String getString(Configuration c, String name, String category, String defaultValue, String comment) {
        Property prop = c.get(category, name, defaultValue);
        prop.setValidationPattern(null);
        prop.comment = comment;
        return prop.getString();
	}
	
	public static boolean getBoolean(Configuration c, String name, String category, boolean defaultValue, String comment) {
        Property prop = c.get(category, name, defaultValue);
        prop.setLanguageKey(name);
        prop.comment = comment;
        return prop.getBoolean(defaultValue);
	}
	
	@SubscribeEvent
	public void onConfigurationChangeEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.modID.equalsIgnoreCase(LaggView.MODID)) {
			loadConfiguration();
		}
	}
	
	public Configuration getConfig() {
		return config;
	}
	
	public void setQuestHudX(double d) {
		questHudX = d;
		config.get("general", "questHudX", 0.0f, "Relative X-position of the quest HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public void setQuestHudY(double d) {
		questHudY = d;
		config.get("general", "questHudY", 0.0f, "Relative Y-position of the quest HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public double getQuestHudX() {
		return this.questHudX;
	}
	
	public double getQuestHudY() {
		return this.questHudY;
	}
	
	public void setTextHudX(double d) {
		textHudX = d;
		config.get("general", "textHudX", 0.0f, "Relative X-position of the main text HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public void setTextHudY(double d) {
		textHudY = d;
		config.get("general", "textHudY", 0.0f, "Relative Y-position of the main text HUD", 0.0f, 1.0f).set((float)d);
		config.save();
	}
	
	public double getTextHudX() {
		return textHudX;
	}
	
	public double getTextHudY() {
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
