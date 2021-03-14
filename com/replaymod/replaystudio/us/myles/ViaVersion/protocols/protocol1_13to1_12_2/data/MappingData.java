package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MappingData {
  public static BiMap<Integer, Integer> oldToNewItems = (BiMap<Integer, Integer>)HashBiMap.create();
  
  public static Map<String, Integer[]> blockTags = (Map)new HashMap<>();
  
  public static Map<String, Integer[]> itemTags = (Map)new HashMap<>();
  
  public static Map<String, Integer[]> fluidTags = (Map)new HashMap<>();
  
  public static BiMap<Short, String> oldEnchantmentsIds = (BiMap<Short, String>)HashBiMap.create();
  
  public static Map<String, String> translateMapping = new HashMap<>();
  
  public static Map<String, String> mojangTranslation = new HashMap<>();
  
  public static EnchantmentMappings enchantmentMappings;
  
  public static SoundMappings soundMappings;
  
  public static BlockMappings blockMappings;
  
  public static void init() {
    JsonObject mapping1_12 = loadData("mapping-1.12.json");
    JsonObject mapping1_13 = loadData("mapping-1.13.json");
    Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 block mapping...");
    blockMappings = new BlockMappingsShortArray(mapping1_12.getAsJsonObject("blocks"), mapping1_13.getAsJsonObject("blocks"));
    Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 item mapping...");
    mapIdentifiers((Map<Integer, Integer>)oldToNewItems, mapping1_12.getAsJsonObject("items"), mapping1_13.getAsJsonObject("items"));
    Via.getPlatform().getLogger().info("Loading new 1.13 tags...");
    loadTags(blockTags, mapping1_13.getAsJsonObject("block_tags"));
    loadTags(itemTags, mapping1_13.getAsJsonObject("item_tags"));
    loadTags(fluidTags, mapping1_13.getAsJsonObject("fluid_tags"));
    Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 enchantment mapping...");
    loadEnchantments((Map<Short, String>)oldEnchantmentsIds, mapping1_12.getAsJsonObject("enchantments"));
    enchantmentMappings = new EnchantmentMappingByteArray(mapping1_12.getAsJsonObject("enchantments"), mapping1_13.getAsJsonObject("enchantments"));
    Via.getPlatform().getLogger().info("Loading 1.12.2 -> 1.13 sound mapping...");
    soundMappings = new SoundMappingShortArray(mapping1_12.getAsJsonArray("sounds"), mapping1_13.getAsJsonArray("sounds"));
    Via.getPlatform().getLogger().info("Loading translation mapping");
    translateMapping = new HashMap<>();
    Map<String, String> translateData = (Map<String, String>)GsonUtil.getGson().fromJson(new InputStreamReader(MappingData.class
          
          .getClassLoader()
          .getResourceAsStream("assets/viaversion/data/mapping-lang-1.12-1.13.json")), (new TypeToken<Map<String, String>>() {
        
        }).getType());
    try {
      String[] lines;
      try (Reader reader = new InputStreamReader(MappingData.class.getClassLoader()
            .getResourceAsStream("mojang-translations/en_US.properties"), StandardCharsets.UTF_8)) {
        lines = CharStreams.toString(reader).split("\n");
      } 
      for (String line : lines) {
        if (!line.isEmpty()) {
          String[] keyAndTranslation = line.split("=", 2);
          if (keyAndTranslation.length == 2) {
            String key = keyAndTranslation[0];
            String translation = keyAndTranslation[1].replaceAll("%(\\d\\$)?d", "%$1s");
            if (!translateData.containsKey(key)) {
              translateMapping.put(key, translation);
            } else {
              String dataValue = translateData.get(keyAndTranslation[0]);
              if (dataValue != null)
                translateMapping.put(key, dataValue); 
            } 
          } 
        } 
      } 
    } catch (IOException e) {
      String[] lines;
      lines.printStackTrace();
    } 
  }
  
  public static JsonObject loadData(String name) {
    InputStream stream = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/" + name);
    InputStreamReader reader = new InputStreamReader(stream);
    try {
      JsonObject jsonObject = (JsonObject)GsonUtil.getGson().fromJson(reader, JsonObject.class);
      return jsonObject;
    } finally {
      try {
        reader.close();
      } catch (IOException iOException) {}
    } 
  }
  
  private static void mapIdentifiers(Map<Integer, Integer> output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
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
  
  private static void mapIdentifiers(byte[] output, JsonObject oldIdentifiers, JsonObject newIdentifiers) {
    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)oldIdentifiers.entrySet()) {
      Map.Entry<String, JsonElement> value = findValue(newIdentifiers, ((JsonElement)entry.getValue()).getAsString());
      if (value == null) {
        Via.getPlatform().getLogger().warning("No key for " + entry.getValue() + " :( ");
        continue;
      } 
      output[Integer.parseInt((String)entry.getKey())] = Byte.parseByte((String)value.getKey());
    } 
  }
  
  private static void mapIdentifiers(short[] output, JsonArray oldIdentifiers, JsonArray newIdentifiers) {
    for (int i = 0; i < oldIdentifiers.size(); i++) {
      JsonElement v = oldIdentifiers.get(i);
      Integer index = findIndex(newIdentifiers, v.getAsString());
      if (index == null) {
        if (!Via.getConfig().isSuppress1_13ConversionErrors() || Via.getManager().isDebug())
          Via.getPlatform().getLogger().warning("No key for " + v + " :( "); 
      } else {
        output[i] = index.shortValue();
      } 
    } 
  }
  
  private static void loadTags(Map<String, Integer[]> output, JsonObject newTags) {
    for (Map.Entry<String, JsonElement> entry : (Iterable<Map.Entry<String, JsonElement>>)newTags.entrySet()) {
      JsonArray ids = ((JsonElement)entry.getValue()).getAsJsonArray();
      Integer[] idsArray = new Integer[ids.size()];
      for (int i = 0; i < ids.size(); i++)
        idsArray[i] = Integer.valueOf(ids.get(i).getAsInt()); 
      output.put(entry.getKey(), idsArray);
    } 
  }
  
  private static void loadEnchantments(Map<Short, String> output, JsonObject enchantments) {
    for (Map.Entry<String, JsonElement> enchantment : (Iterable<Map.Entry<String, JsonElement>>)enchantments.entrySet())
      output.put(Short.valueOf(Short.parseShort(enchantment.getKey())), ((JsonElement)enchantment.getValue()).getAsString()); 
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
  
  public static interface BlockMappings {
    int getNewBlock(int param1Int);
  }
  
  private static class BlockMappingsShortArray implements BlockMappings {
    private short[] oldToNew = new short[4084];
    
    private BlockMappingsShortArray(JsonObject mapping1_12, JsonObject mapping1_13) {
      Arrays.fill(this.oldToNew, (short)-1);
      MappingData.mapIdentifiers(this.oldToNew, mapping1_12, mapping1_13);
      if (Via.getConfig().isSnowCollisionFix())
        this.oldToNew[1248] = 3416; 
      if (Via.getConfig().isInfestedBlocksFix()) {
        this.oldToNew[1552] = 1;
        this.oldToNew[1553] = 14;
        this.oldToNew[1554] = 3983;
        this.oldToNew[1555] = 3984;
        this.oldToNew[1556] = 3985;
        this.oldToNew[1557] = 3986;
      } 
    }
    
    public int getNewBlock(int old) {
      return (old >= 0 && old < this.oldToNew.length) ? this.oldToNew[old] : -1;
    }
  }
  
  public static interface SoundMappings {
    int getNewSound(int param1Int);
  }
  
  private static class SoundMappingShortArray implements SoundMappings {
    private short[] oldToNew = new short[662];
    
    private SoundMappingShortArray(JsonArray mapping1_12, JsonArray mapping1_13) {
      Arrays.fill(this.oldToNew, (short)-1);
      MappingData.mapIdentifiers(this.oldToNew, mapping1_12, mapping1_13);
    }
    
    public int getNewSound(int old) {
      return (old >= 0 && old < this.oldToNew.length) ? this.oldToNew[old] : -1;
    }
  }
  
  public static interface EnchantmentMappings {
    int getNewEnchantment(int param1Int);
  }
  
  private static class EnchantmentMappingByteArray implements EnchantmentMappings {
    private byte[] oldToNew = new byte[72];
    
    private EnchantmentMappingByteArray(JsonObject m1_12, JsonObject m1_13) {
      Arrays.fill(this.oldToNew, (byte)-1);
      MappingData.mapIdentifiers(this.oldToNew, m1_12, m1_13);
    }
    
    public int getNewEnchantment(int old) {
      return (old >= 0 && old < this.oldToNew.length) ? this.oldToNew[old] : -1;
    }
  }
}
