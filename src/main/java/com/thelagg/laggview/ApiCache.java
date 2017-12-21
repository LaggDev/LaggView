package com.thelagg.laggview;

import java.util.*;

import com.thelagg.laggview.apirequests.GuildRequest;
import com.thelagg.laggview.apirequests.NameHistoryRequest;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;

import net.minecraft.client.Minecraft;

import com.thelagg.laggview.apirequests.NameToUUIDRequest;

public class ApiCache {
	public Map<UUID,PlayerRequest> playerCache;
	public Map<UUID,GuildRequest> guildCache;
	public Map<UUID,NameHistoryRequest> nameHistoryCache;
	public Map<String,NameToUUIDRequest> nameToUUIDCache;
	public Map<UUID,SessionRequest> sessionCache;
	public ArrayList<ApiRequest> requestQueue;
	
	public ApiCache() {
		playerCache = new HashMap<UUID,PlayerRequest>();
		guildCache = new HashMap<UUID,GuildRequest>();
		nameHistoryCache = new HashMap<UUID,NameHistoryRequest>();
		nameToUUIDCache = new HashMap<String,NameToUUIDRequest>();
		sessionCache = new HashMap<UUID,SessionRequest>();
		requestQueue = new ArrayList<ApiRequest>();
	}
	
	public PlayerRequest getPlayerResult(String name, int priority) {
		NameToUUIDRequest uuidRequest = getNameToUUIDRequest(name,priority);
		if(uuidRequest==null && priority>=1) {
			return null;
		}
		while(uuidRequest==null) {
			uuidRequest = getNameToUUIDRequest(name,priority);
		}
		if(uuidRequest.getUUID()==null) {
			return null;
		}
		PlayerRequest playerRequest = getPlayerResult(uuidRequest.getUUID(),priority);
		if(playerRequest==null && priority>=1) {
			return null;
		}
		while(playerRequest==null) {
			playerRequest = getPlayerResult(uuidRequest.getUUID(),priority);
		}
		return playerRequest;
	}
	
	public SessionRequest getSessionResult(UUID uuid, int priority) {
		SessionRequest value = sessionCache.get(uuid);
		if(value==null) {
			SessionRequest r = new SessionRequest(uuid,this);
			r.queue(priority);
			if(priority<1) {
				while(value==null) {
					value = sessionCache.get(uuid);
				}
			}
		}
		return value;
	}
	
	public PlayerRequest getPlayerResult(UUID uuid, int priority) {
		PlayerRequest value = playerCache.get(uuid);
		if(value==null) {
			PlayerRequest r = new PlayerRequest(uuid,this);
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
		ApiRequest[] requests = requestQueue.toArray(new ApiRequest[requestQueue.size()]);
		for(ApiRequest alreadyThere : requests) {
			if(r.equals(alreadyThere)) {
				return true;
			}
		}
		return false;
	}
	
	public NameToUUIDRequest getNameToUUIDRequest(String name, int priority) {
		NameToUUIDRequest value = nameToUUIDCache.get(name);
		if(value==null) {
			NameToUUIDRequest r = new NameToUUIDRequest(name,this);
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
		if(requestQueue.size()==0) {
			return;
		}
		ApiRequest r = requestQueue.get(0);
		r.processRequest();

	}
	
}

