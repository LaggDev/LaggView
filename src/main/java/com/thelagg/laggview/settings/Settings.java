package com.thelagg.laggview.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.URLConnectionReader;

import net.minecraft.client.Minecraft;

public class Settings {
	private String toggleRecordingOnHotkey;
	private String toggleRecordingOffHotkey;
	private List<String> hackersToRecord;
	
	public Settings(String toggleRecordingOnHotkey, String toggleRecordingOffHotkey, List<String> hackersToRecord) {
		this.toggleRecordingOnHotkey = toggleRecordingOnHotkey;
		this.toggleRecordingOffHotkey = toggleRecordingOffHotkey;
		this.hackersToRecord = hackersToRecord;
	}
	
	public boolean isValid() {
		for(Field f : Settings.class.getDeclaredFields()) {
			try {
				f.setAccessible(true);
				Object value = f.get(this);
				if(value==null) {
					return false;
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return true;
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
		} catch (IOException | ParseException | JsonEmptyException e) {	
			e.printStackTrace();
			return Settings.getDefaultSettings();
		}
	}
	
	public static Settings fromString(String str) throws ParseException, JsonEmptyException {
		if(str==null || str.trim()=="") {
			throw new JsonEmptyException();
		}
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(str);
		String toggleRecordingOnHotkey = (String) json.get("toggleRecordingOnHotkey");
		String toggleRecordingOffHotkey = (String) json.get("toggleRecordingOffHotkey");
		JSONArray hackersToRecord = (JSONArray)json.get("hackersToRecord");
		Settings settings;
		try {
			settings = new Settings(toggleRecordingOnHotkey,toggleRecordingOffHotkey,(List<String>)hackersToRecord);
		} catch (ClassCastException e) {
			e.printStackTrace();
			return getDefaultSettings();
		}
		if(settings.isValid()) {
			return settings;
		} else {
			return getDefaultSettings();
		}
	}
	
	@SuppressWarnings("unchecked")
	public String toString() {
		JSONObject o = new JSONObject();
		o.put("toggleRecordingOnHotkey", toggleRecordingOnHotkey);
		o.put("toggleRecordingOffHotkey", toggleRecordingOffHotkey);
		return o.toJSONString();
	}
	
	public static Settings loadFromFile() {
		try {
		File f = new File("./laggview-settings.txt");
		if(!f.exists()) {
			f.createNewFile();
			return getDefaultSettings();
		}
		BufferedReader in = new BufferedReader(new FileReader(f));
		String line = in.readLine();
		in.close();
		return fromString(line);
		} catch (IOException | ParseException | JsonEmptyException e) {
			e.printStackTrace();
			return getDefaultSettings();
		}
	}
	
	public static Settings getDefaultSettings() {
		Settings s = new Settings("CTRL + J","CTRL + I",new ArrayList<String>());
		try {
			s.saveToFile();
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		return s;
	}
	
	public void saveToFile() throws IOException {
		File f = new File("./laggview-settings.txt");
		if(f.exists()) {
			f.delete();
		}
		f.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(f));
		out.write(this.toString());
		out.close();
	}
	
	public String getToggleRecordingOnHotkey() {
		return this.toggleRecordingOnHotkey;
	}
	
	public String getToggleRecordingOffHotkey() {
		return this.toggleRecordingOffHotkey;
	}
	
	public void setToggleRecordingOnHotkey(String s) {
		this.toggleRecordingOnHotkey = s;
	}
	
	public void setToggleRecordingOffHotkey(String s) {
		this.toggleRecordingOffHotkey = s;
	}
	
	public static class JsonEmptyException extends Exception {
		private static final long serialVersionUID = -5559138925694721532L;

		public JsonEmptyException() {
			super();
		}
	}
}
