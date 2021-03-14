package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ReferenceCounted;
import java.nio.ByteBuffer;

@Deprecated
public abstract class AbstractDerivedByteBuf extends AbstractByteBuf {
  protected AbstractDerivedByteBuf(int maxCapacity) {
    super(maxCapacity);
  }
  
  public final int refCnt() {
    return refCnt0();
  }
  
  int refCnt0() {
    return unwrap().refCnt();
  }
  
  public final ByteBuf retain() {
    return retain0();
  }
  
  ByteBuf retain0() {
    unwrap().retain();
    return this;
  }
  
  public final ByteBuf retain(int increment) {
    return retain0(increment);
  }
  
  ByteBuf retain0(int increment) {
    unwrap().retain(increment);
    return this;
  }
  
  public final ByteBuf touch() {
    return touch0();
  }
  
  ByteBuf touch0() {
    unwrap().touch();
    return this;
  }
  
  public final ByteBuf touch(Object hint) {
    return touch0(hint);
  }
  
  ByteBuf touch0(Object hint) {
    unwrap().touch(hint);
    return this;
  }
  
  public final boolean release() {
    return release0();
  }
  
  boolean release0() {
    return unwrap().release();
  }
  
  public final boolean release(int decrement) {
    return release0(decrement);
  }
  
  boolean release0(int decrement) {
    return unwrap().release(decrement);
  }
  
  public boolean isReadOnly() {
    return unwrap().isReadOnly();
  }
  
  public ByteBuffer internalNioBuffer(int index, int length) {
    return nioBuffer(index, length);
  }
  
  public ByteBuffer nioBuffer(int index, int length) {
    return unwrap().nioBuffer(index, length);
  }
}
