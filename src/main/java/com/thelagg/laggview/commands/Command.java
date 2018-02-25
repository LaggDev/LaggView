package com.thelagg.laggview.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.LoginException;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.orangemarshall.hudproperty.test.DelayedTask;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.MyPacketHandler;
import com.thelagg.laggview.Util;
import com.thelagg.laggview.apirequests.GuildRequest;
import com.thelagg.laggview.apirequests.NameHistoryRequest;
import com.thelagg.laggview.apirequests.NameToUUIDRequest;
import com.thelagg.laggview.games.Game;
import com.thelagg.laggview.games.MegaWallsGame;
import com.thelagg.laggview.hud.HotkeyGui;
import com.thelagg.laggview.hud.TabOverlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;

public class Command extends CommandBase {

	public static boolean toggleSpeed = false;
	public static ArrayList<Double> times = new ArrayList<Double>();
	private LaggView laggView;
	
	public Command(LaggView m) {
		this.laggView = m;
	}
	
	@Override
	public String getCommandName() {
		return "lagg";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "";
	}
	
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if(args.length<=1) {
			return getListOfStringsMatchingLastWord(args, "name","record","hud","hotkeys","parties","finals","stats","discord","session");
		}
		switch(args[0]) {
		case "record":
			List<String> modifiedList = Util.getPlayerNamesInTab();
			modifiedList.add(0, "toggle");
			modifiedList.add(0,"list");
			return getListOfStringsMatchingLastWord(args,modifiedList);
		case "name":
		case "stats":
			return getListOfStringsMatchingLastWord(args,Util.getPlayerNamesInTab());
		default:
			return Lists.newArrayList();
		}
	}
	
	@Override
	public void processCommand(ICommandSender sender, final String[] args) throws CommandException {
		if(args.length<1) {
			Util.print("/lagg [hud|hotkeys|test|parties|finals|record|stats]");
			return;
		}
		switch(args[0]) {
		case "discord":
			if(args[1].equals("mute") && args.length>=3) {
				if(laggView.discordListener!=null) {
					try {
						laggView.discordListener.muteChannel(Long.parseLong(args[2]));
						ChatComponentText msg = new ChatComponentText(ChatFormatting.GREEN + "Muted channel " + Long.parseLong(args[2]));
						msg.setChatStyle(new ChatStyle()
								.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ChatComponentText("Click to unmute " + args[2])))
								.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/lagg discord unmute " + args[2])));
						Util.print(msg);
					} catch (NumberFormatException e) {
						e.printStackTrace();
						Util.print(ChatFormatting.RED + "Error muting channel");
					}
				} else {
					Util.print(ChatFormatting.RED + "Discord module not yet loaded!");
				}
			} else if (args[1].equals("unmute") && args.length>=3) {
				if(laggView.discordListener!=null) {
					try {
						laggView.discordListener.unMuteChannel(Long.parseLong(args[2]));
						Util.print(ChatFormatting.GREEN + "Unmuted channel " + Long.parseLong(args[2]));
					} catch (NumberFormatException e) {
						e.printStackTrace();
						Util.print(ChatFormatting.RED + "Error unmuting channel");
					}
				} else {
					Util.print(ChatFormatting.RED + "Discord module not yet loaded!");
				}
			} else if (args.length==1) {
				
			} else {
				Util.print(ChatFormatting.RED + "Could not recognize that command.");
			}
			break;
		case "hud":
			new DelayedTask(() -> laggView.hudProperty.openConfigScreen(),1);
			break;
		case "hotkeys":
			new HotkeyGui(laggView.hackerRecorder.getStartRecordingHotkey(),laggView.hackerRecorder.getStopRecordingHotkey(),laggView);
			break;
		case "test":
			new Thread() {
				public void run() {
					GuildRequest r = laggView.apiCache.getGuildResult(UUID.fromString(args[1]), 0);
					Util.print(Boolean.toString(r==null));
					Util.print(Arrays.toString(r.getUUIDs().toArray()));
				}
			}.start();
			
			/*GuiPlayerTabOverlay tab1 = Minecraft.getMinecraft().ingameGUI.getTabList();
			if(tab1 instanceof TabOverlay) {
				TabOverlay tab = (TabOverlay)tab1;
				for(NetworkPlayerInfo playerInfo : tab.getCurrentlyDisplayedPlayers()) {
					if(playerInfo.getGameProfile().getName().equals(args[1])) {
						List<Field> fields = MyPacketHandler.getFields(NetworkPlayerInfo.class);
						String s = playerInfo.toString() + " (" + fields.size() + ") ";
						for(Field f : fields) {
							f.setAccessible(true);
							try {
								s += f.getName() + ":" + f.get(playerInfo) + " ";
							} catch (IllegalArgumentException | IllegalAccessException e) {
								e.printStackTrace();
							}
						}
						Util.print(s);
						for(Entity e : Minecraft.getMinecraft().theWorld.getLoadedEntityList()) {
							if(e.getName().equals(args[1])) {
								Util.print(e.getDisplayName());
								break;
							}
						}
					}
				}
			}*/
			break;
		case "parties":
			Game g = LaggView.getInstance().gameUpdater.getCurrentGame();
			String msg = ChatFormatting.GREEN + "{";
			int i = 0;
			for(ArrayList<String> party : g.getParties()) {
				if(i==0) i++;
				msg += ChatFormatting.values()[i] + Arrays.toString(party.toArray(new String[party.size()]));
				i = (i+1)%16;
			}
			msg += ChatFormatting.GREEN + "}";
			Util.print(msg);
			break;
		case "finals":
			g = LaggView.getInstance().gameUpdater.getCurrentGame();
			if(g==null || !(g instanceof MegaWallsGame)) {
				Util.print("Not currently in a mega walls game");
			} else {
				MegaWallsGame mwGame = (MegaWallsGame)g;
				mwGame.printFinalKillsByTeam();
			}
			break;
		case "record":
				switch(args[1]) {
				case "toggle":
					laggView.hackerRecorder.toggleRecording();
					break;
				case "list":
					laggView.hackerRecorder.printList();
					break;
				case "remove":
					if(args.length==3) {
						laggView.hackerRecorder.remove(args[2]);
					} else {
						Util.print(ChatFormatting.DARK_RED + "Please specify the player");
					}
				break;
				default:
					if(args.length==2) {
						laggView.hackerRecorder.addOrRemove(args[1]);
					} else {
						Util.print("Couldn't recognize that command, sorry :/");
					}
					break;
				}
			break;
		case "stats":
			if(args.length<2) {
				Util.print("/lagg stats <player>");
			} else {
				IChatComponent text = ForgeHooks.newChatWithLinks("thelagg.com/hypixel/player/" + args[1]);
				text.setChatStyle(text.getChatStyle().setColor(EnumChatFormatting.GOLD));
				Util.print(text);
			}
			break;
		case "session":
			IChatComponent text = ForgeHooks.newChatWithLinks("thelagg.com/hypixel/session/" + sender.getName());
			text.setChatStyle(text.getChatStyle().setColor(EnumChatFormatting.GOLD));
			Util.print(text);
			break;
		case "name":
			if(args.length<2) {
				Util.print("/lagg name <player>");
			} else {
				new Thread() {
					public void run() {
						NameToUUIDRequest request1 = laggView.apiCache.getNameToUUIDRequest(args[1], 0);
						NameHistoryRequest r = laggView.apiCache.getNameHistoryResult(request1.getUUID(), 0);
						if(r!=null) {
							r.print();
						} else {
							Util.print(EnumChatFormatting.DARK_RED + "Could not find a player by the name " + args[1]);
						}
					}
				}.start();
			}
			break;
		default:
			System.out.println("that's not a valid command!");
			break;
		}
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}
	
	
}
