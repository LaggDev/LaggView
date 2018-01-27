package com.thelagg.laggview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Timer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.orangemarshall.hudproperty.HudPropertyApi;
import com.thelagg.laggview.hud.GameUpdater;
import com.thelagg.laggview.hud.Hud;
import com.thelagg.laggview.settings.Settings;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid = LaggView.MODID, version = "1.0", name = "Lagg View", acceptedMinecraftVersions = "[1.8.9]", useMetadata = true)
public class LaggView {
	public static final String MODID = "laggview";
	Minecraft mc;
	public HackerMonitor hackerMonitor;
	public ApiCache apiCache;
	public static LaggView instance;
	private long lastLogin;
	public GameUpdater gameUpdater;
	public Hud hud;
	public Logger logger;
	public Settings settings;
	public HudPropertyApi hudProperty;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
        this.logger = LogManager.getLogger("laggview");
    	hudProperty = HudPropertyApi.newInstance();
		instance = this;
		settings = Settings.loadFromFile();
		mc = Minecraft.getMinecraft();
		apiCache = new ApiCache();
		MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(hackerMonitor = new HackerMonitor(mc,this));
        ClientCommandHandler.instance.registerCommand(new Command(this));
        new Timer(10,new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				apiCache.processFirstRequest();
			}
        }).start();
        MinecraftForge.EVENT_BUS.register(gameUpdater = new GameUpdater(mc,this));
        this.hud = new Hud(this,mc);
	}
	
	public static LaggView getInstance() {
		return instance;
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(!(mc.ingameGUI instanceof GuiOverlay)) {
			try {
				mc.ingameGUI = new GuiOverlay(mc,this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
/*
		if(mc.thePlayer!=null && mc.thePlayer.sendQueue!=null && !(mc.thePlayer.sendQueue instanceof MyPacketHandler)) {
			try {
				Field fsendQueue = mc.thePlayer.getClass().getDeclaredField("sendQueue");
				fsendQueue.setAccessible(true);
				NetHandlerPlayClient sendQueue = (NetHandlerPlayClient) fsendQueue.get(mc.thePlayer);
				Field fguiScreen = sendQueue.getClass().getDeclaredField("guiScreenServer");
				fguiScreen.setAccessible(true);
				Field fnetManager = sendQueue.getClass().getDeclaredField("netManager");
				fnetManager.setAccessible(true);
				Field fprofile = sendQueue.getClass().getDeclaredField("profile");
				fprofile.setAccessible(true);
				
				GuiScreen guiScreen = (GuiScreen) fguiScreen.get(sendQueue);
				NetworkManager networkManager = (NetworkManager) fnetManager.get(sendQueue);
				GameProfile gameProfile = (GameProfile) fprofile.get(sendQueue);
				fsendQueue.set(mc.thePlayer, new MyPacketHandler(mc,guiScreen,networkManager,gameProfile));
				
			} catch (Exception e) {
				System.err.println("error replacing PacketHandler");
				e.printStackTrace();
			}
		}*/

	}
	
	@SubscribeEvent
	public void onChat(ClientChatReceivedEvent event) {
		if(event.type==0) {
			LogManager.getLogger(MODID).log(Level.INFO, event.message.getFormattedText());
			Matcher m = Pattern.compile("\u00A7r\u00A7.{1}(.*?)\u00A7r\u00A7e has joined \\(\u00A7r\u00A7b\\d+\u00A7r\u00A7e/\u00A7r\u00A7b\\d+\u00A7r\u00A7e\\)!\u00A7r").matcher(event.message.getFormattedText());
			if(m.find()) {
				long time = System.currentTimeMillis();
				long difference = time - lastLogin;
				LogManager.getLogger(MODID).log(Level.INFO, difference + " " + time + " " + event.message.getFormattedText());
				lastLogin = time;
			}
		}
	}

}
