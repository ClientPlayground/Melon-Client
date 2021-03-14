package com.replaymod.replaystudio.us.myles.ViaVersion.api.type.types.version;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.minecraft.chunks.ChunkSection;
import com.replaymod.replaystudio.us.myles.ViaVersion.api.type.Type;

public class ChunkSectionType1_13 extends Type<ChunkSection> {
  private static final int GLOBAL_PALETTE = 14;
  
  public ChunkSectionType1_13() {
    super("Chunk Section Type", ChunkSection.class);
  }
  
  public ChunkSection read(ByteBuf buffer) throws Exception {
    ChunkSection chunkSection = new ChunkSection();
    int bitsPerBlock = buffer.readUnsignedByte();
    int originalBitsPerBlock = bitsPerBlock;
    if (bitsPerBlock == 0 || bitsPerBlock > 8)
      bitsPerBlock = 14; 
    long maxEntryValue = (1L << bitsPerBlock) - 1L;
    int paletteLength = (bitsPerBlock == 14) ? 0 : ((Integer)Type.VAR_INT.read(buffer)).intValue();
    chunkSection.clearPalette();
    for (int i = 0; i < paletteLength; i++)
      chunkSection.addPaletteEntry(((Integer)Type.VAR_INT.read(buffer)).intValue()); 
    long[] blockData = new long[((Integer)Type.VAR_INT.read(buffer)).intValue()];
    if (blockData.length > 0) {
      int expectedLength = (int)Math.ceil((4096 * bitsPerBlock) / 64.0D);
      if (blockData.length != expectedLength)
        throw new IllegalStateException("Block data length (" + blockData.length + ") does not match expected length (" + expectedLength + ")! bitsPerBlock=" + bitsPerBlock + ", originalBitsPerBlock=" + originalBitsPerBlock); 
      int j;
      for (j = 0; j < blockData.length; j++)
        blockData[j] = buffer.readLong(); 
      for (j = 0; j < 4096; j++) {
        int val, bitIndex = j * bitsPerBlock;
        int startIndex = bitIndex / 64;
        int endIndex = ((j + 1) * bitsPerBlock - 1) / 64;
        int startBitSubIndex = bitIndex % 64;
        if (startIndex == endIndex) {
          val = (int)(blockData[startIndex] >>> startBitSubIndex & maxEntryValue);
        } else {
          int endBitSubIndex = 64 - startBitSubIndex;
          val = (int)((blockData[startIndex] >>> startBitSubIndex | blockData[endIndex] << endBitSubIndex) & maxEntryValue);
        } 
        if (bitsPerBlock == 14) {
          chunkSection.setFlatBlock(j, val);
        } else {
          chunkSection.setPaletteIndex(j, val);
        } 
      } 
    } 
    return chunkSection;
  }
  
  public void write(ByteBuf buffer, ChunkSection chunkSection) throws Exception {
    int bitsPerBlock = 4;
    while (chunkSection.getPaletteSize() > 1 << bitsPerBlock)
      bitsPerBlock++; 
    if (bitsPerBlock > 8)
      bitsPerBlock = 14; 
    long maxEntryValue = (1L << bitsPerBlock) - 1L;
    buffer.writeByte(bitsPerBlock);
    if (bitsPerBlock != 14) {
      Type.VAR_INT.write(buffer, Integer.valueOf(chunkSection.getPaletteSize()));
      for (int i = 0; i < chunkSection.getPaletteSize(); i++)
        Type.VAR_INT.write(buffer, Integer.valueOf(chunkSection.getPaletteEntry(i))); 
    } 
    int length = (int)Math.ceil((4096 * bitsPerBlock) / 64.0D);
    Type.VAR_INT.write(buffer, Integer.valueOf(length));
    long[] data = new long[length];
    for (int index = 0; index < 4096; index++) {
      int value = (bitsPerBlock == 14) ? chunkSection.getFlatBlock(index) : chunkSection.getPaletteIndex(index);
      int bitIndex = index * bitsPerBlock;
      int startIndex = bitIndex / 64;
      int endIndex = ((index + 1) * bitsPerBlock - 1) / 64;
      int startBitSubIndex = bitIndex % 64;
      data[startIndex] = data[startIndex] & (maxEntryValue << startBitSubIndex ^ 0xFFFFFFFFFFFFFFFFL) | (value & maxEntryValue) << startBitSubIndex;
      if (startIndex != endIndex) {
        int endBitSubIndex = 64 - startBitSubIndex;
        data[endIndex] = data[endIndex] >>> endBitSubIndex << endBitSubIndex | (value & maxEntryValue) >> endBitSubIndex;
      } 
    } 
    for (long l : data)
      buffer.writeLong(l); 
  }
}
