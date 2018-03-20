package com.thelagg.laggview.games;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.orangemarshall.hudproperty.HudPropertyApi;
import com.orangemarshall.hudproperty.IRenderer;
import com.orangemarshall.hudproperty.test.DelayedTask;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;
import com.thelagg.laggview.apirequests.StatGetter;
import com.thelagg.laggview.apirequests.StringReplacer;
import com.thelagg.laggview.hud.TabOverlay;
import com.thelagg.laggview.hud.MainHud.HudText;
import com.thelagg.laggview.hud.MainHud.Priority;
import com.thelagg.laggview.quests.Quest;
import com.thelagg.laggview.utils.URLConnectionReader;
import com.thelagg.laggview.utils.Util;

import akka.event.EventBus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class Game {

	private ArrayList<String> playerNames;
	private ArrayList<UUID> playerUUIDs;
	private int coins;
	private ArrayList<ArrayList<String>> parties;
	protected List<ChatMessage> chatMessages;
	private GameType gameType;
	private String serverId;
	private long timeJoined;
	private Object[] lastPartyMessage;
	protected Minecraft mc;
	protected ArrayList<HudText> hudText;
	protected LaggView laggView;
	protected List<UUID> playersToReveal;
	protected boolean showRealNames = true;
	private NetworkPlayerInfo[] lastTickPlayers;
	private ArrayList<Object[]> joins = new ArrayList<Object[]>();
	private ArrayList<Object[]> leaves = new ArrayList<Object[]>();
	protected Quest[] quests;
	
	public Game(GameType type, String serverId, Minecraft mc, LaggView laggView) {
		this.laggView = laggView;
		this.gameType = type;
		this.serverId = serverId;
		this.mc = mc;
		timeJoined = System.currentTimeMillis();
		playerUUIDs = new ArrayList<UUID>();
		playerNames = new ArrayList<String>();
		chatMessages = new ArrayList<ChatMessage>();
		parties = new ArrayList<ArrayList<String>>();
		coins = 0;
		lastPartyMessage = new Object[] {0,"test"};
		hudText = new ArrayList<HudText>();
		updateHudText(new HudText(Priority.COINS,ChatFormatting.GOLD + "Coins: " + coins));
		enter();
		quests = new Quest[0];
	}
	
	public Quest[] getQuests() {
		return quests;
	}
	
	protected Quest getQuest(String name) {
		for(Quest q : quests) {
			if(q.getName().equals(name)) {
				return q;
			}
		}
		return null;
	}
	
	private void updatePlayersToReveal() {
		try {
			if(mc.ingameGUI.getTabList() instanceof TabOverlay) {
				String playersToRevealStr = URLConnectionReader.getText("http://thelagg.com/hypixel/playersToReveal").trim();
				playersToReveal = new ArrayList<UUID>();
				for(String s : playersToRevealStr.split("\\s+")) {
					try {
						this.playersToReveal.add(UUID.fromString(s));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			updatePlayersToReveal();
		}
	}
	
	public ArrayList<ArrayList<String>> getParties() {
		return this.parties;
	}
	
	public GameType getType() {
		return this.gameType;
	}
	
	public String getServerId() {
		return this.serverId;
	}
	
	@Override
	public String toString() {
		return "type:" + gameType.name() + " serverId:" + serverId + " joined:" + timeJoined + " messages:" + chatMessages.size(); 
	}
	
	public void exit() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}
	
	public boolean shouldDelete() {
		return System.currentTimeMillis()-this.timeJoined>1000*60*80;
	}
	
	public void setOld() {
		this.serverId = this.serverId + " old";
	}
	
	public void enter() {
		MinecraftForge.EVENT_BUS.register(this);
		new Thread() {
			public void run() {
				updatePlayersToReveal();
			}
		}.start();
		new DelayedTask(()->laggView.questTracker.updatePlayer(),1);
	}
	
	public void processJoinLeave(NetworkPlayerInfo[] players) {
		if(lastTickPlayers!=null) {
			for(NetworkPlayerInfo p : players) {
				boolean found = false;
				for(NetworkPlayerInfo o : lastTickPlayers) {
					if(p.getGameProfile().getName().equals(o.getGameProfile().getName())) {
						found = true;
						break;
					}
				}
				if(!found) {
					Object[] arr = new Object[] {p, System.currentTimeMillis()};
					joins.add(arr);
				}
			}
			for(NetworkPlayerInfo o : lastTickPlayers) {
				boolean found = false;
				for(NetworkPlayerInfo p : players) {
					if(p.getGameProfile().getName().equals(o.getGameProfile().getName())) {
						found = true;
						break;
					}
				}
				if(!found) {
					Object[] arr = new Object[] {o, System.currentTimeMillis()};
					leaves.add(arr);
				}
			}
		}
		lastTickPlayers = players;
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(Minecraft.getMinecraft().ingameGUI.getTabList() instanceof TabOverlay) {
			TabOverlay tab = (TabOverlay)Minecraft.getMinecraft().ingameGUI.getTabList();
			NetworkPlayerInfo[] players = tab.getCurrentlyDisplayedPlayers();
			processJoinLeave(players);
			for(NetworkPlayerInfo player : players) {
				if(!laggView.apiCache.playerCache.containsKey(player.getGameProfile().getId())) {
					laggView.apiCache.getPlayerResult(player.getGameProfile().getId(), 5);
				}
			}
		}
	}
	
	public void checkBanMsg(String msg) {
		if(msg.equals("\u00A7r\u00A7c\u00A7lA player has been removed from your game for hacking or abuse. \u00A7r\u00A7bThanks for reporting it!\u00A7r")) {
			Object[] last = this.leaves.get(leaves.size()-1);
			NetworkPlayerInfo p = (NetworkPlayerInfo) last[0];
			long time = (long)last[1];
			PlayerRequest r = laggView.apiCache.getPlayerResult(p.getGameProfile().getId(), 0);
			String name = p.getDisplayName().toString();
			if(r!=null && !r.getName().equals(p.getGameProfile().getName())) {
				name += " (" + r.getName() + ")";
			}
			Util.print("[LaggView] Last player to leave was: " + name + " at " + new SimpleDateFormat("KK:mm:ssa").format(new Date(time)));
		}
	}
	
	public boolean containsL(String formattedMsg) {
		return Pattern.compile("(^|\\s|:|\u00A7.{1})L+($|\\s|\u00A7)").matcher(formattedMsg).find();
	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		long time = System.currentTimeMillis();
		if(containsL(event.message.getFormattedText())) event.setCanceled(true);
		if(event.type!=2) {
			chatMessages.add(new ChatMessage(time,event.message.getFormattedText()));
			countCoins(event.message.getFormattedText());
			processParty(time,event.message.getFormattedText());
			processWdr(event.message.getFormattedText());
		}
	}
	
	private void processWdr(String msg) {
		Matcher m = Pattern.compile("\u00A7f\\[WATCHDOG] \u00A7r\u00A7aYou reported \u00A7r\u00A7e(\\S+)\u00A7r\u00A7a for \u00A7r\u00A7e\\[.*?]\u00A7r").matcher(msg);
		if(m.find()) {
			String playerName = m.group(1);
			new Thread() {
				public void run() {
					if(mc.ingameGUI.getTabList() instanceof TabOverlay) {
						TabOverlay tab = (TabOverlay)mc.ingameGUI.getTabList();
						for(NetworkPlayerInfo playerInfo : tab.getCurrentlyDisplayedPlayers()) {
							if(playerInfo.getGameProfile().getName().equals(playerName)) {
								UUID uuid = playerInfo.getGameProfile().getId();
								try {
									int reportsLeft = Integer.parseInt(URLConnectionReader.getText("http://thelagg.com/hypixel/reportPlayer/" + uuid.toString()));
									IChatComponent number = new ChatComponentText(Integer.toString(reportsLeft) + " ");
									IChatComponent endOfMsg = new ChatComponentText("more reports until that user is marked for all laggview users");
									number.getChatStyle().setColor(EnumChatFormatting.RED);
									endOfMsg.getChatStyle().setColor(EnumChatFormatting.GOLD);
									number.appendSibling(endOfMsg);
									Util.print(number);
								} catch (IOException e) {
									e.printStackTrace();
								}
								break;
							}
						}
					}
				}
			}.start();
		}
	}
	
	private void processParty(long time,String msg) {
		Pattern p = Pattern.compile("\u00A7r\u00A7.{1}(.*?)\u00A7r\u00A7e has joined \\(\u00A7r\u00A7b\\d+\u00A7r\u00A7e/\u00A7r\u00A7b\\d+\u00A7r\u00A7e\\)!\u00A7r");
		Matcher m = p.matcher(msg);
		long lastTime;
		try {
			lastTime = ((Long)lastPartyMessage[0]);
		} catch (ClassCastException e) {
			lastTime = ((Integer)lastPartyMessage[0]);
		}
		boolean bool = m.find();
		if(bool && time-lastTime<100) {
			String thisPlayer = m.group(1);
			String otherPlayer = (String)lastPartyMessage[1];
			boolean found = false;
			for(ArrayList<String> party : parties) {
				if(party.contains(otherPlayer)) {
					party.add(thisPlayer);
					found = true;
					break;
				}
			}
			if(!found) {
				ArrayList<String> newParty = new ArrayList<String>();
				newParty.add(thisPlayer);
				newParty.add(otherPlayer);
				parties.add(newParty);
			}
		}
		if(bool) {
			lastPartyMessage[0] = time;
			lastPartyMessage[1] = m.group(1);
		}
	}
	
	protected void removeHudText(Priority p) {
		for(int i = 0; i<this.hudText.size(); i++) {
			HudText entry = this.hudText.get(i);
			if(entry.getPriority()==p) {
				this.hudText.remove(entry);
			}
		}
	}
	
	protected void updateHudText(HudText hudText) {
		for(int i = 0; i<this.hudText.size(); i++) {
			HudText entry = this.hudText.get(i);
			if(hudText.samePriority(entry)) {
				this.hudText.remove(entry);
			}
		}
		this.hudText.add(hudText);
	}
	
	public ArrayList<HudText> getHudText() {
		return this.hudText;
	}
	
	public void countCoins(String msg) {
		Matcher m = Pattern.compile("\u00A7r\u00A76\\+(\\d+) coins").matcher(msg);
		if(m.find()) {
			try {
				coins += Integer.parseInt(m.group(1));
				updateHudText(new HudText(Priority.COINS,ChatFormatting.GOLD + "Coins: " + coins));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method is to be overridden by child classes who want to change the stat that's displayed in tab.
	 * 
	 * @param player the player's NetworkPlayerInfo, to indicate the player whose name is being loaded
	 * @param tabOverlay the tabOverlay that's sending the request
	 * @return
	 */
	public boolean processPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay) {
		return this.genericProcessPlayerTab(player, tabOverlay, "Network Level", (PlayerRequest p) -> p.getNetworkLevelStr());
	}
	
	/**
	 * 
	 * The more generic method used for processPlayerTab(), that doesn't change anything when adding a second name for a hacker or when unscrambling names in tab
	 * 
	 * @param player
	 * @param tabOverlay
	 * @param statname
	 * @param statGetter
	 * @return
	 */
	public boolean genericProcessPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay, String statname, StatGetter statGetter) {
		return this.genericProcessPlayerTab(player, tabOverlay, statname, statGetter, (String s) -> s, (String s) -> s.replaceAll("\u00A7k", ""));
	}
	
	
	/**
	 * 
	 * The less generic method used for processPlayerTab()
	 * 
	 * @param player
	 * @param tabOverlay
	 * @param statname
	 * @param statGetter
	 * @param secondNameReplacer
	 * @param unscrambler
	 * @return
	 */
	public boolean genericProcessPlayerTab(NetworkPlayerInfo player, TabOverlay tabOverlay, String statname, StatGetter statGetter, StringReplacer secondNameReplacer, StringReplacer unscrambler) {
        String s1 = tabOverlay.getPlayerName(player);
        s1 = unscrambler.replaceStr(s1);
        tabOverlay.setFooter(new ChatComponentText(ChatFormatting.GREEN + "Displaying " + ChatFormatting.RED + statname + ChatFormatting.GREEN + " in tab"));
        PlayerRequest playerRequest = laggView.apiCache.getPlayerResult(player.getGameProfile().getId(), 1);
        SessionRequest sessionRequest = laggView.apiCache.getSessionResult(mc.thePlayer.getUniqueID(), 1);
        if(sessionRequest!=null && sessionRequest.timeRequested-System.currentTimeMillis()>60*1000) {
        	laggView.apiCache.update(sessionRequest);
        }

        if(this.showRealNames && playerRequest!=null && playerRequest.getName()!=null && playersToReveal!=null && playersToReveal.contains(player.getGameProfile().getId())) {
        	if(!s1.contains(playerRequest.getName())) {
        		tabOverlay.getSecondNames().put(player, secondNameReplacer.replaceStr(s1));
        	}
        	s1 = s1.replaceAll(player.getGameProfile().getName(), ChatFormatting.DARK_RED + playerRequest.getName());
        }
        
        String kdr = playerRequest==null?"?":statGetter.getStat(playerRequest);
        tabOverlay.getNamesInTab().put(player, s1);
        tabOverlay.getSuffixes().put(player, kdr);
		return true;
	}
	
	public static class ChatMessage {
		private long time;
		private String msg;
		
		public ChatMessage(long time, String msg) {
			this.time = time;
			this.msg = msg;
		}
		
		public void setTime(long l) {
			time = l;
		}
		
		public void setMsg(String s) {
			this.msg = s;
		}
		
		public long getTime() {
			return this.time;
		}
		
		public String getMsg() {
			return this.msg;
		}
	}
	
	public enum GameState {
		PREGAME,
		INGAME,
		POSTGAME
	}
	
	public enum GameType {
		MEGA_WALLS("MEGA WALLS"),
		BLITZ_SURVIVAL_GAMES("BLITZ SG"),
		WARLORDS("WARLORDS"),
		UHC_CHAMPIONS("HYPIXEL"),
		THE_TNT_GAMES("THE TNT GAMES"),
		TNT_RUN("TNT RUN"),
		BOW_SPLEEF("BOW SPLEEF"),
		PVP_RUN("PVP RUN"),
		TNT_TAG("TNT TAG"),
		WIZARDS("WIZARDS"),
		COPS_AND_CRIMS("COPS AND CRIMS"),
		ARCADE_GAMES("ARCADE GAMES"),
		SPEED_UHC("SPEED UHC"),
		SKYWARS("SKYWARS"),
		HOUSING("Housing"),
		CRAZY_WALLS("CRAZY WALLS"),
		SMASH_HEROES("SMASH HEROES"),
		SKYCLASH("SKYCLASH"),
		BED_WARS("BED WARS"),
		MURDER_MYSTERY("MURDER MYSTERY"),
		BUILD_BATTLE("BUILD BATTLE"),
		PROTOTYPE_GAMES("PROTOTYPE"),
		THE_WALLS("THE WALLS"),
		QUAKECRAFT("QUAKECRAFT"),
		VAMPIREZ("VAMPIREZ"),
		PAINTBALL_WARFARE("PAINTBALL"),
		ARENA_BRAWL("ARENA BRAWL"),
		TURBO_KART_RACERS("TURBO KART RACERS"),
		CLASSIC_GAMES("CLASSIC GAMES"),
		BATTLE_ROYALE("BATTLE ROYALE"),
		HIDE_AND_SEEK("HIDE AND SEEK"),
		HYPIXEL_ZOMBIES("ZOMBIES"),
		DUELS("DUELS"),
		MAIN_LOBBY("HYPIXEL"),
		LOBBY("LOBBY"),
		UNKNOWN("UNKNOWN");
		
		private String nameOnScoreboard;
		
		private GameType(String nameOnScoreboard) {
			this.nameOnScoreboard = nameOnScoreboard;
		}
		
		public String getNameOnScoreboard() {
			return this.nameOnScoreboard;
		}
		
	}
	
}
