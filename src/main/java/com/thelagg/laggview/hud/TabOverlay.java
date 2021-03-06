package com.thelagg.laggview.hud;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import com.thelagg.laggview.LaggView;
import com.thelagg.laggview.games.Game;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TabOverlay extends GuiPlayerTabOverlay {
	
    private static final Ordering<NetworkPlayerInfo> field_175252_a = Ordering.from(new TabOverlay.PlayerComparator());
    private final Minecraft mc;
    private final GuiIngame guiIngame;
    private IChatComponent footer;
    private IChatComponent header;
    /** The last time the playerlist was opened (went from not being renderd, to being rendered) */
    private long lastTimeOpened;
    /** Weither or not the playerlist is currently being rendered */
    private boolean isBeingRendered;
    private Map<NetworkPlayerInfo,String> suffixes;
    private Map<NetworkPlayerInfo,String> nameInTab;
    private Map<NetworkPlayerInfo,String> secondNames;
    private LaggView laggView;
    public NetworkPlayerInfo[] currentlyDisplayedPlayers = {};
    
    public Map<NetworkPlayerInfo,String> getSecondNames() {
    	return this.secondNames;
    }
    
    public static void ReplaceTabOverlay(LaggView laggView,GuiOverlay guiOverlay) {
    	Field f = null;
    	try {
    		f = GuiIngame.class.getDeclaredField("overlayPlayerList");
    	} catch (NoSuchFieldException e) {
    		try {
				f = GuiIngame.class.getDeclaredField("field_175196_v");
			} catch (NoSuchFieldException | SecurityException e1) {
				e1.printStackTrace();
			}
    	}
    	f.setAccessible(true);
    	try {
			f.set(guiOverlay, new TabOverlay(Minecraft.getMinecraft(),guiOverlay,laggView));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
    }
    
    public TabOverlay(Minecraft mcIn, GuiIngame guiIngameIn, LaggView laggView)
    {
        super(mcIn,guiIngameIn);
        this.mc = mcIn;
        this.guiIngame = guiIngameIn;
        suffixes = new HashMap<NetworkPlayerInfo,String>();
        nameInTab = new HashMap<NetworkPlayerInfo,String>();
        secondNames = new HashMap<NetworkPlayerInfo,String>();
        this.laggView = laggView;
    }
    
    public void processPlayer(NetworkPlayerInfo player) {
    	Game currentGame = laggView.gameUpdater.getCurrentGame();
    	if(currentGame!=null) {
    		boolean processed = currentGame.processPlayerTab(player, this);
    		if(processed) return;
    	}
    	
        String s1 = this.getPlayerName(player);
        nameInTab.put(player, s1);
        suffixes.put(player, "%ping%");
    }

    public Map<NetworkPlayerInfo,String> getNamesInTab() {
    	return this.nameInTab;
    }

    public Map<NetworkPlayerInfo,String> getSuffixes() {
    	return this.suffixes;
    }
    
    /**
     * Returns the name that should be renderd for the player supplied
     */
    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
    	return (networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName()));
    }

    public static String getPlayerNameStatic(NetworkPlayerInfo networkPlayerInfoIn)
    {
    	if(networkPlayerInfoIn.getDisplayName()!=null) {
    		return networkPlayerInfoIn.getDisplayName().getFormattedText();
    	} else {
    		String str = ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    		return str;
    	}
    }
    
    /**
     * Called by GuiIngame to update the information stored in the playerlist, does not actually render the list,
     * however.
     */
    @Override
    public void updatePlayerList(boolean willBeRendered)
    {
        if (willBeRendered && !this.isBeingRendered)
        {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }

        this.isBeingRendered = willBeRendered;
    }

    public NetworkPlayerInfo[] getCurrentlyDisplayedPlayers() {
    	try {
	    	NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
	    	List<NetworkPlayerInfo> list = field_175252_a.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
	        this.currentlyDisplayedPlayers = list.toArray(new NetworkPlayerInfo[list.size()]);
	        return this.currentlyDisplayedPlayers;
    	} catch (NullPointerException e) {
    		return new NetworkPlayerInfo[] {};
    	}
    }
    
    public void drawTextSmallerCenteredY(String s, double x, double y, double width) {
    	double distance = width;
    	double actualDistance = mc.fontRendererObj.getStringWidth(s);
    	double scaleFraction = Math.min(distance/actualDistance,1.0);
    	double redoScale = 1.0/scaleFraction;
    	y -= ((double)mc.fontRendererObj.FONT_HEIGHT)*scaleFraction/2.0;
    	GlStateManager.pushMatrix();
    	GlStateManager.scale(scaleFraction, scaleFraction, 1.0);
    	x *= redoScale;
    	y *= redoScale;
    	mc.fontRendererObj.drawStringWithShadow(s, (int)x, (int)y, -1);
    	GlStateManager.popMatrix();
    }
    
    /**
     * Renders the playerlist, its background, headers and footers.
     */
    @Override
    public void renderPlayerlist(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn)
    {
    	this.nameInTab = new HashMap<NetworkPlayerInfo,String>();
    	this.secondNames = new HashMap<NetworkPlayerInfo,String>();
    	this.suffixes = new HashMap<NetworkPlayerInfo,String>();
    	NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = field_175252_a.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        int i = 0;
        int j = 0;
        for (NetworkPlayerInfo networkplayerinfo : list)
        {
            int k = this.mc.fontRendererObj.getStringWidth(this.getPlayerName(networkplayerinfo));
            i = Math.max(i, k);

            if (scoreObjectiveIn != null && scoreObjectiveIn.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS)
            {
                k = this.mc.fontRendererObj.getStringWidth(" " + scoreboardIn.getValueFromObjective(networkplayerinfo.getGameProfile().getName(), scoreObjectiveIn).getScorePoints());
                j = Math.max(j, k);
            }
        }

        list = list.subList(0, Math.min(list.size(), 80));
        this.currentlyDisplayedPlayers = list.toArray(new NetworkPlayerInfo[list.size()]);
        int l3 = list.size();
        int i4 = l3;
        int j4;

        for (j4 = 1; i4 > 20; i4 = (l3 + j4 - 1) / j4)
        {
            ++j4;
        }

        boolean flag = this.mc.isIntegratedServerRunning() || this.mc.getNetHandler().getNetworkManager().getIsencrypted();
        int l;

        if (scoreObjectiveIn != null)
        {
            if (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS)
            {
                l = 90;
            }
            else
            {
                l = j;
            }
        }
        else
        {
            l = 0;
        }

        int i1 = Math.min(j4 * ((flag ? 9 : 0) + i + l + 13), width - 50) / j4;
        int j1 = width / 2 - (i1 * j4 + (j4 - 1) * 5) / 2;
        int k1 = 10;
        int l1 = i1 * j4 + (j4 - 1) * 5;
        List<String> list1 = null;
        List<String> list2 = null;

        if (this.header != null)
        {
            list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);

            for (String s : list1)
            {
                l1 = Math.max(l1, this.mc.fontRendererObj.getStringWidth(s));
            }
        }

        if (this.footer != null)
        {
            list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);

            for (String s2 : list2)
            {
                l1 = Math.max(l1, this.mc.fontRendererObj.getStringWidth(s2));
            }
        }

        if (list1 != null)
        {
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list1.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s3 : list1)
            {
                int i2 = this.mc.fontRendererObj.getStringWidth(s3);
                this.mc.fontRendererObj.drawStringWithShadow(s3, (float)(width / 2 - i2 / 2), (float)k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }

            ++k1;
        }

        drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + i4 * 9, Integer.MIN_VALUE);

        for (int k4 = 0; k4 < l3; ++k4)
        {
            int l4 = k4 / i4;
            int i5 = k4 % i4;
            int j2 = j1 + l4 * i1 + l4 * 5;
            int k2 = k1 + i5 * 9;
            drawRect(j2, k2, j2 + i1, k2 + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            if (k4 < list.size())
            {
                NetworkPlayerInfo networkplayerinfo1 = (NetworkPlayerInfo)list.get(k4);
                
                this.processPlayer(networkplayerinfo1);               
                String s1 = this.nameInTab.get(networkplayerinfo1);
                GameProfile gameprofile = networkplayerinfo1.getGameProfile();
                
                if (flag)
                {
                    EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
                    boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE) && (gameprofile.getName().equals("Dinnerbone") || gameprofile.getName().equals("Grumm"));
                    this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
                    int l2 = 8 + (flag1 ? 8 : 0);
                    int i3 = 8 * (flag1 ? -1 : 1);
                    Gui.drawScaledCustomSizeModalRect(j2, k2, 8.0F, (float)l2, 8, i3, 8, 8, 64.0F, 64.0F);

                    if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT))
                    {
                        int j3 = 8 + (flag1 ? 8 : 0);
                        int k3 = 8 * (flag1 ? -1 : 1);
                        Gui.drawScaledCustomSizeModalRect(j2, k2, 40.0F, (float)j3, 8, k3, 8, 8, 64.0F, 64.0F);
                    }

                    j2 += 9;
                }
                
                if (networkplayerinfo1.getGameType() == WorldSettings.GameType.SPECTATOR)
                {
                    s1 = EnumChatFormatting.ITALIC + s1;
                    this.mc.fontRendererObj.drawStringWithShadow(s1, (float)j2, (float)k2, -1862270977);
                }
                else
                {
                	double strLength = mc.fontRendererObj.getStringWidth(s1);
                	double properLength = (this.secondNames.containsKey(networkplayerinfo1)?0.67:1.0)*((double)(j2 - (flag?9:0) + i1 - 11-j2));
                	if(properLength>=strLength) {
                		this.mc.fontRendererObj.drawStringWithShadow(s1, (float)j2, (float)k2, -1);
                	} else {
                		this.drawTextSmallerCenteredY(s1, j2, ((double)k2) + ((double)mc.fontRendererObj.FONT_HEIGHT)/2.0, properLength);
                	}
                	
                    if(this.secondNames.containsKey(networkplayerinfo1)) {
                    	String secondName = this.secondNames.get(networkplayerinfo1);
                    	double x = j2 + (properLength>=strLength?strLength:properLength);
                    	double y = ((double)k2) + ((double)mc.fontRendererObj.FONT_HEIGHT)/2.0;
                    	double distance = j2 - (flag?9:0) + i1 - 11 - x;
                    	this.drawTextSmallerCenteredY(secondName, x, y, distance);
                    }
                }

                if (scoreObjectiveIn != null && networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR)
                {
                    int k5 = j2 + i + 1;
                    int l5 = k5 + l;

                    if (l5 - k5 > 5)
                    {
                        this.drawScoreboardValues(scoreObjectiveIn, k2, gameprofile.getName(), k5, l5, networkplayerinfo1);
                    }
                }

                String suffix = suffixes.get(networkplayerinfo1);
                if(suffix.equals("%ping%")) {
                	this.drawPing(i1, j2 - (flag ? 9 : 0), k2, networkplayerinfo1);
                } else {
	                double stringWidth = mc.fontRendererObj.getStringWidth(suffix);
	                double stringHeight = mc.fontRendererObj.FONT_HEIGHT;
	                double widthRatio = stringWidth/10.0;
	                double heightRatio = stringHeight/8.0;
	                double scale; //FRACTION
	                if(widthRatio>heightRatio) {
	                	scale = 1.0/widthRatio;
	                } else {
	                	scale = 1.0/heightRatio;
	                }
	                GlStateManager.pushMatrix();
	                GlStateManager.scale(scale, scale, 1.0);
	                double x = j2 - (flag ? 9 : 0) + i1 - 11, y = k2 + 2;
	                if(heightRatio>widthRatio) {
	                	x += 10.0-stringWidth*scale;
	                }
	                x = x/scale;
	                y = y/scale;
	                this.mc.fontRendererObj.drawStringWithShadow(EnumChatFormatting.GOLD + suffix, (int)x, (int)y, -1);
	                GlStateManager.popMatrix();
                }
            }
        }

        if (list2 != null)
        {
            k1 = k1 + i4 * 9 + 1;
            drawRect(width / 2 - l1 / 2 - 1, k1 - 1, width / 2 + l1 / 2 + 1, k1 + list2.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String s4 : list2)
            {
                int j5 = this.mc.fontRendererObj.getStringWidth(s4);
                this.mc.fontRendererObj.drawStringWithShadow(s4, (float)(width / 2 - j5 / 2), (float)k1, -1);
                k1 += this.mc.fontRendererObj.FONT_HEIGHT;
            }
        }
    }

    @Override
    protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(icons);
        int i = 0;
        int j = 0;

        if (networkPlayerInfoIn.getResponseTime() < 0)
        {
            j = 5;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 150)
        {
            j = 0;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 300)
        {
            j = 1;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 600)
        {
            j = 2;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 1000)
        {
            j = 3;
        }
        else
        {
            j = 4;
        }

        this.zLevel += 100.0F;
        this.drawTexturedModalRect(p_175245_2_ + p_175245_1_ - 11, p_175245_3_, 0 + i * 10, 176 + j * 8, 10, 8);
        this.zLevel -= 100.0F;
    }

    private void drawScoreboardValues(ScoreObjective p_175247_1_, int p_175247_2_, String p_175247_3_, int p_175247_4_, int p_175247_5_, NetworkPlayerInfo p_175247_6_)
    {
        int i = p_175247_1_.getScoreboard().getValueFromObjective(p_175247_3_, p_175247_1_).getScorePoints();

        if (p_175247_1_.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS)
        {
            this.mc.getTextureManager().bindTexture(icons);

            if (this.lastTimeOpened == p_175247_6_.func_178855_p())
            {
                if (i < p_175247_6_.func_178835_l())
                {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(this.guiIngame.getUpdateCounter() + 20));
                }
                else if (i > p_175247_6_.func_178835_l())
                {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(this.guiIngame.getUpdateCounter() + 10));
                }
            }

            if (Minecraft.getSystemTime() - p_175247_6_.func_178847_n() > 1000L || this.lastTimeOpened != p_175247_6_.func_178855_p())
            {
                p_175247_6_.func_178836_b(i);
                p_175247_6_.func_178857_c(i);
                p_175247_6_.func_178846_a(Minecraft.getSystemTime());
            }

            p_175247_6_.func_178843_c(this.lastTimeOpened);
            p_175247_6_.func_178836_b(i);
            int j = MathHelper.ceiling_float_int((float)Math.max(i, p_175247_6_.func_178860_m()) / 2.0F);
            int k = Math.max(MathHelper.ceiling_float_int((float)(i / 2)), Math.max(MathHelper.ceiling_float_int((float)(p_175247_6_.func_178860_m() / 2)), 10));
            boolean flag = p_175247_6_.func_178858_o() > (long)this.guiIngame.getUpdateCounter() && (p_175247_6_.func_178858_o() - (long)this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;

            if (j > 0)
            {
                float f = Math.min((float)(p_175247_5_ - p_175247_4_ - 4) / (float)k, 9.0F);

                if (f > 3.0F)
                {
                    for (int l = j; l < k; ++l)
                    {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)l * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                    }

                    for (int j1 = 0; j1 < j; ++j1)
                    {
                        this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);

                        if (flag)
                        {
                            if (j1 * 2 + 1 < p_175247_6_.func_178860_m())
                            {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 70, 0, 9, 9);
                            }

                            if (j1 * 2 + 1 == p_175247_6_.func_178860_m())
                            {
                                this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 79, 0, 9, 9);
                            }
                        }

                        if (j1 * 2 + 1 < i)
                        {
                            this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (j1 * 2 + 1 == i)
                        {
                            this.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                }
                else
                {
                    float f1 = MathHelper.clamp_float((float)i / 20.0F, 0.0F, 1.0F);
                    int i1 = (int)((1.0F - f1) * 255.0F) << 16 | (int)(f1 * 255.0F) << 8;
                    String s = "" + (float)i / 2.0F;

                    if (p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s + "hp") >= p_175247_4_)
                    {
                        s = s + "hp";
                    }

                    this.mc.fontRendererObj.drawStringWithShadow(s, (float)((p_175247_5_ + p_175247_4_) / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2), (float)p_175247_2_, i1);
                }
            }
        }
        else
        {
            String s1 = EnumChatFormatting.YELLOW + "" + i;
            this.mc.fontRendererObj.drawStringWithShadow(s1, (float)(p_175247_5_ - this.mc.fontRendererObj.getStringWidth(s1)), (float)p_175247_2_, 16777215);
        }
    }

    @Override
    public void setFooter(IChatComponent footerIn)
    {
        this.footer = footerIn;
    }

    @Override
    public void setHeader(IChatComponent headerIn)
    {
        this.header = headerIn;
    }

    @Override
    public void func_181030_a()
    {
        this.header = null;
        this.footer = null;
    }

    @SideOnly(Side.CLIENT)
    public static class PlayerComparator implements Comparator<NetworkPlayerInfo>
        {
            PlayerComparator() {}

            public int compare(NetworkPlayerInfo p_compare_1_, NetworkPlayerInfo p_compare_2_)
            {
                ScorePlayerTeam scoreplayerteam = p_compare_1_.getPlayerTeam();
                ScorePlayerTeam scoreplayerteam1 = p_compare_2_.getPlayerTeam();
                return ComparisonChain.start().compareTrueFirst(p_compare_1_.getGameType() != WorldSettings.GameType.SPECTATOR, p_compare_2_.getGameType() != WorldSettings.GameType.SPECTATOR).compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "").compare(p_compare_1_.getGameProfile().getName(), p_compare_2_.getGameProfile().getName()).result();
            }
        }

}
