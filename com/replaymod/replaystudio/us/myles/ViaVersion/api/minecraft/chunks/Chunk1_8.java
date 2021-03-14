package com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks;

import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import java.util.ArrayList;
import java.util.List;

public class Chunk1_8 extends BaseChunk {
  private boolean unloadPacket = false;
  
  public boolean isUnloadPacket() {
    return this.unloadPacket;
  }
  
  public Chunk1_8(int x, int z, boolean groundUp, int bitmask, ChunkSection[] sections, int[] biomeData, List<CompoundTag> blockEntities) {
    super(x, z, groundUp, bitmask, sections, biomeData, blockEntities);
  }
  
  public Chunk1_8(int x, int z) {
    this(x, z, true, 0, new ChunkSection[16], (int[])null, new ArrayList<>());
    this.unloadPacket = true;
  }
  
  public boolean hasBiomeData() {
    return (this.biomeData != null && this.groundUp);
  }
  
  public boolean isBiomeData() {
    return (this.biomeData != null);
  }
}
