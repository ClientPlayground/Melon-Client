package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import java.util.List;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

public class FastLzFrameDecoder extends ByteToMessageDecoder {
  private enum State {
    INIT_BLOCK, INIT_BLOCK_PARAMS, DECOMPRESS_DATA, CORRUPTED;
  }
  
  private State currentState = State.INIT_BLOCK;
  
  private final Checksum checksum;
  
  private int chunkLength;
  
  private int originalLength;
  
  private boolean isCompressed;
  
  private boolean hasChecksum;
  
  private int currentChecksum;
  
  public FastLzFrameDecoder() {
    this(false);
  }
  
  public FastLzFrameDecoder(boolean validateChecksums) {
    this(validateChecksums ? new Adler32() : null);
  }
  
  public FastLzFrameDecoder(Checksum checksum) {
    this.checksum = checksum;
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    try {
      int magic;
      byte options;
      int chunkLength;
      switch (this.currentState) {
        case INIT_BLOCK:
          if (in.readableBytes() < 4)
            return; 
          magic = in.readUnsignedMedium();
          if (magic != 4607066)
            throw new DecompressionException("unexpected block identifier"); 
          options = in.readByte();
          this.isCompressed = ((options & 0x1) == 1);
          this.hasChecksum = ((options & 0x10) == 16);
          this.currentState = State.INIT_BLOCK_PARAMS;
        case INIT_BLOCK_PARAMS:
          if (in.readableBytes() < 2 + (this.isCompressed ? 2 : 0) + (this.hasChecksum ? 4 : 0))
            return; 
          this.currentChecksum = this.hasChecksum ? in.readInt() : 0;
          this.chunkLength = in.readUnsignedShort();
          this.originalLength = this.isCompressed ? in.readUnsignedShort() : this.chunkLength;
          this.currentState = State.DECOMPRESS_DATA;
        case DECOMPRESS_DATA:
          chunkLength = this.chunkLength;
          if (in.readableBytes() >= chunkLength) {
            ByteBuf uncompressed;
            byte[] output;
            int outputPtr, idx = in.readerIndex();
            int originalLength = this.originalLength;
            if (originalLength != 0) {
              uncompressed = ctx.alloc().heapBuffer(originalLength, originalLength);
              output = uncompressed.array();
              outputPtr = uncompressed.arrayOffset() + uncompressed.writerIndex();
            } else {
              uncompressed = null;
              output = EmptyArrays.EMPTY_BYTES;
              outputPtr = 0;
            } 
            boolean success = false;
            try {
              if (this.isCompressed) {
                byte[] input;
                int inputPtr;
                if (in.hasArray()) {
                  input = in.array();
                  inputPtr = in.arrayOffset() + idx;
                } else {
                  input = new byte[chunkLength];
                  in.getBytes(idx, input);
                  inputPtr = 0;
                } 
                int decompressedBytes = FastLz.decompress(input, inputPtr, chunkLength, output, outputPtr, originalLength);
                if (originalLength != decompressedBytes)
                  throw new DecompressionException(String.format("stream corrupted: originalLength(%d) and actual length(%d) mismatch", new Object[] { Integer.valueOf(originalLength), Integer.valueOf(decompressedBytes) })); 
              } else {
                in.getBytes(idx, output, outputPtr, chunkLength);
              } 
              Checksum checksum = this.checksum;
              if (this.hasChecksum && checksum != null) {
                checksum.reset();
                checksum.update(output, outputPtr, originalLength);
                int checksumResult = (int)checksum.getValue();
                if (checksumResult != this.currentChecksum)
                  throw new DecompressionException(String.format("stream corrupted: mismatching checksum: %d (expected: %d)", new Object[] { Integer.valueOf(checksumResult), Integer.valueOf(this.currentChecksum) })); 
              } 
              if (uncompressed != null) {
                uncompressed.writerIndex(uncompressed.writerIndex() + originalLength);
                out.add(uncompressed);
              } 
              in.skipBytes(chunkLength);
              this.currentState = State.INIT_BLOCK;
              success = true;
            } finally {
              if (!success && uncompressed != null)
                uncompressed.release(); 
            } 
          } 
          return;
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
}
