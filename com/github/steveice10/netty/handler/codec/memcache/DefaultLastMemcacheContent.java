package com.github.steveice10.netty.handler.codec.memcache;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.ReferenceCounted;

public class DefaultLastMemcacheContent extends DefaultMemcacheContent implements LastMemcacheContent {
  public DefaultLastMemcacheContent() {
    super(Unpooled.buffer());
  }
  
  public DefaultLastMemcacheContent(ByteBuf content) {
    super(content);
  }
  
  public LastMemcacheContent retain() {
    super.retain();
    return this;
  }
  
  public LastMemcacheContent retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public LastMemcacheContent touch() {
    super.touch();
    return this;
  }
  
  public LastMemcacheContent touch(Object hint) {
    super.touch(hint);
    return this;
  }
  
  public LastMemcacheContent copy() {
    return replace(content().copy());
  }
  
  public LastMemcacheContent duplicate() {
    return replace(content().duplicate());
  }
  
  public LastMemcacheContent retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public LastMemcacheContent replace(ByteBuf content) {
    return new DefaultLastMemcacheContent(content);
  }
}
