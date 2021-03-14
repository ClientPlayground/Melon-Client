package me.kaimson.melonclient.ingames.utils.ReplayMod.viewer;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.replay.ReplayFile;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import me.kaimson.melonclient.Events.TypeEvent;
import me.kaimson.melonclient.Events.imp.TickEvent;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.ReplayCore;
import me.kaimson.melonclient.ingames.utils.ReplayMod.core.SettingsRegistry;
import me.kaimson.melonclient.ingames.utils.ReplayMod.viewer.camera.CameraEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S0EPacketSpawnObject;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

@Sharable
public class FullReplaySender extends ChannelDuplexHandler implements ReplaySender {
  private static final List<Class> BAD_PACKETS = Arrays.asList(new Class[] { S39PacketPlayerAbilities.class });
  
  private static int TP_DISTANCE_LIMIT = 128;
  
  private final ReplayHandler replayHandler;
  
  protected boolean asyncMode;
  
  protected int lastTimeStamp;
  
  protected int currentTimeStamp;
  
  protected ReplayFile replayFile;
  
  protected ChannelHandlerContext ctx;
  
  protected ReplayInputStream replayIn;
  
  protected PacketData nextPacket;
  
  private boolean loginPhase;
  
  protected boolean startFromBeginning = true;
  
  protected boolean terminate;
  
  protected double replaySpeed = 1.0D;
  
  protected boolean hasWorldLoaded;
  
  protected Minecraft mc = Minecraft.getMinecraft();
  
  protected final int replayLength;
  
  protected int actualID = -1;
  
  protected boolean allowMovement;
  
  private final File tempResourcePackFolder = Files.createTempDir();
  
  private final EventHandler events = new EventHandler();
  
  private long lastPacketSent;
  
  private long desiredTimeStamp;
  
  private Runnable asyncSender;
  
  public void setAsyncMode(boolean asyncMode) {
    if (this.asyncMode == asyncMode)
      return; 
    this.asyncMode = asyncMode;
    if (asyncMode) {
      this.terminate = false;
      (new Thread(this.asyncSender, "replaymod-async-sender")).start();
    } else {
      this.terminate = true;
    } 
  }
  
  public boolean isAsyncMode() {
    return this.asyncMode;
  }
  
  public void setSyncModeAndWait() {
    if (!this.asyncMode)
      return; 
    this.asyncMode = false;
    this.terminate = true;
    synchronized (this) {
    
    } 
  }
  
  public int currentTimeStamp() {
    if (this.asyncMode) {
      int timePassed = (int)(System.currentTimeMillis() - this.lastPacketSent);
      return this.lastTimeStamp + (int)(timePassed * getReplaySpeed());
    } 
    return this.lastTimeStamp;
  }
  
