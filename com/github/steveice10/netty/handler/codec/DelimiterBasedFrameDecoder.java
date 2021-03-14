package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.util.List;

public class DelimiterBasedFrameDecoder extends ByteToMessageDecoder {
  private final ByteBuf[] delimiters;
  
  private final int maxFrameLength;
  
  private final boolean stripDelimiter;
  
  private final boolean failFast;
  
  private boolean discardingTooLongFrame;
  
  private int tooLongFrameLength;
  
  private final LineBasedFrameDecoder lineBasedDecoder;
  
  public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf delimiter) {
    this(maxFrameLength, true, delimiter);
  }
  
  public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf delimiter) {
    this(maxFrameLength, stripDelimiter, true, delimiter);
  }
  
  public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf delimiter) {
    this(maxFrameLength, stripDelimiter, failFast, new ByteBuf[] { delimiter
          .slice(delimiter.readerIndex(), delimiter.readableBytes()) });
  }
  
  public DelimiterBasedFrameDecoder(int maxFrameLength, ByteBuf... delimiters) {
    this(maxFrameLength, true, delimiters);
  }
  
  public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, ByteBuf... delimiters) {
    this(maxFrameLength, stripDelimiter, true, delimiters);
  }
  
  public DelimiterBasedFrameDecoder(int maxFrameLength, boolean stripDelimiter, boolean failFast, ByteBuf... delimiters) {
    validateMaxFrameLength(maxFrameLength);
    if (delimiters == null)
      throw new NullPointerException("delimiters"); 
    if (delimiters.length == 0)
      throw new IllegalArgumentException("empty delimiters"); 
    if (isLineBased(delimiters) && !isSubclass()) {
      this.lineBasedDecoder = new LineBasedFrameDecoder(maxFrameLength, stripDelimiter, failFast);
      this.delimiters = null;
    } else {
      this.delimiters = new ByteBuf[delimiters.length];
      for (int i = 0; i < delimiters.length; i++) {
        ByteBuf d = delimiters[i];
        validateDelimiter(d);
        this.delimiters[i] = d.slice(d.readerIndex(), d.readableBytes());
      } 
      this.lineBasedDecoder = null;
    } 
    this.maxFrameLength = maxFrameLength;
    this.stripDelimiter = stripDelimiter;
    this.failFast = failFast;
  }
  
  private static boolean isLineBased(ByteBuf[] delimiters) {
    if (delimiters.length != 2)
      return false; 
    ByteBuf a = delimiters[0];
    ByteBuf b = delimiters[1];
    if (a.capacity() < b.capacity()) {
      a = delimiters[1];
      b = delimiters[0];
    } 
    return (a.capacity() == 2 && b.capacity() == 1 && a
      .getByte(0) == 13 && a.getByte(1) == 10 && b
      .getByte(0) == 10);
  }
  
  private boolean isSubclass() {
    return (getClass() != DelimiterBasedFrameDecoder.class);
  }
  
  protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    Object decoded = decode(ctx, in);
    if (decoded != null)
      out.add(decoded); 
  }
  
  protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
    if (this.lineBasedDecoder != null)
      return this.lineBasedDecoder.decode(ctx, buffer); 
    int minFrameLength = Integer.MAX_VALUE;
    ByteBuf minDelim = null;
    for (ByteBuf delim : this.delimiters) {
      int frameLength = indexOf(buffer, delim);
      if (frameLength >= 0 && frameLength < minFrameLength) {
        minFrameLength = frameLength;
        minDelim = delim;
      } 
    } 
    if (minDelim != null) {
      ByteBuf frame;
      int minDelimLength = minDelim.capacity();
      if (this.discardingTooLongFrame) {
        this.discardingTooLongFrame = false;
        buffer.skipBytes(minFrameLength + minDelimLength);
        int tooLongFrameLength = this.tooLongFrameLength;
        this.tooLongFrameLength = 0;
        if (!this.failFast)
          fail(tooLongFrameLength); 
        return null;
      } 
      if (minFrameLength > this.maxFrameLength) {
        buffer.skipBytes(minFrameLength + minDelimLength);
        fail(minFrameLength);
        return null;
      } 
      if (this.stripDelimiter) {
        frame = buffer.readRetainedSlice(minFrameLength);
        buffer.skipBytes(minDelimLength);
      } else {
        frame = buffer.readRetainedSlice(minFrameLength + minDelimLength);
      } 
      return frame;
    } 
    if (!this.discardingTooLongFrame) {
      if (buffer.readableBytes() > this.maxFrameLength) {
        this.tooLongFrameLength = buffer.readableBytes();
        buffer.skipBytes(buffer.readableBytes());
        this.discardingTooLongFrame = true;
        if (this.failFast)
          fail(this.tooLongFrameLength); 
      } 
    } else {
      this.tooLongFrameLength += buffer.readableBytes();
      buffer.skipBytes(buffer.readableBytes());
    } 
    return null;
  }
  
  private void fail(long frameLength) {
    if (frameLength > 0L)
      throw new TooLongFrameException("frame length exceeds " + this.maxFrameLength + ": " + frameLength + " - discarded"); 
    throw new TooLongFrameException("frame length exceeds " + this.maxFrameLength + " - discarding");
  }
  
  private static int indexOf(ByteBuf haystack, ByteBuf needle) {
    for (int i = haystack.readerIndex(); i < haystack.writerIndex(); i++) {
      int haystackIndex = i;
      int needleIndex;
      for (needleIndex = 0; needleIndex < needle.capacity() && 
        haystack.getByte(haystackIndex) == needle.getByte(needleIndex); needleIndex++) {
        haystackIndex++;
        if (haystackIndex == haystack.writerIndex() && needleIndex != needle
          .capacity() - 1)
          return -1; 
      } 
      if (needleIndex == needle.capacity())
        return i - haystack.readerIndex(); 
    } 
    return -1;
  }
  
  private static void validateDelimiter(ByteBuf delimiter) {
    if (delimiter == null)
      throw new NullPointerException("delimiter"); 
    if (!delimiter.isReadable())
      throw new IllegalArgumentException("empty delimiter"); 
  }
  
  private static void validateMaxFrameLength(int maxFrameLength) {
    if (maxFrameLength <= 0)
      throw new IllegalArgumentException("maxFrameLength must be a positive integer: " + maxFrameLength); 
  }
}
