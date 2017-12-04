package com.thelagg.laggview;

public abstract class ApiRequest {
	public long timeQueued;
	public long timeRequested;
	public long timeReceived;
	public String result;
	protected ApiCache apiCache;
	public abstract void processRequest();
	public abstract boolean equals(ApiRequest r);
	
	public void queue(int priority) {
		if(apiCache.isRequestAlreadyQueued(this)) {
			apiCache.requestQueue.add(this);
		}
	}
}
