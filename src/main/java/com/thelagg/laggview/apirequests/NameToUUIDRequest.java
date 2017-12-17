package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.ApiCache;
import com.thelagg.laggview.ApiRequest;
import com.thelagg.laggview.URLConnectionReader;
import com.thelagg.laggview.Util;

public class NameToUUIDRequest extends ApiRequest {
	String name;
	
	public NameToUUIDRequest(String name,ApiCache apiCache) {
		this.name = name;
		this.apiCache = apiCache;
	}

	@Override
	public void processRequest() {
		JSONObject json = new JSONObject();
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/wrapper/raw/nameToUUID/" + this.name));
		} catch (ParseException | IOException e) {
			System.out.println(this.name + " is not a real player, ignoring nameToUUIDRequest");
			json = null;
		}
		this.result = json;
		this.apiCache.nameToUUIDCache.put(name, this);
		this.apiCache.requestQueue.remove(this);
	}

	public UUID getUUID() {		
		return this.result==null?null:Util.getUUID((String)this.result.get("id"));
	}

	@Override
	public boolean equals(ApiRequest r) {
		return r instanceof NameToUUIDRequest && name.equals(((NameToUUIDRequest)r).name);
	}

}
