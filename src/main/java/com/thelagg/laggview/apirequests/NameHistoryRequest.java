package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.URLConnectionReader;
import com.thelagg.laggview.Util;

public class NameHistoryRequest extends ApiRequest {

	private UUID uuid;
	
	public NameHistoryRequest(UUID uuid, ApiCache apiCache) {
		this.uuid = uuid;
		this.apiCache = apiCache;
	}

	@Override
	public void processRequest() {
		JSONObject json = new JSONObject();
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/hypixel/raw/nameHistory/" + this.uuid));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		this.result = json;
		this.apiCache.nameHistoryCache.put(uuid, this);
		this.apiCache.requestQueue.remove(this);
	}
	
	public void print() {
		int i = 0;
		while(result.containsKey(Integer.toString(i))) {
			i++;
		}
		i--;
		for(int j = i; j>=0; j--) {
			String name = (String) this.getObjectAtPath(Integer.toString(j) + "/name");
			Object timeObj = this.getObjectAtPath(Integer.toString(j) + "/changedToAt");
			String date = "";
			if(timeObj!=null) {
				long time = (Long) timeObj;
				date = new SimpleDateFormat("MM/dd/yyyy").format(new Date(time));
			}
			Util.print(ChatFormatting.GOLD + name + " " + ChatFormatting.LIGHT_PURPLE + date);
		}
	}

	@Override
	public boolean equals(ApiRequest r) {
		return r instanceof NameHistoryRequest && ((NameHistoryRequest)r).uuid.equals(this.uuid);
	}
}
