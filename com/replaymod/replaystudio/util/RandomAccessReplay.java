package com.replaymod.replaystudio.util;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.packetlib.io.NetInput;
import com.github.steveice10.packetlib.io.NetOutput;
import com.github.steveice10.packetlib.io.stream.StreamNetInput;
import com.github.steveice10.packetlib.io.stream.StreamNetOutput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetInput;
import com.github.steveice10.packetlib.tcp.io.ByteBufNetOutput;
import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketType;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.protocol.packets.PacketBlockChange;
import com.replaymod.replaystudio.protocol.packets.PacketChunkData;
import com.replaymod.replaystudio.protocol.packets.PacketDestroyEntities;
import com.replaymod.replaystudio.protocol.packets.PacketEntityHeadLook;
import com.replaymod.replaystudio.protocol.packets.PacketEntityTeleport;
import com.replaymod.replaystudio.protocol.packets.PacketNotifyClient;
import com.replaymod.replaystudio.protocol.packets.PacketPlayerListEntry;
import com.replaymod.replaystudio.protocol.packets.PacketSpawnPlayer;
import com.replaymod.replaystudio.protocol.packets.PacketUpdateLight;
import com.replaymod.replaystudio.replay.ReplayFile;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public abstract class RandomAccessReplay<T> {
  private static final String CACHE_ENTRY = "quickModeCache.bin";
  
  private static final String CACHE_INDEX_ENTRY = "quickModeCacheIndex.bin";
  
  private static final int CACHE_VERSION = 2;
  
  private static Logger LOGGER = Logger.getLogger(RandomAccessReplay.class.getName());
  
  private final ReplayFile replayFile;
  
  private final PacketTypeRegistry registry;
  
  private int currentTimeStamp;
  
  private ByteBuf buf;
  
  private NetInput bufInput;
  
  private TreeMap<Integer, Collection<BakedTrackedThing>> thingSpawnsT = new TreeMap<>();
  
  private ListMultimap<Integer, BakedTrackedThing> thingSpawns = Multimaps.newListMultimap(this.thingSpawnsT, ArrayList::new);
  
  private TreeMap<Integer, Collection<BakedTrackedThing>> thingDespawnsT = new TreeMap<>();
  
  private ListMultimap<Integer, BakedTrackedThing> thingDespawns = Multimaps.newListMultimap(this.thingDespawnsT, ArrayList::new);
  
  private List<BakedTrackedThing> activeThings = new LinkedList<>();
  
  private TreeMap<Integer, T> worldTimes = new TreeMap<>();
  
  private TreeMap<Integer, T> thunderStrengths = new TreeMap<>();
  
  private final ByteBuf byteBuf;
  
  private final ByteBufNetOutput byteBufNetOutput;
  
  private final Inflater inflater;
  
  private final Deflater deflater;
  
  public void load(Consumer<Double> progress) throws IOException {
    if (!tryLoadFromCache(progress)) {
      double progressSplit = 0.9D;
      analyseReplay(d -> progress.accept(Double.valueOf(d.doubleValue() * progressSplit)));
      tryLoadFromCache(d -> progress.accept(Double.valueOf(d.doubleValue() * (1.0D - progressSplit) + progressSplit)));
    } 
  }
  
  private boolean tryLoadFromCache(Consumer<Double> progress) throws IOException {
    boolean success = false;
    Optional<InputStream> cacheIndexOpt = this.replayFile.getCache("quickModeCacheIndex.bin");
    if (!cacheIndexOpt.isPresent())
      return false; 
    try (InputStream indexIn = (InputStream)cacheIndexOpt.get()) {
      Optional<InputStream> cacheOpt = this.replayFile.getCache("quickModeCache.bin");
      if (!cacheOpt.isPresent())
        return false; 
      try (InputStream cacheIn = (InputStream)cacheOpt.get()) {
        success = loadFromCache(cacheIn, indexIn, progress);
      } 
    } catch (EOFException e) {
      LOGGER.log(Level.WARNING, "Re-analysing replay due to premature EOF while loading the cache:", e);
    } finally {
      if (!success) {
        this.buf = null;
        this.bufInput = null;
        this.thingSpawnsT.clear();
        this.thingDespawnsT.clear();
        this.worldTimes.clear();
        this.thunderStrengths.clear();
      } 
    } 
    return success;
  }
  
  private boolean loadFromCache(InputStream rawCacheIn, InputStream rawIndexIn, Consumer<Double> progress) throws IOException {
    long sysTimeStart = System.currentTimeMillis();
    StreamNetInput streamNetInput1 = new StreamNetInput(rawCacheIn);
    StreamNetInput streamNetInput2 = new StreamNetInput(rawIndexIn);
    if (streamNetInput2.readVarInt() != 2)
      return false; 
    if (streamNetInput1.readVarInt() != 2)
      return false; 
    if (streamNetInput2.readVarInt() != this.registry.getVersion().getId())
      return false; 
    if (streamNetInput1.readVarInt() != this.registry.getVersion().getId())
      return false; 
    while (true) {
      BakedTrackedThing trackedThing;
      switch (streamNetInput2.readVarInt()) {
        case 0:
          break;
        case 1:
          trackedThing = new BakedEntity((NetInput)streamNetInput2);
          break;
        case 2:
          trackedThing = new BakedChunk((NetInput)streamNetInput2);
          break;
        case 3:
          trackedThing = new BakedWeather((NetInput)streamNetInput2);
          break;
        default:
          return false;
      } 
      this.thingSpawns.put(Integer.valueOf(trackedThing.spawnTime), trackedThing);
      this.thingDespawns.put(Integer.valueOf(trackedThing.despawnTime), trackedThing);
    } 
    readFromCache((NetInput)streamNetInput2, this.worldTimes);
    readFromCache((NetInput)streamNetInput2, this.thunderStrengths);
    int size = streamNetInput2.readVarInt();
    LOGGER.info("Creating quick mode buffer of size: " + (size / 1024) + "KB");
    this.buf = Unpooled.buffer(size);
    int read = 0;
    while (true) {
      int len = this.buf.writeBytes(rawCacheIn, Math.min(size - read, 4096));
      if (len <= 0)
        break; 
      read += len;
      progress.accept(Double.valueOf(read / size));
    } 
    this.bufInput = (NetInput)new ByteBufNetInput(this.buf);
    LOGGER.info("Loaded quick replay from cache in " + (System.currentTimeMillis() - sysTimeStart) + "ms");
    return true;
  }
  
  private void analyseReplay(Consumer<Double> progress) throws IOException {
    TreeMap<Integer, Packet> worldTimes = new TreeMap<>();
    TreeMap<Integer, Packet> thunderStrengths = new TreeMap<>();
    Map<String, PacketPlayerListEntry> playerListEntries = new HashMap<>();
    Map<Integer, Entity> activeEntities = new HashMap<>();
    Map<Long, Chunk> activeChunks = new HashMap<>();
    Packet lastLightUpdate = null;
    Weather activeWeather = null;
    double sysTimeStart = System.currentTimeMillis();
    try(ReplayInputStream in = this.replayFile.getPacketData(this.registry); 
        OutputStream cacheOut = this.replayFile.writeCache("quickModeCache.bin"); 
        OutputStream cacheIndexOut = this.replayFile.writeCache("quickModeCacheIndex.bin")) {
      StreamNetOutput streamNetOutput1 = new StreamNetOutput(cacheOut);
      streamNetOutput1.writeVarInt(2);
      streamNetOutput1.writeVarInt(this.registry.getVersion().getId());
      StreamNetOutput streamNetOutput2 = new StreamNetOutput(cacheIndexOut);
      streamNetOutput2.writeVarInt(2);
      streamNetOutput2.writeVarInt(this.registry.getVersion().getId());
      int index = 0;
      int time = 0;
      double duration = this.replayFile.getMetaData().getDuration();
      PacketData packetData;
      while ((packetData = in.readPacket()) != null) {
        Entity entity;
        PacketPlayerListEntry playerListEntry;
        Iterator<Integer> iterator;
        PacketChunkData packetChunkData1;
        PacketUpdateLight updateLight;
        PacketChunkData chunkData;
        PacketPlayerListEntry.Action action;
        Packet prev;
        Entity entity1;
        List<Packet> spawnPackets;
        PacketChunkData.Column column;
        Chunk chunk, chunk1;
        Entity entity2;
        Chunk chunk2;
        Entity entity3;
        Packet packet = packetData.getPacket();
        time = (int)packetData.getTime();
        progress.accept(Double.valueOf(time / duration));
        Integer entityId = PacketUtils.getEntityId(packet);
        switch (packet.getType()) {
          case SpawnMob:
          case SpawnObject:
          case SpawnPainting:
            entity = new Entity(entityId.intValue(), Collections.singletonList(packet.retain()));
            entity.spawnTime = time;
            entity1 = activeEntities.put(entityId, entity);
            if (entity1 != null)
              index = entity1.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
            break;
          case SpawnPlayer:
            playerListEntry = playerListEntries.get(PacketSpawnPlayer.getPlayerListEntryId(packet));
            spawnPackets = new ArrayList<>();
            if (playerListEntry != null)
              spawnPackets.addAll(PacketPlayerListEntry.write(this.registry, PacketPlayerListEntry.Action.ADD, 
                    
                    Collections.singletonList(playerListEntry))); 
            spawnPackets.add(packet.retain());
            entity2 = new Entity(entityId.intValue(), spawnPackets);
            entity2.spawnTime = time;
            entity3 = activeEntities.put(entityId, entity2);
            if (entity3 != null)
              index = entity3.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
            break;
          case DestroyEntities:
            for (iterator = PacketDestroyEntities.getEntityIds(packet).iterator(); iterator.hasNext(); ) {
              int id = ((Integer)iterator.next()).intValue();
              entity2 = activeEntities.remove(Integer.valueOf(id));
              if (entity2 != null)
                index = entity2.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
            } 
            break;
          case ChunkData:
            packetChunkData1 = PacketChunkData.read(packet);
            column = packetChunkData1.getColumn();
            if (column.isFull()) {
              Packet initialLight = null;
              if (lastLightUpdate != null) {
                PacketUpdateLight packetUpdateLight = PacketUpdateLight.read(lastLightUpdate);
                if (column.x == packetUpdateLight.getX() && column.z == packetUpdateLight.getZ()) {
                  initialLight = lastLightUpdate;
                  lastLightUpdate = null;
                } 
              } 
              Chunk chunk3 = new Chunk(column, initialLight);
              chunk3.spawnTime = time;
              Chunk chunk4 = activeChunks.put(Long.valueOf(coordToLong(column.x, column.z)), chunk3);
              if (chunk4 != null)
                index = chunk4.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
              break;
            } 
            chunk2 = activeChunks.get(Long.valueOf(coordToLong(column.x, column.z)));
            if (chunk2 != null) {
              int sectionY = 0;
              for (PacketChunkData.Chunk section : column.chunks) {
                if (section == null) {
                  sectionY++;
                } else {
                  PacketChunkData.BlockStorage toBlocks = section.blocks;
                  PacketChunkData.BlockStorage fromBlocks = chunk2.currentBlockState[sectionY];
                  for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                      for (int x = 0; x < 16; x++) {
                        int fromState = fromBlocks.get(x, y, z);
                        int toState = toBlocks.get(x, y, z);
                        if (fromState != toState) {
                          IPosition pos = new IPosition(column.x << 4 | x, sectionY << 4 | y, column.z << 4 | z);
                          chunk2.blocks.put(Integer.valueOf(time), new BlockChange(pos, fromState, toState));
                        } 
                      } 
                    } 
                  } 
                  chunk2.currentBlockState[sectionY] = toBlocks;
                  sectionY++;
                } 
              } 
            } 
            break;
          case UpdateLight:
            updateLight = PacketUpdateLight.read(packet);
            chunk = activeChunks.get(Long.valueOf(coordToLong(updateLight.getX(), updateLight.getZ())));
            if (chunk != null && chunk.spawnPackets.size() == 1) {
              List<Packet> list = new ArrayList<>();
              list.add(packet.retain());
              list.addAll(chunk.spawnPackets);
              chunk.spawnPackets = list;
              break;
            } 
            if (lastLightUpdate != null)
              lastLightUpdate.release(); 
            lastLightUpdate = packet.retain();
            break;
          case UnloadChunk:
            chunkData = PacketChunkData.read(packet);
            chunk1 = activeChunks.remove(Long.valueOf(coordToLong(chunkData.getUnloadX(), chunkData.getUnloadZ())));
            if (chunk1 != null)
              index = chunk1.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
            break;
          case BlockChange:
          case MultiBlockChange:
            for (PacketBlockChange record : PacketBlockChange.readSingleOrBulk(packet)) {
              IPosition pos = record.getPosition();
              Chunk chunk3 = activeChunks.get(Long.valueOf(coordToLong(pos.getX() >> 4, pos.getZ() >> 4)));
              if (chunk3 != null) {
                PacketChunkData.BlockStorage blockStorage = chunk3.currentBlockState[pos.getY() >> 4];
                int x = pos.getX() & 0xF, y = pos.getY() & 0xF, z = pos.getZ() & 0xF;
                int prevState = blockStorage.get(x, y, z);
                int newState = record.getId();
                blockStorage.set(x, y, z, newState);
                chunk3.blocks.put(Integer.valueOf(time), new BlockChange(pos, prevState, newState));
              } 
            } 
            break;
          case PlayerListEntry:
            action = PacketPlayerListEntry.getAction(packet);
            for (PacketPlayerListEntry entry : PacketPlayerListEntry.read(packet)) {
              switch (action) {
                case SpawnMob:
                  playerListEntries.put(entry.getId(), entry);
                case SpawnObject:
                  playerListEntries.computeIfPresent(entry.getId(), (key, it) -> PacketPlayerListEntry.updateGamemode(it, entry.getGamemode()));
                case SpawnPainting:
                  playerListEntries.computeIfPresent(entry.getId(), (key, it) -> PacketPlayerListEntry.updateLatency(it, entry.getLatency()));
                case SpawnPlayer:
                  playerListEntries.computeIfPresent(entry.getId(), (key, it) -> PacketPlayerListEntry.updateDisplayName(it, entry.getDisplayName()));
                case DestroyEntities:
                  playerListEntries.remove(entry.getId());
              } 
            } 
            break;
          case Respawn:
            for (Entity entity4 : activeEntities.values())
              index = entity4.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
            activeEntities.clear();
            for (Chunk chunk3 : activeChunks.values())
              index = chunk3.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
            activeChunks.clear();
            if (activeWeather != null)
              index = activeWeather.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
            activeWeather = null;
            break;
          case UpdateTime:
            prev = worldTimes.put(Integer.valueOf(time), packet.retain());
            if (prev != null)
              prev.release(); 
            break;
          case NotifyClient:
            switch (PacketNotifyClient.getAction(packet)) {
              case SpawnMob:
                if (activeWeather != null)
                  index = activeWeather.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
                activeWeather = new Weather();
                activeWeather.spawnTime = time;
                break;
              case SpawnObject:
                if (activeWeather != null) {
                  index = activeWeather.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index);
                  activeWeather = null;
                } 
                break;
              case SpawnPainting:
                if (activeWeather != null) {
                  prev = activeWeather.rainStrengths.put(Integer.valueOf(time), packet.retain());
                  if (prev != null)
                    prev.release(); 
                } 
                break;
              case SpawnPlayer:
                prev = thunderStrengths.put(Integer.valueOf(time), packet.retain());
                if (prev != null)
                  prev.release(); 
                break;
            } 
            break;
        } 
        if (entityId != null) {
          Entity entity4 = activeEntities.get(entityId);
          if (entity4 != null) {
            Location current = entity4.locations.isEmpty() ? null : (Location)entity4.locations.lastEntry().getValue();
            Location updated = PacketUtils.updateLocation(current, packet);
            if (updated != null)
              entity4.locations.put(Integer.valueOf(time), updated); 
          } 
        } 
        packet.release();
      } 
      for (Entity entity : activeEntities.values())
        index = entity.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
      for (Chunk chunk : activeChunks.values())
        index = chunk.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
      if (activeWeather != null)
        index = activeWeather.writeToCache((NetOutput)streamNetOutput2, (NetOutput)streamNetOutput1, time, index); 
      streamNetOutput2.writeByte(0);
      writeToCache((NetOutput)streamNetOutput2, worldTimes);
      writeToCache((NetOutput)streamNetOutput2, thunderStrengths);
      worldTimes.values().forEach(Packet::release);
      thunderStrengths.values().forEach(Packet::release);
      if (lastLightUpdate != null)
        lastLightUpdate.release(); 
      streamNetOutput2.writeVarInt(index);
    } 
    LOGGER.info("Analysed replay in " + (System.currentTimeMillis() - sysTimeStart) + "ms");
  }
  
  public void reset() {
    this.activeThings.clear();
    this.currentTimeStamp = -1;
  }
  
  public void seek(int replayTime) throws IOException {
    if (replayTime > this.currentTimeStamp) {
      this.activeThings.removeIf(thing -> {
            if (thing.despawnTime <= replayTime) {
              try {
                thing.despawn();
              } catch (IOException e) {
                throw new RuntimeException(e);
              } 
              return true;
            } 
            return false;
          });
      for (Collection<BakedTrackedThing> things : (Iterable<Collection<BakedTrackedThing>>)this.thingSpawnsT.subMap(Integer.valueOf(this.currentTimeStamp), false, Integer.valueOf(replayTime), true).values()) {
        for (BakedTrackedThing thing : things) {
          if (thing.despawnTime > replayTime) {
            thing.spawn();
            this.activeThings.add(thing);
          } 
        } 
      } 
      for (BakedTrackedThing thing : this.activeThings)
        thing.play(this.currentTimeStamp, replayTime); 
      playMap(this.worldTimes, this.currentTimeStamp, replayTime, this::dispatch);
      playMap(this.thunderStrengths, this.currentTimeStamp, replayTime, this::dispatch);
    } else {
      this.activeThings.removeIf(thing -> {
            if (thing.spawnTime > replayTime) {
              try {
                thing.despawn();
              } catch (IOException e) {
                throw new RuntimeException(e);
              } 
              return true;
            } 
            return false;
          });
      for (Collection<BakedTrackedThing> things : (Iterable<Collection<BakedTrackedThing>>)this.thingDespawnsT.subMap(Integer.valueOf(replayTime), false, Integer.valueOf(this.currentTimeStamp), true).values()) {
        for (BakedTrackedThing thing : things) {
          if (thing.spawnTime <= replayTime) {
            thing.spawn();
            this.activeThings.add(thing);
          } 
        } 
      } 
      for (BakedTrackedThing thing : this.activeThings)
        thing.rewind(this.currentTimeStamp, replayTime); 
      rewindMap(this.worldTimes, this.currentTimeStamp, replayTime, this::dispatch);
      rewindMap(this.thunderStrengths, this.currentTimeStamp, replayTime, this::dispatch);
    } 
    this.currentTimeStamp = replayTime;
  }
  
  public RandomAccessReplay(ReplayFile replayFile, PacketTypeRegistry registry) {
    this.byteBuf = Unpooled.buffer();
    this.byteBufNetOutput = new ByteBufNetOutput(this.byteBuf);
    this.inflater = new Inflater();
    this.deflater = new Deflater();
    this.replayFile = replayFile;
    this.registry = registry;
  }
  
  private T toMC(Packet packet) {
    int readerIndex = this.byteBuf.readerIndex();
    int writerIndex = this.byteBuf.writerIndex();
    try {
      this.byteBufNetOutput.writeVarInt(packet.getId());
      this.byteBuf.writeBytes(packet.getBuf());
      return decode(this.byteBuf);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      this.byteBuf.readerIndex(readerIndex);
      this.byteBuf.writerIndex(writerIndex);
    } 
  }
  
  private T readPacketFromCache(NetInput in) throws IOException {
    int readerIndex = this.byteBuf.readerIndex();
    int writerIndex = this.byteBuf.writerIndex();
    try {
      int prefix = in.readVarInt();
      int len = prefix >> 1;
      if ((prefix & 0x1) == 1) {
        int fullLen = in.readVarInt();
        this.byteBuf.writeBytes(in.readBytes(len));
        this.byteBuf.ensureWritable(fullLen);
        this.inflater.setInput(this.byteBuf.array(), this.byteBuf.arrayOffset() + this.byteBuf.readerIndex(), len);
        this.inflater.inflate(this.byteBuf.array(), this.byteBuf.arrayOffset() + this.byteBuf.writerIndex(), fullLen);
        this.byteBuf.readerIndex(this.byteBuf.readerIndex() + len);
        this.byteBuf.writerIndex(this.byteBuf.writerIndex() + fullLen);
      } else {
        this.byteBuf.writeBytes(in.readBytes(len));
      } 
      return decode(this.byteBuf);
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      this.byteBuf.readerIndex(readerIndex);
      this.byteBuf.writerIndex(writerIndex);
      this.inflater.reset();
    } 
  }
  
  private List<T> readPacketsFromCache(NetInput in) throws IOException {
    int size = in.readVarInt();
    List<T> packets = new ArrayList<>(size);
    for (int i = 0; i < size; i++)
      packets.add(readPacketFromCache(in)); 
    return packets;
  }
  
  private void readFromCache(NetInput in, SortedMap<Integer, T> packets) throws IOException {
    int time = 0;
    for (int i = in.readVarInt(); i > 0; i--) {
      time += in.readVarInt();
      packets.put(Integer.valueOf(time), readPacketFromCache(in));
    } 
  }
  
  private int writeToCache(NetOutput out, Packet packet) throws IOException {
    int readerIndex = this.byteBuf.readerIndex();
    int writerIndex = this.byteBuf.writerIndex();
    try {
      this.byteBufNetOutput.writeVarInt(packet.getId());
      this.byteBuf.writeBytes(packet.getBuf());
      int rawIndex = this.byteBuf.readerIndex();
      int size = this.byteBuf.readableBytes();
      this.byteBuf.ensureWritable(size);
      this.deflater.setInput(this.byteBuf.array(), this.byteBuf.arrayOffset() + this.byteBuf.readerIndex(), size);
      this.deflater.finish();
      int compressedSize = 0;
      while (!this.deflater.finished() && compressedSize < size)
        compressedSize += this.deflater.deflate(this.byteBuf
            .array(), this.byteBuf
            .arrayOffset() + this.byteBuf.writerIndex() + compressedSize, size - compressedSize); 
      int len = 0;
      if (compressedSize < size) {
        this.byteBuf.readerIndex(rawIndex + size);
        this.byteBuf.writerIndex(rawIndex + size + compressedSize);
        len += writeVarInt(out, compressedSize << 1 | 0x1);
        len += writeVarInt(out, size);
      } else {
        this.byteBuf.readerIndex(rawIndex);
        this.byteBuf.writerIndex(rawIndex + size);
        len += writeVarInt(out, size << 1);
      } 
      while (this.byteBuf.isReadable()) {
        out.writeByte(this.byteBuf.readByte());
        len++;
      } 
      return len;
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      this.byteBuf.readerIndex(readerIndex);
      this.byteBuf.writerIndex(writerIndex);
      this.deflater.reset();
    } 
  }
  
  private int writeToCache(NetOutput out, Collection<Packet> packets) throws IOException {
    int len = writeVarInt(out, packets.size());
    for (Packet packet : packets)
      len += writeToCache(out, packet); 
    return len;
  }
  
  private int writeToCache(NetOutput out, SortedMap<Integer, Packet> packets) throws IOException {
    int len = 0;
    len += writeVarInt(out, packets.size());
    int lastTime = 0;
    for (Map.Entry<Integer, Packet> entry : packets.entrySet()) {
      int time = ((Integer)entry.getKey()).intValue();
      len += writeVarInt(out, time - lastTime);
      lastTime = time;
      len += writeToCache(out, entry.getValue());
    } 
    return len;
  }
  
  private static int writeVarInt(NetOutput out, int i) throws IOException {
    int len = 1;
    while ((i & 0xFFFFFF80) != 0) {
      out.writeByte(i & 0x7F | 0x80);
      i >>>= 7;
      len++;
    } 
    out.writeByte(i);
    return len;
  }
  
  private static long coordToLong(int x, int z) {
    return x << 32L | z & 0xFFFFFFFFL;
  }
  
  private static <V> void playMap(NavigableMap<Integer, V> updates, int currentTimeStamp, int replayTime, IOConsumer<V> update) throws IOException {
    Map.Entry<Integer, V> lastUpdate = updates.floorEntry(Integer.valueOf(replayTime));
    if (lastUpdate != null && ((Integer)lastUpdate.getKey()).intValue() > currentTimeStamp)
      update.accept(lastUpdate.getValue()); 
  }
  
  private static <V> void rewindMap(NavigableMap<Integer, V> updates, int currentTimeStamp, int replayTime, IOConsumer<V> update) throws IOException {
    Map.Entry<Integer, V> lastUpdate = updates.floorEntry(Integer.valueOf(replayTime));
    if (lastUpdate != null && !((Integer)lastUpdate.getKey()).equals(updates.floorKey(Integer.valueOf(currentTimeStamp))))
      update.accept(lastUpdate.getValue()); 
  }
  
  protected abstract T decode(ByteBuf paramByteBuf) throws IOException;
  
  protected abstract void dispatch(T paramT);
  
  private abstract class TrackedThing {
    List<Packet> spawnPackets;
    
    List<Packet> despawnPackets;
    
    int spawnTime;
    
    private TrackedThing(List<Packet> spawnPackets, List<Packet> despawnPackets) {
      this.spawnPackets = spawnPackets;
      this.despawnPackets = despawnPackets;
    }
    
    public int writeToCache(NetOutput indexOut, NetOutput cacheOut, int despawnTime, int index) throws IOException {
      indexOut.writeVarInt(this.spawnTime);
      indexOut.writeVarInt(despawnTime);
      indexOut.writeVarInt(index);
      index += RandomAccessReplay.this.writeToCache(cacheOut, this.spawnPackets);
      indexOut.writeVarInt(index);
      index += RandomAccessReplay.this.writeToCache(cacheOut, this.despawnPackets);
      this.spawnPackets.forEach(Packet::release);
      this.despawnPackets.forEach(Packet::release);
      return index;
    }
  }
  
  private abstract class BakedTrackedThing {
    int indexSpawnPackets;
    
    int indexDespawnPackets;
    
    int spawnTime;
    
    int despawnTime;
    
    private BakedTrackedThing(NetInput in) throws IOException {
      this.spawnTime = in.readVarInt();
      this.despawnTime = in.readVarInt();
      this.indexSpawnPackets = in.readVarInt();
      this.indexDespawnPackets = in.readVarInt();
    }
    
    void dispatch(T packet) {
      RandomAccessReplay.this.dispatch(packet);
    }
    
    void spawn() throws IOException {
      RandomAccessReplay.this.buf.readerIndex(this.indexSpawnPackets);
      RandomAccessReplay.this.readPacketsFromCache(RandomAccessReplay.this.bufInput).forEach(this::dispatch);
    }
    
    void despawn() throws IOException {
      RandomAccessReplay.this.buf.readerIndex(this.indexDespawnPackets);
      RandomAccessReplay.this.readPacketsFromCache(RandomAccessReplay.this.bufInput).forEach(this::dispatch);
    }
    
    abstract void play(int param1Int1, int param1Int2) throws IOException;
    
    abstract void rewind(int param1Int1, int param1Int2) throws IOException;
  }
  
  private class Entity extends TrackedThing {
    private int id;
    
    private NavigableMap<Integer, Location> locations = new TreeMap<>();
    
    private Entity(int entityId, List<Packet> spawnPackets) throws IOException {
      super(spawnPackets, Collections.singletonList(PacketDestroyEntities.write(RandomAccessReplay.this.registry, new int[] { entityId })));
      this.id = entityId;
    }
    
    public int writeToCache(NetOutput indexOut, NetOutput cacheOut, int despawnTime, int index) throws IOException {
      indexOut.writeByte(1);
      index = super.writeToCache(indexOut, cacheOut, despawnTime, index);
      indexOut.writeVarInt(this.id);
      indexOut.writeVarInt(index);
      index += RandomAccessReplay.writeVarInt(cacheOut, this.locations.size());
      int lastTime = 0;
      for (Map.Entry<Integer, Location> entry : this.locations.entrySet()) {
        int time = ((Integer)entry.getKey()).intValue();
        Location loc = entry.getValue();
        index += RandomAccessReplay.writeVarInt(cacheOut, time - lastTime);
        lastTime = time;
        cacheOut.writeDouble(loc.getX());
        cacheOut.writeDouble(loc.getY());
        cacheOut.writeDouble(loc.getZ());
        cacheOut.writeFloat(loc.getYaw());
        cacheOut.writeFloat(loc.getPitch());
        index += 32;
      } 
      return index;
    }
  }
  
  private class BakedEntity extends BakedTrackedThing {
    private int id;
    
    private int index;
    
    private NavigableMap<Integer, Location> locations;
    
    private BakedEntity(NetInput in) throws IOException {
      super(in);
      this.id = in.readVarInt();
      this.index = in.readVarInt();
    }
    
    public void spawn() throws IOException {
      super.spawn();
      RandomAccessReplay.this.buf.readerIndex(this.index);
      NetInput in = RandomAccessReplay.this.bufInput;
      this.locations = new TreeMap<>();
      int time = 0;
      for (int i = in.readVarInt(); i > 0; i--) {
        time += in.readVarInt();
        this.locations.put(Integer.valueOf(time), new Location(in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat()));
      } 
    }
    
    public void despawn() throws IOException {
      super.despawn();
      this.locations = null;
    }
    
    public void play(int currentTimeStamp, int replayTime) throws IOException {
      RandomAccessReplay.playMap((NavigableMap)this.locations, currentTimeStamp, replayTime, l -> {
            dispatch(RandomAccessReplay.this.toMC(PacketEntityTeleport.write(RandomAccessReplay.this.registry, this.id, l, false)));
            dispatch(RandomAccessReplay.this.toMC(PacketEntityHeadLook.write(RandomAccessReplay.this.registry, this.id, l.getYaw())));
          });
    }
    
    public void rewind(int currentTimeStamp, int replayTime) throws IOException {
      RandomAccessReplay.rewindMap((NavigableMap)this.locations, currentTimeStamp, replayTime, l -> {
            dispatch(RandomAccessReplay.this.toMC(PacketEntityTeleport.write(RandomAccessReplay.this.registry, this.id, l, false)));
            dispatch(RandomAccessReplay.this.toMC(PacketEntityHeadLook.write(RandomAccessReplay.this.registry, this.id, l.getYaw())));
          });
    }
  }
  
  private class Chunk extends TrackedThing {
    private TreeMap<Integer, Collection<RandomAccessReplay.BlockChange>> blocksT = new TreeMap<>();
    
    private ListMultimap<Integer, RandomAccessReplay.BlockChange> blocks = Multimaps.newListMultimap(this.blocksT, LinkedList::new);
    
    private PacketChunkData.BlockStorage[] currentBlockState = new PacketChunkData.BlockStorage[16];
    
    private Chunk(PacketChunkData.Column column, Packet initialLight) throws IOException {
      super((initialLight == null) ? 
          Collections.<Packet>singletonList(PacketChunkData.load(column).write(RandomAccessReplay.this.registry)) : 
          Arrays.<Packet>asList(new Packet[] { initialLight, PacketChunkData.load(column).write(RandomAccessReplay.access$1500(this$0)) }, ), Collections.singletonList(PacketChunkData.unload(column.x, column.z).write(RandomAccessReplay.this.registry)));
      PacketChunkData.Chunk[] chunks = column.chunks;
      for (int i = 0; i < this.currentBlockState.length; i++)
        this.currentBlockState[i] = (chunks[i] == null) ? new PacketChunkData.BlockStorage() : (chunks[i]).blocks.copy(); 
    }
    
    public int writeToCache(NetOutput indexOut, NetOutput cacheOut, int despawnTime, int index) throws IOException {
      indexOut.writeByte(2);
      index = super.writeToCache(indexOut, cacheOut, despawnTime, index);
      indexOut.writeVarInt(index);
      index += RandomAccessReplay.writeVarInt(cacheOut, this.blocksT.size());
      int lastTime = 0;
      for (Map.Entry<Integer, Collection<RandomAccessReplay.BlockChange>> entry : this.blocksT.entrySet()) {
        int time = ((Integer)entry.getKey()).intValue();
        index += RandomAccessReplay.writeVarInt(cacheOut, time - lastTime);
        lastTime = time;
        Collection<RandomAccessReplay.BlockChange> blockChanges = entry.getValue();
        index += RandomAccessReplay.writeVarInt(cacheOut, blockChanges.size());
        for (RandomAccessReplay.BlockChange blockChange : blockChanges) {
          Packet.Writer.writePosition(cacheOut, blockChange.pos);
          index += 8;
          index += RandomAccessReplay.writeVarInt(cacheOut, blockChange.from);
          index += RandomAccessReplay.writeVarInt(cacheOut, blockChange.to);
        } 
      } 
      return index;
    }
  }
  
  private class BakedChunk extends BakedTrackedThing {
    private int index;
    
    private TreeMap<Integer, Collection<RandomAccessReplay.BlockChange>> blocksT;
    
    private BakedChunk(NetInput in) throws IOException {
      super(in);
      this.index = in.readVarInt();
    }
    
    public void spawn() throws IOException {
      super.spawn();
      RandomAccessReplay.this.buf.readerIndex(this.index);
      NetInput in = RandomAccessReplay.this.bufInput;
      this.blocksT = new TreeMap<>();
      ListMultimap<Integer, RandomAccessReplay.BlockChange> blocks = Multimaps.newListMultimap(this.blocksT, LinkedList::new);
      int time = 0;
      for (int i = in.readVarInt(); i > 0; i--) {
        time += in.readVarInt();
        for (int j = in.readVarInt(); j > 0; j--)
          blocks.put(Integer.valueOf(time), new RandomAccessReplay.BlockChange(
                Packet.Reader.readPosition(RandomAccessReplay.this.registry, in), in
                .readVarInt(), in
                .readVarInt())); 
      } 
    }
    
    public void despawn() throws IOException {
      super.despawn();
      this.blocksT = null;
    }
    
    public void play(int currentTimeStamp, int replayTime) throws IOException {
      for (Collection<RandomAccessReplay.BlockChange> updates : (Iterable<Collection<RandomAccessReplay.BlockChange>>)this.blocksT.subMap(Integer.valueOf(currentTimeStamp), false, Integer.valueOf(replayTime), true).values()) {
        for (RandomAccessReplay.BlockChange update : updates)
          dispatch(RandomAccessReplay.this.toMC((new PacketBlockChange(update.pos, update.to)).write(RandomAccessReplay.this.registry))); 
      } 
    }
    
    public void rewind(int currentTimeStamp, int replayTime) throws IOException {
      if (currentTimeStamp >= this.despawnTime) {
        play(this.spawnTime - 1, replayTime);
        return;
      } 
      for (Collection<RandomAccessReplay.BlockChange> updates : (Iterable<Collection<RandomAccessReplay.BlockChange>>)this.blocksT.subMap(Integer.valueOf(replayTime), false, Integer.valueOf(currentTimeStamp), true).descendingMap().values()) {
        for (Iterator<RandomAccessReplay.BlockChange> it = ((LinkedList<RandomAccessReplay.BlockChange>)updates).descendingIterator(); it.hasNext(); ) {
          RandomAccessReplay.BlockChange update = it.next();
          dispatch(RandomAccessReplay.this.toMC(PacketBlockChange.write(RandomAccessReplay.this.registry, update.pos, update.from)));
        } 
      } 
    }
  }
  
  private static class BlockChange {
    private IPosition pos;
    
    private int from;
    
    private int to;
    
    private BlockChange(IPosition pos, int from, int to) {
      this.pos = pos;
      this.from = from;
      this.to = to;
    }
  }
  
  private class Weather extends TrackedThing {
    private TreeMap<Integer, Packet> rainStrengths = new TreeMap<>();
    
    private Weather() throws IOException {
      super(Collections.singletonList(PacketNotifyClient.write(RandomAccessReplay.this.registry, PacketNotifyClient.Action.START_RAIN, 0.0F)), 
          Collections.singletonList(PacketNotifyClient.write(RandomAccessReplay.this.registry, PacketNotifyClient.Action.STOP_RAIN, 0.0F)));
    }
    
    public int writeToCache(NetOutput indexOut, NetOutput cacheOut, int despawnTime, int index) throws IOException {
      indexOut.writeByte(3);
      index = super.writeToCache(indexOut, cacheOut, despawnTime, index);
      indexOut.writeVarInt(index);
      index += RandomAccessReplay.this.writeToCache(cacheOut, this.rainStrengths);
      this.rainStrengths.values().forEach(Packet::release);
      return index;
    }
  }
  
  private class BakedWeather extends BakedTrackedThing {
    private int index;
    
    private TreeMap<Integer, T> rainStrengths;
    
    private BakedWeather(NetInput in) throws IOException {
      super(in);
      this.index = in.readVarInt();
    }
    
    public void spawn() throws IOException {
      super.spawn();
      RandomAccessReplay.this.buf.readerIndex(this.index);
      this.rainStrengths = new TreeMap<>();
      RandomAccessReplay.this.readFromCache(RandomAccessReplay.this.bufInput, this.rainStrengths);
    }
    
    public void despawn() throws IOException {
      super.despawn();
      this.rainStrengths = null;
    }
    
    public void play(int currentTimeStamp, int replayTime) throws IOException {
      RandomAccessReplay.playMap(this.rainStrengths, currentTimeStamp, replayTime, this::dispatch);
    }
    
    public void rewind(int currentTimeStamp, int replayTime) throws IOException {
      RandomAccessReplay.rewindMap(this.rainStrengths, currentTimeStamp, replayTime, this::dispatch);
    }
  }
  
  static interface IOConsumer<T> {
    void accept(T param1T) throws IOException;
  }
}
