package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.ResourceLeakTracker;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.nio.ByteOrder;

class SimpleLeakAwareByteBuf extends WrappedByteBuf {
  private final ByteBuf trackedByteBuf;
  
  final ResourceLeakTracker<ByteBuf> leak;
  
  SimpleLeakAwareByteBuf(ByteBuf wrapped, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leak) {
    super(wrapped);
    this.trackedByteBuf = (ByteBuf)ObjectUtil.checkNotNull(trackedByteBuf, "trackedByteBuf");
    this.leak = (ResourceLeakTracker<ByteBuf>)ObjectUtil.checkNotNull(leak, "leak");
  }
  
  SimpleLeakAwareByteBuf(ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leak) {
    this(wrapped, wrapped, leak);
  }
  
  public ByteBuf slice() {
    return newSharedLeakAwareByteBuf(super.slice());
  }
  
  public ByteBuf retainedSlice() {
    return unwrappedDerived(super.retainedSlice());
  }
  
  public ByteBuf retainedSlice(int index, int length) {
    return unwrappedDerived(super.retainedSlice(index, length));
  }
  
  public ByteBuf retainedDuplicate() {
    return unwrappedDerived(super.retainedDuplicate());
  }
  
  public ByteBuf readRetainedSlice(int length) {
    return unwrappedDerived(super.readRetainedSlice(length));
  }
  
  public ByteBuf slice(int index, int length) {
    return newSharedLeakAwareByteBuf(super.slice(index, length));
  }
  
  public ByteBuf duplicate() {
    return newSharedLeakAwareByteBuf(super.duplicate());
  }
  
  public ByteBuf readSlice(int length) {
    return newSharedLeakAwareByteBuf(super.readSlice(length));
  }
  
  public ByteBuf asReadOnly() {
    return newSharedLeakAwareByteBuf(super.asReadOnly());
  }
  
  public ByteBuf touch() {
    return this;
  }
  
  public ByteBuf touch(Object hint) {
    return this;
  }
  
  public boolean release() {
    if (super.release()) {
      closeLeak();
      return true;
    } 
    return false;
  }
  
  public boolean release(int decrement) {
    if (super.release(decrement)) {
      closeLeak();
      return true;
    } 
    return false;
  }
  
  private void closeLeak() {
    boolean closed = this.leak.close(this.trackedByteBuf);
    assert closed;
  }
  
  public ByteBuf order(ByteOrder endianness) {
    if (order() == endianness)
      return this; 
    return newSharedLeakAwareByteBuf(super.order(endianness));
  }
  
  private ByteBuf unwrappedDerived(ByteBuf derived) {
    ByteBuf unwrappedDerived = unwrapSwapped(derived);
    if (unwrappedDerived instanceof AbstractPooledDerivedByteBuf) {
      ((AbstractPooledDerivedByteBuf)unwrappedDerived).parent(this);
      ResourceLeakTracker<ByteBuf> newLeak = AbstractByteBuf.leakDetector.track(derived);
      if (newLeak == null)
        return derived; 
      return newLeakAwareByteBuf(derived, newLeak);
    } 
    return newSharedLeakAwareByteBuf(derived);
  }
  
  private static ByteBuf unwrapSwapped(ByteBuf buf) {
    if (buf instanceof SwappedByteBuf) {
      do {
        buf = buf.unwrap();
      } while (buf instanceof SwappedByteBuf);
      return buf;
    } 
    return buf;
  }
  
  private SimpleLeakAwareByteBuf newSharedLeakAwareByteBuf(ByteBuf wrapped) {
    return newLeakAwareByteBuf(wrapped, this.trackedByteBuf, this.leak);
  }
  
  private SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf wrapped, ResourceLeakTracker<ByteBuf> leakTracker) {
    return newLeakAwareByteBuf(wrapped, wrapped, leakTracker);
  }
  
  protected SimpleLeakAwareByteBuf newLeakAwareByteBuf(ByteBuf buf, ByteBuf trackedByteBuf, ResourceLeakTracker<ByteBuf> leakTracker) {
    return new SimpleLeakAwareByteBuf(buf, trackedByteBuf, leakTracker);
  }
}
