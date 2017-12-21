package com.thelagg.laggview;

import org.json.simple.JSONObject;

public abstract class ApiRequest {
	public long timeQueued;
	public long timeRequested;
	public long timeReceived;
	public JSONObject result;
	protected ApiCache apiCache;
	public abstract void processRequest();
	public abstract boolean equals(ApiRequest r);
	
	public void queue(int priority) {
		if(!apiCache.isRequestAlreadyQueued(this)) {
			apiCache.requestQueue.add(this);
		}
	}
	

	protected Object getNextObject(JSONObject j, String search) {
		return j==null?null:j.get(search);
	}
	
	protected Object getObjectAtPath(String pathStr) {
		String[] path = pathStr.split("/");
		JSONObject j = this.result;
		for(String s : path) {
			Object o = getNextObject(j,s);
			if(o instanceof JSONObject) {
				j = (JSONObject) o;
			} else {
				return o;
			}
		}
		return j;
	}
}
