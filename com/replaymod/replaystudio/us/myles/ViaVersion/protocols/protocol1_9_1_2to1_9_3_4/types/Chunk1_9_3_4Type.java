package com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_1_2to1_9_3_4.types;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.opennbt.tag.builtin.CompoundTag;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.Via;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.Environment;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.BaseChunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.Chunk;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.PartialType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.minecraft.BaseChunkType;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version.Types1_9;
import com.replaymod.replaystudio.us.myles.ViaVersion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class Chunk1_9_3_4Type extends PartialType<Chunk, ClientWorld> {
  public Chunk1_9_3_4Type(ClientWorld param) {
    super(param, Chunk.class);
  }
  
  public Chunk read(ByteBuf input, ClientWorld world) throws Exception {
    int chunkX = input.readInt();
    int chunkZ = input.readInt();
    boolean groundUp = input.readBoolean();
    int primaryBitmask = ((Integer)Type.VAR_INT.read(input)).intValue();
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
        ChunkSection section = (ChunkSection)Types1_9.CHUNK_SECTION.read(input);
        sections[i] = section;
        section.readBlockLight(input);
        if (world.getEnvironment() == Environment.NORMAL)
          section.readSkyLight(input); 
      } 
    } 
    int[] biomeData = groundUp ? new int[256] : null;
    if (groundUp)
      for (int j = 0; j < 256; j++)
        biomeData[j] = input.readByte() & 0xFF;  
    List<CompoundTag> nbtData = new ArrayList<>(Arrays.asList((Object[])Type.NBT_ARRAY.read(input)));
    if (input.readableBytes() > 0) {
      byte[] array = (byte[])Type.REMAINING_BYTES.read(input);
      if (Via.getManager().isDebug())
        Via.getPlatform().getLogger().warning("Found " + array.length + " more bytes than expected while reading the chunk: " + chunkX + "/" + chunkZ); 
    } 
    return (Chunk)new BaseChunk(chunkX, chunkZ, groundUp, primaryBitmask, sections, biomeData, nbtData);
  }
  
  public void write(ByteBuf output, ClientWorld world, Chunk chunk) throws Exception {
    output.writeInt(chunk.getX());
    output.writeInt(chunk.getZ());
    output.writeBoolean(chunk.isGroundUp());
    Type.VAR_INT.write(output, Integer.valueOf(chunk.getBitmask()));
    ByteBuf buf = output.alloc().buffer();
    try {
      for (int i = 0; i < 16; i++) {
        ChunkSection section = chunk.getSections()[i];
        if (section != null) {
          Types1_9.CHUNK_SECTION.write(buf, section);
          section.writeBlockLight(buf);
          if (section.hasSkyLight())
            section.writeSkyLight(buf); 
        } 
      } 
      buf.readerIndex(0);
      Type.VAR_INT.write(output, Integer.valueOf(buf.readableBytes() + (chunk.isBiomeData() ? 256 : 0)));
      output.writeBytes(buf);
    } finally {
      buf.release();
    } 
    if (chunk.isBiomeData())
      for (int biome : chunk.getBiomeData())
        output.writeByte((byte)biome);  
    Type.NBT_ARRAY.write(output, chunk.getBlockEntities().toArray((Object[])new CompoundTag[0]));
  }
  
  public Class<? extends Type> getBaseClass() {
    return (Class)BaseChunkType.class;
  }
}
