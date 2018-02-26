package com.thelagg.laggview.apirequests;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.thelagg.laggview.utils.URLConnectionReader;

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

	public String getNetworkLevelStr() {
		return format(getNetworkLevel());
	}
	
	public double getNetworkLevel() {
		try {
			Long experience = (Long) this.getObjectAtPath("player/networkExp");
			return getExactLevel(experience);
		} catch (ClassCastException | NullPointerException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
    private static double getLevel(double exp) {
        double BASE = 10000.0;
        double GROWTH = 2500.0;
        double HALF_GROWTH = 0.5 * GROWTH;
        double REVERSE_PQ_PREFIX = -(BASE - 0.5 * GROWTH) / GROWTH;
        double REVERSE_CONST = REVERSE_PQ_PREFIX * REVERSE_PQ_PREFIX;
        double GROWTH_DIVIDES_2 = 2 / GROWTH;
        return exp<0 ? 1 : Math.floor(1 + REVERSE_PQ_PREFIX + Math.sqrt(REVERSE_CONST + GROWTH_DIVIDES_2 * exp));
    }

    private static double getExactLevel(double exp) {
            return getLevel(exp) + getPercentageToNextLevel(exp);
    }

    private static double getExpFromLevelToNext(double level) {
        double BASE = 10000.0;
        double GROWTH = 2500.0;
        return level < 1.0 ? BASE : GROWTH * (level - 1.0) + BASE;
    }

    private static double getTotalExpToLevel(double level) {
        double lv = Math.floor(level);
        double x0 = getTotalExpToFullLevel(lv);
        if (level == lv) return x0;
        return (getTotalExpToFullLevel(lv + 1.0) - x0) * (level % 1.0) + x0;
    }

    private static double getTotalExpToFullLevel(double level) {
        double BASE = 10000.0;
        double GROWTH = 2500.0;
        double HALF_GROWTH = 0.5 * GROWTH;
        return (HALF_GROWTH * (level - 2.0) + BASE) * (level - 1.0);
    }

    private static double getPercentageToNextLevel(double exp) {
        double lv = getLevel(exp);
        double x0 = getTotalExpToLevel(lv);
        return (exp - x0) / (getTotalExpToLevel(lv + 1.0) - x0);
    }
	
	public String getVampireZKDRStr() {
		return format(this.getVampireZKDR());
	}
	
	public double getVampireZKDR() {
		return this.getKDRorWLR("player/stats/VampireZ/human_kills", "player/stats/VampireZ/human_deaths");
	}
	
	public String getWallsKDRStr() {
		return format(this.getWallsKDR());
	}
	
	public double getWallsKDR() {
		return this.getKDRorWLR("player/stats/Walls/kills", "player/stats/Walls/deaths");
	}
	
	public String getPaintballKDRStr() {
		return format(this.getPaintballKDR());
	}
	
	public double getPaintballKDR() {
		return this.getKDRorWLR("player/stats/Paintball/kills", "player/stats/Paintball/deaths");
	}
	
	public String getQuakeCraftKDRStr() {
		return format(this.getQuakeCraftKDR());
	}
	
	public double getQuakeCraftKDR() {
		return this.getKDRorWLR("player/stats/Quake/kills", "player/stats/Quake/deaths");
	}
	
	public String getCrazyWallsKDRStr() {
		return format(this.getCrazyWallsKDR());
	}
	
	public double getCrazyWallsKDR() {
		return this.getKDRorWLR("player/stats/TrueCombat/kills", "player/stats/TrueCombat/deaths");
	}
	
	public String getSmashHeroesKDRStr() {
		return format(this.getSmashHeroesKDR());
	}
	
	public double getSmashHeroesKDR() {
		return this.getKDRorWLR("player/stats/SuperSmash/kills", "player/stats/SuperSmash/deaths");
	}
	
	public String getSkyClashKDRStr() {
		return this.format(this.getSkyClashKDR());
	}
	
	public double getSkyClashKDR() {
		return this.getKDRorWLR("player/stats/SkyClash/kills", "player/stats/SkyClash/deaths");
	}
	
	public String getWarlordsWLRStr() {
		return this.format(getWarlordsWLR());
	}
	
	public double getWarlordsWLR() {
		return this.getKDRorWLR("player/stats/Battleground/wins", "player/stats/Battleground/losses");
	}
	
	public String getTNTRunWinsStr() {
		return noDecimal(this.getTNTRunWins());
	}
	
	public double getTNTRunWins() {
		return this.getSingleStat("player/stats/TNTGames/wins_tntrun");
	}
	
	public String getBowSpleefWinsStr() {
		return noDecimal(this.getBowSpleefWins());
	}
	
	public double getBowSpleefWins() {
		return this.getSingleStat("player/stats/TNTGames/wins_bowspleef");
	}
	
	public String getPVPRunWinsStr() {
		return noDecimal(this.getPVPRunWins());
	}
	
	public double getPVPRunWins() {
		return this.getSingleStat("player/stats/TNTGames/wins_pvprun");
	}
	
	public String getPVPRunWLRStr() {
		return this.format(this.getPVPRunWLR());
	}
	
	public double getPVPRunWLR() {
		return this.getKDRorWLR("player/stats/TNTGames/wins_pvprun", "player/stats/TNTGames/deaths_pvprun");
	}
	
	public String getTNTTagWinsStr() {
		return this.noDecimal(this.getTNTTagWins());
	}
	
	public double getTNTTagWins() {
		return this.getSingleStat("player/stats/TNTGames/wins_tntag");
	}
	
	public String getWizardsKDRStr() {
		return format(this.getWizardsKDR());
	}
	
	public double getWizardsKDR() {
		return this.getKDRorWLR("player/stats/TNTGames/kills_capture", "player/stats/TNTGames/deaths_capture");
	}
	
	public String getBowSpleefWLRStr() {
		return format(this.getBowSpleefWLR());
	}
	
	public double getBowSpleefWLR() {
		return this.getKDRorWLR("player/stats/TNTGames/wins_bowspleef", "player/stats/TNTGames/deaths_bowspleef");
	}
	
	public double getKDRorWLR(String topValue, String bottomValue) {
		try {
			Long kills = (Long) getObjectAtPath(topValue);
			Long deaths = (Long)getObjectAtPath(bottomValue);
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
	
	public double getSingleStat(String name) {
		try {
			Long kills = (Long) getObjectAtPath(name);
			return kills;
		} catch (NullPointerException | ClassCastException e) {
			return 0;
		}
	}
	
	public String getMWFinalKDRStr() {
		return format(this.getMWFinalKDR());
	}
	
	public double getBsgKDR() {
		return this.getKDRorWLR("player/stats/HungerGames/kills", "player/stats/HungerGames/deaths");
	}
	
	public String format(double d) {
		DecimalFormat df = new DecimalFormat("0.0");
		return EnumChatFormatting.GOLD + df.format(d);
	}
	
	public String noDecimal(double d) {
		try {
			return EnumChatFormatting.GOLD + Integer.toString((int)d);
		} catch (ClassCastException e) {
			return EnumChatFormatting.GOLD + "0";
		}
	}
	
	public String getUHCKDRStr() {
		return format(this.getUHCKDR());
	}
	
	public String getTntRunWLRStr() {
		return format(this.getTntRunWLR());
	}
	
	public double getTntRunWLR() {
		return this.getKDRorWLR("player/stats/TNTGames/wins_tntrun", "player/stats/TNTGames/deaths_tntrun");
	}
	
	public double getUHCKDR() {
		try {
			Long kills = (Long) getObjectAtPath("player/stats/UHC/kills");
			Long killsSolo = (Long) getObjectAtPath("player/stats/UHC/kills_solo");
			Long deaths = (Long)getObjectAtPath("player/stats/HungerGames/deaths");
			Long deathsSolo = (Long) getObjectAtPath("player/stats/UHC/deaths_solo");
			double killsTotal = kills + killsSolo;
			double deathsTotal = deaths + deathsSolo;
			if(deathsTotal==0) {
				return 0;
			}
			return killsTotal/deathsTotal;
		} catch (NullPointerException | ClassCastException e) {
			return 0;
		}
	}
	
	public String getBsgKDRStr() {
		return format(this.getBsgKDR());
	}
	
	public double getMWFinalKDR() {
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

	public double getBedwarsFinalKDR() {
		return this.getKDRorWLR("player/stats/Bedwars/final_kills_bedwars", "player/stats/Bedwars/losses_bedwars");
	}
	
	public String getBedwarsFinalKDRStr() {
		return format(this.getBedwarsFinalKDR());
	}
	
	public double getSkywarsKDR() {
		return this.getKDRorWLR("player/stats/SkyWars/kills", "player/stats/SkyWars/deaths");
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
		return format(this.getBedwarsLevel());
	}
	
	public String getSkywarsKDRStr() {
		return format(this.getSkywarsKDR());
	}
	
    public static double getBedwarsLevel(double experience) {
        // first few levels are different
        if (experience < 500.0) {
            return 0;
        } else if (experience < 1500.0) {
            return 1;
        } else if (experience < 3500.0) {
            return 2;
        } else if (experience < 5500.0) {
            return 3;
        } else if (experience < 9000.0) {
            return 4;
        }
        experience -= 9000.0;
        return experience / 5000.0 + 4.0;
    }
	
}
