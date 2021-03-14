package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft;

import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public abstract class BaseChunkType extends Type<Chunk> {
  public BaseChunkType() {
    super(Chunk.class);
  }
  
  public BaseChunkType(String typeName) {
    super(typeName, Chunk.class);
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)BaseChunkType.class;
  }
}
