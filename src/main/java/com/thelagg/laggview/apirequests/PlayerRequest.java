package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.ApiCache;
import com.thelagg.laggview.ApiRequest;
import com.thelagg.laggview.URLConnectionReader;

public class PlayerRequest extends ApiRequest {
	public UUID uuid;
	
	public PlayerRequest(UUID uuid, ApiCache apiCache) {
		this.uuid = uuid;
		this.apiCache = apiCache;
	}

	@Override
	public void processRequest() {
		JSONObject json = new JSONObject();
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/wrapper/raw/player/" + this.uuid));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		this.result = json;
		this.apiCache.playerCache.put(uuid, this);
		this.apiCache.requestQueue.remove(this);
	}
	
	public double getFinalKDR() {
		try {
			Long final_kills = (Long) getObjectAtPath("player/stats/Walls3/final_kills");
			Long finalDeaths = (Long)getObjectAtPath("player/stats/Walls3/finalDeaths");
			Long final_deaths = (Long)getObjectAtPath("player/stats/Walls3/final_deaths");
			double finalKillsTotal = final_kills==null?0:final_kills;
			double finalDeathsTotal = (finalDeaths==null?0:finalDeaths) + (final_deaths==null?0:final_deaths); 
			return finalKillsTotal/finalDeathsTotal;
		} catch (NullPointerException e) {
			return 0;
		}
	}
	
	public String getNickname() {
		if((String)getObjectAtPath("player/lastNick")==null) {
			System.out.println("null " + this.getName());
		} else {
			System.out.println((String)getObjectAtPath("player/lastNick") + " " + this.getName());
		}
		return (String)getObjectAtPath("player/lastNick");
	}
	
	@Override
	public boolean equals(ApiRequest r) {
		return ((r instanceof PlayerRequest) && (this.uuid.equals(((PlayerRequest)r).uuid)));
	}

	public String getName() {
		return (String)getObjectAtPath("player/displayname");
	}
	
}
