package me.kaimson.melonclient.ingames.utils.ReplayMod.recording;

import com.github.steveice10.netty.buffer.Unpooled;
import com.google.common.base.Supplier;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.kaimson.melonclient.Client;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketListener extends ChannelInboundHandlerAdapter {
  private static final Minecraft mc = Minecraft.getMinecraft();
  
  private static final Logger logger = LogManager.getLogger();
  
  private final ReplayFile replayFile;
  
  private final ResourcePackRecorder resourcePackRecorder;
  
  private final ExecutorService saveService = Executors.newSingleThreadExecutor();
  
  private final ReplayOutputStream packetOutputStream;
  
  private final ReplayMetaData metaData;
  
  private ChannelHandlerContext context = null;
  
  private final long startTime;
  
  private long lastSentPacket;
  
  private long timePassedWhilePaused;
  
  private volatile boolean serverWasPaused;
  
  private boolean loginPhase;
  
  private final AtomicInteger lastSaveMetaDataId = new AtomicInteger();
  
  public PacketListener(ReplayFile replayFile, ReplayMetaData metaData) throws IOException {
    this.replayFile = replayFile;
    this.loginPhase = false;
    this.metaData = metaData;
    this.resourcePackRecorder = new ResourcePackRecorder(replayFile);
    this.packetOutputStream = new ReplayOutputStream((OutputStream)replayFile.writePacketData());
    this.startTime = metaData.getDate();
    saveMetaData();
  }
  
  private void saveMetaData() {
    int id = this.lastSaveMetaDataId.incrementAndGet();
    this.saveService.submit(() -> {
          if (this.lastSaveMetaDataId.get() != id)
            return; 
          try {
            synchronized (this.replayFile) {
              this.replayFile.writeMetaData(Client.replayCore.getPacketTypeRegistry(true), this.metaData);
            } 
          } catch (Exception e) {
            e.printStackTrace();
            logger.error("Writing metadata:", e);
          } 
        });
  }
  
  public void save(Packet<?> packet) {
    try {
      if (packet instanceof S0CPacketSpawnPlayer) {
        UUID uuid = ((S0CPacketSpawnPlayer)packet).getPlayer();
        Set<String> uuids = new HashSet<>(Arrays.asList(this.metaData.getPlayers()));
        uuids.add(uuid.toString());
        this.metaData.setPlayers(uuids.<String>toArray(new String[uuids.size()]));
        saveMetaData();
      } 
      if (packet instanceof net.minecraft.network.login.server.S03PacketEnableCompression)
        return; 
      if (packet instanceof net.minecraft.network.play.server.S46PacketSetCompressionLevel)
        return; 
      long now = System.currentTimeMillis();
      if (this.serverWasPaused) {
        this.timePassedWhilePaused = now - this.startTime - this.lastSentPacket;
        this.serverWasPaused = false;
      } 
      int timestamp = (int)(now - this.startTime - this.timePassedWhilePaused);
      this.lastSentPacket = timestamp;
      PacketData packetData = getPacketData(timestamp, packet);
      this.saveService.submit(() -> {
            try {
              this.packetOutputStream.write(packetData);
            } catch (IOException e) {
              throw new RuntimeException(e);
            } 
          });
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Writing packet:", e);
    } 
  }
  
  public void channelInactive(ChannelHandlerContext ctx) {
    this.metaData.setDuration((int)this.lastSentPacket);
    saveMetaData();
    Client.log("Shutting down packet listener saver...");
    this.saveService.shutdown();
    try {
      this.saveService.awaitTermination(10L, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.error("Waiting for save service termination:", e);
    } 
    synchronized (this.replayFile) {
      try {
        Client.log("Saving replay...");
        this.replayFile.save();
        this.replayFile.close();
        Client.log("Replay saved!");
      } catch (IOException e) {
        logger.error("Saving replay file:", e);
      } 
    } 
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (ctx == null) {
      if (this.context == null)
        return; 
      ctx = this.context;
    } 
    this.context = ctx;
    if (msg instanceof Packet)
      try {
        Packet<?> packet = (Packet)msg;
        if (packet instanceof S0DPacketCollectItem)
          if (mc.thePlayer != null || ((S0DPacketCollectItem)packet)
            .getEntityID() == mc.thePlayer.getEntityId()) {
            super.channelRead(ctx, msg);
            return;
          }  
        if (packet instanceof S48PacketResourcePackSend) {
          save((Packet<?>)this.resourcePackRecorder.handleResourcePack((S48PacketResourcePackSend)packet));
          return;
        } 
        save(packet);
        if (packet instanceof S3FPacketCustomPayload) {
          S3FPacketCustomPayload p = (S3FPacketCustomPayload)packet;
          if ("Replay|Restrict".equals(p.getChannelName())) {
            S40PacketDisconnect s40PacketDisconnect = new S40PacketDisconnect((IChatComponent)new ChatComponentText("Please update to view this replay."));
            save((Packet<?>)s40PacketDisconnect);
          } 
        } 
      } catch (Exception e) {
        logger.error("Handling packet for recording:", e);
      }  
    super.channelRead(ctx, msg);
  }
  
  private PacketData getPacketData(int timestamp, Packet<?> packet) throws IOException {
    PacketData packetData;
    if (packet instanceof S0FPacketSpawnMob) {
      S0FPacketSpawnMob p = (S0FPacketSpawnMob)packet;
      if (p.field_149043_l == null) {
        p.field_149043_l = new DataWatcher(null);
        if (p.func_149027_c() != null)
          for (DataWatcher.WatchableObject wo : p.func_149027_c())
            p.field_149043_l.addObject(wo.getDataValueId(), wo.getObject());  
      } 
    } 
    if (packet instanceof S0CPacketSpawnPlayer) {
      S0CPacketSpawnPlayer p = (S0CPacketSpawnPlayer)packet;
      if (p.watcher == null) {
        p.watcher = new DataWatcher(null);
        if (p.func_148944_c() != null)
          for (DataWatcher.WatchableObject wo : p.func_148944_c())
            p.watcher.addObject(wo.getDataValueId(), wo.getObject());  
      } 
    } 
    Integer packetId = EnumConnectionState.PLAY.getPacketId(EnumPacketDirection.CLIENTBOUND, packet);
    if (packetId == null)
      throw new IOException("Unknown packet type:" + packet.getClass()); 
    ByteBuf byteBuf = Unpooled.buffer();
    try {
      packet.writePacketData(new PacketBuffer(byteBuf));
      packetData = new PacketData(timestamp, new Packet(Client.replayCore.getPacketTypeRegistry(this.loginPhase), packetId.intValue(), Unpooled.wrappedBuffer(byteBuf.array(), byteBuf.arrayOffset(), byteBuf.readableBytes())));
    } finally {
      byteBuf.release();
      if (packet instanceof S3FPacketCustomPayload)
        ((S3FPacketCustomPayload)packet).getBufferData().release(); 
    } 
    return packetData;
  }
  
  public void addMarker() {
    Entity view = Minecraft.getMinecraft().getRenderViewEntity();
    int timestamp = (int)(System.currentTimeMillis() - this.startTime);
    Marker marker = new Marker();
    marker.setTime(timestamp);
    marker.setX(view.posX);
    marker.setY(view.posY);
    marker.setZ(view.posZ);
    marker.setYaw(view.rotationYaw);
    marker.setPitch(view.rotationPitch);
    this.saveService.submit(() -> {
          synchronized (this.replayFile) {
            try {
              Set<Marker> markers = (Set<Marker>)this.replayFile.getMarkers().or(HashSet::new);
              markers.add(marker);
              this.replayFile.writeMarkers(markers);
            } catch (IOException e) {
              logger.error("Writing markers:", e);
            } 
          } 
        });
  }
  
  public void setServerWasPaused() {
    this.serverWasPaused = true;
  }
}
