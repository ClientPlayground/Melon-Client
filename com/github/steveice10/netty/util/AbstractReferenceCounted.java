package com.github.steveice10.netty.util;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class AbstractReferenceCounted implements ReferenceCounted {
  private static final AtomicIntegerFieldUpdater<AbstractReferenceCounted> refCntUpdater = AtomicIntegerFieldUpdater.newUpdater(AbstractReferenceCounted.class, "refCnt");
  
  private volatile int refCnt = 1;
  
  public final int refCnt() {
    return this.refCnt;
  }
  
  protected final void setRefCnt(int refCnt) {
    refCntUpdater.set(this, refCnt);
  }
  
  public ReferenceCounted retain() {
    return retain0(1);
  }
  
  public ReferenceCounted retain(int increment) {
    return retain0(ObjectUtil.checkPositive(increment, "increment"));
  }
  
  private ReferenceCounted retain0(int increment) {
    int oldRef = refCntUpdater.getAndAdd(this, increment);
    if (oldRef <= 0 || oldRef + increment < oldRef) {
      refCntUpdater.getAndAdd(this, -increment);
      throw new IllegalReferenceCountException(oldRef, increment);
    } 
    return this;
  }
  
  public ReferenceCounted touch() {
    return touch(null);
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
