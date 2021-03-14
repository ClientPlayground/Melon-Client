package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data;

import com.google.common.collect.ObjectArrays;
import com.google.gson.reflect.TypeToken;
import com.replaymod.replaystudio.us.myles.ViaVersion.util.GsonUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class BlockIdData {
  public static Map<String, String[]> blockIdMapping;
  
  public static Map<String, String[]> fallbackReverseMapping;
  
  public static Map<Integer, String> numberIdToString;
  
  public static void init() {
    InputStream stream = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/blockIds1.12to1.13.json");
    InputStreamReader reader = new InputStreamReader(stream);
    try {
      blockIdMapping = (Map)new HashMap<>((Map<? extends String, ? extends String>)GsonUtil.getGson().fromJson(reader, (new TypeToken<Map<String, String[]>>() {
            
            }).getType()));
      fallbackReverseMapping = (Map)new HashMap<>();
      for (Map.Entry<String, String[]> entry : blockIdMapping.entrySet()) {
        for (String val : (String[])entry.getValue()) {
          String[] previous = fallbackReverseMapping.get(val);
          if (previous == null)
            previous = new String[0]; 
          fallbackReverseMapping.put(val, ObjectArrays.concat((Object[])previous, entry.getKey()));
        } 
      } 
    } finally {
      try {
        reader.close();
      } catch (IOException iOException) {}
    } 
    InputStream blockS = MappingData.class.getClassLoader().getResourceAsStream("assets/viaversion/data/blockNumberToString1.12.json");
    InputStreamReader blockR = new InputStreamReader(blockS);
    try {
      numberIdToString = new HashMap<>((Map<? extends Integer, ? extends String>)GsonUtil.getGson().fromJson(blockR, (new TypeToken<Map<Integer, String>>() {
            
            }).getType()));
    } finally {
      try {
        blockR.close();
      } catch (IOException iOException) {}
    } 
  }
}
