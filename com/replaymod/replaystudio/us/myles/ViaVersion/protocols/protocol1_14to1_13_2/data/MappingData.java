package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MappingData {
  public static BiMap<Integer, Integer> oldToNewItems = (BiMap<Integer, Integer>)HashBiMap.create();
  
  public static BlockMappings blockStateMappings;
  
  public static BlockMappings blockMappings;
  
  public static SoundMappings soundMappings;
  
  public static Set<Integer> motionBlocking;
  
  public static Set<Integer> nonFullBlocks;
  
  public static void init() {
    JsonObject mapping1_13_2 = loadData("mapping-1.13.2.json");
    JsonObject mapping1_14 = loadData("mapping-1.14.json");
    Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 blockstate mapping...");
    blockStateMappings = new BlockMappingsShortArray(mapping1_13_2.getAsJsonObject("blockstates"), mapping1_14.getAsJsonObject("blockstates"));
    Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 block mapping...");
    blockMappings = new BlockMappingsShortArray(mapping1_13_2.getAsJsonObject("blocks"), mapping1_14.getAsJsonObject("blocks"));
    Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 item mapping...");
    mapIdentifiers((Map<Integer, Integer>)oldToNewItems, mapping1_13_2.getAsJsonObject("items"), mapping1_14.getAsJsonObject("items"));
    Via.getPlatform().getLogger().info("Loading 1.13.2 -> 1.14 sound mapping...");
    soundMappings = new SoundMappingShortArray(mapping1_13_2.getAsJsonArray("sounds"), mapping1_14.getAsJsonArray("sounds"));
    Via.getPlatform().getLogger().info("Loading 1.14 blockstates...");
    JsonObject blockStates = mapping1_14.getAsJsonObject("blockstates");
    Map<String, Integer> blockStateMap = new HashMap<>(blockStates.entrySet().size());
    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)blockStates.entrySet())
      blockStateMap.put(((JsonElement)entry.getValue()).getAsString(), Integer.valueOf(Integer.parseInt(entry.getKey()))); 
    Via.getPlatform().getLogger().info("Loading 1.14 heightmap data...");
    JsonObject heightMapData = loadData("heightMapData-1.14.json");
    JsonArray motionBlocking = heightMapData.getAsJsonArray("MOTION_BLOCKING");
    MappingData.motionBlocking = new HashSet<>(motionBlocking.size());
    for (JsonElement blockState : motionBlocking) {
      String key = blockState.getAsString();
      Integer id = blockStateMap.get(key);
      if (id == null) {
        Via.getPlatform().getLogger().warning("Unknown blockstate " + key + " :(");
        continue;
      } 
      MappingData.motionBlocking.add(id);
    } 
    if (Via.getConfig().isNonFullBlockLightFix()) {
      nonFullBlocks = new HashSet<>();
      for (Map.Entry<String, JsonElement> blockstates : (Iterable<Map.Entry<String, JsonElement>>)mapping1_13_2.getAsJsonObject("blockstates").entrySet()) {
        String state = ((JsonElement)blockstates.getValue()).getAsString();
        if (state.contains("_slab") || state.contains("_stairs") || state.contains("_wall["))
          nonFullBlocks.add(Integer.valueOf(blockStateMappings.getNewBlock(Integer.parseInt(blockstates.getKey())))); 
      } 
      nonFullBlocks.add(Integer.valueOf(blockStateMappings.getNewBlock(8163)));
      for (int i = 3060; i <= 3067; i++)
        nonFullBlocks.add(Integer.valueOf(blockStateMappings.getNewBlock(i))); 
    } 
  }
  
  public static JsonObject loadData(String name) {
    InputStream stream = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
    InputStreamReader reader = new InputStreamReader(stream);
    try {
      return (JsonObject)GsonUtil.getGson().fromJson(reader, JsonObject.class);
    } finally {
      try {
        reader.close();
      } catch (IOException iOException) {}
    } 
  }
  
  public static void mapIdentifiers(Map<Integer, Integer> output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)oldIdentifiers.entrySet()) {
      Map.Entry<String, JsonElement> value = findValue(newIdentifiers, ((JsonElement)entry.getValue()).getAsString());
      if (value == null) {
        if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
          Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( "); 
        continue;
      } 
      output.put(Integer.valueOf(Integer.parseInt(entry.getKey())), Integer.valueOf(Integer.parseInt(value.getKey())));
    } 
  }
  
  private static void mapIdentifiers(short[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)oldIdentifiers.entrySet()) {
      Map.Entry<String, JsonElement> value = findValue(newIdentifiers, ((JsonElement)entry.getValue()).getAsString());
      if (value == null) {
        if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
          Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( "); 
        continue;
      } 
      output[Integer.parseInt((String)entry.getKey())] = Short.parseShort((String)value.getKey());
    } 
  }
  
  private static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers) {
    mapIdentifiers(output, oldIdentifiers, newIdentifiers, true);
  }
  
  private static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers, boolean warnOnMissing) {
    for (int i = 0; i < oldIdentifiers.size(); i++) {
      JsonElement v = oldIdentifiers.get(i);
      Integer index = findIndex(newIdentifiers, v.getAsString());
      if (index == null) {
        if ((warnOnMissing && !Via.getConfig().isSuppress1_13ConversionErrors()) || Via.getManager().isDebug())
          Via.getPlatform().getLogger().warning("No key for " + v + " :( "); 
      } else {
        output[i] = index.shortValue();
      } 
    } 
  }
  
  private static Map.Entry<String, JsonElement> findValue(JsonObject object, String needle) {
    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)object.entrySet()) {
      String value = ((JsonElement)entry.getValue()).getAsString();
      if (value.equals(needle))
        return entry; 
    } 
    return null;
  }
  
  private static Integer findIndex(JsonArray array, String value) {
    for (int i = 0; i < array.size(); i++) {
      JsonElement v = array.get(i);
      if (v.getAsString().equals(value))
        return Integer.valueOf(i); 
    } 
    return null;
  }
  
  public static interface SoundMappings {
    int getNewSound(int param1Int);
  }
  
  public static class SoundMappingShortArray implements SoundMappings {
    private short[] oldToNew;
    
    public SoundMappingShortArray(JsonArray mapping1_13_2, JsonArray mapping1_14, boolean warnOnMissing) {
      this.oldToNew = new short[mapping1_13_2.size()];
      Arrays.fill(this.oldToNew, (short)-1);
      MappingData.mapIdentifiers(this.oldToNew, mapping1_13_2, mapping1_14, warnOnMissing);
    }
    
    public SoundMappingShortArray(JsonArray mapping1_13_2, JsonArray mapping1_14) {
      this(mapping1_13_2, mapping1_14, true);
    }
    
    public int getNewSound(int old) {
      return (old >= 0 && old < this.oldToNew.length) ? this.oldToNew[old] : -1;
    }
  }
  
  public static interface BlockMappings {
    int getNewBlock(int param1Int);
  }
  
  public static class BlockMappingsShortArray implements BlockMappings {
    private short[] oldToNew;
    
    public BlockMappingsShortArray(JsonObject mapping1_13_2, JsonObject mapping1_14) {
      this.oldToNew = new short[mapping1_13_2.entrySet().size()];
      Arrays.fill(this.oldToNew, (short)-1);
      MappingData.mapIdentifiers(this.oldToNew, mapping1_13_2, mapping1_14);
    }
    
    public int getNewBlock(int old) {
      return (old >= 0 && old < this.oldToNew.length) ? this.oldToNew[old] : -1;
    }
  }
}
