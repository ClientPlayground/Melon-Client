package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.handler.codec.memcache.FullMemcacheMessage;
import com.github.steveice10.netty.handler.codec.memcache.LastMemcacheContent;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheContent;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheMessage;
import com.github.steveice10.netty.util.ReferenceCounted;

public class DefaultFullBinaryMemcacheResponse extends DefaultBinaryMemcacheResponse implements FullBinaryMemcacheResponse {
  private final ByteBuf content;
  
  public DefaultFullBinaryMemcacheResponse(ByteBuf key, ByteBuf extras) {
    this(key, extras, Unpooled.buffer(0));
  }
  
  public DefaultFullBinaryMemcacheResponse(ByteBuf key, ByteBuf extras, ByteBuf content) {
    super(key, extras);
    if (content == null)
      throw new NullPointerException("Supplied content is null."); 
    this.content = content;
    setTotalBodyLength(keyLength() + extrasLength() + content.readableBytes());
  }
  
  public ByteBuf content() {
    return this.content;
  }
  
  public FullBinaryMemcacheResponse retain() {
    super.retain();
    return this;
  }
  
  public FullBinaryMemcacheResponse retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public FullBinaryMemcacheResponse touch() {
    super.touch();
    return this;
  }
  
  public FullBinaryMemcacheResponse touch(Object hint) {
    super.touch(hint);
    this.content.touch(hint);
    return this;
  }
  
  protected void deallocate() {
    super.deallocate();
    this.content.release();
  }
  
  public FullBinaryMemcacheResponse copy() {
    ByteBuf key = key();
    if (key != null)
      key = key.copy(); 
    ByteBuf extras = extras();
    if (extras != null)
      extras = extras.copy(); 
    return new DefaultFullBinaryMemcacheResponse(key, extras, content().copy());
  }
  
  public FullBinaryMemcacheResponse duplicate() {
    ByteBuf key = key();
    if (key != null)
      key = key.duplicate(); 
    ByteBuf extras = extras();
    if (extras != null)
      extras = extras.duplicate(); 
    return new DefaultFullBinaryMemcacheResponse(key, extras, content().duplicate());
  }
  
  public FullBinaryMemcacheResponse retainedDuplicate() {
    return replace(content().retainedDuplicate());
  }
  
  public FullBinaryMemcacheResponse replace(ByteBuf content) {
    ByteBuf key = key();
    if (key != null)
      key = key.retainedDuplicate(); 
    ByteBuf extras = extras();
    if (extras != null)
      extras = extras.retainedDuplicate(); 
    return new DefaultFullBinaryMemcacheResponse(key, extras, content);
  }
}
