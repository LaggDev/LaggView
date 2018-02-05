import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

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

public class DiscordListener extends ListenerAdapter
{
    public static void main(String[] args) {
        try {
			DiscordListener dcListener = new DiscordListener("MjA5NDYxMDk0Mzg3ODEwMzE0.DU6-PQ.GuKnyjQoF3K71vSvx5inSHNchyQ");
		} catch (InterruptedException | LoginException e) {
			e.printStackTrace();
		}
    }
	
	
	List<Long> mutedChannelIds;
	boolean includeBotMessages;
	JDA jda;
	String token;
	
	public DiscordListener(String token) throws LoginException, InterruptedException {
		JDA jda = null;
		jda = new JDABuilder(AccountType.CLIENT).setToken(token).buildBlocking();
        mutedChannelIds = this.loadMutedChannels();
		jda.addEventListener(this);
	}
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
    	if(event.getAuthor().isBot() && event.getMessage().getType()!=MessageType.DEFAULT) {
    		return;
    	}
    	
        if (event.isFromType(ChannelType.TEXT)) {
        	String guildName = event.getGuild().getName();
        	String channelName = event.getChannel().getName();
        	String author = event.getAuthor().getName();
        	String content = event.getMessage().getContentStripped();
        	System.out.println("[" + guildName + "][" + channelName + "] " + author + ": " + content);
        	System.out.println(getChannelConfigName(event));
        } else if (event.isFromType(ChannelType.PRIVATE)) {
        	String author = event.getAuthor().getName();
        	String content = event.getMessage().getContentStripped();
            System.out.println("[PM] " + author + ": " + content);
            System.out.println(getChannelConfigName(event));
        } else if (event.isFromType(ChannelType.GROUP)) {
        	String author = event.getAuthor().getName();
        	String content = event.getMessage().getContentStripped();
        	String groupName = event.getGroup().getName();
        	if(groupName==null) groupName = "Unnamed";
        	System.out.println("[GROUP][" + groupName + "] " + author + ": " + content);
        	System.out.println(getChannelConfigName(event));
        }      
    }
    
    public boolean checkIfMuted(MessageReceivedEvent event) {
    	long id = event.getChannel().getIdLong();
    	String channelConfigName = getChannelConfigName(event);
    	
    }
    
    public List<Long> loadMutedChannels() {
    	try {
			File f = getConfigFile();
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line;
			ArrayList<Long> channels = new ArrayList<Long>();
			while((line=in.readLine())!=null) {
				Matcher m = Pattern.compile("\\((\\d+))").matcher(line);
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
			return channels;
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<Long>();
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
    		return "(" + event.getChannel().getId() + ")" + " TextChannel > " + event.getGuild().getName() + " > " + event.getChannel().getName();
    	} else if (event.isFromType(ChannelType.PRIVATE)) {
    		return "(" + event.getChannel().getId() + ")" + " PM > " + event.getChannel().getName();
    	} else {
    		
    		return "(" + event.getChannel().getId() + ")" + " Group > " + (event.getGroup().getName()==null?"Unnamed":event.getGroup().getName());
    	}
    }
}