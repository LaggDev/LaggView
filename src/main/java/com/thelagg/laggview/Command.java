package com.thelagg.laggview;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import com.google.common.collect.Ordering;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.apirequests.NameHistoryRequest;
import com.thelagg.laggview.apirequests.NameToUUIDRequest;
import com.thelagg.laggview.apirequests.PlayerRequest;
import com.thelagg.laggview.apirequests.SessionRequest;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;

public class Command extends CommandBase {

	public static boolean toggleSpeed = false;
	public static ArrayList<Double> times = new ArrayList<Double>();
	private LaggView main;
	
	public Command(LaggView m) {
		this.main = m;
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
	public void processCommand(ICommandSender sender, final String[] args) throws CommandException {
		switch(args[0]) {
		case "test":
			Game g = LaggView.getInstance().gameUpdater.getCurrentGame();
			Util.print(ChatFormatting.GOLD + g.toString());
			break;
		case "record":
				switch(args[1]) {
				case "add":
					String hackerName = args[2].toLowerCase();
					if(!main.hackerMonitor.hackerList.contains(hackerName)) {
						main.hackerMonitor.hackerList.add(hackerName);
						Util.print(EnumChatFormatting.DARK_PURPLE + "Added " + EnumChatFormatting.GOLD + hackerName + EnumChatFormatting.DARK_PURPLE + " to hacker list");
					} else {
						Util.print(EnumChatFormatting.GOLD + hackerName + EnumChatFormatting.DARK_RED + " is already on your hacker list!");
					}
					break;
				case "remove":
					hackerName = args[2].toLowerCase();
					if(main.hackerMonitor.hackerList.contains(hackerName)) {
						main.hackerMonitor.hackerList.remove(hackerName);
						Util.print(EnumChatFormatting.DARK_PURPLE + "Removed " + EnumChatFormatting.GOLD + hackerName + EnumChatFormatting.DARK_PURPLE + " from hacker list");
					} else {
						Util.print(EnumChatFormatting.GOLD + hackerName + EnumChatFormatting.DARK_RED + " is not on your hacker list!");
					}	
				break;
				default:
					Util.print("Couldn't recognize that command, sorry :/");
					break;
				}
			break;
		case "stats":
			if(args[1]==null) {
				Util.print("/stats <player>");
			} else {
				Util.print(EnumChatFormatting.GOLD + "thelagg.com/wrapper/player/" + args[1]);
			}
			break;
		case "scoretest":
			List<String> scorelist = GuiOverlay.getSidebarScores(Minecraft.getMinecraft().theWorld.getScoreboard());
			for(String str : scorelist) {
				Util.print(str);
			}
	        Util.print(EnumChatFormatting.GREEN + "Done! :)");
			break;
		case "tabtest":
			Minecraft mc = Minecraft.getMinecraft();
			NetHandlerPlayClient nethandlerplayclient = mc.thePlayer.sendQueue;
			Ordering<NetworkPlayerInfo> field_175252_a = Ordering.from(new TabOverlay.PlayerComparator());
	        List<NetworkPlayerInfo> list = field_175252_a.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
	        for(NetworkPlayerInfo player : list) {
	        	if(player!=null) {
		        	Util.print(TabOverlay.getPlayerNameStatic(player) + " " + player.getLocationSkin().getResourcePath());
	        	}
	        }
	        Util.print(EnumChatFormatting.GREEN + "Done! :)");
			break;
		case "speed":
			new Thread() {
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					toggleSpeed = true;
					try {
						Thread.sleep(Integer.parseInt(args[1]));
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					toggleSpeed = false;
					times = new ArrayList<Double>();
				}
			}.start();

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
