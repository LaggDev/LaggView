package com.thelagg.laggview.settings;

import com.thelagg.laggview.LaggView;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement.DummyCategoryElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;


import java.util.ArrayList;
import java.util.List;

public class ModGuiConfig extends GuiConfig {
	public ModGuiConfig(GuiScreen guiScreen) {
		super(guiScreen, 
				new ConfigElement(Config.getConfig().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
		LaggView.MODID,
		false,
		false,
		"Lagg View Config");
	}
	
}
