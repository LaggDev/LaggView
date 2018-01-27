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

import org.apache.logging.log4j.Level;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.URLConnectionReader;

import net.minecraft.client.Minecraft;

public class Settings {
	private String toggleRecordingOnHotkey;
	private String toggleRecordingOffHotkey;
	private List<String> hackersToRecord;
	private double textHudX;
	private double textHudY;
	private boolean toggleRecording;
	
	public Settings(String toggleRecordingOnHotkey, String toggleRecordingOffHotkey, List<String> hackersToRecord, double textHudX, double textHudY,
			boolean toggleRecording) {
		this.toggleRecordingOnHotkey = toggleRecordingOnHotkey;
		this.toggleRecordingOffHotkey = toggleRecordingOffHotkey;
		this.hackersToRecord = hackersToRecord;
		this.textHudX = textHudX;
		this.textHudY = textHudY;
		this.toggleRecording = toggleRecording;
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
	
	public static Settings fromString(String str) {
		try {
			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(str);
			String toggleRecordingOnHotkey = (String) json.get("toggleRecordingOnHotkey");
			String toggleRecordingOffHotkey = (String) json.get("toggleRecordingOffHotkey");
			JSONArray hackersToRecord = (JSONArray)json.get("hackersToRecord");
			double textHudX = (Double)json.get("textHudX");
			double textHudY = (Double)json.get("textHudY");
			boolean toggleRecording = (Boolean)json.get("toggleRecording");
			Settings settings = new Settings(toggleRecordingOnHotkey,toggleRecordingOffHotkey,(List<String>)hackersToRecord,textHudX,textHudY,toggleRecording);
			if(settings.isValid()) {
				return settings;
			} else {
				return getDefaultSettings();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return getDefaultSettings();
		}

	}
	
	@SuppressWarnings("unchecked")
	public String toString() {
		JSONObject o = new JSONObject();
		o.put("toggleRecordingOnHotkey", toggleRecordingOnHotkey);
		o.put("toggleRecordingOffHotkey", toggleRecordingOffHotkey);
		o.put("hackersToRecord", hackersToRecord);
		o.put("textHudX", textHudX);
		o.put("textHudY", textHudY);
		o.put("toggleRecording", toggleRecording);
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
		} catch (IOException e) {
			e.printStackTrace();
			return getDefaultSettings();
		}
	}
	
	public static Settings getDefaultSettings() {
		Settings s = new Settings("CTRL + J","CTRL + I",new ArrayList<String>(),0,0,true);
		s.saveToFile();
		return s;
	}
	
	public void saveToFile() {
		try {
			File f = new File("./laggview-settings.txt");
			if(f.exists()) {
				f.delete();
			}
			f.createNewFile();
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			out.write(this.toString());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			LaggView.getInstance().logger.log(Level.ERROR, "Error saving laggview settings");
		}
	}
	
	public String getToggleRecordingOnHotkey() {
		return this.toggleRecordingOnHotkey;
	}
	
	public String getToggleRecordingOffHotkey() {
		return this.toggleRecordingOffHotkey;
	}
	
	public void setToggleRecordingOnHotkey(String s) {
		this.toggleRecordingOnHotkey = s;
		this.saveToFile();
	}
	
	public void setToggleRecordingOffHotkey(String s) {
		this.toggleRecordingOffHotkey = s;
		this.saveToFile();
	}

	public void setTextHudX(double value) {
		this.textHudX = value;
		this.saveToFile();
	}
	
	public void setTextHudY(double value) {
		this.textHudY = value;
		this.saveToFile();
	}
	
	public double getTextHudX() {
		return this.textHudX;
	}
	
	public double getTextHudY() {
		return this.textHudY;
	}
	
	public void setHackerList(List<String> list) {
		this.hackersToRecord = list;
		this.saveToFile();
	}
	
	public List<String> getHackerList() {
		return this.hackersToRecord;
	}
	
	public void setToggleRecording(boolean b) {
		this.toggleRecording = b;
		this.saveToFile();
	}
	
	public boolean getToggleRecording() {
		return this.toggleRecording;
	}
	
}
