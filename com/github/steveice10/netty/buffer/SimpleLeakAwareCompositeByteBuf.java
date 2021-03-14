package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ResourceLeakTracker;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.nio.ByteOrder;

class SimpleLeakAwareCompositeByteBuf extends WrappedCompositeByteBuf {
  final ResourceLeakTracker<ByteBuf> leak;
  
  SimpleLeakAwareCompositeByteBuf(CompositeByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak) {
    super(wrapped);
    this.leak = (ResourceLeakTracker<ByteBuf>)ObjectUtil.checkNotNull(leak, "leak");
  }
  
  public boolean release() {
    ByteBuf unwrapped = unwrap();
    if (super.release()) {
      closeLeak(unwrapped);
      return true;
    } 
    return false;
  }
  
  public boolean release(int decrement) {
    ByteBuf unwrapped = unwrap();
    if (super.release(decrement)) {
      closeLeak(unwrapped);
      return true;
    } 
    return false;
  }
  
  private void closeLeak(ByteBuf trackedByteBuf) {
    boolean closed = this.leak.close(trackedByteBuf);
    assert closed;
  }
  
  public ByteBuf order(ByteOrder endianness) {
    if (order() == endianness)
      return this; 
    return newLeakAwareByteBuf(super.order(endianness));
  }
  
  public ByteBuf slice() {
    return newLeakAwareByteBuf(super.slice());
  }
  
  public ByteBuf retainedSlice() {
    return newLeakAwareByteBuf(super.retainedSlice());
  }
  
  public ByteBuf slice(int index, int length) {
    return newLeakAwareByteBuf(super.slice(index, length));
  }
  
  public ByteBuf retainedSlice(int index, int length) {
    return newLeakAwareByteBuf(super.retainedSlice(index, length));
  }
  
  public ByteBuf duplicate() {
    return newLeakAwareByteBuf(super.duplicate());
  }
  
  public ByteBuf retainedDuplicate() {
    return newLeakAwareByteBuf(super.retainedDuplicate());
  }
  
  public ByteBuf readSlice(int length) {
    return newLeakAwareByteBuf(super.readSlice(length));
  }
  
  public ByteBuf readRetainedSlice(int length) {
    return newLeakAwareByteBuf(super.readRetainedSlice(length));
  }
  
  public ByteBuf asReadOnly() {
    return newLeakAwareByteBuf(super.asReadOnly());
  }
  
  private SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped) {
    return newLeakAwareByteBuf(wrapped, unwrap(), this.leak);
  }
  
  protected SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker) {
    return new SimpleLeakAwareByteBuf(wrapped, trackedByteBuf, leakTracker);
  }
}
