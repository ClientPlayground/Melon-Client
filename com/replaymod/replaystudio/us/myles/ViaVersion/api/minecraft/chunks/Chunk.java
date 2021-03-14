package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import java.util.List;

public interface Chunk {
  int getX();
  
  int getZ();
  
  boolean isBiomeData();
  
  int getBitmask();
  
  ChunkSection[] getSections();
  
  int[] getBiomeData();
  
  void setBiomeData(int[] paramArrayOfint);
  
  CompoundTag getHeightMap();
  
  void setHeightMap(CompoundTag paramCompoundTag);
  
  List<CompoundTag> getBlockEntities();
  
  boolean isGroundUp();
}
