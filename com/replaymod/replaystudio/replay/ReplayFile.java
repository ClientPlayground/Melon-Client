package com.replaymod.replaystudio.replay;

import com.google.common.base.Optional;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.data.ModInfo;
import com.replaymod.replaystudio.data.ReplayAssetEntry;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public interface ReplayFile extends Closeable {
  Optional<InputStream> get(String paramString) throws IOException;
  
  Optional<InputStream> getCache(String paramString) throws IOException;
  
  Map<String, InputStream> getAll(Pattern paramPattern) throws IOException;
  
  OutputStream write(String paramString) throws IOException;
  
  OutputStream writeCache(String paramString) throws IOException;
  
  void remove(String paramString) throws IOException;
  
  void removeCache(String paramString) throws IOException;
  
  void save() throws IOException;
  
  void saveTo(File paramFile) throws IOException;
  
  ReplayMetaData getMetaData() throws IOException;
  
  void writeMetaData(PacketTypeRegistry paramPacketTypeRegistry, ReplayMetaData paramReplayMetaData) throws IOException;
  
  ReplayInputStream getPacketData(PacketTypeRegistry paramPacketTypeRegistry) throws IOException;
  
  ReplayOutputStream writePacketData() throws IOException;
  
  Map<Integer, String> getResourcePackIndex() throws IOException;
  
  void writeResourcePackIndex(Map<Integer, String> paramMap) throws IOException;
  
  Optional<InputStream> getResourcePack(String paramString) throws IOException;
  
  OutputStream writeResourcePack(String paramString) throws IOException;
  
  Map<String, Timeline> getTimelines(PathingRegistry paramPathingRegistry) throws IOException;
  
  void writeTimelines(PathingRegistry paramPathingRegistry, Map<String, Timeline> paramMap) throws IOException;
  
  Optional<BufferedImage> getThumb() throws IOException;
  
  void writeThumb(BufferedImage paramBufferedImage) throws IOException;
  
  Optional<Set<UUID>> getInvisiblePlayers() throws IOException;
  
  void writeInvisiblePlayers(Set<UUID> paramSet) throws IOException;
  
  Optional<Set<Marker>> getMarkers() throws IOException;
  
  void writeMarkers(Set<Marker> paramSet) throws IOException;
  
  Collection<ReplayAssetEntry> getAssets() throws IOException;
  
  Optional<InputStream> getAsset(UUID paramUUID) throws IOException;
  
  OutputStream writeAsset(ReplayAssetEntry paramReplayAssetEntry) throws IOException;
  
  void removeAsset(UUID paramUUID) throws IOException;
  
  Collection<ModInfo> getModInfo() throws IOException;
  
  void writeModInfo(Collection<ModInfo> paramCollection) throws IOException;
}
