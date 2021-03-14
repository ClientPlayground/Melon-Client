package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultByteBufHolder implements ByteBufHolder {
  private final ByteBuf data;
  
  public DefaultByteBufHolder(ByteBuf data) {
    if (data == null)
      throw new NullPointerException("data"); 
    this.data = data;
  }
  
  public ByteBuf content() {
    if (this.data.refCnt() <= 0)
      throw new IllegalReferenceCountException(this.data.refCnt()); 
    return this.data;
  }
  
  public ByteBufHolder copy() {
    return replace(this.data.copy());
  }
  
  public ByteBufHolder duplicate() {
    return replace(this.data.duplicate());
  }
  
  public ByteBufHolder retainedDuplicate() {
    return replace(this.data.retainedDuplicate());
  }
  
  public ByteBufHolder replace(ByteBuf content) {
    return new DefaultByteBufHolder(content);
  }
  
  public int refCnt() {
    return this.data.refCnt();
  }
  
  public ByteBufHolder retain() {
    this.data.retain();
    return this;
  }
  
  public ByteBufHolder retain(int increment) {
    this.data.retain(increment);
    return this;
  }
  
  public ByteBufHolder touch() {
    this.data.touch();
    return this;
  }
  
  public ByteBufHolder touch(Object hint) {
    this.data.touch(hint);
    return this;
  }
  
  public boolean release() {
    return this.data.release();
  }
  
  public boolean release(int decrement) {
    return this.data.release(decrement);
  }
  
  protected final String contentToString() {
    return this.data.toString();
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + '(' + contentToString() + ')';
  }
  
  public boolean equals(Object o) {
    if (this == o)
      return true; 
    if (o instanceof ByteBufHolder)
      return this.data.equals(((ByteBufHolder)o).content()); 
    return false;
  }
  
  public int hashCode() {
    return this.data.hashCode();
  }
}
