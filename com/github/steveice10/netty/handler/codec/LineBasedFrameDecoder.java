package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.ByteProcessor;
import java.util.List;

public class LineBasedFrameDecoder extends ByteToMessageDecoder {
  private final int maxLength;
  
  private final boolean failFast;
  
  private final boolean stripDelimiter;
  
  private boolean discarding;
  
  private int discardedBytes;
  
  private int offset;
  
  public LineBasedFrameDecoder(int maxLength) {
    this(maxLength, true, false);
  }
  
  public LineBasedFrameDecoder(int maxLength, boolean stripDelimiter, boolean failFast) {
    this.maxLength = maxLength;
    this.failFast = failFast;
    this.stripDelimiter = stripDelimiter;
  }
  
  protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Object decoded = decode(ctx, in);
    if (decoded != null)
      out.add(decoded); 
  }
  
  protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
    int eol = findEndOfLine(buffer);
    if (!this.discarding) {
      if (eol >= 0) {
        ByteBuf frame;
        int i = eol - buffer.readerIndex();
        int delimLength = (buffer.getByte(eol) == 13) ? 2 : 1;
        if (i > this.maxLength) {
          buffer.readerIndex(eol + delimLength);
          fail(ctx, i);
          return null;
        } 
        if (this.stripDelimiter) {
          frame = buffer.readRetainedSlice(i);
          buffer.skipBytes(delimLength);
        } else {
          frame = buffer.readRetainedSlice(i + delimLength);
        } 
        return frame;
      } 
      int length = buffer.readableBytes();
      if (length > this.maxLength) {
        this.discardedBytes = length;
        buffer.readerIndex(buffer.writerIndex());
        this.discarding = true;
        this.offset = 0;
        if (this.failFast)
          fail(ctx, "over " + this.discardedBytes); 
      } 
      return null;
    } 
    if (eol >= 0) {
      int length = this.discardedBytes + eol - buffer.readerIndex();
      int delimLength = (buffer.getByte(eol) == 13) ? 2 : 1;
      buffer.readerIndex(eol + delimLength);
      this.discardedBytes = 0;
      this.discarding = false;
      if (!this.failFast)
        fail(ctx, length); 
    } else {
      this.discardedBytes += buffer.readableBytes();
      buffer.readerIndex(buffer.writerIndex());
    } 
    return null;
  }
  
  private void fail(ChannelHandlerContext ctx, int length) {
    fail(ctx, String.valueOf(length));
  }
  
  private void fail(ChannelHandlerContext ctx, String length) {
    ctx.fireExceptionCaught(new TooLongFrameException("frame length (" + length + ") exceeds the allowed maximum (" + this.maxLength + ')'));
  }
  
  private int findEndOfLine(ByteBuf buffer) {
    int totalLength = buffer.readableBytes();
    int i = buffer.forEachByte(buffer.readerIndex() + this.offset, totalLength - this.offset, ByteProcessor.FIND_LF);
    if (i >= 0) {
      this.offset = 0;
      if (i > 0 && buffer.getByte(i - 1) == 13)
        i--; 
    } else {
      this.offset = totalLength;
    } 
    return i;
  }
}
