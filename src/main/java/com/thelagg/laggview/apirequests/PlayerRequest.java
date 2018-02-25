package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.URLConnectionReader;

import net.minecraft.util.EnumChatFormatting;

public class PlayerRequest extends ApiRequest {
	public UUID uuid;
	
	public PlayerRequest(UUID uuid, ApiCache apiCache) {
		this.uuid = uuid;
		this.apiCache = apiCache;
	}

	@Override
	public void processRequest() {
		JSONObject json = new JSONObject();
		try {
			JSONParser parser = new JSONParser();
			json = (JSONObject) parser.parse(URLConnectionReader.getText("http://thelagg.com/hypixel/raw/player/" + this.uuid));
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		this.result = json;
		this.apiCache.playerCache.put(uuid, this);
		this.apiCache.requestQueue.remove(this);
	}
	
	public String getFinalKDRString() {
		double finalKdr = this.getFinalKDR();
		DecimalFormat df = new DecimalFormat("0.0");
		return EnumChatFormatting.GOLD + df.format(finalKdr);
	}
	
	public double getFinalKDR() {
		try {
			Long final_kills = (Long) getObjectAtPath("player/stats/Walls3/final_kills");
			Long finalKills = (Long) getObjectAtPath("player/stats/Walls3/finalKills");
			//Long finalDeaths = (Long)getObjectAtPath("player/stats/Walls3/finalDeaths");
			Long final_deaths = (Long)getObjectAtPath("player/stats/Walls3/final_deaths");
			double finalKillsTotal = (final_kills==null?0:final_kills) - (finalKills==null?0:finalKills);
			double finalDeathsTotal = (final_deaths==null?0:final_deaths);
			if(finalDeathsTotal==0) {
				return 0;
			}
			return finalKillsTotal/finalDeathsTotal;
		} catch (NullPointerException | ClassCastException e) {
			return 0;
		}
	}
	
	public String getNickname() {
		return (String)getObjectAtPath("player/lastNick");
	}
	
	@Override
	public boolean equals(ApiRequest r) {
		return ((r instanceof PlayerRequest) && (this.uuid.equals(((PlayerRequest)r).uuid)));
	}

	public String getName() {
		return (String)getObjectAtPath("player/displayname");
	}

	public double getSkywarsKDR() {
		try {
			Long kills = (Long) getObjectAtPath("player/stats/SkyWars/kills");
			Long deaths = (Long)getObjectAtPath("player/stats/SkyWars/deaths");
			double killsTotal = kills;
			double deathsTotal = deaths;
			if(deathsTotal==0) {
				return 0;
			}
			return killsTotal/deathsTotal;
		} catch (NullPointerException | ClassCastException e) {
			return 0;
		}
	}
	
	public double getBedwarsLevel() {
		try {
			Long experience = (Long) getObjectAtPath("player/stats/Bedwars/Experience");
			return getBedwarsLevel(experience);
		} catch (NullPointerException | ClassCastException e) {
			return 0;
		}
	}
	
	public String getBedwarsLevelStr() {
		double level = this.getBedwarsLevel();
		DecimalFormat df = new DecimalFormat("0.0");
		return EnumChatFormatting.GOLD + df.format(level);
	}
	
	public String getSkywarsKDRStr() {
		double kdr = this.getSkywarsKDR();
		DecimalFormat df = new DecimalFormat("0.0");
		return EnumChatFormatting.GOLD + df.format(kdr);
	}
	
    public static double getBedwarsLevel(long experience) {
        // first few levels are different
        if (experience < 500) {
            return 0;
        } else if (experience < 1500) {
            return 1;
        } else if (experience < 3500) {
            return 2;
        } else if (experience < 5500) {
            return 3;
        } else if (experience < 9000) {
            return 4;
        }
        experience -= 9000;
        return experience / 5000 + 4;
    }
	
}
