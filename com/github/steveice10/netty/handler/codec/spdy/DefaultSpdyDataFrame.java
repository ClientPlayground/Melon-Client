package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSpdyDataFrame extends DefaultSpdyStreamFrame implements SpdyDataFrame {
  private final ByteBuf data;
  
  public DefaultSpdyDataFrame(int streamId) {
    this(streamId, Unpooled.buffer(0));
  }
  
  public DefaultSpdyDataFrame(int streamId, ByteBuf data) {
    super(streamId);
    if (data == null)
      throw new NullPointerException("data"); 
    this.data = validate(data);
  }
  
  private static ByteBuf validate(ByteBuf data) {
    if (data.readableBytes() > 16777215)
      throw new IllegalArgumentException("data payload cannot exceed 16777215 bytes"); 
    return data;
  }
  
  public SpdyDataFrame setStreamId(int streamId) {
    super.setStreamId(streamId);
    return this;
  }
  
  public SpdyDataFrame setLast(boolean last) {
    super.setLast(last);
    return this;
  }
  
  public ByteBuf content() {
    if (this.data.refCnt() <= 0)
      throw new IllegalReferenceCountException(this.data.refCnt()); 
    return this.data;
  }
  
  public SpdyDataFrame copy() {
    return replace(content().copy());
  }
  
  public SpdyDataFrame duplicate() {
    return replace(content().duplicate());
  }
  
  public SpdyDataFrame retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public SpdyDataFrame replace(ByteBuf content) {
    SpdyDataFrame frame = new DefaultSpdyDataFrame(streamId(), content);
    frame.setLast(isLast());
    return frame;
  }
  
  public int refCnt() {
    return this.data.refCnt();
  }
  
  public SpdyDataFrame retain() {
    this.data.retain();
    return this;
  }
  
  public SpdyDataFrame retain(int increment) {
    this.data.retain(increment);
    return this;
  }
  
  public SpdyDataFrame touch() {
    this.data.touch();
    return this;
  }
  
  public SpdyDataFrame touch(Object hint) {
    this.data.touch(hint);
    return this;
  }
  
  public boolean release() {
    return this.data.release();
  }
  
  public boolean release(int decrement) {
    return this.data.release(decrement);
  }
  
  public String toString() {
    StringBuilder buf = (new StringBuilder()).append(StringUtil.simpleClassName(this)).append("(last: ").append(isLast()).append(')').append(StringUtil.NEWLINE).append("--> Stream-ID = ").append(streamId()).append(StringUtil.NEWLINE).append("--> Size = ");
    if (refCnt() == 0) {
      buf.append("(freed)");
    } else {
      buf.append(content().readableBytes());
    } 
    return buf.toString();
  }
}
