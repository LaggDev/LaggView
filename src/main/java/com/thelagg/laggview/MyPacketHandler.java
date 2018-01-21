package com.thelagg.laggview;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.GuardianSound;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.GuiScreenDemo;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.gui.GuiWinGame;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.particle.EntityPickupFX;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.player.inventory.LocalBlockIntercommunication;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.stream.MetadataAchievement;
import net.minecraft.client.stream.MetadataCombat;
import net.minecraft.client.stream.MetadataPlayerDeath;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S00PacketKeepAlive;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S10PacketSpawnPainting;
import net.minecraft.network.play.server.S11PacketSpawnExperienceOrb;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1EPacketRemoveEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.network.play.server.S20PacketEntityProperties;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S24PacketBlockAction;
import net.minecraft.network.play.server.S25PacketBlockBreakAnim;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S28PacketEffect;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.network.play.server.S31PacketWindowProperty;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.network.play.server.S33PacketUpdateSign;
import net.minecraft.network.play.server.S34PacketMaps;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.S36PacketSignEditorOpen;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.network.play.server.S42PacketCombatEvent;
import net.minecraft.network.play.server.S43PacketCamera;
import net.minecraft.network.play.server.S44PacketWorldBorder;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.network.play.server.S46PacketSetCompressionLevel;
import net.minecraft.network.play.server.S47PacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.network.play.server.S49PacketUpdateEntityNBT;
import net.minecraft.potion.PotionEffect;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.Explosion;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MyPacketHandler extends NetHandlerPlayClient {
	
	public MyPacketHandler(Minecraft mcIn, GuiScreen p_i46300_2_, NetworkManager p_i46300_3_, GameProfile p_i46300_4_) throws Exception {
		super(mcIn, p_i46300_2_, p_i46300_3_, p_i46300_4_);
		mcIn.playerController = new PlayerControllerMP(mcIn, this);
	}

	public Logger getLogger() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("MyPacketHandler.this.getLogger()");
			f.setAccessible(true);
			return (Logger)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public NetworkManager getNetManager() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("netManager");
			f.setAccessible(true);
			return (NetworkManager)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public GameProfile getProfile() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("profile");
			f.setAccessible(true);
			return (GameProfile)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}	
	
	public GuiScreen getGuiScreenSaver() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("guiScreenSaver");
			f.setAccessible(true);
			return (GuiScreen)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Minecraft getGameController() {
		return Minecraft.getMinecraft();
	}
	
	public WorldClient getClientWorldController() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("clientWorldController");
			f.setAccessible(true);
			return (WorldClient)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
		
	public boolean getDoneLoadingTerrain() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("doneLoadingTerrain");
			f.setAccessible(true);
			return (boolean)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}
	
	public boolean getField_147308_k() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("field_147308_k");
			f.setAccessible(true);
			return (boolean)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public Random getAvRandomizer() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("avRandomizer");
			f.setAccessible(true);
			return (Random)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setField_147308_k(boolean b) {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("field_147308_k");
			f.setAccessible(true);
			f.set(this, b);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Map<UUID, NetworkPlayerInfo> getPlayerInfoMapGetter() {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("playerInfoMap");
			f.setAccessible(true);
			return (Map<UUID, NetworkPlayerInfo>)f.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setDoneLoadingTerrain(boolean b) {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("doneLoadingTerrain");
			f.setAccessible(true);
			f.set(this, b);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setClientWorldController(WorldClient wc) {
		try {
			Field f = NetHandlerPlayClient.class.getDeclaredField("clientWorldController");
			f.setAccessible(true);
			f.set(this, wc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void addToSendQueue(Packet p) {
		try {
			Field f = this.getClass().getSuperclass().getDeclaredField("netManager");
			f.setAccessible(true);
			NetworkManager netManager = (NetworkManager) f.get(this);
			netManager.sendPacket(p);
			Field[] fields = getFields(p.getClass()).toArray(new Field[getFields(p.getClass()).size()]);
			String[] values = new String[fields.length];
			for(int i = 0; i<fields.length; i++) {
				fields[i].setAccessible(true);
				values[i] = fields[i].getName() + ":";
				values[i] += fields[i].get(p)==null?"null":fields[i].get(p).toString();
			}
			if(p instanceof C03PacketPlayer) {
				return;
			}
			LogManager.getLogger(LaggView.MODID).log(Level.INFO, "packet - " + p.toString() + Arrays.toString(values));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private static ArrayList<Field> getFields(Class<?> myclass) {
		if(myclass.getName().contains("Object")) {
			return new ArrayList<Field>();
		}
		ArrayList<Field> fields = new ArrayList<Field>();
		for(Field f : myclass.getDeclaredFields()) {
			fields.add(f);
		}
		if(myclass.getSuperclass()!=null) {
			fields.addAll(getFields(myclass.getSuperclass()));
		}
		return fields;
	}
	
	private void printPacket(Packet<?> packetIn) {
		try {
	        Field[] fields = getFields(packetIn.getClass()).toArray(new Field[getFields(packetIn.getClass()).size()]);
			String[] values = new String[fields.length];
			for(int i = 0; i<fields.length; i++) {
				fields[i].setAccessible(true);
				values[i] = fields[i].getName() + ":";
				values[i] += fields[i].get(packetIn)==null?"null":fields[i].get(packetIn).toString();
			}
			LogManager.getLogger(LaggView.MODID).log(Level.INFO, "packet - " + packetIn.toString() + Arrays.toString(values));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public void handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity packetIn)
    {
		printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, Minecraft.getMinecraft());
        double d0 = (double)packetIn.func_149051_d() / 32.0D;
        double d1 = (double)packetIn.func_149050_e() / 32.0D;
        double d2 = (double)packetIn.func_149049_f() / 32.0D;
        Entity entity = null;
        
        WorldClient clientWorldController = null;
        try {
        	Field f = NetHandlerPlayClient.class.getDeclaredField("clientWorldController");
        	f.setAccessible(true);
        	clientWorldController = (WorldClient) f.get(this);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        if (packetIn.func_149053_g() == 1)
        {
            entity = new EntityLightningBolt(clientWorldController, d0, d1, d2);
        }

        if (entity != null)
        {
            entity.serverPosX = packetIn.func_149051_d();
            entity.serverPosY = packetIn.func_149050_e();
            entity.serverPosZ = packetIn.func_149049_f();
            entity.rotationYaw = 0.0F;
            entity.rotationPitch = 0.0F;
            entity.setEntityId(packetIn.func_149052_c());
            clientWorldController.addWeatherEffect(entity);
        }
    }
	
	@Override
    public void handleEntityStatus(S19PacketEntityStatus packetIn)
    {
		printPacket(packetIn);
        WorldClient clientWorldController = null;
        try {
        	Field f = NetHandlerPlayClient.class.getDeclaredField("clientWorldController");
        	f.setAccessible(true);
        	clientWorldController = (WorldClient) f.get(this);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    	
    	PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, Minecraft.getMinecraft());
        Entity entity = packetIn.getEntity(clientWorldController);
        
        if (entity != null)
        {
            if (packetIn.getOpCode() == 21)
            {
                Minecraft.getMinecraft().getSoundHandler().playSound(new GuardianSound((EntityGuardian)entity));
            }
            else
            {
                entity.handleStatusUpdate(packetIn.getOpCode());
            }
        }
    }
	
    public void handleUpdateHealth(S06PacketUpdateHealth packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        this.getGameController().thePlayer.setPlayerSPHealth(packetIn.getHealth());
        this.getGameController().thePlayer.getFoodStats().setFoodLevel(packetIn.getFoodLevel());
        this.getGameController().thePlayer.getFoodStats().setFoodSaturationLevel(packetIn.getSaturationLevel());
    }

    public void handleSetExperience(S1FPacketSetExperience packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        this.getGameController().thePlayer.setXPStats(packetIn.func_149397_c(), packetIn.getTotalExperience(), packetIn.getLevel());
    }

    public void handleRespawn(S07PacketRespawn packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());

        if (packetIn.getDimensionID() != this.getGameController().thePlayer.dimension)
        {
        	this.setDoneLoadingTerrain(false);
            Scoreboard scoreboard = this.getClientWorldController().getScoreboard();
            this.setClientWorldController(new WorldClient(this, new WorldSettings(0L, packetIn.getGameType(), false, this.getGameController().theWorld.getWorldInfo().isHardcoreModeEnabled(), packetIn.getWorldType()), packetIn.getDimensionID(), packetIn.getDifficulty(), this.getGameController().mcProfiler));
            this.getClientWorldController().setWorldScoreboard(scoreboard);
            this.getGameController().loadWorld(this.getClientWorldController());
            this.getGameController().thePlayer.dimension = packetIn.getDimensionID();
            this.getGameController().displayGuiScreen(new GuiDownloadTerrain(this));
        }

        this.getGameController().setDimensionAndSpawnPlayer(packetIn.getDimensionID());
        this.getGameController().playerController.setGameType(packetIn.getGameType());
    }

    /**
     * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
     */
    public void handleExplosion(S27PacketExplosion packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Explosion explosion = new Explosion(this.getGameController().theWorld, (Entity)null, packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getStrength(), packetIn.getAffectedBlockPositions());
        explosion.doExplosionB(true);
        this.getGameController().thePlayer.motionX += (double)packetIn.func_149149_c();
        this.getGameController().thePlayer.motionY += (double)packetIn.func_149144_d();
        this.getGameController().thePlayer.motionZ += (double)packetIn.func_149147_e();
    }

    /**
     * Displays a GUI by ID. In order starting from id 0: Chest, Workbench, Furnace, Dispenser, Enchanting table,
     * Brewing stand, Villager merchant, Beacon, Anvil, Hopper, Dropper, Horse
     */
    public void handleOpenWindow(S2DPacketOpenWindow packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        EntityPlayerSP entityplayersp = this.getGameController().thePlayer;

        if ("minecraft:container".equals(packetIn.getGuiId()))
        {
            entityplayersp.displayGUIChest(new InventoryBasic(packetIn.getWindowTitle(), packetIn.getSlotCount()));
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        }
        else if ("minecraft:villager".equals(packetIn.getGuiId()))
        {
            entityplayersp.displayVillagerTradeGui(new NpcMerchant(entityplayersp, packetIn.getWindowTitle()));
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        }
        else if ("EntityHorse".equals(packetIn.getGuiId()))
        {
            Entity entity = this.getClientWorldController().getEntityByID(packetIn.getEntityId());

            if (entity instanceof EntityHorse)
            {
                entityplayersp.displayGUIHorse((EntityHorse)entity, new AnimalChest(packetIn.getWindowTitle(), packetIn.getSlotCount()));
                entityplayersp.openContainer.windowId = packetIn.getWindowId();
            }
        }
        else if (!packetIn.hasSlots())
        {
            entityplayersp.displayGui(new LocalBlockIntercommunication(packetIn.getGuiId(), packetIn.getWindowTitle()));
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        }
        else
        {
            ContainerLocalMenu containerlocalmenu = new ContainerLocalMenu(packetIn.getGuiId(), packetIn.getWindowTitle(), packetIn.getSlotCount());
            entityplayersp.displayGUIChest(containerlocalmenu);
            entityplayersp.openContainer.windowId = packetIn.getWindowId();
        }
    }

    /**
     * Handles pickin up an ItemStack or dropping one in your inventory or an open (non-creative) container
     */
    public void handleSetSlot(S2FPacketSetSlot packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        EntityPlayer entityplayer = this.getGameController().thePlayer;

        if (packetIn.func_149175_c() == -1)
        {
            entityplayer.inventory.setItemStack(packetIn.func_149174_e());
        }
        else
        {
            boolean flag = false;

            if (this.getGameController().currentScreen instanceof GuiContainerCreative)
            {
                GuiContainerCreative guicontainercreative = (GuiContainerCreative)this.getGameController().currentScreen;
                flag = guicontainercreative.getSelectedTabIndex() != CreativeTabs.tabInventory.getTabIndex();
            }

            if (packetIn.func_149175_c() == 0 && packetIn.func_149173_d() >= 36 && packetIn.func_149173_d() < 45)
            {
                ItemStack itemstack = entityplayer.inventoryContainer.getSlot(packetIn.func_149173_d()).getStack();

                if (packetIn.func_149174_e() != null && (itemstack == null || itemstack.stackSize < packetIn.func_149174_e().stackSize))
                {
                    packetIn.func_149174_e().animationsToGo = 5;
                }

                entityplayer.inventoryContainer.putStackInSlot(packetIn.func_149173_d(), packetIn.func_149174_e());
            }
            else if (packetIn.func_149175_c() == entityplayer.openContainer.windowId && (packetIn.func_149175_c() != 0 || !flag))
            {
                entityplayer.openContainer.putStackInSlot(packetIn.func_149173_d(), packetIn.func_149174_e());
            }
        }
    }

    /**
     * Verifies that the server and client are synchronized with respect to the inventory/container opened by the player
     * and confirms if it is the case.
     */
    public void handleConfirmTransaction(S32PacketConfirmTransaction packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Container container = null;
        EntityPlayer entityplayer = this.getGameController().thePlayer;

        if (packetIn.getWindowId() == 0)
        {
            container = entityplayer.inventoryContainer;
        }
        else if (packetIn.getWindowId() == entityplayer.openContainer.windowId)
        {
            container = entityplayer.openContainer;
        }

        if (container != null && !packetIn.func_148888_e())
        {
            this.addToSendQueue(new C0FPacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
        }
    }

    /**
     * Handles the placement of a specified ItemStack in a specified container/inventory slot
     */
    public void handleWindowItems(S30PacketWindowItems packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        EntityPlayer entityplayer = this.getGameController().thePlayer;

        if (packetIn.func_148911_c() == 0)
        {
            entityplayer.inventoryContainer.putStacksInSlots(packetIn.getItemStacks());
        }
        else if (packetIn.func_148911_c() == entityplayer.openContainer.windowId)
        {
            entityplayer.openContainer.putStacksInSlots(packetIn.getItemStacks());
        }
    }

    /**
     * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
     */
    public void handleSignEditorOpen(S36PacketSignEditorOpen packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        TileEntity tileentity = this.getClientWorldController().getTileEntity(packetIn.getSignPosition());

        if (!(tileentity instanceof TileEntitySign))
        {
            tileentity = new TileEntitySign();
            tileentity.setWorldObj(this.getClientWorldController());
            tileentity.setPos(packetIn.getSignPosition());
        }

        this.getGameController().thePlayer.openEditSign((TileEntitySign)tileentity);
    }

    /**
     * Updates a specified sign with the specified text lines
     */
    public void handleUpdateSign(S33PacketUpdateSign packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        boolean flag = false;

        if (this.getGameController().theWorld.isBlockLoaded(packetIn.getPos()))
        {
            TileEntity tileentity = this.getGameController().theWorld.getTileEntity(packetIn.getPos());

            if (tileentity instanceof TileEntitySign)
            {
                TileEntitySign tileentitysign = (TileEntitySign)tileentity;

                if (tileentitysign.getIsEditable())
                {
                    System.arraycopy(packetIn.getLines(), 0, tileentitysign.signText, 0, 4);
                    tileentitysign.markDirty();
                }

                flag = true;
            }
        }

        if (!flag && this.getGameController().thePlayer != null)
        {
            this.getGameController().thePlayer.addChatMessage(new ChatComponentText("Unable to locate sign at " + packetIn.getPos().getX() + ", " + packetIn.getPos().getY() + ", " + packetIn.getPos().getZ()));
        }
    }

    /**
     * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
     * beacons, skulls, flowerpot
     */
    public void handleUpdateTileEntity(S35PacketUpdateTileEntity packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());

        if (this.getGameController().theWorld.isBlockLoaded(packetIn.getPos()))
        {
            TileEntity tileentity = this.getGameController().theWorld.getTileEntity(packetIn.getPos());
            int i = packetIn.getTileEntityType();

            if (i == 1 && tileentity instanceof TileEntityMobSpawner || i == 2 && tileentity instanceof TileEntityCommandBlock || i == 3 && tileentity instanceof TileEntityBeacon || i == 4 && tileentity instanceof TileEntitySkull || i == 5 && tileentity instanceof TileEntityFlowerPot || i == 6 && tileentity instanceof TileEntityBanner)
            {
                tileentity.readFromNBT(packetIn.getNbtCompound());
            }
            else
            {
                tileentity.onDataPacket(this.getNetManager(), packetIn);
            }
        }
    }

    /**
     * Sets the progressbar of the opened window to the specified value
     */
    public void handleWindowProperty(S31PacketWindowProperty packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        EntityPlayer entityplayer = this.getGameController().thePlayer;

        if (entityplayer.openContainer != null && entityplayer.openContainer.windowId == packetIn.getWindowId())
        {
            entityplayer.openContainer.updateProgressBar(packetIn.getVarIndex(), packetIn.getVarValue());
        }
    }

    public void handleEntityEquipment(S04PacketEntityEquipment packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Entity entity = this.getClientWorldController().getEntityByID(packetIn.getEntityID());

        if (entity != null)
        {
            entity.setCurrentItemOrArmor(packetIn.getEquipmentSlot(), packetIn.getItemStack());
        }
    }

    /**
     * Resets the ItemStack held in hand and closes the window that is opened
     */
    public void handleCloseWindow(S2EPacketCloseWindow packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        this.getGameController().thePlayer.closeScreenAndDropStack();
    }

    /**
     * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote
     * for setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players
     * accessing a (Ender)Chest
     */
    public void handleBlockAction(S24PacketBlockAction packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        this.getGameController().theWorld.addBlockEvent(packetIn.getBlockPosition(), packetIn.getBlockType(), packetIn.getData1(), packetIn.getData2());
    }

    /**
     * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
     */
    public void handleBlockBreakAnim(S25PacketBlockBreakAnim packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        this.getGameController().theWorld.sendBlockBreakProgress(packetIn.getBreakerId(), packetIn.getPosition(), packetIn.getProgress());
    }

    public void handleMapChunkBulk(S26PacketMapChunkBulk packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());

        for (int i = 0; i < packetIn.getChunkCount(); ++i)
        {
            int j = packetIn.getChunkX(i);
            int k = packetIn.getChunkZ(i);
            this.getClientWorldController().doPreChunk(j, k, true);
            this.getClientWorldController().invalidateBlockReceiveRegion(j << 4, 0, k << 4, (j << 4) + 15, 256, (k << 4) + 15);
            Chunk chunk = this.getClientWorldController().getChunkFromChunkCoords(j, k);
            chunk.fillChunk(packetIn.getChunkBytes(i), packetIn.getChunkSize(i), true);
            this.getClientWorldController().markBlockRangeForRenderUpdate(j << 4, 0, k << 4, (j << 4) + 15, 256, (k << 4) + 15);

            if (!(this.getClientWorldController().provider instanceof WorldProviderSurface))
            {
                chunk.resetRelightChecks();
            }
        }
    }

    public void handleChangeGameState(S2BPacketChangeGameState packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        EntityPlayer entityplayer = this.getGameController().thePlayer;
        int i = packetIn.getGameState();
        float f = packetIn.func_149137_d();
        int j = MathHelper.floor_float(f + 0.5F);

        if (i >= 0 && i < S2BPacketChangeGameState.MESSAGE_NAMES.length && S2BPacketChangeGameState.MESSAGE_NAMES[i] != null)
        {
            entityplayer.addChatComponentMessage(new ChatComponentTranslation(S2BPacketChangeGameState.MESSAGE_NAMES[i], new Object[0]));
        }

        if (i == 1)
        {
            this.getClientWorldController().getWorldInfo().setRaining(true);
            this.getClientWorldController().setRainStrength(0.0F);
        }
        else if (i == 2)
        {
            this.getClientWorldController().getWorldInfo().setRaining(false);
            this.getClientWorldController().setRainStrength(1.0F);
        }
        else if (i == 3)
        {
            this.getGameController().playerController.setGameType(WorldSettings.GameType.getByID(j));
        }
        else if (i == 4)
        {
            this.getGameController().displayGuiScreen(new GuiWinGame());
        }
        else if (i == 5)
        {
            GameSettings gamesettings = this.getGameController().gameSettings;

            if (f == 0.0F)
            {
                this.getGameController().displayGuiScreen(new GuiScreenDemo());
            }
            else if (f == 101.0F)
            {
                this.getGameController().ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.movement", new Object[] {GameSettings.getKeyDisplayString(gamesettings.keyBindForward.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindLeft.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindBack.getKeyCode()), GameSettings.getKeyDisplayString(gamesettings.keyBindRight.getKeyCode())}));
            }
            else if (f == 102.0F)
            {
                this.getGameController().ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.jump", new Object[] {GameSettings.getKeyDisplayString(gamesettings.keyBindJump.getKeyCode())}));
            }
            else if (f == 103.0F)
            {
                this.getGameController().ingameGUI.getChatGUI().printChatMessage(new ChatComponentTranslation("demo.help.inventory", new Object[] {GameSettings.getKeyDisplayString(gamesettings.keyBindInventory.getKeyCode())}));
            }
        }
        else if (i == 6)
        {
            this.getClientWorldController().playSound(entityplayer.posX, entityplayer.posY + (double)entityplayer.getEyeHeight(), entityplayer.posZ, "random.successful_hit", 0.18F, 0.45F, false);
        }
        else if (i == 7)
        {
            this.getClientWorldController().setRainStrength(f);
        }
        else if (i == 8)
        {
            this.getClientWorldController().setThunderStrength(f);
        }
        else if (i == 10)
        {
            this.getClientWorldController().spawnParticle(EnumParticleTypes.MOB_APPEARANCE, entityplayer.posX, entityplayer.posY, entityplayer.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
            this.getClientWorldController().playSound(entityplayer.posX, entityplayer.posY, entityplayer.posZ, "mob.guardian.curse", 1.0F, 1.0F, false);
        }
    }

    /**
     * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
     * MapItemRenderer for it
     */
    public void handleMaps(S34PacketMaps packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        MapData mapdata = ItemMap.loadMapData(packetIn.getMapId(), this.getGameController().theWorld);
        packetIn.setMapdataTo(mapdata);
        this.getGameController().entityRenderer.getMapItemRenderer().updateMapTexture(mapdata);
    }

    public void handleEffect(S28PacketEffect packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());

        if (packetIn.isSoundServerwide())
        {
            this.getGameController().theWorld.playBroadcastSound(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        }
        else
        {
            this.getGameController().theWorld.playAuxSFX(packetIn.getSoundType(), packetIn.getSoundPos(), packetIn.getSoundData());
        }
    }

    /**
     * Updates the players statistics or achievements
     */
    public void handleStatistics(S37PacketStatistics packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        boolean flag = false;

        for (Entry<StatBase, Integer> entry : packetIn.func_148974_c().entrySet())
        {
            StatBase statbase = (StatBase)entry.getKey();
            int i = ((Integer)entry.getValue()).intValue();

            if (statbase.isAchievement() && i > 0)
            {
                if (this.getField_147308_k() && this.getGameController().thePlayer.getStatFileWriter().readStat(statbase) == 0)
                {
                    Achievement achievement = (Achievement)statbase;
                    this.getGameController().guiAchievement.displayAchievement(achievement);
                    this.getGameController().getTwitchStream().func_152911_a(new MetadataAchievement(achievement), 0L);

                    if (statbase == AchievementList.openInventory)
                    {
                        this.getGameController().gameSettings.showInventoryAchievementHint = false;
                        this.getGameController().gameSettings.saveOptions();
                    }
                }

                flag = true;
            }

            this.getGameController().thePlayer.getStatFileWriter().unlockAchievement(this.getGameController().thePlayer, statbase, i);
        }

        if (!this.getField_147308_k() && !flag && this.getGameController().gameSettings.showInventoryAchievementHint)
        {
            this.getGameController().guiAchievement.displayUnformattedAchievement(AchievementList.openInventory);
        }

        this.setField_147308_k(true);

        if (this.getGameController().currentScreen instanceof IProgressMeter)
        {
            ((IProgressMeter)this.getGameController().currentScreen).doneLoading();
        }
    }

    public void handleEntityEffect(S1DPacketEntityEffect packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Entity entity = this.getClientWorldController().getEntityByID(packetIn.getEntityId());

        if (entity instanceof EntityLivingBase)
        {
            PotionEffect potioneffect = new PotionEffect(packetIn.getEffectId() & 0xff, packetIn.getDuration(), packetIn.getAmplifier(), false, packetIn.func_179707_f());
            potioneffect.setPotionDurationMax(packetIn.func_149429_c());
            ((EntityLivingBase)entity).addPotionEffect(potioneffect);
        }
    }

    public void handleCombatEvent(S42PacketCombatEvent packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Entity entity = this.getClientWorldController().getEntityByID(packetIn.field_179775_c);
        EntityLivingBase entitylivingbase = entity instanceof EntityLivingBase ? (EntityLivingBase)entity : null;

        if (packetIn.eventType == S42PacketCombatEvent.Event.END_COMBAT)
        {
            long i = (long)(1000 * packetIn.field_179772_d / 20);
            MetadataCombat metadatacombat = new MetadataCombat(this.getGameController().thePlayer, entitylivingbase);
            this.getGameController().getTwitchStream().func_176026_a(metadatacombat, 0L - i, 0L);
        }
        else if (packetIn.eventType == S42PacketCombatEvent.Event.ENTITY_DIED)
        {
            Entity entity1 = this.getClientWorldController().getEntityByID(packetIn.field_179774_b);

            if (entity1 instanceof EntityPlayer)
            {
                MetadataPlayerDeath metadataplayerdeath = new MetadataPlayerDeath((EntityPlayer)entity1, entitylivingbase);
                metadataplayerdeath.func_152807_a(packetIn.deathMessage);
                this.getGameController().getTwitchStream().func_152911_a(metadataplayerdeath, 0L);
            }
        }
    }

    public void handleServerDifficulty(S41PacketServerDifficulty packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        this.getGameController().theWorld.getWorldInfo().setDifficulty(packetIn.getDifficulty());
        this.getGameController().theWorld.getWorldInfo().setDifficultyLocked(packetIn.isDifficultyLocked());
    }

    public void handleCamera(S43PacketCamera packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Entity entity = packetIn.getEntity(this.getClientWorldController());

        if (entity != null)
        {
            this.getGameController().setRenderViewEntity(entity);
        }
    }

    public void handleWorldBorder(S44PacketWorldBorder packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        packetIn.func_179788_a(this.getClientWorldController().getWorldBorder());
    }

    @SuppressWarnings("incomplete-switch")
    public void handleTitle(S45PacketTitle packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        S45PacketTitle.Type s45packettitle$type = packetIn.getType();
        String s = null;
        String s1 = null;
        String s2 = packetIn.getMessage() != null ? packetIn.getMessage().getFormattedText() : "";

        switch (s45packettitle$type)
        {
            case TITLE:
                s = s2;
                break;
            case SUBTITLE:
                s1 = s2;
                break;
            case RESET:
                this.getGameController().ingameGUI.displayTitle("", "", -1, -1, -1);
                this.getGameController().ingameGUI.func_175177_a();
                return;
        }

        this.getGameController().ingameGUI.displayTitle(s, s1, packetIn.getFadeInTime(), packetIn.getDisplayTime(), packetIn.getFadeOutTime());
    }

    public void handleSetCompressionLevel(S46PacketSetCompressionLevel packetIn)
    {
    	printPacket(packetIn);
        if (!this.getNetManager().isLocalChannel())
        {
            this.getNetManager().setCompressionTreshold(packetIn.func_179760_a());
        }
    }

    public void handlePlayerListHeaderFooter(S47PacketPlayerListHeaderFooter packetIn)
    {
    	printPacket(packetIn);
        this.getGameController().ingameGUI.getTabList().setHeader(packetIn.getHeader().getFormattedText().length() == 0 ? null : packetIn.getHeader());
        this.getGameController().ingameGUI.getTabList().setFooter(packetIn.getFooter().getFormattedText().length() == 0 ? null : packetIn.getFooter());
    }

    public void handleRemoveEntityEffect(S1EPacketRemoveEntityEffect packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Entity entity = this.getClientWorldController().getEntityByID(packetIn.getEntityId());

        if (entity instanceof EntityLivingBase)
        {
            ((EntityLivingBase)entity).removePotionEffectClient(packetIn.getEffectId());
        }
    }

    @SuppressWarnings("incomplete-switch")
    public void handlePlayerListItem(S38PacketPlayerListItem packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());

        for (S38PacketPlayerListItem.AddPlayerData s38packetplayerlistitem$addplayerdata : packetIn.func_179767_a())
        {
            if (packetIn.func_179768_b() == S38PacketPlayerListItem.Action.REMOVE_PLAYER)
            {
                this.getPlayerInfoMapGetter().remove(s38packetplayerlistitem$addplayerdata.getProfile().getId());
            }
            else
            {
                NetworkPlayerInfo networkplayerinfo = (NetworkPlayerInfo)this.getPlayerInfoMapGetter().get(s38packetplayerlistitem$addplayerdata.getProfile().getId());

                if (packetIn.func_179768_b() == S38PacketPlayerListItem.Action.ADD_PLAYER)
                {
                    networkplayerinfo = new NetworkPlayerInfo(s38packetplayerlistitem$addplayerdata);
                    this.getPlayerInfoMapGetter().put(networkplayerinfo.getGameProfile().getId(), networkplayerinfo);
                }

                if (networkplayerinfo != null)
                {
                	try {
	                	Method m1 = NetworkPlayerInfo.class.getDeclaredMethod("setGameType", WorldSettings.GameType.class);
	                	Method m2 = NetworkPlayerInfo.class.getDeclaredMethod("setResponseTime", int.class);
	                	m1.setAccessible(true);
	                	m2.setAccessible(true);
	                	switch (packetIn.func_179768_b())
	                    {
	                        case ADD_PLAYER:
	                            m1.invoke(networkplayerinfo, s38packetplayerlistitem$addplayerdata.getGameMode());
	                            m2.invoke(networkplayerinfo, s38packetplayerlistitem$addplayerdata.getPing());
	                        	//networkplayerinfo.setGameType(s38packetplayerlistitem$addplayerdata.getGameMode());
	                            //networkplayerinfo.setResponseTime(s38packetplayerlistitem$addplayerdata.getPing());
	                            break;
	                        case UPDATE_GAME_MODE:
	                            //networkplayerinfo.setGameType(s38packetplayerlistitem$addplayerdata.getGameMode());
	                            m1.invoke(networkplayerinfo, s38packetplayerlistitem$addplayerdata.getGameMode());
	                            break;
	                        case UPDATE_LATENCY:
	                            //networkplayerinfo.setResponseTime(s38packetplayerlistitem$addplayerdata.getPing());
	                            m2.invoke(networkplayerinfo, s38packetplayerlistitem$addplayerdata.getPing());
	                            break;
	                        case UPDATE_DISPLAY_NAME:
	                            networkplayerinfo.setDisplayName(s38packetplayerlistitem$addplayerdata.getDisplayName());
	                    }
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                }
            }
        }
    }

    public void handleKeepAlive(S00PacketKeepAlive packetIn)
    {
    	printPacket(packetIn);
        this.addToSendQueue(new C00PacketKeepAlive(packetIn.func_149134_c()));
    }

    public void handlePlayerAbilities(S39PacketPlayerAbilities packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        EntityPlayer entityplayer = this.getGameController().thePlayer;
        entityplayer.capabilities.isFlying = packetIn.isFlying();
        entityplayer.capabilities.isCreativeMode = packetIn.isCreativeMode();
        entityplayer.capabilities.disableDamage = packetIn.isInvulnerable();
        entityplayer.capabilities.allowFlying = packetIn.isAllowFlying();
        entityplayer.capabilities.setFlySpeed(packetIn.getFlySpeed());
        entityplayer.capabilities.setPlayerWalkSpeed(packetIn.getWalkSpeed());
    }

    /**
     * Displays the available command-completion options the server knows of
     */
    public void handleTabComplete(S3APacketTabComplete packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        String[] astring = packetIn.func_149630_c();

        if (this.getGameController().currentScreen instanceof GuiChat)
        {
            GuiChat guichat = (GuiChat)this.getGameController().currentScreen;
            guichat.onAutocompleteResponse(astring);
        }
    }

    public void handleSoundEffect(S29PacketSoundEffect packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        this.getGameController().theWorld.playSound(packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn.getSoundName(), packetIn.getVolume(), packetIn.getPitch(), false);
    }

    public void handleResourcePack(S48PacketResourcePackSend packetIn)
    {
    	printPacket(packetIn);
        final String s = packetIn.getURL();
        final String s1 = packetIn.getHash();

        if (s.startsWith("level://"))
        {
            String s2 = s.substring("level://".length());
            File file1 = new File(this.getGameController().mcDataDir, "saves");
            File file2 = new File(file1, s2);

            if (file2.isFile())
            {
                this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.ACCEPTED));
                Futures.addCallback(this.getGameController().getResourcePackRepository().setResourcePackInstance(file2), new FutureCallback<Object>()
                {
                    public void onSuccess(Object p_onSuccess_1_)
                    {
                        MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                    }
                    public void onFailure(Throwable p_onFailure_1_)
                    {
                        MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                    }
                });
            }
            else
            {
                this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
            }
        }
        else
        {
            if (this.getGameController().getCurrentServerData() != null && this.getGameController().getCurrentServerData().getResourceMode() == ServerData.ServerResourceMode.ENABLED)
            {
                this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.ACCEPTED));
                Futures.addCallback(this.getGameController().getResourcePackRepository().downloadResourcePack(s, s1), new FutureCallback<Object>()
                {
                    public void onSuccess(Object p_onSuccess_1_)
                    {
                        MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                    }
                    public void onFailure(Throwable p_onFailure_1_)
                    {
                        MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                    }
                });
            }
            else if (this.getGameController().getCurrentServerData() != null && this.getGameController().getCurrentServerData().getResourceMode() != ServerData.ServerResourceMode.PROMPT)
            {
                this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.DECLINED));
            }
            else
            {
                this.getGameController().addScheduledTask(new Runnable()
                {
                    public void run()
                    {
                        MyPacketHandler.this.getGameController().displayGuiScreen(new GuiYesNo(new GuiYesNoCallback()
                        {
                            public void confirmClicked(boolean result, int id)
                            {

                                if (result)
                                {
                                    if (MyPacketHandler.this.getGameController().getCurrentServerData() != null)
                                    {
                                        MyPacketHandler.this.getGameController().getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.ENABLED);
                                    }

                                    MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.ACCEPTED));
                                    Futures.addCallback(MyPacketHandler.this.getGameController().getResourcePackRepository().downloadResourcePack(s, s1), new FutureCallback<Object>()
                                    {
                                        public void onSuccess(Object p_onSuccess_1_)
                                        {
                                            MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.SUCCESSFULLY_LOADED));
                                        }
                                        public void onFailure(Throwable p_onFailure_1_)
                                        {
                                            MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                                        }
                                    });
                                }
                                else
                                {
                                    if (MyPacketHandler.this.getGameController().getCurrentServerData() != null)
                                    {
                                        MyPacketHandler.this.getGameController().getCurrentServerData().setResourceMode(ServerData.ServerResourceMode.DISABLED);
                                    }

                                    MyPacketHandler.this.getNetManager().sendPacket(new C19PacketResourcePackStatus(s1, C19PacketResourcePackStatus.Action.DECLINED));
                                }

                                ServerList.func_147414_b(MyPacketHandler.this.getGameController().getCurrentServerData());
                                MyPacketHandler.this.getGameController().displayGuiScreen((GuiScreen)null);
                            }
                        }, I18n.format("multiplayer.texturePrompt.line1", new Object[0]), I18n.format("multiplayer.texturePrompt.line2", new Object[0]), 0));
                    }
                });
            }
        }
    }

    public void handleEntityNBT(S49PacketUpdateEntityNBT packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Entity entity = packetIn.getEntity(this.getClientWorldController());

        if (entity != null)
        {
            entity.clientUpdateEntityNBT(packetIn.getTagCompound());
        }
    }

    /**
     * Handles packets that have room for a channel specification. Vanilla implemented channels are "MC|TrList" to
     * acquire a MerchantRecipeList trades for a villager merchant, "MC|Brand" which sets the server brand? on the
     * player instance and finally "MC|RPack" which the server uses to communicate the identifier of the default server
     * resourcepack for the client to load.
     */
    public void handleCustomPayload(S3FPacketCustomPayload packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());

        if ("MC|TrList".equals(packetIn.getChannelName()))
        {
            PacketBuffer packetbuffer = packetIn.getBufferData();

            try
            {
                int i = packetbuffer.readInt();
                GuiScreen guiscreen = this.getGameController().currentScreen;

                if (guiscreen != null && guiscreen instanceof GuiMerchant && i == this.getGameController().thePlayer.openContainer.windowId)
                {
                    IMerchant imerchant = ((GuiMerchant)guiscreen).getMerchant();
                    MerchantRecipeList merchantrecipelist = MerchantRecipeList.readFromBuf(packetbuffer);
                    imerchant.setRecipes(merchantrecipelist);
                }
            }
            catch (IOException ioexception)
            {
                MyPacketHandler.this.getLogger().error((String)"Couldn\'t load trade info", (Throwable)ioexception);
            }
            finally
            {
                packetbuffer.release();
            }
        }
        else if ("MC|Brand".equals(packetIn.getChannelName()))
        {
            this.getGameController().thePlayer.setClientBrand(packetIn.getBufferData().readStringFromBuffer(32767));
        }
        else if ("MC|BOpen".equals(packetIn.getChannelName()))
        {
            ItemStack itemstack = this.getGameController().thePlayer.getCurrentEquippedItem();

            if (itemstack != null && itemstack.getItem() == Items.written_book)
            {
                this.getGameController().displayGuiScreen(new GuiScreenBook(this.getGameController().thePlayer, itemstack, false));
            }
        }
    }

    /**
     * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
     */
    public void handleScoreboardObjective(S3BPacketScoreboardObjective packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Scoreboard scoreboard = this.getClientWorldController().getScoreboard();

        if (packetIn.func_149338_e() == 0)
        {
            ScoreObjective scoreobjective = scoreboard.addScoreObjective(packetIn.func_149339_c(), IScoreObjectiveCriteria.DUMMY);
            scoreobjective.setDisplayName(packetIn.func_149337_d());
            scoreobjective.setRenderType(packetIn.func_179817_d());
        }
        else
        {
            ScoreObjective scoreobjective1 = scoreboard.getObjective(packetIn.func_149339_c());

            if (packetIn.func_149338_e() == 1)
            {
                scoreboard.removeObjective(scoreobjective1);
            }
            else if (packetIn.func_149338_e() == 2)
            {
                scoreobjective1.setDisplayName(packetIn.func_149337_d());
                scoreobjective1.setRenderType(packetIn.func_179817_d());
            }
        }
    }

    /**
     * Either updates the score with a specified value or removes the score for an objective
     */
    public void handleUpdateScore(S3CPacketUpdateScore packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Scoreboard scoreboard = this.getClientWorldController().getScoreboard();
        ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.getObjectiveName());

        if (packetIn.getScoreAction() == S3CPacketUpdateScore.Action.CHANGE)
        {
            Score score = scoreboard.getValueFromObjective(packetIn.getPlayerName(), scoreobjective);
            score.setScorePoints(packetIn.getScoreValue());
        }
        else if (packetIn.getScoreAction() == S3CPacketUpdateScore.Action.REMOVE)
        {
            if (StringUtils.isNullOrEmpty(packetIn.getObjectiveName()))
            {
                scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), (ScoreObjective)null);
            }
            else if (scoreobjective != null)
            {
                scoreboard.removeObjectiveFromEntity(packetIn.getPlayerName(), scoreobjective);
            }
        }
    }

    /**
     * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below
     * name)
     */
    public void handleDisplayScoreboard(S3DPacketDisplayScoreboard packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Scoreboard scoreboard = this.getClientWorldController().getScoreboard();

        if (packetIn.func_149370_d().length() == 0)
        {
            scoreboard.setObjectiveInDisplaySlot(packetIn.func_149371_c(), (ScoreObjective)null);
        }
        else
        {
            ScoreObjective scoreobjective = scoreboard.getObjective(packetIn.func_149370_d());
            scoreboard.setObjectiveInDisplaySlot(packetIn.func_149371_c(), scoreobjective);
        }
    }

    /**
     * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
     * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
     */
    public void handleTeams(S3EPacketTeams packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Scoreboard scoreboard = this.getClientWorldController().getScoreboard();
        ScorePlayerTeam scoreplayerteam;

        if (packetIn.func_149307_h() == 0)
        {
            scoreplayerteam = scoreboard.createTeam(packetIn.func_149312_c());
        }
        else
        {
            scoreplayerteam = scoreboard.getTeam(packetIn.func_149312_c());
        }

        if (packetIn.func_149307_h() == 0 || packetIn.func_149307_h() == 2)
        {
            scoreplayerteam.setTeamName(packetIn.func_149306_d());
            scoreplayerteam.setNamePrefix(packetIn.func_149311_e());
            scoreplayerteam.setNameSuffix(packetIn.func_149309_f());
            scoreplayerteam.setChatFormat(EnumChatFormatting.func_175744_a(packetIn.func_179813_h()));
            scoreplayerteam.func_98298_a(packetIn.func_149308_i());
            Team.EnumVisible team$enumvisible = Team.EnumVisible.func_178824_a(packetIn.func_179814_i());

            if (team$enumvisible != null)
            {
                scoreplayerteam.setNameTagVisibility(team$enumvisible);
            }
        }

        if (packetIn.func_149307_h() == 0 || packetIn.func_149307_h() == 3)
        {
            for (String s : packetIn.func_149310_g())
            {
                scoreboard.addPlayerToTeam(s, packetIn.func_149312_c());
            }
        }

        if (packetIn.func_149307_h() == 4)
        {
            for (String s1 : packetIn.func_149310_g())
            {
                scoreboard.removePlayerFromTeam(s1, scoreplayerteam);
            }
        }

        if (packetIn.func_149307_h() == 1)
        {
            scoreboard.removeTeam(scoreplayerteam);
        }
    }

    /**
     * Spawns a specified number of particles at the specified location with a randomized displacement according to
     * specified bounds
     */
    public void handleParticles(S2APacketParticles packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());

        if (packetIn.getParticleCount() == 0)
        {
            double d0 = (double)(packetIn.getParticleSpeed() * packetIn.getXOffset());
            double d2 = (double)(packetIn.getParticleSpeed() * packetIn.getYOffset());
            double d4 = (double)(packetIn.getParticleSpeed() * packetIn.getZOffset());

            try
            {
                this.getClientWorldController().spawnParticle(packetIn.getParticleType(), packetIn.isLongDistance(), packetIn.getXCoordinate(), packetIn.getYCoordinate(), packetIn.getZCoordinate(), d0, d2, d4, packetIn.getParticleArgs());
            }
            catch (Throwable var17)
            {
                MyPacketHandler.this.getLogger().warn("Could not spawn particle effect " + packetIn.getParticleType());
            }
        }
        else
        {
            for (int i = 0; i < packetIn.getParticleCount(); ++i)
            {
                double d1 = this.getAvRandomizer().nextGaussian() * (double)packetIn.getXOffset();
                double d3 = this.getAvRandomizer().nextGaussian() * (double)packetIn.getYOffset();
                double d5 = this.getAvRandomizer().nextGaussian() * (double)packetIn.getZOffset();
                double d6 = this.getAvRandomizer().nextGaussian() * (double)packetIn.getParticleSpeed();
                double d7 = this.getAvRandomizer().nextGaussian() * (double)packetIn.getParticleSpeed();
                double d8 = this.getAvRandomizer().nextGaussian() * (double)packetIn.getParticleSpeed();

                try
                {
                    this.getClientWorldController().spawnParticle(packetIn.getParticleType(), packetIn.isLongDistance(), packetIn.getXCoordinate() + d1, packetIn.getYCoordinate() + d3, packetIn.getZCoordinate() + d5, d6, d7, d8, packetIn.getParticleArgs());
                }
                catch (Throwable var16)
                {
                    MyPacketHandler.this.getLogger().warn("Could not spawn particle effect " + packetIn.getParticleType());
                    return;
                }
            }
        }
    }

    /**
     * Updates en entity's attributes and their respective modifiers, which are used for speed bonusses (player
     * sprinting, animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie
     * maxHealth and knockback resistance as well as reinforcement spawning chance.
     */
    public void handleEntityProperties(S20PacketEntityProperties packetIn)
    {
    	printPacket(packetIn);
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.getGameController());
        Entity entity = this.getClientWorldController().getEntityByID(packetIn.getEntityId());

        if (entity != null)
        {
            if (!(entity instanceof EntityLivingBase))
            {
                throw new IllegalStateException("Server tried to update attributes of a non-living entity (actually: " + entity + ")");
            }
            else
            {
                BaseAttributeMap baseattributemap = ((EntityLivingBase)entity).getAttributeMap();

                for (S20PacketEntityProperties.Snapshot s20packetentityproperties$snapshot : packetIn.func_149441_d())
                {
                    IAttributeInstance iattributeinstance = baseattributemap.getAttributeInstanceByName(s20packetentityproperties$snapshot.func_151409_a());

                    if (iattributeinstance == null)
                    {
                        iattributeinstance = baseattributemap.registerAttribute(new RangedAttribute((IAttribute)null, s20packetentityproperties$snapshot.func_151409_a(), 0.0D, 2.2250738585072014E-308D, Double.MAX_VALUE));
                    }

                    iattributeinstance.setBaseValue(s20packetentityproperties$snapshot.func_151410_b());
                    iattributeinstance.removeAllModifiers();

                    for (AttributeModifier attributemodifier : s20packetentityproperties$snapshot.func_151408_c())
                    {
                        iattributeinstance.applyModifier(attributemodifier);
                    }
                }
            }
        }
    }

    /**
     * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
     */
    public NetworkManager getNetworkManager()
    {
        return this.getNetManager();
    }

    public Collection<NetworkPlayerInfo> getPlayerInfoMap()
    {
        return this.getPlayerInfoMapGetter().values();
    }

    public NetworkPlayerInfo getPlayerInfo(UUID p_175102_1_)
    {
        return (NetworkPlayerInfo)this.getPlayerInfoMapGetter().get(p_175102_1_);
    }

    /**
     * Gets the client's description information about another player on the server.
     */
    public NetworkPlayerInfo getPlayerInfo(String p_175104_1_)
    {
        for (NetworkPlayerInfo networkplayerinfo : this.getPlayerInfoMapGetter().values())
        {
            if (networkplayerinfo.getGameProfile().getName().equals(p_175104_1_))
            {
                return networkplayerinfo;
            }
        }

        return null;
    }

    public GameProfile getGameProfile()
    {
        return this.getProfile();
    }
	
}
