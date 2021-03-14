package com.replaymod.replaystudio.filter;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.google.gson.JsonObject;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.protocol.packets.PacketBlockChange;
import com.replaymod.replaystudio.protocol.packets.PacketChunkData;
import com.replaymod.replaystudio.protocol.packets.PacketDestroyEntities;
import com.replaymod.replaystudio.protocol.packets.PacketEntityMovement;
import com.replaymod.replaystudio.protocol.packets.PacketMapData;
import com.replaymod.replaystudio.protocol.packets.PacketSetSlot;
import com.replaymod.replaystudio.protocol.packets.PacketTeam;
import com.replaymod.replaystudio.protocol.packets.PacketUpdateLight;
import com.replaymod.replaystudio.protocol.packets.PacketWindowItems;
import com.replaymod.replaystudio.stream.PacketStream;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Triple;
import com.replaymod.replaystudio.util.DPosition;
import com.replaymod.replaystudio.util.IPosition;
import com.replaymod.replaystudio.util.PacketUtils;
import com.replaymod.replaystudio.util.Utils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.MutablePair;

public class SquashFilter implements StreamFilter {
  private static final long POS_MIN = -128L;
  
  private static final long POS_MAX = 127L;
  
  private PacketTypeRegistry registry;
  
  private static class Team {
    private final String name;
    
    private Packet create;
    
    private Packet update;
    
    private Packet remove;
    
    private final Set<String> added = new HashSet<>();
    
    private final Set<String> removed = new HashSet<>();
    
    private Team(String name) {
      this.name = name;
    }
    
    public Team copy() {
      Team copy = new Team(this.name);
      copy.create = (this.create != null) ? this.create.copy() : null;
      copy.update = (this.update != null) ? this.update.copy() : null;
      copy.remove = (this.remove != null) ? this.remove.copy() : null;
      copy.added.addAll(this.added);
      copy.removed.addAll(this.removed);
      return copy;
    }
    
    void release() {
      if (this.create != null) {
        this.create.release();
        this.create = null;
      } 
      if (this.update != null) {
        this.update.release();
        this.update = null;
      } 
      if (this.remove != null) {
        this.remove.release();
        this.remove = null;
      } 
    }
  }
  
  private static class Entity {
    private boolean complete;
    
    private boolean despawned;
    
    private List<PacketData> packets = new ArrayList<>();
    
    private long lastTimestamp = 0L;
    
    private Packet teleport;
    
    private long dx = 0L;
    
    private long dy = 0L;
    
    private long dz = 0L;
    
    private Float yaw = null;
    
    private Float pitch = null;
    
    private boolean onGround = false;
    
    Entity copy() {
      Entity copy = new Entity();
      copy.complete = this.complete;
      copy.despawned = this.despawned;
      this.packets.forEach(it -> copy.packets.add(it.copy()));
      copy.lastTimestamp = this.lastTimestamp;
      copy.teleport = (this.teleport != null) ? this.teleport.copy() : null;
      copy.dx = this.dx;
      copy.dy = this.dy;
      copy.dz = this.dz;
      copy.yaw = this.yaw;
      copy.pitch = this.pitch;
      copy.onGround = this.onGround;
      return copy;
    }
    
    void release() {
      if (this.teleport != null) {
        this.teleport.release();
        this.teleport = null;
      } 
      this.packets.forEach(PacketData::release);
      this.packets.clear();
    }
    
    private Entity() {}
  }
  
  private final List<PacketData> unhandled = new ArrayList<>();
  
  private final Map<Integer, Entity> entities = new HashMap<>();
  
  private final Map<String, Team> teams = new HashMap<>();
  
  private final Map<Integer, PacketData> mainInventoryChanges = new HashMap<>();
  
  private final Map<Integer, Packet> maps = new HashMap<>();
  
  private final List<PacketData> currentWorld = new ArrayList<>();
  
  private final List<PacketData> currentWindow = new ArrayList<>();
  
  private final List<PacketData> closeWindows = new ArrayList<>();
  
  private final Map<PacketType, PacketData> latestOnly = new HashMap<>();
  
