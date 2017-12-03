package com.thelagg.laggview;

public abstract class ApiRequest {
	public long timeQueued;
	public long timeRequested;
	public long timeReceived;
	public String result;
	ApiCache apiCache;
	public abstract void processRequest();
	public abstract boolean equals(ApiRequest r);
	
	public void queue(int priority) {
		apiCache.requestQueue.add(this);
	}
}
