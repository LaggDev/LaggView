package com.thelagg.laggview;

import java.util.*;

import com.thelagg.laggview.apirequests.GuildRequest;
import com.thelagg.laggview.apirequests.NameHistoryRequest;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.NameToUUIDRequest;

public class ApiCache {
	Map<UUID,PlayerRequest> playerCache = new HashMap<UUID,PlayerRequest>();
	Map<UUID,GuildRequest> guildCache = new HashMap<UUID,GuildRequest>();
	Map<UUID,NameHistoryRequest> nameHistoryCache = new HashMap<UUID,NameHistoryRequest>();
	Map<UUID,NameToUUIDRequest> nameToUUIDCache = new HashMap<UUID,NameToUUIDRequest>();
	ArrayList<ApiRequest> requestQueue = new ArrayList<ApiRequest>();
	
	public PlayerRequest getPlayerResult(String name, int priority) {
		NameToUUIDRequest uuidRequest = getUuidToNameRequest(name,priority);
		while(uuidRequest==null) {
			uuidRequest = getUuidToNameRequest(name,priority);
		}
		PlayerRequest playerRequest = getPlayerResult(uuidRequest.getUUID(),priority);
		while(playerRequest==null) {
			playerRequest = getPlayerResult(uuidRequest.getUUID(),priority);
		}
		return playerRequest;
	}
	
	public PlayerRequest getPlayerResult(UUID uuid, int priority) {
		PlayerRequest value = playerCache.get(uuid);
		if(value==null) {
			PlayerRequest r = new PlayerRequest(uuid);
			r.queue(priority);
			if(priority<1) {
				while(value==null) {
					value = playerCache.get(uuid);
				}
			}
		}
		return value;
	}
	
	public boolean isRequestAlreadyQueued(ApiRequest r) {
		for(ApiRequest alreadyThere : requestQueue) {
			if(r.equals(alreadyThere)) {
				return true;
			}
		}
		return false;
	}
	
	public NameToUUIDRequest getUuidToNameRequest(String name, int priority) {
		NameToUUIDRequest value = nameToUUIDCache.get(name);
		if(value==null) {
			NameToUUIDRequest r = new NameToUUIDRequest(name);
			if(!isRequestAlreadyQueued(r)) {
				r.queue(priority);
			}
			if(priority<1) {
				while(value==null) {
					value = nameToUUIDCache.get(name);
				}
			}
		}
		return value;		
	}
	
}

