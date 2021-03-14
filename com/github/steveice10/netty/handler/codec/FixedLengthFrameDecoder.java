package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.util.List;

public class FixedLengthFrameDecoder extends ByteToMessageDecoder {
  private final int frameLength;
  
  public FixedLengthFrameDecoder(int frameLength) {
    if (frameLength <= 0)
      throw new IllegalArgumentException("frameLength must be a positive integer: " + frameLength); 
    this.frameLength = frameLength;
  }
  
  protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Object decoded = decode(ctx, in);
    if (decoded != null)
      out.add(decoded); 
  }
  
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    if (in.readableBytes() < this.frameLength)
      return null; 
    return in.readRetainedSlice(this.frameLength);
  }
}
