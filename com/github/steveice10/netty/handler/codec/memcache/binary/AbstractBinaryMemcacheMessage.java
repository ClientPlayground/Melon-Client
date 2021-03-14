package com.github.steveice10.netty.handler.codec.memcache.binary;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.handler.codec.memcache.AbstractMemcacheObject;
import com.github.steveice10.netty.handler.codec.memcache.MemcacheMessage;
import com.github.steveice10.netty.util.ReferenceCounted;

public abstract class AbstractBinaryMemcacheMessage extends AbstractMemcacheObject implements BinaryMemcacheMessage {
  private ByteBuf key;
  
  private ByteBuf extras;
  
  private byte magic;
  
  private byte opcode;
  
  private short keyLength;
  
  private byte extrasLength;
  
  private byte dataType;
  
  private int totalBodyLength;
  
  private int opaque;
  
  private long cas;
  
  protected AbstractBinaryMemcacheMessage(ByteBuf key, ByteBuf extras) {
    this.key = key;
    this.keyLength = (key == null) ? 0 : (short)key.readableBytes();
    this.extras = extras;
    this.extrasLength = (extras == null) ? 0 : (byte)extras.readableBytes();
    this.totalBodyLength = this.keyLength + this.extrasLength;
  }
  
  public ByteBuf key() {
    return this.key;
  }
  
  public ByteBuf extras() {
    return this.extras;
  }
  
  public BinaryMemcacheMessage setKey(ByteBuf key) {
    if (this.key != null)
      this.key.release(); 
    this.key = key;
    short oldKeyLength = this.keyLength;
    this.keyLength = (key == null) ? 0 : (short)key.readableBytes();
    this.totalBodyLength = this.totalBodyLength + this.keyLength - oldKeyLength;
    return this;
  }
  
  public BinaryMemcacheMessage setExtras(ByteBuf extras) {
    if (this.extras != null)
      this.extras.release(); 
    this.extras = extras;
    short oldExtrasLength = (short)this.extrasLength;
    this.extrasLength = (extras == null) ? 0 : (byte)extras.readableBytes();
    this.totalBodyLength = this.totalBodyLength + this.extrasLength - oldExtrasLength;
    return this;
  }
  
  public byte magic() {
    return this.magic;
  }
  
  public BinaryMemcacheMessage setMagic(byte magic) {
    this.magic = magic;
    return this;
  }
  
  public long cas() {
    return this.cas;
  }
  
  public BinaryMemcacheMessage setCas(long cas) {
    this.cas = cas;
    return this;
  }
  
  public int opaque() {
    return this.opaque;
  }
  
  public BinaryMemcacheMessage setOpaque(int opaque) {
    this.opaque = opaque;
    return this;
  }
  
  public int totalBodyLength() {
    return this.totalBodyLength;
  }
  
  public BinaryMemcacheMessage setTotalBodyLength(int totalBodyLength) {
    this.totalBodyLength = totalBodyLength;
    return this;
  }
  
  public byte dataType() {
    return this.dataType;
  }
  
  public BinaryMemcacheMessage setDataType(byte dataType) {
    this.dataType = dataType;
    return this;
  }
  
  public byte extrasLength() {
    return this.extrasLength;
  }
  
  BinaryMemcacheMessage setExtrasLength(byte extrasLength) {
    this.extrasLength = extrasLength;
    return this;
  }
  
  public short keyLength() {
    return this.keyLength;
  }
  
  BinaryMemcacheMessage setKeyLength(short keyLength) {
    this.keyLength = keyLength;
    return this;
  }
  
  public byte opcode() {
    return this.opcode;
  }
  
  public BinaryMemcacheMessage setOpcode(byte opcode) {
    this.opcode = opcode;
    return this;
  }
  
  public BinaryMemcacheMessage retain() {
    super.retain();
    return this;
  }
  
  public BinaryMemcacheMessage retain(int increment) {
    super.retain(increment);
    return this;
  }
  
  protected void deallocate() {
    if (this.key != null)
      this.key.release(); 
    if (this.extras != null)
      this.extras.release(); 
  }
  
  public BinaryMemcacheMessage touch() {
    super.touch();
    return this;
  }
  
  public BinaryMemcacheMessage touch(Object hint) {
    if (this.key != null)
      this.key.touch(hint); 
    if (this.extras != null)
      this.extras.touch(hint); 
    return this;
  }
}
