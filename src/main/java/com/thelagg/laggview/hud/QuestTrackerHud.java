package com.thelagg.laggview.hud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.common.collect.Lists;
import com.orangemarshall.hudproperty.IRenderer;
import com.orangemarshall.hudproperty.util.ScreenPosition;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.games.Game;
import com.thelagg.laggview.hud.MainHud.HudText;
import com.thelagg.laggview.modules.QuestTracker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class QuestTrackerHud implements IRenderer {

	private LaggView laggView;
	private QuestTracker tracker;
	private Minecraft mc;
	
	public QuestTrackerHud(QuestTracker tracker, LaggView laggView, Minecraft mc) {
		this.tracker = tracker;
		this.laggView = laggView;
		this.mc = mc;
	}
	
	@Override
	public void save(ScreenPosition pos) {
		laggView.config.setTextHudX(pos.getRelativeX());
		laggView.config.setTextHudY(pos.getRelativeY());
	}

	@Override
	public ScreenPosition load() {
		return ScreenPosition.fromRelativePosition(laggView.config.getTextHudX(), laggView.config.getTextHudY());
	}

	@Override
	public int getHeight() {
		String[] text = new String[] {"\u00A76QuestTracker","\u00A76dummyText1","\u00A76dummyText2","\u00A76dummyText3","\u00A76dummyText4"};
		FontRenderer fr = mc.fontRendererObj;
		int height = fr.FONT_HEIGHT;
		int spaceBetweenLines = height/8;
		int numberofLines = text.length;
		int totalHeight = Math.max(0, numberofLines*height + (numberofLines-1)*spaceBetweenLines);
		return totalHeight;
	}

	@Override
	public int getWidth() {
		String[] text = new String[] {"\u00A76QuestTracker","\u00A76dummyText1","\u00A76dummyText2","\u00A76dummyText3","\u00A76dummyText4"};
		FontRenderer fr = mc.fontRendererObj;
		int maxWidth = 0;
		for(String textmsg : text) {
			if(fr.getStringWidth(textmsg)>maxWidth) {
				maxWidth = fr.getStringWidth(textmsg);
			}
		}
		return maxWidth;
	}

	@Override
	public void render(ScreenPosition position) {
		ArrayList<HudText> text = tracker.getText();
		FontRenderer fr = mc.fontRendererObj;
		int height = fr.FONT_HEIGHT;
		int spaceBetweenLines = height/8;
		int i = 0;
		for(HudText textmsg : text) {
			fr.drawString(textmsg.getMsg(), position.getAbsoluteX(), position.getAbsoluteY() + (height + spaceBetweenLines)*i, -1);			
			i++;
		}
	}

	@Override
	public void renderDummy(ScreenPosition position) {
		String[] text = new String[] {"\u00A76QuestTracker","\u00A76dummyText1","\u00A76dummyText2","\u00A76dummyText3","\u00A76dummyText4"};
		FontRenderer fr = mc.fontRendererObj;
		int height = fr.FONT_HEIGHT;
		int spaceBetweenLines = height/8;
		int i = 0;
		for(String textmsg : text) {
			fr.drawString(textmsg, position.getAbsoluteX(), position.getAbsoluteY() + (height + spaceBetweenLines)*i, -1);			
			i++;
		}
	}

}
