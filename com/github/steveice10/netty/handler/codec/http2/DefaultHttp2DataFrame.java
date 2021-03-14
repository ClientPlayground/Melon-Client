package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

public final class DefaultHttp2DataFrame extends AbstractHttp2StreamFrame implements Http2DataFrame {
  private final ByteBuf content;
  
  private final boolean endStream;
  
  private final int padding;
  
  private final int initialFlowControlledBytes;
  
  public DefaultHttp2DataFrame(ByteBuf content) {
    this(content, false);
  }
  
  public DefaultHttp2DataFrame(boolean endStream) {
    this(Unpooled.EMPTY_BUFFER, endStream);
  }
  
  public DefaultHttp2DataFrame(ByteBuf content, boolean endStream) {
    this(content, endStream, 0);
  }
  
  public DefaultHttp2DataFrame(ByteBuf content, boolean endStream, int padding) {
    this.content = (ByteBuf)ObjectUtil.checkNotNull(content, "content");
    this.endStream = endStream;
    Http2CodecUtil.verifyPadding(padding);
    this.padding = padding;
    if (content().readableBytes() + padding > 2147483647L)
      throw new IllegalArgumentException("content + padding must be <= Integer.MAX_VALUE"); 
    this.initialFlowControlledBytes = content().readableBytes() + padding;
  }
  
  public DefaultHttp2DataFrame stream(Http2FrameStream stream) {
    super.stream(stream);
    return this;
  }
  
  public String name() {
    return "DATA";
  }
  
  public boolean isEndStream() {
    return this.endStream;
  }
  
  public int padding() {
    return this.padding;
  }
  
  public ByteBuf content() {
    if (this.content.refCnt() <= 0)
      throw new IllegalReferenceCountException(this.content.refCnt()); 
    return this.content;
  }
  
  public int initialFlowControlledBytes() {
    return this.initialFlowControlledBytes;
  }
  
  public DefaultHttp2DataFrame copy() {
    return replace(content().copy());
  }
  
  public DefaultHttp2DataFrame duplicate() {
    return replace(content().duplicate());
  }
  
  public DefaultHttp2DataFrame retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public DefaultHttp2DataFrame replace(ByteBuf content) {
    return new DefaultHttp2DataFrame(content, this.endStream, this.padding);
  }
  
  public int refCnt() {
    return this.content.refCnt();
  }
  
  public boolean release() {
    return this.content.release();
  }
  
  public boolean release(int decrement) {
    return this.content.release(decrement);
  }
  
  public DefaultHttp2DataFrame retain() {
    this.content.retain();
    return this;
  }
  
  public DefaultHttp2DataFrame retain(int increment) {
    this.content.retain(increment);
    return this;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(stream=" + stream() + ", content=" + this.content + ", endStream=" + this.endStream + ", padding=" + this.padding + ')';
  }
  
  public DefaultHttp2DataFrame touch() {
    this.content.touch();
    return this;
  }
  
  public DefaultHttp2DataFrame touch(Object hint) {
    this.content.touch(hint);
    return this;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof DefaultHttp2DataFrame))
      return false; 
    DefaultHttp2DataFrame other = (DefaultHttp2DataFrame)o;
    return (super.equals(other) && this.content.equals(other.content()) && this.endStream == other.endStream && this.padding == other.padding);
  }
  
  public int hashCode() {
    int hash = super.hashCode();
    hash = hash * 31 + this.content.hashCode();
    hash = hash * 31 + (this.endStream ? 0 : 1);
    hash = hash * 31 + this.padding;
    return hash;
  }
}
