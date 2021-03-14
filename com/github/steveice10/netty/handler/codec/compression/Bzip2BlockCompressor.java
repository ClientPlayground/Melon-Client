package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.ByteProcessor;

final class Bzip2BlockCompressor {
  private final ByteProcessor writeProcessor = new ByteProcessor() {
      public boolean process(byte value) throws Exception {
        return Bzip2BlockCompressor.this.write(value);
      }
    };
  
  private final Bzip2BitWriter writer;
  
  private final Crc32 crc = new Crc32();
  
  private final byte[] block;
  
  private int blockLength;
  
  private final int blockLengthLimit;
  
  private final boolean[] blockValuesPresent = new boolean[256];
  
  private final int[] bwtBlock;
  
  private int rleCurrentValue = -1;
  
  private int rleLength;
  
  Bzip2BlockCompressor(Bzip2BitWriter writer, int blockSize) {
    this.writer = writer;
    this.block = new byte[blockSize + 1];
    this.bwtBlock = new int[blockSize + 1];
    this.blockLengthLimit = blockSize - 6;
  }
  
  private void writeSymbolMap(ByteBuf out) {
    Bzip2BitWriter writer = this.writer;
    boolean[] blockValuesPresent = this.blockValuesPresent;
    boolean[] condensedInUse = new boolean[16];
    int i;
    for (i = 0; i < condensedInUse.length; i++) {
      for (int j = 0, k = i << 4; j < 16; j++, k++) {
        if (blockValuesPresent[k])
          condensedInUse[i] = true; 
      } 
    } 
    for (boolean isCondensedInUse : condensedInUse)
      writer.writeBoolean(out, isCondensedInUse); 
    for (i = 0; i < condensedInUse.length; i++) {
      if (condensedInUse[i])
        for (int j = 0, k = i << 4; j < 16; j++, k++)
          writer.writeBoolean(out, blockValuesPresent[k]);  
    } 
  }
  
  private void writeRun(int value, int runLength) {
    int blockLength = this.blockLength;
    byte[] block = this.block;
    this.blockValuesPresent[value] = true;
    this.crc.updateCRC(value, runLength);
    byte byteValue = (byte)value;
    switch (runLength) {
      case 1:
        block[blockLength] = byteValue;
        this.blockLength = blockLength + 1;
        return;
      case 2:
        block[blockLength] = byteValue;
        block[blockLength + 1] = byteValue;
        this.blockLength = blockLength + 2;
        return;
      case 3:
        block[blockLength] = byteValue;
        block[blockLength + 1] = byteValue;
        block[blockLength + 2] = byteValue;
        this.blockLength = blockLength + 3;
        return;
    } 
    runLength -= 4;
    this.blockValuesPresent[runLength] = true;
    block[blockLength] = byteValue;
    block[blockLength + 1] = byteValue;
    block[blockLength + 2] = byteValue;
    block[blockLength + 3] = byteValue;
    block[blockLength + 4] = (byte)runLength;
    this.blockLength = blockLength + 5;
  }
  
  boolean write(int value) {
    if (this.blockLength > this.blockLengthLimit)
      return false; 
    int rleCurrentValue = this.rleCurrentValue;
    int rleLength = this.rleLength;
    if (rleLength == 0) {
      this.rleCurrentValue = value;
      this.rleLength = 1;
    } else if (rleCurrentValue != value) {
      writeRun(rleCurrentValue & 0xFF, rleLength);
      this.rleCurrentValue = value;
      this.rleLength = 1;
    } else if (rleLength == 254) {
      writeRun(rleCurrentValue & 0xFF, 255);
      this.rleLength = 0;
    } else {
      this.rleLength = rleLength + 1;
    } 
    return true;
  }
  
  int write(ByteBuf buffer, int offset, int length) {
    int index = buffer.forEachByte(offset, length, this.writeProcessor);
    return (index == -1) ? length : (index - offset);
  }
  
  void close(ByteBuf out) {
    if (this.rleLength > 0)
      writeRun(this.rleCurrentValue & 0xFF, this.rleLength); 
    this.block[this.blockLength] = this.block[0];
    Bzip2DivSufSort divSufSort = new Bzip2DivSufSort(this.block, this.bwtBlock, this.blockLength);
    int bwtStartPointer = divSufSort.bwt();
    Bzip2BitWriter writer = this.writer;
    writer.writeBits(out, 24, 3227993L);
    writer.writeBits(out, 24, 2511705L);
    writer.writeInt(out, this.crc.getCRC());
    writer.writeBoolean(out, false);
    writer.writeBits(out, 24, bwtStartPointer);
    writeSymbolMap(out);
    Bzip2MTFAndRLE2StageEncoder mtfEncoder = new Bzip2MTFAndRLE2StageEncoder(this.bwtBlock, this.blockLength, this.blockValuesPresent);
    mtfEncoder.encode();
    Bzip2HuffmanStageEncoder huffmanEncoder = new Bzip2HuffmanStageEncoder(writer, mtfEncoder.mtfBlock(), mtfEncoder.mtfLength(), mtfEncoder.mtfAlphabetSize(), mtfEncoder.mtfSymbolFrequencies());
    huffmanEncoder.encode(out);
  }
  
  int availableSize() {
    if (this.blockLength == 0)
      return this.blockLengthLimit + 2; 
    return this.blockLengthLimit - this.blockLength + 1;
  }
  
  boolean isFull() {
    return (this.blockLength > this.blockLengthLimit);
  }
  
  boolean isEmpty() {
    return (this.blockLength == 0 && this.rleLength == 0);
  }
  
  int crc() {
    return this.crc.getCRC();
  }
}
