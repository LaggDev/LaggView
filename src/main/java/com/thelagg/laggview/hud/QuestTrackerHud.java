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
import com.thelagg.laggview.quests.Quest;
import com.thelagg.laggview.settings.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class QuestTrackerHud implements IRenderer {

	private LaggView laggView;
	private Minecraft mc;
	
	public QuestTrackerHud(LaggView laggView, Minecraft mc) {
		this.laggView = laggView;
		this.mc = mc;
		laggView.hudProperty.register(this);
	}
	
	@Override
	public void save(ScreenPosition pos) {
		Config.setQuestHudX(pos.getRelativeX());
		Config.setQuestHudY(pos.getRelativeY());
	}

	@Override
	public ScreenPosition load() {
		return ScreenPosition.fromRelativePosition(Config.getQuestHudX(), Config.getQuestHudY());
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
		Game g = laggView.gameUpdater.getCurrentGame();
		if(g==null) return;
		ArrayList<String> text = new ArrayList<String>();
		
		for(Quest q : g.getQuests()) {
			String s = q.toString();
			for(String s2 : s.split("\n")) {
				text.add(s2);
			}
		}
		
		FontRenderer fr = mc.fontRendererObj;
		int height = fr.FONT_HEIGHT;
		int spaceBetweenLines = height/8;
		int i = 0;
		for(String textmsg : text) {
			fr.drawString(textmsg, position.getAbsoluteX(), position.getAbsoluteY() + (height + spaceBetweenLines)*i, -1);			
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
