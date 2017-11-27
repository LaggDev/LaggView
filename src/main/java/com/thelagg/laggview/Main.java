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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import scala.actors.threadpool.Arrays;

@Mod(modid = "laggview", version = "1.0", name = "Lagg View", acceptedMinecraftVersions = "[1.8.9]", useMetadata = true)
public class Main {
	Minecraft mc;
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        ClientCommandHandler.instance.registerCommand(new Command());
        mc = Minecraft.getMinecraft();
	}
	
	@SubscribeEvent
	public void tick(ClientTickEvent event) {
		if(Command.toggleSpeed) {
			//approximate speed of sprint jumping = ~0.3163898580149468
			//max motion horizontally (speed 3) = 0.24822147100660083
			double x = mc.thePlayer.motionX;
			double y = mc.thePlayer.motionY;
			double z = mc.thePlayer.motionZ;
			mc.thePlayer.addChatMessage(new ChatComponentText("x:" + x + " y:" + y + " z:" + z ));
			double horizontal = Math.sqrt(x*x + z*z);
			double total = Math.sqrt(x*x + y*y + z*z);
			Command.times.add(horizontal);
			System.out.println(x + " " + y + " " + z + " " + horizontal + " " + total);
			double totalCount = 0;
			double totalTime = 0;
			for(int i = 0; i<Command.times.size(); i++) {
				totalTime += Command.times.get(i);
				totalCount++;
			}
			System.out.println(Arrays.toString(Command.times.toArray(new Double[Command.times.size()])));
			System.out.println("average time: " + totalTime/totalCount);
		}
		if(mc.theWorld==null) return;
		List<Entity> entities = mc.theWorld.getLoadedEntityList();
		for(Entity e : entities) {
			if(e instanceof EntityArrow && !(e.motionX==0&&e.motionY==0&&e.motionZ==0)) {
				EntityArrow arrow = (EntityArrow)e;
				double x = arrow.motionX;
				double y = arrow.motionY;
				double z = arrow.motionZ;
				//Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("x:" + x + " y:" + y + " z:" + z ));
				double horizontal = Math.sqrt(x*x + z*z);
				double total = Math.sqrt(x*x + y*y + z*z);
				//System.out.println(x + " " + y + " " + z + " " + horizontal + " " + total);
			}
		}
		if(mc.ingameGUI!=null && !(mc.ingameGUI instanceof GuiOverlay)) {
			mc.ingameGUI = new GuiOverlay(mc);
		}
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent.Pre event) {
		if(event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
			Minecraft mc = Minecraft.getMinecraft();
			TabOverlay t = new TabOverlay(mc,mc.ingameGUI);
			t.setHeader(new ChatComponentText(EnumChatFormatting.GREEN + " Lagg View" + EnumChatFormatting.RED + " BETA " + EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + "(ã�¥ï½¡â—•â€¿â€¿â—•ï½¡)ã�¥"));
			t.renderPlayerlist(new ScaledResolution(mc).getScaledWidth(), mc.theWorld.getScoreboard(), mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(3));
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	public void onBowShot(ArrowLooseEvent event) {
		Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(event.entityPlayer.getDisplayNameString() + " charge: " + event.charge));
	}

}
