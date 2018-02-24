package com.thelagg.laggview.commands;

import java.util.ArrayList;
import java.util.List;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

public class TeamInvite extends CommandBase {

	@Override
	public String getCommandName() {
		return "ti";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/ti <name>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		if(args.length<1) {
			Util.print(ChatFormatting.RED + "/ti <name>");
		} else {
			Minecraft.getMinecraft().thePlayer.sendChatMessage("/team invite " + args[0]);
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
