package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import java.util.Arrays;
import java.util.List;

public class BaseChunk implements Chunk {
  protected int x;
  
  protected int z;
  
  protected boolean groundUp;
  
  protected int bitmask;
  
  protected ChunkSection[] sections;
  
  protected int[] biomeData;
  
  protected CompoundTag heightMap;
  
  protected List<CompoundTag> blockEntities;
  
  public BaseChunk(int x, int z, boolean groundUp, int bitmask, ChunkSection[] sections, int[] biomeData, CompoundTag heightMap, List<CompoundTag> blockEntities) {
    this.x = x;
    this.z = z;
    this.groundUp = groundUp;
    this.bitmask = bitmask;
    this.sections = sections;
    this.biomeData = biomeData;
    this.heightMap = heightMap;
    this.blockEntities = blockEntities;
  }
  
  public void setX(int x) {
    this.x = x;
  }
  
  public void setZ(int z) {
    this.z = z;
  }
  
  public void setGroundUp(boolean groundUp) {
    this.groundUp = groundUp;
  }
  
  public void setBitmask(int bitmask) {
    this.bitmask = bitmask;
  }
  
  public void setSections(ChunkSection[] sections) {
    this.sections = sections;
  }
  
  public void setBiomeData(int[] biomeData) {
    this.biomeData = biomeData;
  }
  
  public void setHeightMap(CompoundTag heightMap) {
    this.heightMap = heightMap;
  }
  
  public void setBlockEntities(List<CompoundTag> blockEntities) {
    this.blockEntities = blockEntities;
  }
  
  public boolean equals(Object o) {
    if (o == this)
      return true; 
    if (!(o instanceof BaseChunk))
      return false; 
    BaseChunk other = (BaseChunk)o;
    if (!other.canEqual(this))
      return false; 
    if (getX() != other.getX())
      return false; 
    if (getZ() != other.getZ())
      return false; 
    if (isGroundUp() != other.isGroundUp())
      return false; 
    if (getBitmask() != other.getBitmask())
      return false; 
    if (!Arrays.deepEquals((Object[])getSections(), (Object[])other.getSections()))
      return false; 
    if (!Arrays.equals(getBiomeData(), other.getBiomeData()))
      return false; 
    Object this$heightMap = getHeightMap(), other$heightMap = other.getHeightMap();
    if ((this$heightMap == null) ? (other$heightMap != null) : !this$heightMap.equals(other$heightMap))
      return false; 
    Object<CompoundTag> this$blockEntities = (Object<CompoundTag>)getBlockEntities(), other$blockEntities = (Object<CompoundTag>)other.getBlockEntities();
    return !((this$blockEntities == null) ? (other$blockEntities != null) : !this$blockEntities.equals(other$blockEntities));
  }
  
  protected boolean canEqual(Object other) {
    return other instanceof BaseChunk;
  }
  
  public int hashCode() {
    int PRIME = 59;
    result = 1;
    result = result * 59 + getX();
    result = result * 59 + getZ();
    result = result * 59 + (isGroundUp() ? 79 : 97);
    result = result * 59 + getBitmask();
    result = result * 59 + Arrays.deepHashCode((Object[])getSections());
    result = result * 59 + Arrays.hashCode(getBiomeData());
    Object $heightMap = getHeightMap();
    result = result * 59 + (($heightMap == null) ? 43 : $heightMap.hashCode());
    Object<CompoundTag> $blockEntities = (Object<CompoundTag>)getBlockEntities();
    return result * 59 + (($blockEntities == null) ? 43 : $blockEntities.hashCode());
  }
  
  public String toString() {
    return "BaseChunk(x=" + getX() + ", z=" + getZ() + ", groundUp=" + isGroundUp() + ", bitmask=" + getBitmask() + ", sections=" + Arrays.deepToString((Object[])getSections()) + ", biomeData=" + Arrays.toString(getBiomeData()) + ", heightMap=" + getHeightMap() + ", blockEntities=" + getBlockEntities() + ")";
  }
  
  public int getX() {
    return this.x;
  }
  
  public int getZ() {
    return this.z;
  }
  
  public boolean isGroundUp() {
    return this.groundUp;
  }
  
  public int getBitmask() {
    return this.bitmask;
  }
  
  public ChunkSection[] getSections() {
    return this.sections;
  }
  
  public int[] getBiomeData() {
    return this.biomeData;
  }
  
  public CompoundTag getHeightMap() {
    return this.heightMap;
  }
  
  public List<CompoundTag> getBlockEntities() {
    return this.blockEntities;
  }
  
  public BaseChunk(int x, int z, boolean groundUp, int bitmask, ChunkSection[] sections, int[] biomeData, List<CompoundTag> blockEntities) {
    this.x = x;
    this.z = z;
    this.groundUp = groundUp;
    this.bitmask = bitmask;
    this.sections = sections;
    this.biomeData = biomeData;
    this.blockEntities = blockEntities;
  }
  
  public boolean isBiomeData() {
    return (this.biomeData != null);
  }
}
