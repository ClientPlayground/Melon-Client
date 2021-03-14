package me.kaimson.melonclient.ingames.utils.ReplayMod.recording;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.replay.ZipReplayFile;
import com.replaymod.replaystudio.studio.ReplayStudio;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import me.kaimson.melonclient.Client;
import me.kaimson.melonclient.Events.EventHandler;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.ServerEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S05PacketSpawnPosition;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraft.network.play.server.S0APacketUseBed;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S19PacketEntityHeadLook;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S49PacketUpdateEntityNBT;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;

public class ConnectionEventHandler {
  private static final String packetHandlerKey = "packet_handler";
  
  private static final String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";
  
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
  
  private final Minecraft mc = Minecraft.getMinecraft();
  
  private PacketListener packetListener;
  
  private static RecordingEventHandler recordingEventHandler;
  
  public static RecordingEventHandler getRecordingEventHandler() {
    return recordingEventHandler;
  }
  
  public ConnectionEventHandler() {
    EventHandler.register(this);
    Client.log("Registered connection handler successfully!");
  }
  
  public void onConnectedToServer(NetworkManager networkManager) {
    try {
      String worldName;
      boolean local = networkManager.isLocalChannel();
      if (local) {
        if (this.mc.getIntegratedServer().getEntityWorld().getWorldType() == WorldType.DEBUG_WORLD) {
          Client.log("Debug World recording is not supported.");
          return;
        } 
        if (!((Boolean)Client.replayCore.getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.RECORD_SINGLEPLAYER)).booleanValue()) {
          Client.log("Singleplayer Recording is disabled");
          return;
        } 
      } else if (!((Boolean)Client.replayCore.getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.RECORD_SERVER)).booleanValue()) {
        Client.log("Multiplayer Recording is disabled");
        return;
      } 
      if (local) {
        worldName = this.mc.getIntegratedServer().getWorldName();
      } else if (Minecraft.getMinecraft().getCurrentServerData() != null) {
        worldName = (Minecraft.getMinecraft().getCurrentServerData()).serverIP;
      } else {
        Client.log("Recording not started as the world is neither local nor remote (probably a replay).");
        return;
      } 
      File folder = Client.replayCore.getReplayFolder();
      String name = sdf.format(Calendar.getInstance().getTime());
      File currentFile = new File(folder, Utils.replayNameToFileName(name));
      ZipReplayFile zipReplayFile = new ZipReplayFile((Studio)new ReplayStudio(), currentFile);
      ReplayMetaData metaData = new ReplayMetaData();
      metaData.setSingleplayer(local);
      metaData.setServerName(worldName);
      metaData.setGenerator("ReplayMod v" + Client.replayCore.getVersion());
      metaData.setDate(System.currentTimeMillis());
      metaData.setMcVersion(ReplayCore.getMinecraftVersion());
      this.packetListener = new PacketListener((ReplayFile)zipReplayFile, metaData);
      networkManager.channel.pipeline().addBefore("packet_handler", "replay_recorder", (ChannelHandler)this.packetListener);
      EntityPlayerSP playerIn = this.mc.thePlayer;
      this.packetListener.save((Packet<?>)new S01PacketJoinGame(playerIn.getEntityId(), this.mc.playerController.getCurrentGameType(), this.mc.theWorld.getWorldInfo().isHardcoreModeEnabled(), this.mc.theWorld.provider.getDimensionId(), this.mc.theWorld.getDifficulty(), 8, this.mc.theWorld.getWorldInfo().getTerrainType(), this.mc.theWorld.getGameRules().hasRule("reducedDebugInfo")));
      this.packetListener.save((Packet<?>)new S3FPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString("vanilla")));
      this.packetListener.save((Packet<?>)new S05PacketSpawnPosition(this.mc.theWorld.getSpawnPoint()));
      this.packetListener.save((Packet<?>)new S39PacketPlayerAbilities(playerIn.capabilities));
      this.packetListener.save((Packet<?>)new S09PacketHeldItemChange(playerIn.inventory.currentItem));
      loadChunks(this.packetListener);
      this.packetListener.save((Packet<?>)new S08PacketPlayerPosLook(playerIn.posX, playerIn.posY, playerIn.posZ, playerIn.rotationYaw, playerIn.rotationPitch, Collections.emptySet()));
      LinkedList<Packet<?>> packets = new LinkedList();
      saveEntityPackets(this.packetListener, (Entity)this.mc.thePlayer, packets);
      saveSpecificPackets(this.packetListener, packets);
      for (Object<Packet<?>> localPacket = (Object<Packet<?>>)packets.iterator(); ((Iterator)localPacket).hasNext(); ) {
        Packet<?> packet = ((Iterator)localPacket).next();
        this.packetListener.save(packet);
        Client.log("Saving packet: " + packet);
      } 
      saveScoreboard(this.mc.theWorld.getScoreboard());
      recordingEventHandler = new RecordingEventHandler(this.packetListener);
      recordingEventHandler.register();
      Client.sendChatMessage(ChatFormatting.GRAY + "[" + ChatFormatting.GOLD + "ReplayMod" + ChatFormatting.GRAY + "] " + ChatFormatting.GREEN + "Recording started!");
    } catch (IllegalArgumentException e) {
      Client.warn("Ignoring " + e.getMessage(), new Object[0]);
    } catch (IOException e) {
      e.printStackTrace();
      Client.sendChatMessage(ChatFormatting.GRAY + "[" + ChatFormatting.GOLD + "ReplayMod" + ChatFormatting.GRAY + "] " + ChatFormatting.RED + "Recording failed!");
    } 
  }
  
  private void loadChunks(PacketListener packetListener) {
    List<Chunk> chunks = (Minecraft.getMinecraft()).theWorld.clientChunkProvider.chunkListing;
    packetListener.save((Packet<?>)new S26PacketMapChunkBulk(chunks));
    for (Chunk chunk : chunks) {
      for (TileEntity tileEntity : chunk.getTileEntityMap().values()) {
        Packet<?> packet = tileEntity.getDescriptionPacket();
        if (packet != null)
          packetListener.save(packet); 
      } 
    } 
  }
  
  private void saveEntityPackets(PacketListener packetListener, Entity entityIn, List<Packet<?>> packets) {
    if (entityIn instanceof EntityPlayer) {
      S38PacketPlayerListItem packetPlayerListItem = new S38PacketPlayerListItem();
      packetPlayerListItem.action = S38PacketPlayerListItem.Action.ADD_PLAYER;
      if (entityIn instanceof EntityPlayerMP) {
        EntityPlayerMP entity = (EntityPlayerMP)entityIn;
        packetPlayerListItem.getEntries().add(new S38PacketPlayerListItem.AddPlayerData(entity.getGameProfile(), entity.ping, entity.theItemInWorldManager.getGameType(), entity.getDisplayName()));
      } else {
        packetPlayerListItem.getEntries().add(new S38PacketPlayerListItem.AddPlayerData(((EntityPlayer)entityIn).getGameProfile(), 1, WorldSettings.GameType.CREATIVE, entityIn.getDisplayName()));
      } 
      packetListener.save((Packet<?>)packetPlayerListItem);
    } 
    Packet<?> packet = getAddEntityPacket(entityIn);
    if (packet != null)
      packetListener.save(packet); 
    if (entityIn.getDataWatcher().getIsBlank())
      packetListener.save((Packet<?>)new S1CPacketEntityMetadata(entityIn.getEntityId(), entityIn.getDataWatcher(), true)); 
    NBTTagCompound nbtTagCompound = entityIn.getNBTTagCompound();
    if (nbtTagCompound != null)
      packetListener.save((Packet<?>)new S49PacketUpdateEntityNBT(entityIn.getEntityId(), nbtTagCompound)); 
    if (packet instanceof net.minecraft.network.play.server.S0FPacketSpawnMob || entityIn.ridingEntity != null)
      if (packets != null) {
        packets.add(new S1BPacketEntityAttach(0, entityIn, entityIn.ridingEntity));
      } else {
        packetListener.save((Packet<?>)new S1BPacketEntityAttach(0, entityIn, entityIn.ridingEntity));
      }  
    if (entityIn instanceof EntityLiving && ((EntityLiving)entityIn).getLeashedToEntity() != null)
      if (packets != null) {
        packets.add(new S1BPacketEntityAttach(1, entityIn, ((EntityLiving)entityIn).getLeashedToEntity()));
      } else {
        packetListener.save((Packet<?>)new S1BPacketEntityAttach(1, entityIn, ((EntityLiving)entityIn).getLeashedToEntity()));
      }  
    if (entityIn instanceof EntityLivingBase)
      for (int i = 0; i < 5; i++) {
        Object stack = ((EntityLivingBase)entityIn).getEquipmentInSlot(i);
        if (stack != null)
          packetListener.save((Packet<?>)new S04PacketEntityEquipment(entityIn.getEntityId(), i, (ItemStack)stack)); 
      }  
    if (entityIn instanceof EntityPlayer) {
      EntityPlayer entityPlayer = (EntityPlayer)entityIn;
      if (entityPlayer.isPlayerSleeping())
        packetListener.save((Packet<?>)new S0APacketUseBed(entityPlayer, new BlockPos(entityIn))); 
    } 
    if (entityIn instanceof EntityLivingBase) {
      EntityLivingBase entityLivingBase = (EntityLivingBase)entityIn;
      for (Object stack = entityLivingBase.getActivePotionEffects().iterator(); ((Iterator)stack).hasNext(); ) {
        PotionEffect localPotionEffect = ((Iterator<PotionEffect>)stack).next();
        packetListener.save((Packet<?>)new S1DPacketEntityEffect(entityIn.getEntityId(), localPotionEffect));
      } 
    } 
    if (entityIn instanceof EntityPlayer) {
      int j = MathHelper.floor_float(entityIn.rotationYaw * 256.0F / 360.0F);
      Object stack = new S19PacketEntityHeadLook(entityIn, (byte)j);
      packetListener.save((Packet)stack);
    } 
  }
  
  private void saveSpecificPackets(PacketListener packetListener, List<Packet<?>> packets) {
    List<Entity> localList = (Minecraft.getMinecraft()).theWorld.loadedEntityList;
    for (Entity localEntity : localList) {
      if (!(localEntity instanceof EntityPlayerSP)) {
        Client.log("Saving entity packets: " + localEntity.getClass().getName());
        saveEntityPackets(packetListener, localEntity, packets);
      } 
    } 
  }
  
  private Packet<?> getAddEntityPacket(Entity entity) {
    if (entity instanceof EntityPlayer)
      return (Packet<?>)new S0CPacketSpawnPlayer((EntityPlayer)entity); 
    return null;
  }
  
  private void saveScoreboard(Scoreboard scoreboard) {
    HashSet<ScoreObjective> localHashSet = Sets.newHashSet();
    for (ScorePlayerTeam scorePlayerTeam : scoreboard.getTeams()) {
      if (scorePlayerTeam.getChatFormat() != null)
        this.packetListener.save((Packet<?>)new S3EPacketTeams(scorePlayerTeam, 0)); 
    } 
    for (int i = 0; i < 19; i++) {
      ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(i);
      if (scoreObjective != null && !localHashSet.contains(scoreObjective)) {
        for (Packet<?> localPacket : getScoreboardObjectives(scoreboard, scoreObjective))
          this.packetListener.save(localPacket); 
        localHashSet.add(scoreObjective);
      } 
    } 
  }
  
  private List<Packet<?>> getScoreboardObjectives(Scoreboard scoreboard, ScoreObjective scoreObjective) {
    ArrayList<S3BPacketScoreboardObjective> localArrayList = Lists.newArrayList();
    localArrayList.add(new S3BPacketScoreboardObjective(scoreObjective, 0));
    for (int i = 0; i < 19; i++) {
      if (scoreboard.getObjectiveInDisplaySlot(i) == scoreObjective)
        localArrayList.add(new S3DPacketDisplayScoreboard(i, scoreObjective)); 
    } 
    for (Score localaum : scoreboard.getSortedScores(scoreObjective))
      localArrayList.add(new S3CPacketUpdateScore(localaum)); 
    return (List)localArrayList;
  }
  
  @TypeEvent
  public void onServerLeave(ServerEvent.Leave e) {
    stopRecording();
  }
  
  public void stopRecording() {
    if (this.packetListener != null) {
      recordingEventHandler.unregister();
      recordingEventHandler = null;
      this.packetListener = null;
    } 
  }
}
