package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultMemcacheContent extends AbstractMemcacheObject implements MemcacheContent {
  private final ByteBuf content;
  
  public DefaultMemcacheContent(ByteBuf content) {
    if (content == null)
      throw new NullPointerException("Content cannot be null."); 
    this.content = content;
  }
  
  public ByteBuf content() {
    return this.content;
  }
  
  public MemcacheContent copy() {
    return replace(this.content.copy());
  }
  
  public MemcacheContent duplicate() {
    return replace(this.content.duplicate());
  }
  
  public MemcacheContent retainedDuplicate() {
    return replace(this.content.retainedDuplicate());
  }
  
  public MemcacheContent replace(ByteBuf content) {
    return new DefaultMemcacheContent(content);
  }
  
  public MemcacheContent retain() {
    super.retain();
    return this;
  }
  
  public MemcacheContent retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public MemcacheContent touch() {
    super.touch();
    return this;
  }
  
  public MemcacheContent touch(Object hint) {
    this.content.touch(hint);
    return this;
  }
  
  protected void deallocate() {
    this.content.release();
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(data: " + 
      content() + ", decoderResult: " + decoderResult() + ')';
  }
}
