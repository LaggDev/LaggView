package com.thelagg.laggview.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import com.thelagg.laggview.Game;
import com.thelagg.laggview.LaggView;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Hud {
	private LaggView laggView;
	private Minecraft mc;
	
	public Hud(LaggView laggView, Minecraft mc) {
		MinecraftForge.EVENT_BUS.register(this);
		this.laggView = laggView;
		this.mc = mc;
	}
	
	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent event) {
		if(event.isCancelable() || ElementType.EXPERIENCE != event.type) return;
		Game g = laggView.gameUpdater.getCurrentGame();
		ArrayList<HudText> text = new ArrayList<HudText>();
		if(g!=null) {
			text.addAll(g.getHudText());
		}
		Collections.sort(text,new Comparator<HudText>() {
			@Override
			public int compare(HudText ht1, HudText ht2) {
				if(ht1.priority.value<ht2.priority.value) {
					return -1;
				} else if (ht1.priority.value==ht2.priority.value){
					return 0;
				} else {
					return 1;
				}
			}
		});
		FontRenderer fr = mc.fontRendererObj;
		int height = fr.FONT_HEIGHT;
		int offset = height/2;
		int spaceBetweenLines = height/8;
		int i = 0;
		for(HudText textmsg : text) {
			fr.drawString(textmsg.msg, offset, offset + (height + spaceBetweenLines)*i, -1);			
			i++;
		}
	}
	
	public enum Priority {
		COINS(0),
		FINAL_KILLS(1),
		FINAL_ASSISTS(2),
		KILLS(3),
		ASSISTS(4);

		public int value;
		private Priority(int priority) {
			this.value = priority;
		}
	}
	
	public static class HudText {
		private Priority priority;
		private String msg;
		
		public boolean samePriority(HudText t) {
			return t.priority == this.priority;
		}
		
		public HudText(Priority priority,String msg) {
			this.priority = priority;
			this.msg = msg;
		}
	}
}
