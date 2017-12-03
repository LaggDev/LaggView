package com.thelagg.laggview;
//--username LeviBengs@gmail.com --password levi1506

import java.util.List;

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
public class Main {
	Minecraft mc;
	public HackerMonitor hackerMonitor;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		mc = Minecraft.getMinecraft();
		//MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(hackerMonitor = new HackerMonitor(mc));
        ClientCommandHandler.instance.registerCommand(new Command(this));
	}
	
	@EventHandler
	public void postinit(FMLPostInitializationEvent event) {
		try {
			mc.ingameGUI = new GuiOverlay(mc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
