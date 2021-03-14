package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import java.util.zip.Checksum;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.xxhash.XXHashFactory;

public class Lz4FrameDecoder extends ByteToMessageDecoder {
  private enum State {
    INIT_BLOCK, DECOMPRESS_DATA, FINISHED, CORRUPTED;
  }
  
  private State currentState = State.INIT_BLOCK;
  
  private LZ4FastDecompressor decompressor;
  
  private ByteBufChecksum checksum;
  
  private int blockType;
  
  private int compressedLength;
  
  private int decompressedLength;
  
  private int currentChecksum;
  
  public Lz4FrameDecoder() {
    this(false);
  }
  
  public Lz4FrameDecoder(boolean validateChecksums) {
    this(LZ4Factory.fastestInstance(), validateChecksums);
  }
  
  public Lz4FrameDecoder(LZ4Factory factory, boolean validateChecksums) {
    this(factory, validateChecksums ? 
        XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum() : null);
  }
  
  public Lz4FrameDecoder(LZ4Factory factory, Checksum checksum) {
    if (factory == null)
      throw new NullPointerException("factory"); 
    this.decompressor = factory.fastDecompressor();
    this.checksum = (checksum == null) ? null : ByteBufChecksum.wrapChecksum(checksum);
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      int blockType;
      int compressedLength;
      int decompressedLength;
      int currentChecksum;
      switch (this.currentState) {
        case INIT_BLOCK:
          if (in.readableBytes() >= 21) {
            long magic = in.readLong();
            if (magic != 5501767354678207339L)
              throw new DecompressionException("unexpected block identifier"); 
            int token = in.readByte();
            int compressionLevel = (token & 0xF) + 10;
            int i = token & 0xF0;
            int j = Integer.reverseBytes(in.readInt());
            if (j < 0 || j > 33554432)
              throw new DecompressionException(String.format("invalid compressedLength: %d (expected: 0-%d)", new Object[] { Integer.valueOf(j), Integer.valueOf(33554432) })); 
            int k = Integer.reverseBytes(in.readInt());
            int maxDecompressedLength = 1 << compressionLevel;
            if (k < 0 || k > maxDecompressedLength)
              throw new DecompressionException(String.format("invalid decompressedLength: %d (expected: 0-%d)", new Object[] { Integer.valueOf(k), Integer.valueOf(maxDecompressedLength) })); 
            if ((k == 0 && j != 0) || (k != 0 && j == 0) || (i == 16 && k != j))
              throw new DecompressionException(String.format("stream corrupted: compressedLength(%d) and decompressedLength(%d) mismatch", new Object[] { Integer.valueOf(j), Integer.valueOf(k) })); 
            int m = Integer.reverseBytes(in.readInt());
            if (k == 0 && j == 0) {
              if (m != 0)
                throw new DecompressionException("stream corrupted: checksum error"); 
              this.currentState = State.FINISHED;
              this.decompressor = null;
              this.checksum = null;
            } else {
              this.blockType = i;
              this.compressedLength = j;
              this.decompressedLength = k;
              this.currentChecksum = m;
              this.currentState = State.DECOMPRESS_DATA;
            } 
          } 
          return;
        case DECOMPRESS_DATA:
          blockType = this.blockType;
          compressedLength = this.compressedLength;
          decompressedLength = this.decompressedLength;
          currentChecksum = this.currentChecksum;
        case FINISHED:
        case CORRUPTED:
          in.skipBytes(in.readableBytes());
          return;
      } 
      throw new IllegalStateException();
    } catch (Exception e) {
      this.currentState = State.CORRUPTED;
      throw e;
    } 
  }
  
  public boolean isClosed() {
    return (this.currentState == State.FINISHED);
  }
}
