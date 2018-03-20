package com.thelagg.laggview.quests;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.orangemarshall.hudproperty.test.DelayedTask;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.modules.QuestTracker;

public class Quest {
	private boolean active;
	private String questName;
	private Map<String,Object[]> stats;
	
	public Quest(String questName, Map<String,Object[]> stats) {
		this.questName = questName;
		this.stats = stats;
		new DelayedTask(() -> LaggView.getInstance().questTracker.update(),1);
	}
	
	public Quest(String questName, String name, int required) {
		this(questName,null);
		Map<String,Object[]> map = new HashMap<String,Object[]>();
		map.put("only_stat", new Object[] {name,0,required});
		this.stats = map;
	}
	
	public Quest(String questName, String stat1, String name1, int required1, String stat2, String name2, int required2) {
		this(questName,null);
		stats = new HashMap<String,Object[]>();
		stats.put(stat1, new Object[] {name1,0,required1});
		stats.put(stat2, new Object[] {name2,0,required2});
	}
	
	public Quest(String questName, String stat1, String name1, int required1, String stat2, String name2, int required2, String stat3, String name3, int required3) {
		this(questName,stat1,name1,required1,stat2,name2,required2);
		stats.put(stat3, new Object[] {name3,0,required3});
	}
	
	public String getName() {
		return this.questName;
	}
	
	public void increaseValue(int amount) {
		Object[] statsPair = stats.get(stats.keySet().toArray()[0]);
		statsPair[1]= (int)statsPair[1] + amount;
		if((int)statsPair[1]>(int)statsPair[2]) {
			statsPair[1] = statsPair[2];
		}
	}
	
	public void increaseValue(String stat, int amount) {
		Object[] statsPair = stats.get(stat);
		statsPair[1]= (int)statsPair[1] + amount;
		if((int)statsPair[1]>(int)statsPair[2]) {
			statsPair[1] = statsPair[2];
		}
	}
	
	public String formatQuestName(String s) {
		s = s.replaceAll("_", " ");
		for(int i = 0; i<s.length(); i++) {
			if(i==0) {
				s = s.substring(0,1).toUpperCase() + s.substring(1,s.length());
			} else {
				if(s.charAt(i-1)==' ') {
					s = s.substring(0,i) + s.substring(i,i+1).toUpperCase() + s.substring(i+1,s.length());
				}
			}
		}
		return s;
	}
	
	private boolean allDone() {
		boolean done = true;
		for(String s : stats.keySet()) {
			if((int)stats.get(s)[1]<(int)stats.get(s)[2]) {
				done = false;
			}
		}
		return done;
	}
	
	@Override
	public String toString() {
		String s = "";
		s += (allDone()?ChatFormatting.GREEN:ChatFormatting.RED) + (stats.keySet().size()==1?(String)stats.get(stats.keySet().toArray()[0])[0]:formatQuestName(questName));
		if(stats.keySet().size()==1) {
			s += ": " + ChatFormatting.BLUE + Integer.toString((int)stats.get(stats.keySet().toArray()[0])[1]) + "/" + Integer.toString((int)stats.get(stats.keySet().toArray()[0])[2]); 
		} else {
			for(String key : stats.keySet()) {
				s += "\n";
				Object[] pair = stats.get(key);
				s += "  " + ((int)pair[1]<(int)pair[2]?ChatFormatting.RED:ChatFormatting.GREEN) + (String)stats.get(key)[0] + ": " + ChatFormatting.BLUE + pair[1] + "/" + pair[2];
			}
		}
		return s;
	}
	
	public boolean isWeekly() {
		return this.questName.toLowerCase().contains("weekly");
	}
	
	public void update(PlayerRequest p) {
		active = p.getObjectAtPath("player/quests/" + questName + "/active")!=null;
		if(active) {
			try {
				Object ogobj = p.getObjectAtPath("player/quests/" + questName + "/active/objectives");
				if(ogobj instanceof JSONArray) {
					for(String key : stats.keySet()) {
						Object[] pair = stats.get(key);
						pair[1] = 0;
					}
				} else {
					JSONObject obj = (JSONObject)ogobj;
					for(Object keyObj : obj.keySet()) {
						String key = (String)keyObj;
						int amount = (int)(long)(Long) obj.get(key);
						if(stats.keySet().size()==1) {
							Object[] pair = stats.get(stats.keySet().toArray()[0]);
							pair[1] = amount;
						} else {
							Object[] pair = stats.get(key);
							pair[1] = amount;
						}
					}	
				}
			} catch (Exception e) {
				e.printStackTrace();
				for(String key : stats.keySet()) {
					Object[] pair = stats.get(key);
					pair[1] = 0;
				}
			}
		} else {
			Object arrtmp = p.getObjectAtPath("player/quests/" + questName + "/completions");
			if(arrtmp!=null) {
				try {
					JSONArray arr = (JSONArray)arrtmp;
					JSONObject o = (JSONObject)arr.get(arr.size()-1);
					Long lastFinished = (Long)o.get("time");
					if(this.isWeekly()) {
						if(lastFinished<getLastFriday().getMillis()) {
							for(String key : stats.keySet()) {
								Object[] pair = stats.get(key);
								pair[1] = 0;
							}
						} else {
							for(String key : stats.keySet()) {
								Object[] pair = stats.get(key);
								pair[1] = pair[2];
							}
						}
					} else {
						if(lastFinished<DateTime.now().withTimeAtStartOfDay().getMillis()) {
							for(String key : stats.keySet()) {
								Object[] pair = stats.get(key);
								pair[1] = 0;
							}
						} else {
							for(String key : stats.keySet()) {
								Object[] pair = stats.get(key);
								pair[1] = pair[2];
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					for(String key : stats.keySet()) {
						Object[] pair = stats.get(key);
						pair[1] = 0;
					}
				}
			} else {
				for(String key : stats.keySet()) {
					Object[] pair = stats.get(key);
					pair[1] = 0;
				}
			}
		}
	}
	
	private DateTime getLastFriday() {
		DateTime today = DateTime.now();
		DateTime sameDayLastWeek = today.minusWeeks(1);
		DateTime thisFriday = today.withDayOfWeek(DateTimeConstants.FRIDAY).withTimeAtStartOfDay();
		if(today.isBefore(thisFriday)) {
			return sameDayLastWeek.withDayOfWeek(DateTimeConstants.FRIDAY).withTimeAtStartOfDay();
		}
		return thisFriday;
	}
}
