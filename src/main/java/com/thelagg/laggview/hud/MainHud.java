package com.thelagg.laggview.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.orangemarshall.hudproperty.IRenderer;
import com.orangemarshall.hudproperty.util.ScreenPosition;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.games.Game;
import com.thelagg.laggview.settings.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class MainHud implements IRenderer {
	private LaggView laggView;
	private Minecraft mc;
	
	public MainHud(LaggView laggView, Minecraft mc) {
		laggView.hudProperty.register(this);
		this.laggView = laggView;
		this.mc = mc;
	}
	
	public enum Priority {
		COINS(0),
		FINAL_KILLS(1),
		FINAL_ASSISTS(2),
		KILLS(3),
		ASSISTS(4), 
		SOULS(5),
		WITHER_TIMER(6),
		BEDS_DESTROYED(7), 
		DAMAGE(8), 
		HEALING (9);

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
		
		public Priority getPriority() {
			return priority;
		}
		
		public String getMsg() {
			return msg;
		}
		
		public HudText(Priority priority,String msg) {
			this.priority = priority;
			this.msg = msg;
		}
	}

	@Override
	public void save(ScreenPosition pos) {
		Config.setTextHudX(pos.getRelativeX());
		Config.setTextHudY(pos.getRelativeY());
	}

	@Override
	public ScreenPosition load() {
		return ScreenPosition.fromRelativePosition(Config.getTextHudX(), Config.getTextHudY());
	}

	@Override
	public int getHeight() {
		Game g = laggView.gameUpdater.getCurrentGame();
		ArrayList<HudText> text = new ArrayList<HudText>();
		if(g!=null) {
			text.addAll(g.getHudText());
		}
		FontRenderer fr = mc.fontRendererObj;
		int height = fr.FONT_HEIGHT;
		int spaceBetweenLines = height/8;
		int numberofLines = text.size();
		int totalHeight = Math.max(0, numberofLines*height + (numberofLines-1)*spaceBetweenLines);
		return totalHeight;
	}

	@Override
	public int getWidth() {
		Game g = laggView.gameUpdater.getCurrentGame();
		ArrayList<HudText> text = new ArrayList<HudText>();
		if(g!=null) {
			text.addAll(g.getHudText());
		}
		FontRenderer fr = mc.fontRendererObj;
		int maxWidth = 0;
		for(HudText textmsg : text) {
			if(fr.getStringWidth(textmsg.msg)>maxWidth) {
				maxWidth = fr.getStringWidth(textmsg.msg);
			}
		}
		return maxWidth;
	}

	@Override
	public void render(ScreenPosition position) {
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
		int spaceBetweenLines = height/8;
		int i = 0;
		for(HudText textmsg : text) {
			fr.drawString(textmsg.msg, position.getAbsoluteX(), position.getAbsoluteY() + (height + spaceBetweenLines)*i, -1);			
			i++;
		}
	}

	@Override
	public void renderDummy(ScreenPosition position) {
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
		int spaceBetweenLines = height/8;
		int i = 0;
		for(HudText textmsg : text) {
			fr.drawString(textmsg.msg, position.getAbsoluteX(), position.getAbsoluteY() + (height + spaceBetweenLines)*i, -1);			
			i++;
		}
	}
}
