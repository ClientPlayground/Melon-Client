package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import java.nio.ByteOrder;

public class ChunkSectionType1_8 extends Type<ChunkSection> {
  public ChunkSectionType1_8() {
    super("Chunk Section Type", ChunkSection.class);
  }
  
  public ChunkSection read(ByteBuf buffer) throws Exception {
    ChunkSection chunkSection = new ChunkSection();
    ByteBuf littleEndianView = buffer.order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < 4096; i++) {
      int mask = littleEndianView.readShort();
      int type = mask >> 4;
      int data = mask & 0xF;
      chunkSection.setBlock(i, type, data);
    } 
    return chunkSection;
  }
  
  public void write(ByteBuf buffer, ChunkSection chunkSection) throws Exception {
    throw new UnsupportedOperationException();
  }
}
