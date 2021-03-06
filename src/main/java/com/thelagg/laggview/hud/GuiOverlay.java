package com.thelagg.laggview.hud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.thelagg.laggview.LaggView;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.GuiIngameForge;

public class GuiOverlay extends GuiIngameForge {
	
	LaggView laggView;
	List<String> scoreboard = new ArrayList<String>();
	
    public GuiOverlay(Minecraft mcIn, LaggView laggView) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    	super(mcIn);
    	this.laggView = laggView;
    	TabOverlay.ReplaceTabOverlay(laggView,this);
	}
    
	/**
     * Filters scores from a scoreboards objective and returns the values in a list
     *
     * Output was designed around Hypixels scoreboard setup.
     *
     * @param scoreboard the target scoreboard
     * @return returns an empty list if no scores were found.
     */
    public static List<String> getSidebarScores(Scoreboard scoreboard) {
        List<String> found = new ArrayList<>();

        ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebar != null) {
            List<Score> scores = new ArrayList<>(scoreboard.getScores());


            /*Scores retrieved here do not care for ordering, this is done by the Scoreboard its self.
              We'll need to do this our selves in this case.

              This will appear backwars in chat, but remember that the scoreboard reverses this order
              to ensure highest scores go first.
             */
            scores.sort(Comparator.comparingInt(Score::getScorePoints));

            found = scores.stream()
                    .filter(score -> score.getObjective().getName().equals(sidebar.getName()))
                    .map(score -> score.getPlayerName() + getSuffixFromContainingTeam(scoreboard, score.getPlayerName()))
                    .collect(Collectors.toList());

        }
        return found;
    }

    /**
     * Filters through Scoreboard teams searching for a team
     * that contains the last part of our scoreboard message.
     *
     *
     * @param scoreboard The target scoreboard
     * @param member The message we're searching for inside a teams member collection.
     * @return If no team was found, an empty suffix is returned
     */
    private static String getSuffixFromContainingTeam(Scoreboard scoreboard, String member) {
        String suffix = null;
        for (ScorePlayerTeam team : scoreboard.getTeams()) {
            if (team.getMembershipCollection().contains(member)) {
                suffix = team.getColorSuffix();
                break;
            }
        }
        return (suffix == null ? "" : suffix);
    }
    
    public NetworkPlayerInfo getNetworkPlayerInfo(List<NetworkPlayerInfo> list,String name) {
    	for(NetworkPlayerInfo player : list) {
        	if(TabOverlay.getPlayerNameStatic(player).replaceAll("\u00A7.{1}", "").equals(name)) {
        		return player;
        	}
        }
    	return null;
    }
    
    public String[] getScoreboard() {
    	return scoreboard.toArray(new String[scoreboard.size()]);
    }
    
    @Override
    protected void renderScoreboard(ScoreObjective p_180475_1_, ScaledResolution p_180475_2_)
    {
    	this.scoreboard = new ArrayList<String>();
        Scoreboard scoreboard = p_180475_1_.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(p_180475_1_);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, new Predicate<Score>()
        {
            public boolean apply(Score p_apply_1_)
            {
                return p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#");
            }
        }));

        if (list.size() > 15)
        {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        }
        else
        {
            collection = list;
        }

        int i = this.getFontRenderer().getStringWidth(p_180475_1_.getDisplayName());

        for (Score score : collection)
        {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            i = Math.max(i, this.getFontRenderer().getStringWidth(s));
        }

        int i1 = collection.size() * this.getFontRenderer().FONT_HEIGHT;
        int j1 = p_180475_2_.getScaledHeight() / 2 + i1 / 3;
        int k1 = 3;
        int l1 = p_180475_2_.getScaledWidth() - i - k1;
        int j = 0;
        
        Minecraft mc = Minecraft.getMinecraft();
		NetHandlerPlayClient nethandlerplayclient = mc.thePlayer.sendQueue;
		Ordering<NetworkPlayerInfo> field_175252_a = Ordering.from(new TabOverlay.PlayerComparator());
        List<NetworkPlayerInfo> tabList = field_175252_a.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        
        
        for (Score score1 : collection)
        {
            ++j;
            ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
            String s1 = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());            
            String s2 = EnumChatFormatting.RED + "" /*+ score1.getScorePoints()*/;
            int k = j1 - j * this.getFontRenderer().FONT_HEIGHT;
            int l = p_180475_2_.getScaledWidth() - k1 + 2;
            drawRect(l1 - 2, k, l, k + this.getFontRenderer().FONT_HEIGHT, 1342177280);
            this.getFontRenderer().drawString(s1, l1, k, 553648127);
            this.getFontRenderer().drawString(s2, l - this.getFontRenderer().getStringWidth(s2), k, 553648127);

            if (j == collection.size())
            {
                String s3 = p_180475_1_.getDisplayName();
                drawRect(l1 - 2, k - this.getFontRenderer().FONT_HEIGHT - 1, l, k - 1, 1610612736);
                drawRect(l1 - 2, k - 1, l, k, 1342177280);
                this.getFontRenderer().drawString(s3, l1 + i / 2 - this.getFontRenderer().getStringWidth(s3) / 2, k - this.getFontRenderer().FONT_HEIGHT, 553648127);
            }
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            this.scoreboard.add(s1);
            if(s1.contains("\u00A7a")) {
            	Matcher m = Pattern.compile("\u00A7a([a-zA-Z0-9_]+)[^\\x00-\\x7F]*([a-zA-Z0-9_]*)").matcher(s1);
            	if(m.find()) {
	            	String name = m.group(1) + m.group(2);
	            	NetworkPlayerInfo player = getNetworkPlayerInfo(tabList,name);
	            	if(player!=null) {
		        		EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(player.getGameProfile().getId());
		                this.mc.getTextureManager().bindTexture(player.getLocationSkin());
		                Gui.drawScaledCustomSizeModalRect(l1 + this.getFontRenderer().getStringWidth(s1) + 2, k, 8.0F, 8, 8, 8, 8, 8, 64.0F, 64.0F);
		                if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT))
		                {
		                    Gui.drawScaledCustomSizeModalRect(l1 + this.getFontRenderer().getStringWidth(s1) + 2, k, 40.0F, 8, 8, 8, 8, 8, 64.0F, 64.0F);
		                }
	            	}
            	}
            }
        }
    }
    
}