  public void terminateReplay() {
    if (this.terminate)
      return; 
    this.terminate = true;
    me.kaimson.melonclient.Events.EventHandler.unregister(this);
    try {
      channelInactive(this.ctx);
      this.ctx.channel().pipeline().close();
      FileUtils.deleteDirectory(this.tempResourcePackFolder);
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  private class EventHandler {
    private EventHandler() {}
    
    @TypeEvent
    public void onWorldTick(TickEvent.ClientTick event) {
      if (event.phase != TickEvent.Phase.START)
        return; 
      if (FullReplaySender.this.mc.theWorld != null)
        for (EntityPlayer playerEntity : FullReplaySender.this.mc.theWorld.playerEntities) {
          if (playerEntity instanceof net.minecraft.client.entity.EntityOtherPlayerMP)
            playerEntity.onUpdate(); 
        }  
    }
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (this.terminate && this.asyncMode)
      return; 
    if (msg instanceof Packet)
      super.channelRead(ctx, msg); 
    if (msg instanceof byte[])
      try {
        Packet<?> p = deserializePacket((byte[])msg);
        if (p != null) {
          p = processPacket(p);
          if (p != null)
            super.channelRead(ctx, p); 
          if (!this.asyncMode && this.mc.theWorld != null && (
            p instanceof net.minecraft.network.play.server.S0CPacketSpawnPlayer || p instanceof S0EPacketSpawnObject || p instanceof net.minecraft.network.play.server.S0FPacketSpawnMob || p instanceof net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity || p instanceof net.minecraft.network.play.server.S10PacketSpawnPainting || p instanceof net.minecraft.network.play.server.S11PacketSpawnExperienceOrb || p instanceof net.minecraft.network.play.server.S13PacketDestroyEntities)) {
            WorldClient worldClient = this.mc.theWorld;
            Iterator<Entity> iter = ((World)worldClient).loadedEntityList.iterator();
            while (iter.hasNext()) {
              Entity entity = iter.next();
              if (entity.isDead) {
                int chunkX = entity.chunkCoordX;
                int chunkY = entity.chunkCoordZ;
                if (entity.addedToChunk && worldClient.getChunkProvider().chunkExists(chunkX, chunkY))
                  worldClient.getChunkFromChunkCoords(chunkX, chunkY).removeEntity(entity); 
                iter.remove();
                worldClient.onEntityRemoved(entity);
              } 
            } 
          } 
        } 
      } catch (Exception e) {
        e.printStackTrace();
      }  
  }
  
  private Packet<?> deserializePacket(byte[] bytes) throws IOException, IllegalAccessException, InstantiationException {
    ByteBuf bb = Unpooled.wrappedBuffer(bytes);
    PacketBuffer pb = new PacketBuffer(bb);
    int i = pb.readVarIntFromBuffer();
    EnumConnectionState state = this.loginPhase ? EnumConnectionState.LOGIN : EnumConnectionState.PLAY;
    Packet<?> p = state.getPacket(EnumPacketDirection.CLIENTBOUND, i);
    p.readPacketData(pb);
    return p;
  }
  
  protected Packet<?> processPacket(Packet<?> p) throws Exception {
    S01PacketJoinGame s01PacketJoinGame;
    S07PacketRespawn s07PacketRespawn;
    if (p instanceof net.minecraft.network.login.server.S02PacketLoginSuccess) {
      this.loginPhase = false;
      return p;
    } 
    if (p instanceof S3FPacketCustomPayload) {
      S3FPacketCustomPayload packet = (S3FPacketCustomPayload)p;
      if ("Replay|Restrict".equals(packet.getChannelName())) {
        String unknown = this.replayHandler.getRestrictions().handle(packet);
        if (unknown == null)
          return null; 
        terminateReplay();
        ReplayCore.getInstance().runLater(() -> {
              try {
                this.replayHandler.endReplay();
              } catch (IOException e) {
                e.printStackTrace();
              } 
            });
      } 
    } 
    if (p instanceof S40PacketDisconnect) {
      IChatComponent reason = ((S40PacketDisconnect)p).getReason();
      String message = reason.getUnformattedText();
      if ("Please update to view this replay.".equals(message))
        return null; 
    } 
    if (BAD_PACKETS.contains(p.getClass()))
      return null; 
    if (p instanceof S3FPacketCustomPayload) {
      S3FPacketCustomPayload packet = (S3FPacketCustomPayload)p;
      if ("MC|BOpen".equals(packet.getChannelName()))
        return null; 
    } 
    if (p instanceof S48PacketResourcePackSend) {
      S48PacketResourcePackSend packet = (S48PacketResourcePackSend)p;
      String url = packet.getURL();
      if (url.startsWith("replay://")) {
        int id = Integer.parseInt(url.substring("replay://".length()));
        Map<Integer, String> index = this.replayFile.getResourcePackIndex();
        if (index != null) {
          String hash = index.get(Integer.valueOf(id));
          if (hash != null) {
            File file = new File(this.tempResourcePackFolder, hash + ".zip");
            if (!file.exists())
              IOUtils.copy((InputStream)this.replayFile.getResourcePack(hash).get(), new FileOutputStream(file)); 
            this.mc.getResourcePackRepository().setResourcePackInstance(file);
          } 
        } 
        return null;
      } 
    } 
    if (p instanceof S01PacketJoinGame) {
      S01PacketJoinGame packet = (S01PacketJoinGame)p;
      int entId = packet.getEntityId();
      this.allowMovement = true;
      this.actualID = entId;
      entId = -1789435;
      int dimension = packet.getDimension();
      EnumDifficulty difficulty = packet.getDifficulty();
      int maxPlayers = packet.getMaxPlayers();
      WorldType worldType = packet.getWorldType();
      s01PacketJoinGame = new S01PacketJoinGame(entId, WorldSettings.GameType.SPECTATOR, false, dimension, difficulty, maxPlayers, worldType, false);
    } 
    if (s01PacketJoinGame instanceof S07PacketRespawn) {
      S07PacketRespawn respawn = (S07PacketRespawn)s01PacketJoinGame;
      s07PacketRespawn = new S07PacketRespawn(respawn.getDimensionID(), respawn.getDifficulty(), respawn.getWorldType(), WorldSettings.GameType.SPECTATOR);
      this.allowMovement = true;
    } 
    if (s07PacketRespawn instanceof S08PacketPlayerPosLook) {
      final S08PacketPlayerPosLook ppl = (S08PacketPlayerPosLook)s07PacketRespawn;
      if (!this.hasWorldLoaded)
        this.hasWorldLoaded = true; 
      if (this.mc.currentScreen instanceof net.minecraft.client.gui.GuiDownloadTerrain)
        this.mc.displayGuiScreen(null); 
      if (this.replayHandler.shouldSuppressCameraMovements())
        return null; 
      CameraEntity cent = this.replayHandler.getCameraEntity();
      for (Object relative : ppl.func_179834_f()) {
        if (relative == S08PacketPlayerPosLook.EnumFlags.X || relative == S08PacketPlayerPosLook.EnumFlags.Y || relative == S08PacketPlayerPosLook.EnumFlags.Z)
          return null; 
      } 
      if (cent != null) {
        if (!this.allowMovement && Math.abs(cent.posX - ppl.getX()) <= TP_DISTANCE_LIMIT && 
          Math.abs(cent.posZ - ppl.getZ()) <= TP_DISTANCE_LIMIT)
          return null; 
        this.allowMovement = false;
      } 
      (new Runnable() {
          public void run() {
            if (FullReplaySender.this.mc.theWorld == null || !FullReplaySender.this.mc.isCallingFromMinecraftThread()) {
              ReplayCore.getInstance().runLater(this);
              return;
            } 
            CameraEntity cent = FullReplaySender.this.replayHandler.getCameraEntity();
            cent.setCameraPosition(ppl.getX(), ppl.getY(), ppl.getZ());
          }
        }).run();
    } 
    if (s07PacketRespawn instanceof S2BPacketChangeGameState) {
      S2BPacketChangeGameState pg = (S2BPacketChangeGameState)s07PacketRespawn;
      int reason = pg.getGameState();
      if (reason != 1 && reason != 2 && reason != 7 && reason != 8)
        return null; 
    } 
    if (s07PacketRespawn instanceof net.minecraft.network.play.server.S02PacketChat && 
      !((Boolean)ReplayCore.getInstance().getSettingsRegistry().get((SettingsRegistry.SettingKey)Setting.SHOW_CHAT)).booleanValue())
      return null; 
    return this.asyncMode ? processPacketAsync((Packet)s07PacketRespawn) : processPacketSync((Packet)s07PacketRespawn);
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
    super.channelActive(ctx);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    promise.setSuccess();
  }
  
  public void flush(ChannelHandlerContext ctx) throws Exception {}
  
  public double getReplaySpeed() {
    if (!paused())
      return this.replaySpeed; 
    return 0.0D;
  }
  
  public void setReplaySpeed(double d) {
    if (d != 0.0D)
      this.replaySpeed = d; 
    this.mc.timer.timerSpeed = (float)d;
  }
  
  public FullReplaySender(ReplayHandler replayHandler, ReplayFile file, boolean asyncMode) throws IOException {
    this.desiredTimeStamp = -1L;
    this.asyncSender = new Runnable() {
        public void run() {
          try {
            while (FullReplaySender.this.ctx == null && !FullReplaySender.this.terminate)
              Thread.sleep(10L); 
            while (!FullReplaySender.this.terminate) {
              synchronized (FullReplaySender.this) {
                if (FullReplaySender.this.replayIn == null)
                  FullReplaySender.this.replayIn = FullReplaySender.this.replayFile.getPacketData(ReplayCore.getInstance().getPacketTypeRegistry(true)); 
                label62: while (true) {
                  try {
                    if (FullReplaySender.this.paused() && FullReplaySender.this.hasWorldLoaded)
                      if (!FullReplaySender.this.terminate && !FullReplaySender.this.startFromBeginning && FullReplaySender.this.desiredTimeStamp == -1L) {
                        Thread.sleep(10L);
                        continue;
                      }  
                    if (FullReplaySender.this.terminate)
                      break; 
                    if (!FullReplaySender.this.startFromBeginning) {
                      if (FullReplaySender.this.nextPacket == null)
                        FullReplaySender.this.nextPacket = new FullReplaySender.PacketData(FullReplaySender.this.replayIn, FullReplaySender.this.loginPhase); 
                      int nextTimeStamp = FullReplaySender.this.nextPacket.timestamp;
                      if (!FullReplaySender.this.isHurrying() && FullReplaySender.this.hasWorldLoaded) {
                        int timeWait = (int)Math.round((nextTimeStamp - FullReplaySender.this.lastTimeStamp) / FullReplaySender.this.replaySpeed);
                        long timeDiff = System.currentTimeMillis() - FullReplaySender.this.lastPacketSent;
                        long timeToSleep = Math.max(0L, timeWait - timeDiff);
                        Thread.sleep(timeToSleep);
                        FullReplaySender.this.lastPacketSent = System.currentTimeMillis();
                      } 
                      FullReplaySender.this.channelRead(FullReplaySender.this.ctx, FullReplaySender.this.nextPacket.bytes);
                      FullReplaySender.this.nextPacket = null;
                      FullReplaySender.this.lastTimeStamp = nextTimeStamp;
                      if (FullReplaySender.this.isHurrying() && FullReplaySender.this.lastTimeStamp > FullReplaySender.this.desiredTimeStamp && !FullReplaySender.this.startFromBeginning) {
                        FullReplaySender.this.desiredTimeStamp = -1L;
                        FullReplaySender.this.replayHandler.moveCameraToTargetPosition();
                        FullReplaySender.this.setReplaySpeed(0.0D);
                      } 
                      continue;
                    } 
                  } catch (EOFException eof) {
                    FullReplaySender.this.setReplaySpeed(0.0D);
                    while (FullReplaySender.this.paused() && FullReplaySender.this.hasWorldLoaded && FullReplaySender.this.desiredTimeStamp == -1L && !FullReplaySender.this.terminate)
                      Thread.sleep(10L); 
                    if (FullReplaySender.this.terminate)
                      break; 
                  } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                  } 
                  FullReplaySender.this.hasWorldLoaded = false;
                  FullReplaySender.this.lastTimeStamp = 0;
                  FullReplaySender.this.loginPhase = true;
                  FullReplaySender.this.startFromBeginning = false;
                  FullReplaySender.this.nextPacket = null;
                  FullReplaySender.this.lastPacketSent = System.currentTimeMillis();
                  if (FullReplaySender.this.replayIn != null) {
                    FullReplaySender.this.replayIn.close();
                    FullReplaySender.this.replayIn = null;
                    break label62;
                  } 
                } 
              } 
            } 
          } catch (Exception e) {
            e.printStackTrace();
          } 
        }
      };
    this.replayHandler = replayHandler;
    this.replayFile = file;
    this.asyncMode = asyncMode;
    this.replayLength = file.getMetaData().getDuration();
    me.kaimson.melonclient.Events.EventHandler.register(this);
    if (asyncMode)
      (new Thread(this.asyncSender, "replaymod-async-sender")).start(); 
  }
  
  public boolean isHurrying() {
    return (this.desiredTimeStamp != -1L);
  }
  
  public void stopHurrying() {
    this.desiredTimeStamp = -1L;
  }
  
  public long getDesiredTimestamp() {
    return this.desiredTimeStamp;
  }
  
  public void jumpToTime(int millis) {
    Preconditions.checkState(this.asyncMode, "Can only jump in async mode. Use sendPacketsTill(int) instead.");
    if (millis < this.lastTimeStamp && !isHurrying())
      this.startFromBeginning = true; 
    this.desiredTimeStamp = millis;
  }
  
  protected Packet processPacketAsync(Packet p) {
    if (this.desiredTimeStamp - this.lastTimeStamp > 1000L) {
      if (p instanceof net.minecraft.network.play.server.S2APacketParticles)
        return null; 
      if (p instanceof S0EPacketSpawnObject) {
        S0EPacketSpawnObject pso = (S0EPacketSpawnObject)p;
        int type = pso.getType();
        if (type == 76)
          return null; 
      } 
    } 
    return p;
  }
  
  public void sendPacketsTill(int timestamp) {
    Preconditions.checkState(!this.asyncMode, "This method cannot be used in async mode. Use jumpToTime(int) instead.");
    try {
      while (this.ctx == null && !this.terminate)
        Thread.sleep(10L); 
      synchronized (this) {
        if (timestamp == this.lastTimeStamp)
          return; 
        if (timestamp < this.lastTimeStamp) {
          this.hasWorldLoaded = false;
          this.lastTimeStamp = 0;
          if (this.replayIn != null) {
            this.replayIn.close();
            this.replayIn = null;
          } 
          this.loginPhase = true;
          this.startFromBeginning = false;
          this.nextPacket = null;
          this.replayHandler.restartedReplay();
        } 
        if (this.replayIn == null)
          this.replayIn = this.replayFile.getPacketData(ReplayCore.getInstance().getPacketTypeRegistry(true)); 
        while (true) {
          try {
            PacketData pd;
            if (this.nextPacket != null) {
              pd = this.nextPacket;
              this.nextPacket = null;
            } else {
              pd = new PacketData(this.replayIn, this.loginPhase);
            } 
            int nextTimeStamp = pd.timestamp;
            if (nextTimeStamp > timestamp) {
              this.nextPacket = pd;
              break;
            } 
            channelRead(this.ctx, pd.bytes);
          } catch (EOFException eof) {
            this.replayIn = null;
            break;
          } catch (IOException e) {
            e.printStackTrace();
          } 
        } 
        this.lastPacketSent = System.currentTimeMillis();
        this.lastTimeStamp = timestamp;
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  protected Packet processPacketSync(Packet p) {
    if (p instanceof S21PacketChunkData) {
      S21PacketChunkData packet = (S21PacketChunkData)p;
      if (packet.getExtractedSize() == 0) {
        int x = packet.getChunkX();
        int z = packet.getChunkZ();
        WorldClient worldClient = this.mc.theWorld;
        IChunkProvider chunkProvider = worldClient.getChunkProvider();
        Chunk chunk = chunkProvider.provideChunk(x, z);
        if (!chunk.isEmpty()) {
          List<Entity> entitiesInChunk = new ArrayList<>();
          for (ClassInheritanceMultiMap classInheritanceMultiMap : chunk.getEntityLists())
            entitiesInChunk.addAll((Collection<? extends Entity>)classInheritanceMultiMap); 
          for (Entity entity : entitiesInChunk) {
            for (int i = 0; i < 4; i++) {
              entity.onUpdate();
              int chunkX = MathHelper.floor_double(entity.posX / 16.0D);
              int chunkZ = MathHelper.floor_double(entity.posZ / 16.0D);
              if (entity.chunkCoordX != chunkX || entity.chunkCoordZ != chunkZ) {
                chunk.removeEntityAtIndex(entity, entity.chunkCoordY);
                Chunk newChunk = chunkProvider.chunkExists(chunkX, chunkZ) ? chunkProvider.provideChunk(chunkX, chunkZ) : null;
                if (newChunk != null) {
                  newChunk.addEntity(entity);
                } else {
                  entity.addedToChunk = false;
                } 
              } 
            } 
          } 
        } 
      } 
    } 
    return p;
  }
  
  private static final class PacketData {
    private static final ByteBuf byteBuf = Unpooled.buffer();
    
    private static final NetOutput netOutput = (NetOutput)new ByteBufNetOutput(byteBuf);
    
    private final int timestamp;
    
    private final byte[] bytes;
    
    PacketData(ReplayInputStream in, boolean loginPhase) throws IOException {
      com.replaymod.replaystudio.PacketData data = in.readPacket();
      if (data == null)
        throw new EOFException(); 
      this.timestamp = (int)data.getTime();
      Packet packet = data.getPacket();
      synchronized (byteBuf) {
        byteBuf.markReaderIndex();
        byteBuf.markWriterIndex();
        netOutput.writeVarInt(packet.getId());
        int idSize = byteBuf.readableBytes();
        int contentSize = packet.getBuf().readableBytes();
        this.bytes = new byte[idSize + contentSize];
        byteBuf.readBytes(this.bytes, 0, idSize);
        packet.getBuf().readBytes(this.bytes, idSize, contentSize);
        byteBuf.resetReaderIndex();
        byteBuf.resetWriterIndex();
      } 
      packet.getBuf().release();
    }
  }
}
