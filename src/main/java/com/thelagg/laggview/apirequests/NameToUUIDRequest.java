package com.thelagg.laggview.apirequests;

import java.util.UUID;

import com.thelagg.laggview.ApiRequest;

public class NameToUUIDRequest extends ApiRequest {
	String name;
	
	public NameToUUIDRequest(String name) {
		this.name = name;
	}

	@Override
	public void processRequest() {
		
		this.apiCache.nameToUUIDCache.put(name, this);
		this.apiCache.requestQueue.remove(this);
	}

	public UUID getUUID() {		
		return null;
	}

	@Override
	public boolean equals(ApiRequest r) {
		return r instanceof NameToUUIDRequest && name==((NameToUUIDRequest)r).name;
	}

}
