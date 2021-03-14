package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChunkSection {
  public static final int SIZE = 4096;
  
  public static final int LIGHT_LENGTH = 2048;
  
  private List<Integer> palette = new ArrayList<>();
  
  private Map<Integer, Integer> inversePalette = new HashMap<>();
  
  private final int[] blocks;
  
  private NibbleArray blockLight;
  
  private NibbleArray skyLight;
  
  private int nonAirBlocksCount;
  
  public int getNonAirBlocksCount() {
    return this.nonAirBlocksCount;
  }
  
  public void setNonAirBlocksCount(int nonAirBlocksCount) {
    this.nonAirBlocksCount = nonAirBlocksCount;
  }
  
  public ChunkSection() {
    this.blocks = new int[4096];
    this.blockLight = new NibbleArray(4096);
    addPaletteEntry(0);
  }
  
  public void setBlock(int x, int y, int z, int type, int data) {
    setFlatBlock(index(x, y, z), type << 4 | data & 0xF);
  }
  
  public void setFlatBlock(int x, int y, int z, int type) {
    setFlatBlock(index(x, y, z), type);
  }
  
  public int getBlockId(int x, int y, int z) {
    return getFlatBlock(x, y, z) >> 4;
  }
  
  public int getBlockData(int x, int y, int z) {
    return getFlatBlock(x, y, z) & 0xF;
  }
  
  public int getFlatBlock(int x, int y, int z) {
    int index = this.blocks[index(x, y, z)];
    return ((Integer)this.palette.get(index)).intValue();
  }
  
  public int getFlatBlock(int idx) {
    int index = this.blocks[idx];
    return ((Integer)this.palette.get(index)).intValue();
  }
  
  public void setBlock(int idx, int type, int data) {
    setFlatBlock(idx, type << 4 | data & 0xF);
  }
  
  public void setPaletteIndex(int idx, int index) {
    this.blocks[idx] = index;
  }
  
  public int getPaletteIndex(int idx) {
    return this.blocks[idx];
  }
  
  public int getPaletteSize() {
    return this.palette.size();
  }
  
  public int getPaletteEntry(int index) {
    if (index < 0 || index >= this.palette.size())
      throw new IndexOutOfBoundsException(); 
    return ((Integer)this.palette.get(index)).intValue();
  }
  
  public void setPaletteEntry(int index, int id) {
    if (index < 0 || index >= this.palette.size())
      throw new IndexOutOfBoundsException(); 
    int oldId = ((Integer)this.palette.set(index, Integer.valueOf(id))).intValue();
    if (oldId == id)
      return; 
    this.inversePalette.put(Integer.valueOf(id), Integer.valueOf(index));
    if (((Integer)this.inversePalette.get(Integer.valueOf(oldId))).intValue() == index) {
      this.inversePalette.remove(Integer.valueOf(oldId));
      for (int i = 0; i < this.palette.size(); i++) {
        if (((Integer)this.palette.get(i)).intValue() == oldId) {
          this.inversePalette.put(Integer.valueOf(oldId), Integer.valueOf(i));
          break;
        } 
      } 
    } 
  }
  
  public void replacePaletteEntry(int oldId, int newId) {
    Integer index = this.inversePalette.remove(Integer.valueOf(oldId));
    if (index == null)
      return; 
    this.inversePalette.put(Integer.valueOf(newId), index);
    for (int i = 0; i < this.palette.size(); i++) {
      if (((Integer)this.palette.get(i)).intValue() == oldId)
        this.palette.set(i, Integer.valueOf(newId)); 
    } 
  }
  
  public void addPaletteEntry(int id) {
    this.inversePalette.put(Integer.valueOf(id), Integer.valueOf(this.palette.size()));
    this.palette.add(Integer.valueOf(id));
  }
  
  public void clearPalette() {
    this.palette.clear();
    this.inversePalette.clear();
  }
  
  public void setFlatBlock(int idx, int id) {
    Integer index = this.inversePalette.get(Integer.valueOf(id));
    if (index == null) {
      index = Integer.valueOf(this.palette.size());
      this.palette.add(Integer.valueOf(id));
      this.inversePalette.put(Integer.valueOf(id), index);
    } 
    this.blocks[idx] = index.intValue();
  }
  
  public void setBlockLight(byte[] data) {
    if (data.length != 2048)
      throw new IllegalArgumentException("Data length != 2048"); 
    if (this.blockLight == null) {
      this.blockLight = new NibbleArray(data);
    } else {
      this.blockLight.setHandle(data);
    } 
  }
  
  public void setSkyLight(byte[] data) {
    if (data.length != 2048)
      throw new IllegalArgumentException("Data length != 2048"); 
    if (this.skyLight == null) {
      this.skyLight = new NibbleArray(data);
    } else {
      this.skyLight.setHandle(data);
    } 
  }
  
  public byte[] getBlockLight() {
    return (this.blockLight == null) ? null : this.blockLight.getHandle();
  }
  
  public NibbleArray getBlockLightNibbleArray() {
    return this.blockLight;
  }
  
  public byte[] getSkyLight() {
    return (this.skyLight == null) ? null : this.skyLight.getHandle();
  }
  
  public NibbleArray getSkyLightNibbleArray() {
    return this.skyLight;
  }
  
  public void readBlockLight(ByteBuf input) {
    if (this.blockLight == null)
      this.blockLight = new NibbleArray(4096); 
    input.readBytes(this.blockLight.getHandle());
  }
  
  public void readSkyLight(ByteBuf input) {
    if (this.skyLight == null)
      this.skyLight = new NibbleArray(4096); 
    input.readBytes(this.skyLight.getHandle());
  }
  
  public static int index(int x, int y, int z) {
    return y << 8 | z << 4 | x;
  }
  
  public void writeBlockLight(ByteBuf output) {
    output.writeBytes(this.blockLight.getHandle());
  }
  
  public void writeSkyLight(ByteBuf output) {
    output.writeBytes(this.skyLight.getHandle());
  }
  
  public boolean hasSkyLight() {
    return (this.skyLight != null);
  }
  
  public boolean hasBlockLight() {
    return (this.blockLight != null);
  }
}
