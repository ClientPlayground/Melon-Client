package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_14to1_13_2.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.BaseChunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.PartialType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_13;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class Chunk1_14Type extends PartialType<Chunk, ClientWorld> {
  public Chunk1_14Type(ClientWorld param) {
    super(param, Chunk.class);
  }
  
  public Chunk read(ByteBuf input, ClientWorld world) throws Exception {
    int chunkX = input.readInt();
    int chunkZ = input.readInt();
    boolean groundUp = input.readBoolean();
    int primaryBitmask = ((Integer)Type.VAR_INT.read(input)).intValue();
    CompoundTag heightMap = (CompoundTag)Type.NBT.read(input);
    Type.VAR_INT.read(input);
    BitSet usedSections = new BitSet(16);
    ChunkSection[] sections = new ChunkSection[16];
    int i;
    for (i = 0; i < 16; i++) {
      if ((primaryBitmask & 1 << i) != 0)
        usedSections.set(i); 
    } 
    for (i = 0; i < 16; i++) {
      if (usedSections.get(i)) {
        short nonAirBlocksCount = input.readShort();
        ChunkSection section = (ChunkSection)Types1_13.CHUNK_SECTION.read(input);
        section.setNonAirBlocksCount(nonAirBlocksCount);
        sections[i] = section;
      } 
    } 
    int[] biomeData = groundUp ? new int[256] : null;
    if (groundUp)
      for (int j = 0; j < 256; j++)
        biomeData[j] = input.readInt();  
    List<CompoundTag> nbtData = new ArrayList<>(Arrays.asList((Object[])Type.NBT_ARRAY.read(input)));
    if (input.readableBytes() > 0) {
      byte[] array = (byte[])Type.REMAINING_BYTES.read(input);
      if (Via.getManager().isDebug())
        Via.getPlatform().getLogger().warning("Found " + array.length + " more bytes than expected while reading the chunk: " + chunkX + "/" + chunkZ); 
    } 
    return (Chunk)new BaseChunk(chunkX, chunkZ, groundUp, primaryBitmask, sections, biomeData, heightMap, nbtData);
  }
  
  public void write(ByteBuf output, ClientWorld world, Chunk chunk) throws Exception {
    output.writeInt(chunk.getX());
    output.writeInt(chunk.getZ());
    output.writeBoolean(chunk.isGroundUp());
    Type.VAR_INT.write(output, Integer.valueOf(chunk.getBitmask()));
    Type.NBT.write(output, chunk.getHeightMap());
    ByteBuf buf = output.alloc().buffer();
    try {
      for (int i = 0; i < 16; i++) {
        ChunkSection section = chunk.getSections()[i];
        if (section != null) {
          buf.writeShort(section.getNonAirBlocksCount());
          Types1_13.CHUNK_SECTION.write(buf, section);
        } 
      } 
      buf.readerIndex(0);
      Type.VAR_INT.write(output, Integer.valueOf(buf.readableBytes() + (chunk.isBiomeData() ? 1024 : 0)));
      output.writeBytes(buf);
    } finally {
      buf.release();
    } 
    if (chunk.isBiomeData())
      for (int value : chunk.getBiomeData())
        output.writeInt(value & 0xFF);  
    Type.NBT_ARRAY.write(output, chunk.getBlockEntities().toArray((Object[])new CompoundTag[0]));
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)BaseChunkType.class;
  }
}