  private final Map<Long, ChunkData> chunks = new HashMap<>();
  
  private final Map<Long, Long> unloadedChunks = new HashMap<>();
  
  public SquashFilter copy() {
    SquashFilter copy = new SquashFilter();
    copy.registry = this.registry;
    this.teams.forEach((key, value) -> (Team)copy.teams.put(key, value.copy()));
    this.entities.forEach((key, value) -> (Entity)copy.entities.put(key, value.copy()));
    this.unhandled.forEach(it -> copy.unhandled.add(it.copy()));
    this.mainInventoryChanges.forEach((key, value) -> (PacketData)copy.mainInventoryChanges.put(key, value.copy()));
    this.maps.forEach((key, value) -> (Packet)copy.maps.put(key, value.copy()));
    this.currentWorld.forEach(it -> copy.currentWorld.add(it.copy()));
    this.currentWindow.forEach(it -> copy.currentWindow.add(it.copy()));
    this.closeWindows.forEach(it -> copy.closeWindows.add(it.copy()));
    this.latestOnly.forEach((key, value) -> (PacketData)copy.latestOnly.put(key, value.copy()));
    this.chunks.forEach((key, value) -> (ChunkData)copy.chunks.put(key, value.copy()));
    copy.unloadedChunks.putAll(this.unloadedChunks);
    return copy;
  }
  
  public void release() {
    this.teams.values().forEach(Team::release);
    this.entities.values().forEach(Entity::release);
    this.unhandled.forEach(PacketData::release);
    this.mainInventoryChanges.values().forEach(PacketData::release);
    this.maps.values().forEach(Packet::release);
    this.currentWorld.forEach(PacketData::release);
    this.currentWindow.forEach(PacketData::release);
    this.closeWindows.forEach(PacketData::release);
    this.latestOnly.values().forEach(PacketData::release);
  }
  
  public void onStart(PacketStream stream) {}
  
