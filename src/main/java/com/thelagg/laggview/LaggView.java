package com.thelagg.laggview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;
import javax.swing.Timer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.realmsclient.gui.ChatFormatting;
import com.orangemarshall.hudproperty.HudPropertyApi;
import com.orangemarshall.hudproperty.IRenderer;
import com.thelagg.laggview.commands.TeamInvite;
import com.thelagg.laggview.hud.GameUpdater;
import com.thelagg.laggview.hud.Hud;
import com.thelagg.laggview.settings.Settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@Mod(modid = LaggView.MODID, version = "1.0", name = "Lagg View", acceptedMinecraftVersions = "[1.8.9]", useMetadata = true)
public class LaggView {
	public static final String MODID = "laggview";
	Minecraft mc;
	public HackerRecorder hackerRecorder;
	public GuildMonitor hackerMonitor;
	public ApiCache apiCache;
	public static LaggView instance;
	private long lastLogin;
	public GameUpdater gameUpdater;
	public Hud hud;
	public Logger logger;
	public Settings settings;
	public HudPropertyApi hudProperty;
	public DiscordListener discordListener;
	private boolean warnedAboutIncompatibility = false;
	private String[] incompatibleMods = new String[] {"sidebarmod","oldanimations"};
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
        this.logger = LogManager.getLogger("laggview");
    	hudProperty = HudPropertyApi.newInstance();
		instance = this;
		settings = Settings.loadFromFile();
		mc = Minecraft.getMinecraft();
		apiCache = new ApiCache();
		MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(hackerRecorder = new HackerRecorder(mc,this));
        MinecraftForge.EVENT_BUS.register(hackerMonitor = new GuildMonitor(mc,this));
        ClientCommandHandler.instance.registerCommand(new Command(this));
        ClientCommandHandler.instance.registerCommand(new TeamInvite());
        new Timer(10,new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				apiCache.processFirstRequest();
			}
        }).start();
        MinecraftForge.EVENT_BUS.register(gameUpdater = new GameUpdater(mc,this));
        this.hud = new Hud(this,mc);
        new Thread(() -> loadDiscordListener()).start();
        try {
			incompatibleMods = URLConnectionReader.getText("http://thelagg.com/hypixel/incompatiblemods").split(",");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadDiscordListener() {
		try {
			File f = new File(System.getProperty("user.home") + "\\laggview\\discordtoken.txt");
			if(f.exists()) setDiscordListener(new String(Files.readAllBytes(f.toPath())));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setDiscordListener(String token) {
		Util.print(ChatFormatting.GREEN + "Loading Discord Module");
		try {	
			discordListener = new DiscordListener(token,false);
			Util.print(ChatFormatting.GREEN + "Discord module loaded succesfully");
			File folder = new File(System.getProperty("user.home") + "\\laggview");
			if(folder.exists() && folder.isFile()) folder.delete();
			if(!folder.exists()) folder.mkdir();
			File file = new File(folder.getPath() + "\\discordtoken.txt");
			if(file.exists()) {
				file.delete();
			}
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		try {
				Files.write(file.toPath(), (token).getBytes(), StandardOpenOption.APPEND);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Util.print(ChatFormatting.RED + "Error loading Discord module");
		}
	}
	
	public boolean hasMod(String modId) {
		return getMod(modId)!=null;
	}
	
	public ModContainer getMod(String modId) {
		List<ModContainer> mods = Loader.instance().getModList();
		for(ModContainer m : mods) {
			if(m.getModId().equals(modId)) return m;
		} 
		return null;
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) throws IncompatibleModException {
		if(hasMod("mwtools")) {
			try {
				Field flistener = EventBus.class.getDeclaredField("listeners");
				flistener.setAccessible(true);
				ConcurrentHashMap<Object, ArrayList<IEventListener>> listeners = (ConcurrentHashMap<Object, ArrayList<IEventListener>>) flistener.get(MinecraftForge.EVENT_BUS);
				for(Object o : listeners.keySet()) {
					if(o.getClass().getName().equals("cowzgonecrazy.megawallstools.hudproperty.HudPropertyApi")) {
						Field fregisteredRenderers = o.getClass().getDeclaredField("registeredRenderers");
						fregisteredRenderers.setAccessible(true);
						Set<Object> registeredRenderers = (Set<Object>) fregisteredRenderers.get(o);
						Object[] renderers = registeredRenderers.toArray(new Object[registeredRenderers.size()]);
						for(Object renderer : renderers) {
							System.out.println(renderer.getClass().getName());
							if(renderer.getClass().getName().equals("cowzgonecrazy.megawallstools.Modules.KillCounter")
									|| renderer.getClass().getName().equals("cowzgonecrazy.megawallstools.Modules.CoinCounter")) {
								registeredRenderers.remove(renderer);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void warnIncompatibleMods() {
		if(this.warnedAboutIncompatibility || mc.theWorld==null || mc.thePlayer==null) {
			return;
		}
		for(String s : this.incompatibleMods) {
			this.warnIncompatibleMod(s);
		}
		this.warnedAboutIncompatibility = true;
	}
	
	public void warnIncompatibleMod(String modId) {
		if(hasMod(modId)) {
			for(int i = 0; i<5; i++) {
				Util.print(ChatFormatting.DARK_RED + "Lagg View is known to have problems when used alongisde "+ ChatFormatting.RED + modId + ChatFormatting.DARK_RED + " mod. It is recommended you remove this mod for full functionality.");
			}
		}
	}
	
	public static LaggView getInstance() {
		return instance;
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		warnIncompatibleMods();
		if(mc.thePlayer!=null && mc.thePlayer.sendQueue!=null && !(mc.thePlayer.sendQueue instanceof MyPacketHandler)) {
			MyPacketHandler.replacePacketHandler();
		}
		MyPacketHandler.replaceNetworkManagerPacketHandler();
		if(!(mc.ingameGUI instanceof GuiOverlay)) {
			try {
				mc.ingameGUI = new GuiOverlay(mc,this);
				System.out.println("replacing gui overlay");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(!(mc.ingameGUI.getTabList() instanceof TabOverlay) && mc.ingameGUI instanceof GuiOverlay) {
			TabOverlay.ReplaceTabOverlay(this, (GuiOverlay)mc.ingameGUI);
		}
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
	
	public static class IncompatibleModException extends Exception {
		private static final long serialVersionUID = 6962780092461975254L;

		public IncompatibleModException(String modId) {
			super("LaggView is currently incompatible with " + modId + " mod. Sorry for any inconvenience.");
		}
	}

}
