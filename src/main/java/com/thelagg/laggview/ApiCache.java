package com.thelagg.laggview;

import java.util.*;

import com.thelagg.laggview.apirequests.GuildRequest;
import com.thelagg.laggview.apirequests.NameHistoryRequest;
import com.thelagg.laggview.apirequests.PlayerRequest;

import net.minecraft.client.Minecraft;

import com.thelagg.laggview.apirequests.NameToUUIDRequest;

public class ApiCache {
	public Map<UUID,PlayerRequest> playerCache = new HashMap<UUID,PlayerRequest>();
	public Map<UUID,GuildRequest> guildCache = new HashMap<UUID,GuildRequest>();
	public Map<UUID,NameHistoryRequest> nameHistoryCache = new HashMap<UUID,NameHistoryRequest>();
	public Map<String,NameToUUIDRequest> nameToUUIDCache = new HashMap<String,NameToUUIDRequest>();
	public ArrayList<ApiRequest> requestQueue = new ArrayList<ApiRequest>();
	
	public PlayerRequest getPlayerResult(String name, int priority) {
		NameToUUIDRequest uuidRequest = getNameToUUIDRequest(name,priority);
		while(uuidRequest==null) {
			uuidRequest = getNameToUUIDRequest(name,priority);
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
	
	public NameToUUIDRequest getNameToUUIDRequest(String name, int priority) {
		NameToUUIDRequest value = nameToUUIDCache.get(name);
		if(value==null) {
			NameToUUIDRequest r = new NameToUUIDRequest(name);
			r.queue(priority);
			if(priority<1) {
				while(value==null) {
					value = nameToUUIDCache.get(name);
				}
			}
		}
		return value;		
	}
	
	public void processFirstRequest() {
		if(requestQueue.get(0)==null) {
			return;
		}
		ApiRequest r = requestQueue.get(0);
		r.processRequest();
	}
	
}

