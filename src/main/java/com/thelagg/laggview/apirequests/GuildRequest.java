package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.utils.URLConnectionReader;
import com.thelagg.laggview.utils.Util;

public class GuildRequest extends ApiRequest {

	private UUID uuid;
	private String name;
	
	public GuildRequest(UUID uuid, ApiCache cache) {
		this.uuid = uuid;
		this.apiCache = cache;
	}
	
	public GuildRequest(String guildName, ApiCache cache) {
		this.name = guildName;
		this.apiCache = cache;
	}
	
	@Override
	public void processRequest() {
		JSONObject json = new JSONObject();
		if(this.uuid!=null) {
			try {
				JSONParser parser = new JSONParser();
				json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/hypixel/raw/guild/player/" + this.uuid));
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
			this.result = json;
			this.apiCache.guildCache.put(uuid, this);
			this.apiCache.requestQueue.remove(this);
		} else {
			try {
				JSONParser parser = new JSONParser();
				json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/hypixel/raw/guild/" + this.name));
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
			this.result = json;
			this.apiCache.guildCache.put(name, this);
			this.apiCache.requestQueue.remove(this);
		}
	}

	@Override
	public boolean equals(ApiRequest r) {
		if(r instanceof GuildRequest) {
			GuildRequest g = (GuildRequest)r;
			if(g.name!=null && this.name!=null && this.name.equals(g.name)) {
				return true;
			} else if (g.uuid!=null && this.uuid!=null && this.uuid.equals(g.uuid)) {
				return true;
			}
		}
		return false;
	}

	public List<UUID> getUUIDs() {
		JSONArray arr = (JSONArray) this.getObjectAtPath("guild/members");
		System.out.println(arr.size());
		List<UUID> uuids = new ArrayList<UUID>();
		for(Object o : arr) {
			JSONObject jsonObj = (JSONObject)o;
			UUID uuid = Util.getUUID((String) jsonObj.get("uuid"));
			uuids.add(uuid);
		}
		return uuids;
	}

}
