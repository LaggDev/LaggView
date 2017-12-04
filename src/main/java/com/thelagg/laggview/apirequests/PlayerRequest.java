package com.thelagg.laggview.apirequests;

import java.util.UUID;

import com.thelagg.laggview.ApiRequest;

public class PlayerRequest extends ApiRequest {
	public UUID uuid;
	
	public PlayerRequest(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public void processRequest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean equals(ApiRequest r) {
		return r instanceof PlayerRequest && uuid==((PlayerRequest)r).uuid;
	}
	
}
