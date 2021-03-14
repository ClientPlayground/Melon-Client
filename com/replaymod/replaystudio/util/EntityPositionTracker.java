package com.replaymod.replaystudio.util;

import com.github.steveice10.packetlib.io.stream.StreamNetInput;
import com.github.steveice10.packetlib.io.stream.StreamNetOutput;
import com.google.common.base.Optional;
import com.replaymod.replaystudio.PacketData;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.protocol.Packet;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import com.replaymod.replaystudio.replay.ReplayFile;
import com.replaymod.replaystudio.replay.ReplayMetaData;
import com.replaymod.replaystudio.us.myles.ViaVersion.packets.State;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;

public class EntityPositionTracker {
  private static final String CACHE_ENTRY = "entity_positions.bin";
  
  private static final String OLD_CACHE_ENTRY = "entity_positions.json";
  
  private final ReplayFile replayFile;
  
  private volatile Map<Integer, NavigableMap<Long, Location>> entityPositions;
  
  public EntityPositionTracker(ReplayFile replayFile) {
    this.replayFile = replayFile;
  }
  
  public void load(Consumer<Double> progressMonitor) throws IOException {
    Optional<InputStream> cached;
    synchronized (this.replayFile) {
      Optional<InputStream> oldCache = this.replayFile.get("entity_positions.json");
      if (oldCache.isPresent()) {
        ((InputStream)oldCache.get()).close();
        this.replayFile.remove("entity_positions.json");
      } 
      cached = this.replayFile.getCache("entity_positions.bin");
    } 
    if (cached.isPresent()) {
      try (InputStream in = (InputStream)cached.get()) {
        loadFromCache(in);
      } catch (EOFException e) {
        loadFromPacketData(progressMonitor);
        synchronized (this.replayFile) {
          this.replayFile.removeCache("entity_positions.bin");
        } 
        saveToCache();
      } 
    } else {
      loadFromPacketData(progressMonitor);
      saveToCache();
    } 
  }
  
  private void loadFromCache(InputStream rawIn) throws IOException {
    StreamNetInput streamNetInput = new StreamNetInput(rawIn);
    this.entityPositions = new TreeMap<>();
    for (int i = streamNetInput.readVarInt(); i > 0; i--) {
      int entityId = streamNetInput.readVarInt();
      TreeMap<Long, Location> locationMap = new TreeMap<>();
      long time = 0L;
      for (int j = streamNetInput.readVarInt(); j > 0; j--) {
        time += streamNetInput.readVarLong();
        locationMap.put(Long.valueOf(time), new Location(streamNetInput
              .readDouble(), streamNetInput.readDouble(), streamNetInput.readDouble(), streamNetInput.readFloat(), streamNetInput.readFloat()));
      } 
      this.entityPositions.put(Integer.valueOf(entityId), locationMap);
    } 
  }
  
  private void saveToCache() throws IOException {
    synchronized (this.replayFile) {
      Optional<InputStream> cached = this.replayFile.getCache("entity_positions.bin");
      if (cached.isPresent()) {
        ((InputStream)cached.get()).close();
        return;
      } 
      try (OutputStream rawOut = this.replayFile.writeCache("entity_positions.bin")) {
        StreamNetOutput streamNetOutput = new StreamNetOutput(rawOut);
        streamNetOutput.writeVarInt(this.entityPositions.size());
        for (Map.Entry<Integer, NavigableMap<Long, Location>> entry : this.entityPositions.entrySet()) {
          streamNetOutput.writeVarInt(((Integer)entry.getKey()).intValue());
          streamNetOutput.writeVarInt(((NavigableMap)entry.getValue()).size());
          long time = 0L;
          for (Map.Entry<Long, Location> locEntry : (Iterable<Map.Entry<Long, Location>>)((NavigableMap)entry.getValue()).entrySet()) {
            streamNetOutput.writeVarLong(((Long)locEntry.getKey()).longValue() - time);
            time = ((Long)locEntry.getKey()).longValue();
            Location loc = locEntry.getValue();
            streamNetOutput.writeDouble(loc.getX());
            streamNetOutput.writeDouble(loc.getY());
            streamNetOutput.writeDouble(loc.getZ());
            streamNetOutput.writeFloat(loc.getYaw());
            streamNetOutput.writeFloat(loc.getPitch());
          } 
        } 
      } 
    } 
  }
  
  private void loadFromPacketData(Consumer<Double> progressMonitor) throws IOException {
    int replayLength;
    ReplayInputStream origIn;
    synchronized (this.replayFile) {
      ReplayMetaData metaData = this.replayFile.getMetaData();
      replayLength = Math.max(1, metaData.getDuration());
      origIn = this.replayFile.getPacketData(PacketTypeRegistry.get(metaData.getProtocolVersion(), State.LOGIN));
    } 
    Map<Integer, NavigableMap<Long, Location>> entityPositions = new HashMap<>();
    try (ReplayInputStream in = origIn) {
      PacketData packetData;
      while ((packetData = in.readPacket()) != null) {
        Packet packet = packetData.getPacket();
        Integer entityID = PacketUtils.getEntityId(packet);
        if (entityID == null) {
          packet.release();
          continue;
        } 
        NavigableMap<Long, Location> positions = entityPositions.get(entityID);
        if (positions == null)
          entityPositions.put(entityID, positions = new TreeMap<>()); 
        Location oldPosition = positions.isEmpty() ? null : (Location)positions.lastEntry().getValue();
        Location newPosition = PacketUtils.updateLocation(oldPosition, packet);
        if (newPosition != null) {
          positions.put(Long.valueOf(packetData.getTime()), newPosition);
          double progress = packetData.getTime() / replayLength;
          progressMonitor.accept(Double.valueOf(Math.min(1.0D, Math.max(0.0D, progress))));
        } 
        packet.release();
      } 
    } 
    this.entityPositions = entityPositions;
  }
  
  public Location getEntityPositionAtTimestamp(int entityID, long timestamp) {
    if (this.entityPositions == null)
      throw new IllegalStateException("Not yet initialized."); 
    NavigableMap<Long, Location> positions = this.entityPositions.get(Integer.valueOf(entityID));
    if (positions == null)
      return null; 
    Map.Entry<Long, Location> lower = positions.floorEntry(Long.valueOf(timestamp));
    Map.Entry<Long, Location> higher = positions.higherEntry(Long.valueOf(timestamp));
    if (lower == null || higher == null)
      return null; 
    double r = ((((Long)higher.getKey()).longValue() - timestamp) / (((Long)higher.getKey()).longValue() - ((Long)lower.getKey()).longValue()));
    Location l = lower.getValue();
    Location h = higher.getValue();
    return new Location(l
        .getX() + (h.getX() - l.getX()) * r, l
        .getY() + (h.getY() - l.getY()) * r, l
        .getZ() + (h.getZ() - l.getZ()) * r, l
        .getYaw() + (h.getYaw() - l.getYaw()) * (float)r, l
        .getPitch() + (h.getPitch() - l.getPitch()) * (float)r);
  }
}
