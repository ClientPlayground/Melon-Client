package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheMessage;
import com.github.steveice10.netty.util.ReferenceCounted;

public class DefaultBinaryMemcacheResponse extends AbstractBinaryMemcacheMessage implements BinaryMemcacheResponse {
  public static final byte RESPONSE_MAGIC_BYTE = -127;
  
  private short status;
  
  public DefaultBinaryMemcacheResponse() {
    this(null, null);
  }
  
  public DefaultBinaryMemcacheResponse(ByteBuf key) {
    this(key, null);
  }
  
  public DefaultBinaryMemcacheResponse(ByteBuf key, ByteBuf extras) {
    super(key, extras);
    setMagic((byte)-127);
  }
  
  public short status() {
    return this.status;
  }
  
  public BinaryMemcacheResponse setStatus(short status) {
    this.status = status;
    return this;
  }
  
  public BinaryMemcacheResponse retain() {
    super.retain();
    return this;
  }
  
  public BinaryMemcacheResponse retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  public BinaryMemcacheResponse touch() {
    super.touch();
    return this;
  }
  
  public BinaryMemcacheResponse touch(Object hint) {
    super.touch(hint);
    return this;
  }
}
