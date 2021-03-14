package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.DefaultByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public final class DefaultHttp2GoAwayFrame extends DefaultByteBufHolder implements Http2GoAwayFrame {
  private final long errorCode;
  
  private final int lastStreamId;
  
  private int extraStreamIds;
  
  public DefaultHttp2GoAwayFrame(Http2Error error) {
    this(error.code());
  }
  
  public DefaultHttp2GoAwayFrame(long errorCode) {
    this(errorCode, Unpooled.EMPTY_BUFFER);
  }
  
  public DefaultHttp2GoAwayFrame(Http2Error error, ByteBuf content) {
    this(error.code(), content);
  }
  
  public DefaultHttp2GoAwayFrame(long errorCode, ByteBuf content) {
    this(-1, errorCode, content);
  }
  
  DefaultHttp2GoAwayFrame(int lastStreamId, long errorCode, ByteBuf content) {
    super(content);
    this.errorCode = errorCode;
    this.lastStreamId = lastStreamId;
  }
  
  public String name() {
    return "GOAWAY";
  }
  
  public long errorCode() {
    return this.errorCode;
  }
  
  public int extraStreamIds() {
    return this.extraStreamIds;
  }
  
  public Http2GoAwayFrame setExtraStreamIds(int extraStreamIds) {
    if (extraStreamIds < 0)
      throw new IllegalArgumentException("extraStreamIds must be non-negative"); 
    this.extraStreamIds = extraStreamIds;
    return this;
  }
  
  public int lastStreamId() {
    return this.lastStreamId;
  }
  
  public Http2GoAwayFrame copy() {
    return new DefaultHttp2GoAwayFrame(this.lastStreamId, this.errorCode, content().copy());
  }
  
  public Http2GoAwayFrame duplicate() {
    return (Http2GoAwayFrame)super.duplicate();
  }
  
  public Http2GoAwayFrame retainedDuplicate() {
    return (Http2GoAwayFrame)super.retainedDuplicate();
  }
  
  public Http2GoAwayFrame replace(ByteBuf content) {
    return (new DefaultHttp2GoAwayFrame(this.errorCode, content)).setExtraStreamIds(this.extraStreamIds);
  }
  
  public Http2GoAwayFrame retain() {
    super.retain();
    return this;
  }
  
  public Http2GoAwayFrame retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public Http2GoAwayFrame touch() {
    super.touch();
    return this;
  }
  
  public Http2GoAwayFrame touch(Object hint) {
    super.touch(hint);
    return this;
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof DefaultHttp2GoAwayFrame))
      return false; 
    DefaultHttp2GoAwayFrame other = (DefaultHttp2GoAwayFrame)o;
    return (this.errorCode == other.errorCode && this.extraStreamIds == other.extraStreamIds && super.equals(other));
  }
  
  public int hashCode() {
    int hash = super.hashCode();
    hash = hash * 31 + (int)(this.errorCode ^ this.errorCode >>> 32L);
    hash = hash * 31 + this.extraStreamIds;
    return hash;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(errorCode=" + this.errorCode + ", content=" + content() + ", extraStreamIds=" + this.extraStreamIds + ", lastStreamId=" + this.lastStreamId + ')';
  }
}