  public boolean onPacket(PacketStream stream, PacketData data) throws IOException {
    PacketData prev;
    PacketUpdateLight updateLight;
    PacketChunkData chunkData;
    Team team;
    Packet packet1, packet = data.getPacket();
    PacketType type = packet.getType();
    this.registry = packet.getRegistry();
    long lastTimestamp = data.getTime();
    Integer entityId = PacketUtils.getEntityId(packet);
    if (entityId != null) {
      if (entityId.intValue() == -1) {
        for (Iterator<Integer> iterator = PacketUtils.getEntityIds(packet).iterator(); iterator.hasNext(); ) {
          Entity entity;
          int id = ((Integer)iterator.next()).intValue();
          if (type == PacketType.DestroyEntities) {
            entity = this.entities.computeIfAbsent(Integer.valueOf(id), i -> new Entity());
            entity.release();
            entity.despawned = true;
            if (entity.complete)
              this.entities.remove(Integer.valueOf(id)); 
          } else {
            entity = this.entities.compute(Integer.valueOf(id), (i, e) -> (e == null || e.despawned) ? new Entity() : e);
            entity.packets.add(data.retain());
          } 
          entity.lastTimestamp = lastTimestamp;
        } 
      } else {
        Entity entity = this.entities.compute(entityId, (i, e) -> (e == null || e.despawned) ? new Entity() : e);
        if (type == PacketType.EntityMovement || type == PacketType.EntityPosition || type == PacketType.EntityRotation || type == PacketType.EntityPositionRotation) {
          Triple<DPosition, Pair<Float, Float>, Boolean> movement = PacketEntityMovement.getMovement(packet);
          DPosition deltaPos = (DPosition)movement.getFirst();
          Pair<Float, Float> yawPitch = (Pair<Float, Float>)movement.getSecond();
          if (deltaPos != null) {
            Entity entity1 = entity;
            entity1.dx = (long)(entity1.dx + deltaPos.getX() * 32.0D);
            entity1 = entity;
            entity1.dy = (long)(entity1.dy + deltaPos.getY() * 32.0D);
            entity1 = entity;
            entity1.dz = (long)(entity1.dz + deltaPos.getZ() * 32.0D);
          } 
          if (yawPitch != null) {
            entity.yaw = (Float)yawPitch.getKey();
            entity.pitch = (Float)yawPitch.getValue();
          } 
          entity.onGround = ((Boolean)movement.getThird()).booleanValue();
        } else if (type == PacketType.EntityTeleport) {
          if (entity.teleport != null)
            entity.teleport.release(); 
          entity.dx = entity.dy = entity.dz = 0L;
          entity.yaw = entity.pitch = null;
          entity.teleport = packet.retain();
        } else {
          if (PacketUtils.isSpawnEntityPacket(packet))
            entity.complete = true; 
          entity.packets.add(data.retain());
        } 
        entity.lastTimestamp = lastTimestamp;
      } 
      return false;
    } 
    switch (type) {
      case PlayerActionAck:
      case SpawnParticle:
        return false;
      case Respawn:
        this.currentWorld.forEach(PacketData::release);
        this.currentWorld.clear();
        this.chunks.clear();
        this.unloadedChunks.clear();
        this.currentWindow.forEach(PacketData::release);
        this.currentWindow.clear();
        this.entities.values().forEach(Entity::release);
        this.entities.clear();
      case JoinGame:
      case SetExperience:
      case PlayerAbilities:
      case Difficulty:
      case UpdateViewPosition:
      case UpdateViewDistance:
        prev = this.latestOnly.put(type, data.retain());
        if (prev != null)
          prev.release(); 
      case UpdateLight:
        updateLight = PacketUpdateLight.read(packet);
        ((ChunkData)this.chunks.computeIfAbsent(Long.valueOf(ChunkData.coordToLong(updateLight.getX(), updateLight.getZ())), idx -> new ChunkData(data.getTime(), updateLight.getX(), updateLight.getZ()))).updateLight(updateLight);
      case ChunkData:
      case UnloadChunk:
        chunkData = PacketChunkData.read(packet);
        if (chunkData.isUnload()) {
          unloadChunk(data.getTime(), chunkData.getUnloadX(), chunkData.getUnloadZ());
        } else {
          updateChunk(data.getTime(), chunkData.getColumn());
        } 
      case BulkChunkData:
        for (PacketChunkData.Column column : PacketChunkData.readBulk(packet))
          updateChunk(data.getTime(), column); 
      case BlockChange:
        updateBlock(data.getTime(), PacketBlockChange.read(packet));
      case MultiBlockChange:
        for (PacketBlockChange change : PacketBlockChange.readBulk(packet))
          updateBlock(data.getTime(), change); 
      case PlayerPositionRotation:
      case BlockBreakAnim:
      case BlockValue:
      case Explosion:
      case OpenTileEntityEditor:
      case PlayEffect:
      case PlaySound:
      case SpawnPosition:
      case UpdateSign:
      case UpdateTileEntity:
      case UpdateTime:
      case WorldBorder:
      case NotifyClient:
        this.currentWorld.add(data.retain());
      case CloseWindow:
        this.currentWindow.forEach(PacketData::release);
        this.currentWindow.clear();
        this.closeWindows.add(data.retain());
      case ConfirmTransaction:
        return false;
      case OpenWindow:
      case TradeList:
      case WindowProperty:
        this.currentWindow.add(data.retain());
      case WindowItems:
        if (PacketWindowItems.getWindowId(packet) == 0) {
          PacketData packetData = this.latestOnly.put(type, data.retain());
          if (packetData != null)
            packetData.release(); 
        } else {
          this.currentWindow.add(data.retain());
        } 
      case SetSlot:
        if (PacketSetSlot.getWindowId(packet) == 0) {
          PacketData packetData = this.mainInventoryChanges.put(Integer.valueOf(PacketSetSlot.getSlot(packet)), data.retain());
          if (packetData != null)
            packetData.release(); 
        } else {
          this.currentWindow.add(data.retain());
        } 
      case Team:
        team = this.teams.computeIfAbsent(PacketTeam.getName(packet), x$0 -> new Team(x$0));
        switch (PacketTeam.getAction(packet)) {
          case PlayerActionAck:
            if (team.create != null)
              team.create.release(); 
            team.create = packet.retain();
            break;
          case SpawnParticle:
            if (team.update != null)
              team.update.release(); 
            team.update = packet.retain();
            break;
          case Respawn:
            if (team.remove != null)
              team.remove.release(); 
            team.remove = packet.retain();
            if (team.create != null) {
              team.release();
              this.teams.remove(team.name);
            } 
            break;
          case JoinGame:
            for (String player : PacketTeam.getPlayers(packet)) {
              if (!team.removed.remove(player))
                team.added.add(player); 
            } 
            break;
          case SetExperience:
            for (String player : PacketTeam.getPlayers(packet)) {
              if (!team.added.remove(player))
                team.removed.add(player); 
            } 
            break;
        } 
      case MapData:
        packet1 = this.maps.put(Integer.valueOf(PacketMapData.getMapId(packet)), packet.retain());
        if (packet1 != null)
          packet1.release(); 
    } 
    this.unhandled.add(data.retain());
  }
  
