package com.thelagg.laggview.settings;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.URLConnectionReader;

import net.minecraft.client.Minecraft;

public class Settings {
	private StatInTab tabStat;
	
	public Settings(StatInTab tabStat) {
		this.tabStat = tabStat;
	}
	
	public StatInTab getStatInTab() {
		return tabStat;
	}
	
	public void setStatInTab(StatInTab s) {
		this.tabStat = s;
	}
	
	public void sendToServer() {
		try {
			URLConnectionReader.getText("http://thelagg.com/wrapper/settings/send?json=" + this.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Settings getFromServer() {
		UUID uuid = Minecraft.getMinecraft().thePlayer.getUniqueID();
		try {
			String str = URLConnectionReader.getText("http://thelagg.com/wrapper/settings/get?uuid=" + uuid);
			return Settings.fromString(str);
		} catch (IOException e) {	
			e.printStackTrace();
		}
		return null;
	}
	
	public static Settings fromString(String str) {
		JSONParser parser = new JSONParser();
		try {
			JSONObject json = (JSONObject) parser.parse(str);
			String tabValueStr = (String) json.get("tabStat");
			StatInTab tabStatNew = StatInTab.valueOf(tabValueStr);
			return new Settings(tabStatNew);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public String toString() {
		JSONObject o = new JSONObject();
		o.put("tabStat", tabStat.toString());
		return o.toJSONString();
	}
}

enum StatInTab {
	MW_FINAL_KDR,
	SW_KDR,
	BSG_KDR,
	BW_FINAL_KDR
}