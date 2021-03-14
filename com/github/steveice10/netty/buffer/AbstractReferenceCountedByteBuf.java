package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.IllegalReferenceCountException;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class AbstractReferenceCountedByteBuf extends AbstractByteBuf {
  private static final AtomicIntegerFieldUpdater<AbstractReferenceCountedByteBuf> refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCountedByteBuf.class, "refCnt");
  
  private volatile int refCnt;
  
  protected AbstractReferenceCountedByteBuf(int maxCapacity) {
    super(maxCapacity);
    refCntUpdater.set(this, 1);
  }
  
  public int refCnt() {
    return this.refCnt;
  }
  
  protected final void setRefCnt(int refCnt) {
    refCntUpdater.set(this, refCnt);
  }
  
  public ByteBuf retain() {
    return retain0(1);
  }
  
  public ByteBuf retain(int increment) {
    return retain0(ObjectUtil.checkPositive(increment, "increment"));
  }
  
  private ByteBuf retain0(int increment) {
    int oldRef = refCntUpdater.getAndAdd(this, increment);
    if (oldRef <= 0 || oldRef + increment < oldRef) {
      refCntUpdater.getAndAdd(this, -increment);
      throw new IllegalReferenceCountException(oldRef, increment);
    } 
    return this;
  }
  
  public ByteBuf touch() {
    return this;
  }
  
  public ByteBuf touch(Object hint) {
    return this;
  }
  
  public boolean release() {
    return release0(1);
  }
  
  public boolean release(int decrement) {
    return release0(ObjectUtil.checkPositive(decrement, "decrement"));
  }
  
  private boolean release0(int decrement) {
    int oldRef = refCntUpdater.getAndAdd(this, -decrement);
    if (oldRef == decrement) {
      deallocate();
      return true;
    } 
    if (oldRef < decrement || oldRef - decrement > oldRef) {
      refCntUpdater.getAndAdd(this, decrement);
      throw new IllegalReferenceCountException(oldRef, -decrement);
    } 
    return false;
  }
  
  protected abstract void deallocate();
}
