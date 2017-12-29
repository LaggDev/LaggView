package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.ApiCache;
import com.thelagg.laggview.ApiRequest;
import com.thelagg.laggview.URLConnectionReader;
import com.thelagg.laggview.Util;

public class SessionRequest extends ApiRequest {

	public UUID uuid;
	
	public SessionRequest(UUID uuid, ApiCache apiCache) {
		this.apiCache = apiCache;
		this.uuid = uuid;
	}

	@Override
	public void processRequest() {
		JSONObject json = new JSONObject();
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/wrapper/raw/session/" + this.uuid));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		this.result = json;
		this.apiCache.sessionCache.put(uuid, this);
		this.apiCache.requestQueue.remove(this);
	}
	
	public UUID[] getUUIDs() {
		try {
			JSONArray arr = (JSONArray) this.getObjectAtPath("session/players");
			if(arr==null) return null;
			ArrayList<UUID> uuids = new ArrayList<UUID>();
			for(int i = 0; i<arr.size(); i++) {
				uuids.add(Util.getUUID((String)(arr.get(i))));
			}
			return uuids.toArray(new UUID[arr.size()]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public long getTime() {
		try {
			return (Long)this.getObjectAtPath("timeRequested");
		} catch (Exception e) {
			return System.currentTimeMillis();
		}
	}
	
	public PlayerRequest findByNick(String nick) {
		if(this.getUUIDs()==null) {
			return null;
		}
		for(UUID uuid : this.getUUIDs()) {
			PlayerRequest r = this.apiCache.getPlayerResult(uuid, 1);
			if(r!=null && r.getNickname()!=null && r.getNickname().equals(nick)) {
				return r;
			}
		}
		return null;
	}
	
	@Override
	public boolean equals(ApiRequest r) {
		return r instanceof SessionRequest && ((SessionRequest)(r)).uuid.equals(this.uuid);
	}

}
