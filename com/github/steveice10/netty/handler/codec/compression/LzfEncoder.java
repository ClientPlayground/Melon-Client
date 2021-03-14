package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import com.ning.compress.BufferRecycler;
import com.ning.compress.lzf.ChunkEncoder;
import com.ning.compress.lzf.LZFEncoder;
import com.ning.compress.lzf.util.ChunkEncoderFactory;

public class LzfEncoder extends MessageToByteEncoder<ByteBuf> {
  private static final int MIN_BLOCK_TO_COMPRESS = 16;
  
  private final ChunkEncoder encoder;
  
  private final BufferRecycler recycler;
  
  public LzfEncoder() {
    this(false, 65535);
  }
  
  public LzfEncoder(boolean safeInstance) {
    this(safeInstance, 65535);
  }
  
  public LzfEncoder(int totalLength) {
    this(false, totalLength);
  }
  
  public LzfEncoder(boolean safeInstance, int totalLength) {
    super(false);
    if (totalLength < 16 || totalLength > 65535)
      throw new IllegalArgumentException("totalLength: " + totalLength + " (expected: " + '\020' + '-' + Character.MAX_VALUE + ')'); 
    this
      
      .encoder = safeInstance ? ChunkEncoderFactory.safeNonAllocatingInstance(totalLength) : ChunkEncoderFactory.optimalNonAllocatingInstance(totalLength);
    this.recycler = BufferRecycler.instance();
  }
  
  protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
    byte[] input;
    int inputPtr, length = in.readableBytes();
    int idx = in.readerIndex();
    if (in.hasArray()) {
      input = in.array();
      inputPtr = in.arrayOffset() + idx;
    } else {
      input = this.recycler.allocInputBuffer(length);
      in.getBytes(idx, input, 0, length);
      inputPtr = 0;
    } 
    int maxOutputLength = LZFEncoder.estimateMaxWorkspaceSize(length);
    out.ensureWritable(maxOutputLength);
    byte[] output = out.array();
    int outputPtr = out.arrayOffset() + out.writerIndex();
    int outputLength = LZFEncoder.appendEncoded(this.encoder, input, inputPtr, length, output, outputPtr) - outputPtr;
    out.writerIndex(out.writerIndex() + outputLength);
    in.skipBytes(length);
    if (!in.hasArray())
      this.recycler.releaseInputBuffer(input); 
  }
}
