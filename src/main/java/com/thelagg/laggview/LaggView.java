package com.thelagg.laggview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Timer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import scala.actors.threadpool.Arrays;

@Mod(modid = "laggview", version = "1.0", name = "Lagg View", acceptedMinecraftVersions = "[1.8.9]", useMetadata = true)
public class LaggView {
	Minecraft mc;
	public HackerMonitor hackerMonitor;
	public ApiCache apiCache;
	public static LaggView instance;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		instance = this;
		mc = Minecraft.getMinecraft();
		apiCache = new ApiCache();
		MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(hackerMonitor = new HackerMonitor(mc));
        ClientCommandHandler.instance.registerCommand(new Command(this));
        new Timer(10,new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				apiCache.processFirstRequest();
			}
        }).start();
	}
	
	public static LaggView getInstance() {
		return instance;
	}
	
	@SubscribeEvent
	public void onTick(ClientTickEvent event) {
		if(!(mc.ingameGUI instanceof GuiOverlay)) {
			try {
				mc.ingameGUI = new GuiOverlay(mc);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