  public void onEnd(PacketStream stream, long timestamp) throws IOException {
    PacketData join = this.latestOnly.remove(PacketType.JoinGame);
    PacketData respawn = this.latestOnly.remove(PacketType.Respawn);
    if (join != null)
      stream.insert(timestamp, join.getPacket()); 
    if (respawn != null)
      stream.insert(timestamp, respawn.getPacket()); 
    PacketData updateViewPosition = this.latestOnly.remove(PacketType.UpdateViewPosition);
    PacketData updateViewDistance = this.latestOnly.remove(PacketType.UpdateViewDistance);
    if (updateViewPosition != null)
      stream.insert(timestamp, updateViewPosition.getPacket()); 
    if (updateViewDistance != null)
      stream.insert(timestamp, updateViewDistance.getPacket()); 
    List<PacketData> result = new ArrayList<>();
    result.addAll(this.unhandled);
    result.addAll(this.currentWorld);
    result.addAll(this.currentWindow);
    result.addAll(this.closeWindows);
    result.addAll(this.mainInventoryChanges.values());
    result.addAll(this.latestOnly.values());
    for (Map.Entry<Integer, Entity> e : this.entities.entrySet()) {
      Entity entity = e.getValue();
      if (entity.despawned) {
        result.add(new PacketData(entity.lastTimestamp, PacketDestroyEntities.write(this.registry, new int[] { ((Integer)e.getKey()).intValue() })));
        entity.release();
        continue;
      } 
      for (PacketData data : entity.packets) {
        Packet packet = data.getPacket();
        for (Iterator<Integer> iterator = PacketUtils.getEntityIds(packet).iterator(); iterator.hasNext(); ) {
          int i = ((Integer)iterator.next()).intValue();
          Entity other = this.entities.get(Integer.valueOf(i));
          if (other != null) {
            if (other.despawned)
              continue; 
            continue;
          } 
          packet.release();
        } 
        result.add(data);
      } 
      if (entity.teleport != null)
        result.add(new PacketData(entity.lastTimestamp, entity.teleport)); 
      while (entity.dx != 0L && entity.dy != 0L && entity.dz != 0L) {
        long mx = Utils.within(entity.dx, -128L, 127L);
        long my = Utils.within(entity.dy, -128L, 127L);
        long mz = Utils.within(entity.dz, -128L, 127L);
        Entity entity1 = entity;
        entity1.dx = entity1.dx - mx;
        entity1 = entity;
        entity1.dy = entity1.dy - my;
        entity1 = entity;
        entity1.dz = entity1.dz - mz;
        DPosition deltaPos = new DPosition(mx / 32.0D, my / 32.0D, mz / 32.0D);
        result.add(new PacketData(entity.lastTimestamp, PacketEntityMovement.write(this.registry, ((Integer)e
                .getKey()).intValue(), deltaPos, null, entity.onGround)));
      } 
      if (entity.yaw != null && entity.pitch != null)
        result.add(new PacketData(entity.lastTimestamp, PacketEntityMovement.write(this.registry, ((Integer)e
                .getKey()).intValue(), null, new Pair(entity.yaw, entity.pitch), entity.onGround))); 
    } 
    for (Map.Entry<Long, Long> e : this.unloadedChunks.entrySet()) {
      int x = ChunkData.longToX(((Long)e.getKey()).longValue());
      int z = ChunkData.longToZ(((Long)e.getKey()).longValue());
      result.add(new PacketData(((Long)e.getValue()).longValue(), PacketChunkData.unload(x, z).write(this.registry)));
    } 
    for (ChunkData chunk : this.chunks.values()) {
      PacketChunkData.Column column = new PacketChunkData.Column(chunk.x, chunk.z, chunk.changes, chunk.biomeData, chunk.tileEntities, chunk.heightmaps, chunk.biomes);
      if (column.isFull() || !Utils.containsOnlyNull((Object[])chunk.changes))
        result.add(new PacketData(chunk.firstAppearance, PacketChunkData.load(column).write(this.registry))); 
      for (Map<Short, MutablePair<Long, PacketBlockChange>> e : chunk.blockChanges) {
        if (e != null)
          for (MutablePair<Long, PacketBlockChange> pair : e.values())
            result.add(new PacketData(((Long)pair.getLeft()).longValue(), ((PacketBlockChange)pair.getRight()).write(this.registry)));  
      } 
      if (chunk.hasLight())
        result.add(new PacketData(chunk.firstAppearance, (new PacketUpdateLight(chunk
                .x, chunk.z, Arrays.asList(chunk.skyLight), Arrays.asList(chunk.blockLight))).write(this.registry))); 
    } 
    result.sort(Comparator.comparingLong(PacketData::getTime));
    for (PacketData data : result)
      add(stream, timestamp, data.getPacket()); 
    for (Team team : this.teams.values()) {
      if (team.create != null)
        add(stream, timestamp, team.create); 
      if (team.update != null)
        add(stream, timestamp, team.update); 
      if (team.remove != null) {
        add(stream, timestamp, team.remove);
        continue;
      } 
      if (!team.added.isEmpty())
        add(stream, timestamp, PacketTeam.addPlayers(this.registry, team.name, team.added)); 
      if (!team.removed.isEmpty())
        add(stream, timestamp, PacketTeam.removePlayers(this.registry, team.name, team.removed)); 
    } 
    for (Packet packet : this.maps.values())
      add(stream, timestamp, packet); 
  }
  
