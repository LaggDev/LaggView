package com.thelagg.laggview;

import java.util.*;

import com.thelagg.laggview.apirequests.PlayerRequest;

public class ApiCache {
	Map<UUID,String> playerCache = new HashMap<UUID,String>();
	Map<UUID,String> guildCache = new HashMap<UUID,String>();
	Map<UUID,String> nameHistoryCache = new HashMap<UUID,String>();
	
	ArrayList<ApiRequest> requestQueue = new ArrayList<ApiRequest>();
	
	public String getPlayerResult(String name, int priority) {
		String value = playerCache.get(name);
		if(value==null) {
			PlayerRequest r = new PlayerRequest(name);
			r.queue(priority);
			if(priority<1) {
				while(value==null) {
					value = playerCache.get(name);
				}
			}
		}
		return value;
	}
	
	public String getPlayerResult(UUID uuid, int priority) {
		return "";
	}
	
}

