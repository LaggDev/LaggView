package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.ApiCache;
import com.thelagg.laggview.ApiRequest;
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
			json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/wrapper/raw/nameHistory/" + this.uuid));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		this.result = json;
		this.apiCache.nameHistoryCache.put(uuid, this);
		this.apiCache.requestQueue.remove(this);
	}
	
	public void print() {
		System.out.println(this.result.toJSONString());
	}

	@Override
	public boolean equals(ApiRequest r) {
		return r instanceof NameHistoryRequest && ((NameHistoryRequest)r).uuid.equals(this.uuid);
	}
}
