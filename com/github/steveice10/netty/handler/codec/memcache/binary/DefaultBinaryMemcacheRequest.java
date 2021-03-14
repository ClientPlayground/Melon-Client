package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheMessage;
import com.github.steveice10.netty.util.ReferenceCounted;

public class DefaultBinaryMemcacheRequest extends AbstractBinaryMemcacheMessage implements BinaryMemcacheRequest {
  public static final byte REQUEST_MAGIC_BYTE = -128;
  
  private short reserved;
  
  public DefaultBinaryMemcacheRequest() {
    this(null, null);
  }
  
  public DefaultBinaryMemcacheRequest(ByteBuf key) {
    this(key, null);
  }
  
  public DefaultBinaryMemcacheRequest(ByteBuf key, ByteBuf extras) {
    super(key, extras);
    setMagic(-128);
  }
  
  public short reserved() {
    return this.reserved;
  }
  
  public BinaryMemcacheRequest setReserved(short reserved) {
    this.reserved = reserved;
    return this;
  }
  
  public BinaryMemcacheRequest retain() {
    super.retain();
    return this;
  }
  
  public BinaryMemcacheRequest retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public BinaryMemcacheRequest touch() {
    super.touch();
    return this;
  }
  
  public BinaryMemcacheRequest touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
