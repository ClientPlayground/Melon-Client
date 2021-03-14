package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.storage;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.Pair;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.StoredObject;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.data.UserConnection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Position;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.NibbleArray;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.data.MappingData;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_13to1_12_2.packets.WorldPackets;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class BlockConnectionStorage extends StoredObject {
  private Map<Long, Pair<byte[], NibbleArray>> blockStorage = createLongObjectMap();
  
  private static Constructor<?> fastUtilLongObjectHashMap;
  
  static {
    try {
      fastUtilLongObjectHashMap = Class.forName("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap").getConstructor(new Class[0]);
      Via.getPlatform().getLogger().info("Using FastUtil Long2ObjectOpenHashMap for block connections");
    } catch (ClassNotFoundException|NoSuchMethodException classNotFoundException) {}
  }
  
  private static HashMap<Short, Short> reverseBlockMappings = new HashMap<>();
  
  static {
    for (int i = 0; i < 4096; i++) {
      int newBlock = MappingData.blockMappings.getNewBlock(i);
      if (newBlock != -1)
        reverseBlockMappings.put(Short.valueOf((short)newBlock), Short.valueOf((short)i)); 
    } 
  }
  
  public BlockConnectionStorage(UserConnection user) {
    super(user);
  }
  
  public void store(Position position, int blockState) {
    Short mapping = reverseBlockMappings.get(Short.valueOf((short)blockState));
    if (mapping == null)
      return; 
    blockState = mapping.shortValue();
    long pair = getChunkSectionIndex(position);
    Pair<byte[], NibbleArray> map = getChunkSection(pair, ((blockState & 0xF) != 0));
    int blockIndex = encodeBlockPos(position);
    ((byte[])map.getKey())[blockIndex] = (byte)(blockState >> 4);
    NibbleArray nibbleArray = (NibbleArray)map.getValue();
    if (nibbleArray != null)
      nibbleArray.set(blockIndex, blockState); 
  }
  
  public int get(Position position) {
    long pair = getChunkSectionIndex(position);
    Pair<byte[], NibbleArray> map = this.blockStorage.get(Long.valueOf(pair));
    if (map == null)
      return 0; 
    short blockPosition = encodeBlockPos(position);
    NibbleArray nibbleArray = (NibbleArray)map.getValue();
    return WorldPackets.toNewId((((byte[])map
        .getKey())[blockPosition] & 0xFF) << 4 | ((nibbleArray == null) ? 0 : nibbleArray
        .get(blockPosition)));
  }
  
  public void remove(Position position) {
    long pair = getChunkSectionIndex(position);
    Pair<byte[], NibbleArray> map = this.blockStorage.get(Long.valueOf(pair));
    if (map == null)
      return; 
    int blockIndex = encodeBlockPos(position);
    NibbleArray nibbleArray = (NibbleArray)map.getValue();
    if (nibbleArray != null) {
      nibbleArray.set(blockIndex, 0);
      boolean allZero = true;
      for (int j = 0; j < 4096; j++) {
        if (nibbleArray.get(j) != 0) {
          allZero = false;
          break;
        } 
      } 
      if (allZero)
        map.setValue(null); 
    } 
    ((byte[])map.getKey())[blockIndex] = 0;
    byte[] arrayOfByte;
    int i;
    byte b;
    for (arrayOfByte = (byte[])map.getKey(), i = arrayOfByte.length, b = 0; b < i; ) {
      short entry = (short)arrayOfByte[b];
      if (entry != 0)
        return; 
      b++;
    } 
    this.blockStorage.remove(Long.valueOf(pair));
  }
  
  public void clear() {
    this.blockStorage.clear();
  }
  
  public void unloadChunk(int x, int z) {
    for (int y = 0; y < 256; y += 16)
      this.blockStorage.remove(Long.valueOf(getChunkSectionIndex(x << 4, y, z << 4))); 
  }
  
  private Pair<byte[], NibbleArray> getChunkSection(long index, boolean requireNibbleArray) {
    Pair<byte[], NibbleArray> map = this.blockStorage.get(Long.valueOf(index));
    if (map == null) {
      map = new Pair(new byte[4096], null);
      this.blockStorage.put(Long.valueOf(index), map);
    } 
    if (map.getValue() == null && requireNibbleArray)
      map.setValue(new NibbleArray(4096)); 
    return map;
  }
  
  private long getChunkSectionIndex(int x, int y, int z) {
    return ((x >> 4) & 0x3FFFFFFL) << 38L | ((y >> 4) & 0xFFFL) << 26L | (z >> 4) & 0x3FFFFFFL;
  }
  
  private long getChunkSectionIndex(Position position) {
    return getChunkSectionIndex(position.getX().intValue(), position.getY().intValue(), position.getZ().intValue());
  }
  
  private short encodeBlockPos(int x, int y, int z) {
    return (short)((y & 0xF) << 8 | (x & 0xF) << 4 | z & 0xF);
  }
  
  private short encodeBlockPos(Position pos) {
    return encodeBlockPos(pos.getX().intValue(), pos.getY().intValue(), pos.getZ().intValue());
  }
  
  private <T> Map<Long, T> createLongObjectMap() {
    if (fastUtilLongObjectHashMap != null)
      try {
        return (Map<Long, T>)fastUtilLongObjectHashMap.newInstance(new Object[0]);
      } catch (IllegalAccessException|InstantiationException|java.lang.reflect.InvocationTargetException e) {
        e.printStackTrace();
      }  
    return new HashMap<>();
  }
}