  public String getName() {
    return "squash";
  }
  
  public void init(Studio studio, JsonObject config) {}
  
  private void add(PacketStream stream, long timestamp, Packet packet) {
    stream.insert(new PacketData(timestamp, packet));
  }
  
  private void updateBlock(long time, PacketBlockChange record) {
    IPosition pos = record.getPosition();
    ((ChunkData)this.chunks.computeIfAbsent(
        Long.valueOf(ChunkData.coordToLong(pos.getX() >> 4, pos.getZ() >> 4)), idx -> new ChunkData(time, pos.getX() >> 4, pos.getZ() >> 4)))
      
      .updateBlock(time, record);
  }
  
  private void unloadChunk(long time, int x, int z) {
    long coord = ChunkData.coordToLong(x, z);
    this.chunks.remove(Long.valueOf(coord));
    this.unloadedChunks.put(Long.valueOf(coord), Long.valueOf(time));
  }
  
  private void updateChunk(long time, PacketChunkData.Column column) {
    long coord = ChunkData.coordToLong(column.x, column.z);
    this.unloadedChunks.remove(Long.valueOf(coord));
    ChunkData chunk = this.chunks.get(Long.valueOf(coord));
    if (chunk == null)
      this.chunks.put(Long.valueOf(coord), chunk = new ChunkData(time, column.x, column.z)); 
    chunk.update(column.chunks, column.biomeData, column.tileEntities, column.heightMaps, column.biomes);
  }
  
  private static class ChunkData {
    private final long firstAppearance;
    
    private final int x;
    
    private final int z;
    
    private final PacketChunkData.Chunk[] changes = new PacketChunkData.Chunk[16];
    
    private byte[] biomeData;
    
    private Map<Short, MutablePair<Long, PacketBlockChange>>[] blockChanges = (Map<Short, MutablePair<Long, PacketBlockChange>>[])new Map[16];
    
