package com.thelagg.laggview.commands;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.orangemarshall.hudproperty.test.DelayedTask;
import com.thelagg.laggview.settings.Config;
import com.thelagg.laggview.utils.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.ForgeHooks;

public class StatsCommand extends CommandBase {

	@Override
	public String getCommandName() {
		return "stats";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/stats <name>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length<1) {
			Util.print("/lagg stats <player>");
		} else {
			String url = "http://thelagg.com/hypixel/player/" + args[0];
			if(Config.getOpenStatsInBrowser() && Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException | URISyntaxException e) {
					e.printStackTrace();
				}
				new DelayedTask(() -> Minecraft.getMinecraft().displayGuiScreen(new GuiChat()),2);
			} else {
				IChatComponent text = ForgeHooks.newChatWithLinks(url);
				text.setChatStyle(text.getChatStyle().setColor(EnumChatFormatting.GOLD));
				Util.print(text);
			}
		}
	}
	
	@Override
	public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
		if(args.length<=1) {
			return getListOfStringsMatchingLastWord(args,Util.getPlayerNamesInTab());
		} else {
			return new ArrayList<String>();
		}
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

}
