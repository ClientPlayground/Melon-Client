package com.replaymod.replaystudio.replay;

import com.google.common.base.Optional;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.replaymod.replaystudio.Studio;
import com.replaymod.replaystudio.data.Marker;
import com.replaymod.replaystudio.data.ModInfo;
import com.replaymod.replaystudio.data.ReplayAssetEntry;
import com.replaymod.replaystudio.io.ReplayInputStream;
import com.replaymod.replaystudio.io.ReplayOutputStream;
import com.replaymod.replaystudio.pathing.PathingRegistry;
import com.replaymod.replaystudio.pathing.path.Timeline;
import com.replaymod.replaystudio.pathing.serialize.TimelineSerialization;
import com.replaymod.replaystudio.protocol.PacketTypeRegistry;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public abstract class AbstractReplayFile implements ReplayFile {
  private static final String ENTRY_META_DATA = "metaData.json";
  
  protected static final String ENTRY_RECORDING = "recording.tmcpr";
  
  private static final String ENTRY_RESOURCE_PACK = "resourcepack/%s.zip";
  
  private static final String ENTRY_RESOURCE_PACK_INDEX = "resourcepack/index.json";
  
  private static final String ENTRY_THUMB = "thumb";
  
  private static final String ENTRY_VISIBILITY_OLD = "visibility";
  
  private static final String ENTRY_VISIBILITY = "visibility.json";
  
  private static final String ENTRY_MARKERS = "markers.json";
  
  private static final String ENTRY_ASSET = "asset/%s_%s.%s";
  
  private static final Pattern PATTERN_ASSETS = Pattern.compile("asset/.*");
  
  private static final String ENTRY_MODS = "mods.json";
  
  private static final byte[] THUMB_MAGIC_NUMBERS = new byte[] { 0, 1, 1, 2, 3, 5, 8 };
  
  protected final Studio studio;
  
  public AbstractReplayFile(Studio studio) throws IOException {
    this.studio = studio;
  }
  
  public ReplayMetaData getMetaData() throws IOException {
    Optional<InputStream> in = get("metaData.json");
    if (!in.isPresent())
      return null; 
    try (Reader is = new InputStreamReader((InputStream)in.get())) {
      return (ReplayMetaData)(new Gson()).fromJson(is, ReplayMetaData.class);
    } 
  }
  
  public void writeMetaData(PacketTypeRegistry registry, ReplayMetaData metaData) throws IOException {
    metaData.setFileFormat("MCPR");
    if (registry != null) {
      metaData.setFileFormatVersion(14);
      metaData.setProtocolVersion(registry.getVersion().getId());
    } 
    if (metaData.getGenerator() == null)
      metaData.setGenerator("ReplayStudio v" + this.studio.getVersion()); 
    try (OutputStream out = write("metaData.json")) {
      String json = (new Gson()).toJson(metaData);
      out.write(json.getBytes());
    } 
  }
  
  public ReplayInputStream getPacketData(PacketTypeRegistry registry) throws IOException {
    Optional<InputStream> in = get("recording.tmcpr");
    if (!in.isPresent())
      return null; 
    ReplayMetaData metaData = getMetaData();
    return new ReplayInputStream(registry, (InputStream)in.get(), metaData.getFileFormatVersion(), metaData.getRawProtocolVersionOr0());
  }
  
  public ReplayOutputStream writePacketData() throws IOException {
    return new ReplayOutputStream(write("recording.tmcpr"));
  }
  
  public Map<Integer, String> getResourcePackIndex() throws IOException {
    Optional<InputStream> in = get("resourcepack/index.json");
    if (!in.isPresent())
      return null; 
    Map<Integer, String> index = new HashMap<>();
    try (Reader is = new InputStreamReader((InputStream)in.get())) {
      JsonObject array = (JsonObject)(new Gson()).fromJson(is, JsonObject.class);
      for (Map.Entry<String, JsonElement> e : (Iterable<Map.Entry<String, JsonElement>>)array.entrySet()) {
        try {
          index.put(Integer.valueOf(Integer.parseInt(e.getKey())), ((JsonElement)e.getValue()).getAsString());
        } catch (NumberFormatException numberFormatException) {}
      } 
    } 
    return index;
  }
  
  public void writeResourcePackIndex(Map<Integer, String> index) throws IOException {
    try (OutputStream out = write("resourcepack/index.json")) {
      String json = (new Gson()).toJson(index);
      out.write(json.getBytes());
    } 
  }
  
  public Optional<InputStream> getResourcePack(String hash) throws IOException {
    return get(String.format("resourcepack/%s.zip", new Object[] { hash }));
  }
  
  public OutputStream writeResourcePack(String hash) throws IOException {
    return write(String.format("resourcepack/%s.zip", new Object[] { hash }));
  }
  
  public Map<String, Timeline> getTimelines(PathingRegistry pathingRegistry) throws IOException {
    return (new TimelineSerialization(pathingRegistry, this)).load();
  }
  
  public void writeTimelines(PathingRegistry pathingRegistry, Map<String, Timeline> timelines) throws IOException {
    (new TimelineSerialization(pathingRegistry, this)).save(timelines);
  }
  
  public Optional<BufferedImage> getThumb() throws IOException {
    Optional<InputStream> in = get("thumb");
    if (in.isPresent()) {
      int i = 7;
      while (i > 0)
        i = (int)(i - ((InputStream)in.get()).skip(i)); 
      return Optional.of(ImageIO.read((InputStream)in.get()));
    } 
    return Optional.absent();
  }
  
  public void writeThumb(BufferedImage image) throws IOException {
    try (OutputStream out = write("thumb")) {
      out.write(THUMB_MAGIC_NUMBERS);
      ImageIO.write(image, "jpg", out);
    } 
  }
  
  public Optional<Set<UUID>> getInvisiblePlayers() throws IOException {
    Optional<InputStream> in = get("visibility.json");
    if (!in.isPresent()) {
      in = get("visibility");
      if (!in.isPresent())
        return Optional.absent(); 
    } 
    Set<UUID> uuids = new HashSet<>();
    try (Reader is = new InputStreamReader((InputStream)in.get())) {
      JsonObject json = (JsonObject)(new Gson()).fromJson(is, JsonObject.class);
      for (JsonElement e : json.getAsJsonArray("hidden"))
        uuids.add(UUID.fromString(e.getAsString())); 
    } 
    return Optional.of(uuids);
  }
  
  public void writeInvisiblePlayers(Set<UUID> uuids) throws IOException {
    try (OutputStream out = write("visibility.json")) {
      JsonObject root = new JsonObject();
      JsonArray array = new JsonArray();
      root.add("hidden", (JsonElement)array);
      for (UUID uuid : uuids)
        array.add((JsonElement)new JsonPrimitive(uuid.toString())); 
      String json = (new Gson()).toJson((JsonElement)root);
      out.write(json.getBytes());
    } 
  }
  
  public Optional<Set<Marker>> getMarkers() throws IOException {
    Optional<InputStream> in = get("markers.json");
    if (in.isPresent())
      try (Reader is = new InputStreamReader((InputStream)in.get())) {
        JsonArray json = (JsonArray)(new Gson()).fromJson(is, JsonArray.class);
        Set<Marker> markers = new HashSet<>();
        for (JsonElement element : json) {
          JsonObject obj = element.getAsJsonObject();
          JsonObject value = obj.getAsJsonObject("value");
          JsonObject position = value.getAsJsonObject("position");
          Marker marker = new Marker();
          marker.setTime(obj.get("realTimestamp").getAsInt());
          marker.setX(position.get("x").getAsDouble());
          marker.setY(position.get("y").getAsDouble());
          marker.setZ(position.get("z").getAsDouble());
          marker.setYaw(position.get("yaw").getAsFloat());
          marker.setPitch(position.get("pitch").getAsFloat());
          marker.setRoll(position.get("roll").getAsFloat());
          if (value.has("name"))
            marker.setName(value.get("name").getAsString()); 
          markers.add(marker);
        } 
        return Optional.of(markers);
      }  
    return Optional.absent();
  }
  
  public void writeMarkers(Set<Marker> markers) throws IOException {
    try (OutputStream out = write("markers.json")) {
      JsonArray root = new JsonArray();
      for (Marker marker : markers) {
        JsonObject entry = new JsonObject();
        JsonObject value = new JsonObject();
        JsonObject position = new JsonObject();
        entry.add("realTimestamp", (JsonElement)new JsonPrimitive(Integer.valueOf(marker.getTime())));
        value.add("name", (marker.getName() == null) ? null : (JsonElement)new JsonPrimitive(marker.getName()));
        position.add("x", (JsonElement)new JsonPrimitive(Double.valueOf(marker.getX())));
        position.add("y", (JsonElement)new JsonPrimitive(Double.valueOf(marker.getY())));
        position.add("z", (JsonElement)new JsonPrimitive(Double.valueOf(marker.getZ())));
        position.add("yaw", (JsonElement)new JsonPrimitive(Float.valueOf(marker.getYaw())));
        position.add("pitch", (JsonElement)new JsonPrimitive(Float.valueOf(marker.getPitch())));
        position.add("roll", (JsonElement)new JsonPrimitive(Float.valueOf(marker.getRoll())));
        value.add("position", (JsonElement)position);
        entry.add("value", (JsonElement)value);
        root.add((JsonElement)entry);
      } 
      out.write((new Gson()).toJson((JsonElement)root).getBytes());
    } 
  }
  
  public Collection<ReplayAssetEntry> getAssets() throws IOException {
    Map<String, InputStream> entries = getAll(PATTERN_ASSETS);
    entries.values().forEach(Closeables::closeQuietly);
    List<ReplayAssetEntry> list = new ArrayList<>();
    for (String key : entries.keySet()) {
      int delim = key.indexOf('_');
      UUID uuid = UUID.fromString(key.substring(0, delim));
      String name = key.substring(delim + 1, key.lastIndexOf('.'));
      String extension = key.substring(key.lastIndexOf('.'));
      list.add(new ReplayAssetEntry(uuid, extension, name));
    } 
    return list;
  }
  
  public Optional<InputStream> getAsset(UUID uuid) throws IOException {
    Map<String, InputStream> entries = getAll(Pattern.compile("asset/" + Pattern.quote(uuid.toString()) + "_.*"));
    if (entries.isEmpty())
      return Optional.absent(); 
    return Optional.of(entries.values().iterator().next());
  }
  
  public OutputStream writeAsset(ReplayAssetEntry asset) throws IOException {
    return write(String.format("asset/%s_%s.%s", new Object[] { asset.getUuid().toString(), asset.getName(), asset.getFileExtension() }));
  }
  
  public void removeAsset(UUID uuid) throws IOException {
    Collection<ReplayAssetEntry> assets = getAssets();
    for (ReplayAssetEntry asset : assets) {
      if (asset.getUuid().equals(uuid))
        remove(String.format("asset/%s_%s.%s", new Object[] { asset.getUuid().toString(), asset.getName(), asset.getFileExtension() })); 
    } 
  }
  
  public Collection<ModInfo> getModInfo() throws IOException {
    Optional<InputStream> in = get("mods.json");
    if (in.isPresent())
      try (Reader is = new InputStreamReader((InputStream)in.get())) {
        JsonArray json = ((JsonObject)(new Gson()).fromJson(is, JsonObject.class)).getAsJsonArray("requiredMods");
        List<ModInfo> modInfoList = new ArrayList<>();
        for (JsonElement element : json) {
          JsonObject obj = element.getAsJsonObject();
          modInfoList.add(new ModInfo(obj
                .get("modID").getAsString(), obj
                .get("modName").getAsString(), obj
                .get("modVersion").getAsString()));
        } 
        return modInfoList;
      }  
    return Collections.emptyList();
  }
  
  public void writeModInfo(Collection<ModInfo> modInfo) throws IOException {
    try (OutputStream out = write("mods.json")) {
      JsonObject root = new JsonObject();
      JsonArray array = new JsonArray();
      for (ModInfo mod : modInfo) {
        JsonObject entry = new JsonObject();
        entry.addProperty("modID", mod.getId());
        entry.addProperty("modName", mod.getName());
        entry.addProperty("modVersion", mod.getVersion());
        array.add((JsonElement)entry);
      } 
      root.add("requiredMods", (JsonElement)array);
      out.write((new Gson()).toJson((JsonElement)root).getBytes());
    } 
  }
}