    private CompoundTag[] tileEntities;
    
    private CompoundTag heightmaps;
    
    private byte[][] skyLight = new byte[18][];
    
    private byte[][] blockLight = new byte[18][];
    
    private int[] biomes;
    
    ChunkData(long firstAppearance, int x, int z) {
      this.firstAppearance = firstAppearance;
      this.x = x;
      this.z = z;
    }
    
    ChunkData copy() {
      ChunkData copy = new ChunkData(this.firstAppearance, this.x, this.z);
      int i;
      for (i = 0; i < this.changes.length; i++)
        copy.changes[i] = (this.changes[i] != null) ? this.changes[i].copy() : null; 
      copy.biomeData = this.biomeData;
      for (i = 0; i < this.blockChanges.length; i++) {
        if (this.blockChanges[i] != null) {
          Map<Short, MutablePair<Long, PacketBlockChange>> copyMap = new HashMap<>();
          copy.blockChanges[i] = copyMap;
          this.blockChanges[i].forEach((key, value) -> (MutablePair)copyMap.put(key, new MutablePair(value.left, value.right)));
        } 
      } 
      copy.tileEntities = this.tileEntities;
      copy.heightmaps = this.heightmaps;
      copy.skyLight = (byte[][])this.skyLight.clone();
      copy.blockLight = (byte[][])this.blockLight.clone();
      copy.biomes = this.biomes;
      return copy;
    }
    
    void update(PacketChunkData.Chunk[] newChunks, byte[] newBiomeData, CompoundTag[] newTileEntities, CompoundTag newHeightmaps, int[] newBiomes) {
      for (int i = 0; i < newChunks.length; i++) {
        if (newChunks[i] != null) {
          this.changes[i] = newChunks[i];
          this.blockChanges[i] = null;
        } 
      } 
      if (newBiomeData != null)
        this.biomeData = newBiomeData; 
      if (newTileEntities != null)
        this.tileEntities = newTileEntities; 
      if (newHeightmaps != null)
        this.heightmaps = newHeightmaps; 
      if (newBiomes != null)
        this.biomes = newBiomes; 
    }
    
    private void updateLight(PacketUpdateLight packet) {
      int i = 0;
      for (byte[] light : packet.getSkyLight()) {
        if (light != null)
          this.skyLight[i] = light; 
        i++;
      } 
      i = 0;
      for (byte[] light : packet.getBlockLight()) {
        if (light != null)
          this.blockLight[i] = light; 
        i++;
      } 
    }
    
    private boolean hasLight() {
      for (byte[] light : this.skyLight) {
        if (light != null)
          return true; 
      } 
      for (byte[] light : this.blockLight) {
        if (light != null)
          return true; 
      } 
      return false;
    }
    
    private MutablePair<Long, PacketBlockChange> blockChanges(IPosition pos) {
      int x = pos.getX();
      int y = pos.getY();
      int chunkY = y / 16;
      int z = pos.getZ();
      if (chunkY < 0 || chunkY >= this.blockChanges.length)
        return null; 
      if (this.blockChanges[chunkY] == null)
        this.blockChanges[chunkY] = new HashMap<>(); 
      short index = (short)((x & 0xF) << 10 | (y & 0xF) << 5 | z & 0xF);
      return this.blockChanges[chunkY].computeIfAbsent(Short.valueOf(index), k -> MutablePair.of(Long.valueOf(0L), null));
    }
    
    void updateBlock(long time, PacketBlockChange change) {
      MutablePair<Long, PacketBlockChange> pair = blockChanges(change.getPosition());
      if (pair != null && ((Long)pair.getLeft()).longValue() < time) {
        pair.setLeft(Long.valueOf(time));
        pair.setRight(change);
      } 
    }
    
    private static long coordToLong(int x, int z) {
      return x << 32L | z & 0xFFFFFFFFL;
    }
    
    private static int longToX(long coord) {
      return (int)(coord >> 32L);
    }
    
    private static int longToZ(long coord) {
      return (int)(coord & 0xFFFFFFFFL);
    }
  }
}
