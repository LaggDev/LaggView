package com.thelagg.laggview.apirequests;

import java.util.UUID;

import com.thelagg.laggview.ApiRequest;

public class PlayerRequest extends ApiRequest {
	public String name;
	public UUID uuid;
	
	public PlayerRequest(String name) {
		this.name = name;
	}
	
	public PlayerRequest(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public void processRequest() {
		// TODO Auto-generated method stub
		
	}
	
}
