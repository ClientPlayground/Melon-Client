package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class FastLzFrameEncoder extends MessageToByteEncoder<ByteBuf> {
  private final int level;
  
  private final Checksum checksum;
  
  public FastLzFrameEncoder() {
    this(0, null);
  }
  
  public FastLzFrameEncoder(int level) {
    this(level, null);
  }
  
  public FastLzFrameEncoder(boolean validateChecksums) {
    this(0, validateChecksums ? new Adler32() : null);
  }
  
  public FastLzFrameEncoder(int level, Checksum checksum) {
    super(false);
    if (level != 0 && level != 1 && level != 2)
      throw new IllegalArgumentException(String.format("level: %d (expected: %d or %d or %d)", new Object[] { Integer.valueOf(level), Integer.valueOf(0), Integer.valueOf(1), Integer.valueOf(2) })); 
    this.level = level;
    this.checksum = checksum;
  }
  
  protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
    Checksum checksum = this.checksum;
    while (true) {
      byte blockType;
      int chunkLength;
      if (!in.isReadable())
        return; 
      int idx = in.readerIndex();
      int length = Math.min(in.readableBytes(), 65535);
      int outputIdx = out.writerIndex();
      out.setMedium(outputIdx, 4607066);
      int outputOffset = outputIdx + 4 + ((checksum != null) ? 4 : 0);
      if (length < 32) {
        blockType = 0;
        out.ensureWritable(outputOffset + 2 + length);
        byte[] output = out.array();
        int outputPtr = out.arrayOffset() + outputOffset + 2;
        if (checksum != null) {
          byte[] input;
          int inputPtr;
          if (in.hasArray()) {
            input = in.array();
            inputPtr = in.arrayOffset() + idx;
          } else {
            input = new byte[length];
            in.getBytes(idx, input);
            inputPtr = 0;
          } 
          checksum.reset();
          checksum.update(input, inputPtr, length);
          out.setInt(outputIdx + 4, (int)checksum.getValue());
          System.arraycopy(input, inputPtr, output, outputPtr, length);
        } else {
          in.getBytes(idx, output, outputPtr, length);
        } 
        chunkLength = length;
      } else {
        byte[] input;
        int inputPtr;
        if (in.hasArray()) {
          input = in.array();
          inputPtr = in.arrayOffset() + idx;
        } else {
          input = new byte[length];
          in.getBytes(idx, input);
          inputPtr = 0;
        } 
        if (checksum != null) {
          checksum.reset();
          checksum.update(input, inputPtr, length);
          out.setInt(outputIdx + 4, (int)checksum.getValue());
        } 
        int maxOutputLength = FastLz.calculateOutputBufferLength(length);
        out.ensureWritable(outputOffset + 4 + maxOutputLength);
        byte[] output = out.array();
        int outputPtr = out.arrayOffset() + outputOffset + 4;
        int compressedLength = FastLz.compress(input, inputPtr, length, output, outputPtr, this.level);
        if (compressedLength < length) {
          blockType = 1;
          chunkLength = compressedLength;
          out.setShort(outputOffset, chunkLength);
          outputOffset += 2;
        } else {
          blockType = 0;
          System.arraycopy(input, inputPtr, output, outputPtr - 2, length);
          chunkLength = length;
        } 
      } 
      out.setShort(outputOffset, length);
      out.setByte(outputIdx + 3, blockType | ((checksum != null) ? 16 : 0));
      out.writerIndex(outputOffset + 2 + chunkLength);
      in.skipBytes(length);
    } 
  }
}
