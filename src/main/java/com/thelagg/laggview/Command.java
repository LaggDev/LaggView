package com.thelagg.laggview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.orangemarshall.hudproperty.test.DelayedTask;
import com.thelagg.laggview.apirequests.NameHistoryRequest;
import com.thelagg.laggview.apirequests.NameToUUIDRequest;
import com.thelagg.laggview.games.MegaWallsGame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;

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
			return getListOfStringsMatchingLastWord(args, "name","record","hud","hotkeys","parties","finals","stats");
		}
		switch(args[0]) {
		case "record":
			List<String> modifiedList = getPlayerNamesInTab();
			modifiedList.add(0, "toggle");
			modifiedList.add(0,"list");
			return getListOfStringsMatchingLastWord(args,modifiedList);
		case "name":
		case "stats":
			return getListOfStringsMatchingLastWord(args,this.getPlayerNamesInTab());
		default:
			return Lists.newArrayList();
		}
	}
	
	public List<String> getPlayerNamesInTab() {
		List<String> list = new ArrayList<String>();
		if(Minecraft.getMinecraft().ingameGUI.getTabList() instanceof TabOverlay) {
			TabOverlay tab = (TabOverlay)Minecraft.getMinecraft().ingameGUI.getTabList();
			for(NetworkPlayerInfo i : tab.getCurrentlyDisplayedPlayers()) {
				list.add(i.getGameProfile().getName());
			}
		}
		return list;
	}

	@Override
	public void processCommand(ICommandSender sender, final String[] args) throws CommandException {
		if(args.length<1) {
			Util.print("/lagg [hud|hotkeys|test|parties|finals|record|stats]");
			return;
		}
		switch(args[0]) {
		case "hud":
			new DelayedTask(() -> laggView.hudProperty.openConfigScreen(), 1);
			break;
		case "hotkeys":
			new HotkeyGui(laggView.hackerMonitor.getStartRecordingHotkey(),laggView.hackerMonitor.getStopRecordingHotkey(),laggView);
			break;
		case "test":
			Game g = LaggView.getInstance().gameUpdater.getCurrentGame();
			Util.print(ChatFormatting.GOLD + g.toString());
			break;
		case "parties":
			g = LaggView.getInstance().gameUpdater.getCurrentGame();
			String msg = ChatFormatting.GREEN + "{";
			int i = 0;
			for(ArrayList<String> party : g.getParties()) {
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
					laggView.hackerMonitor.toggleRecording();
					break;
				case "list":
					laggView.hackerMonitor.printList();
					break;
				case "remove":
					if(args.length==3) {
						laggView.hackerMonitor.remove(args[2]);
					} else {
						Util.print(ChatFormatting.DARK_RED + "Please specify the player");
					}
				break;
				default:
					if(args.length==2) {
						laggView.hackerMonitor.addOrRemove(args[1]);
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
				Util.print(EnumChatFormatting.GOLD + "thelagg.com/wrapper/player/" + args[1]);
			}
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
