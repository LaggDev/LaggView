package com.thelagg.laggview.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;
import javax.swing.Timer;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.thelagg.laggview.utils.Util;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild.NotificationLevel;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.HoverEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.ForgeHooks;
import scala.actors.threadpool.Arrays;

public class DiscordListener extends ListenerAdapter
{	
	private List<Long> mutedChannelIds;
	private boolean includeBotMessages;
	private JDA jda;
	private String token;
	private Map<Long,String> idToName = new HashMap<Long,String>();
	private Timer timer;
	
	public DiscordListener(String token,boolean includeBotMessages) throws LoginException, InterruptedException {
		this.token = token;
		this.includeBotMessages = includeBotMessages;
        this.loadMutedChannels();
        jda = new JDABuilder(AccountType.CLIENT).setToken(token).buildBlocking();
		jda.addEventListener(this);
		timer = new Timer(60*1000,e -> DiscordListener.this.loadMutedChannels());
		timer.start();
	}
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
    	if((event.getAuthor().isBot() && !this.includeBotMessages)|| event.getMessage().getType()!=MessageType.DEFAULT) {
    		return;
    	}
    	
        if (event.isFromType(ChannelType.TEXT)) {
        	long guildId = event.getGuild().getIdLong();
        	if(mutedChannelIds.contains(guildId)) return;
        	String guildName = event.getGuild().getName();
        	String channelName = event.getChannel().getName();
        	String author = event.getAuthor().getName();
        	IChatComponent content = ForgeHooks.newChatWithLinks(event.getMessage().getContentStripped());
        	long id = event.getChannel().getIdLong();
        	if(mutedChannelIds.contains(id)) return;
        	this.idToName.put(id,getChannelConfigName(event));
        	ChatComponentText firstMsg = new ChatComponentText(ChatFormatting.DARK_PURPLE + "[" + guildName + "][" + channelName + "]");
        	ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ChatComponentText("Click to mute " + id)));
        	style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lagg discord mute " + id));
        	firstMsg.setChatStyle(style);
        	IChatComponent secondMsg = new ChatComponentText(" " + ChatFormatting.AQUA + author + ": " + ChatFormatting.WHITE).appendSibling(content);
        	IChatComponent msg = firstMsg.appendSibling(secondMsg);
        	secondMsg.getChatStyle().setParentStyle(null);
        	Util.print(msg);
        } else if (event.isFromType(ChannelType.PRIVATE)) {
        	String author = event.getAuthor().getName();
        	IChatComponent content = ForgeHooks.newChatWithLinks(event.getMessage().getContentStripped());
        	long id = event.getAuthor().getIdLong();
        	if(mutedChannelIds.contains(id)) return;
        	this.idToName.put(id,getChannelConfigName(event));
        	ChatComponentText firstMsg = new ChatComponentText(ChatFormatting.DARK_PURPLE + "[PM]");
        	ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ChatComponentText("Click to mute " + id)));
        	style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lagg discord mute " + id));
        	firstMsg.setChatStyle(style);
        	IChatComponent secondMsg = new ChatComponentText(" " + ChatFormatting.AQUA + author + ": ").appendSibling(content);
        	IChatComponent msg = firstMsg.appendSibling(secondMsg);
        	secondMsg.getChatStyle().setParentStyle(null);
            Util.print(msg);
        } else if (event.isFromType(ChannelType.GROUP)) {
        	String author = event.getAuthor().getName();
        	IChatComponent content = ForgeHooks.newChatWithLinks(event.getMessage().getContentStripped());
        	String groupName = event.getGroup().getName();
        	if(groupName==null) groupName = "Unnamed";
        	long id = event.getGroup().getIdLong();
        	if(mutedChannelIds.contains(id)) return;
        	this.idToName.put(id,getChannelConfigName(event));
        	ChatComponentText firstMsg = new ChatComponentText(ChatFormatting.DARK_PURPLE + "[" + groupName + "]");
        	ChatStyle style = new ChatStyle().setChatHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ChatComponentText("Click to mute " + id)));
        	style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lagg discord mute " + id));
        	firstMsg.setChatStyle(style);
        	IChatComponent secondMsg = new ChatComponentText(" " + ChatFormatting.AQUA + author + ": " + ChatFormatting.WHITE).appendSibling(content);
        	IChatComponent msg = firstMsg.appendSibling(secondMsg);
        	secondMsg.getChatStyle().setParentStyle(null);
            Util.print(msg);
        }      
    }
    
    public void unMuteChannel(long id) {
    	if(!this.mutedChannelIds.contains(id)) {
    		return;
    	}
    	this.mutedChannelIds.remove(id);
    	try {
    		BufferedReader in = new BufferedReader(new FileReader(getConfigFile()));
    		ArrayList<String> lines = new ArrayList<String>();
    		String line;
    		while((line=in.readLine())!=null) {
    			lines.add(line);
    		}
    		in.close();
    		getConfigFile().delete();
    		getConfigFile().createNewFile();
    		PrintWriter out = new PrintWriter(new FileWriter(getConfigFile()));
    		for(String s : lines) {
    			if(!s.contains(Long.toString(id))) {
    				out.println(s);
    			}
    		}
    		out.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    public void muteChannel(long id) {
    	if(!idToName.containsKey(id) || this.mutedChannelIds.contains(id)) {
    		return;
    	}
    	String channelName = idToName.get(id);
    	muteChannel(id,channelName);
    }
    
    public void muteChannel(long id, String channelName) {
    	try {
    		Files.write(getConfigFile().toPath(), ("\r\n" + id + " " + channelName).getBytes(), StandardOpenOption.APPEND);
    		if(!this.mutedChannelIds.contains(id)) this.mutedChannelIds.add(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void loadMutedChannels() {
    	try {
			File f = getConfigFile();
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line;
			ArrayList<Long> channels = new ArrayList<Long>();
			while((line=in.readLine())!=null) {
				Matcher m = Pattern.compile("(\\d+)(\\s|$)").matcher(line);
				if(m.find()) {
					try {
						long l = Long.parseLong(m.group(1));
						channels.add(l);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			in.close();
			this.mutedChannelIds = channels;
		} catch (IOException e) {
			e.printStackTrace();
			this.mutedChannelIds = new ArrayList<Long>();
		}
    }
    
    public File getConfigFile() throws IOException {
    	File f = new File("./discordconfig.txt");
    	if(!f.exists()) {
    		f.createNewFile();
    	}
    	return f;
    }

    public static String getChannelConfigName(MessageReceivedEvent event) {
    	if(event.isFromType(ChannelType.TEXT)) {
    		return "TextChannel > " + event.getGuild().getName() + " > " + event.getChannel().getName();
    	} else if (event.isFromType(ChannelType.PRIVATE)) {
    		return "PM > " + event.getChannel().getName();
    	} else {    		
    		return "Group > " + (event.getGroup().getName()==null?"Unnamed":event.getGroup().getName());
    	}
    }
}