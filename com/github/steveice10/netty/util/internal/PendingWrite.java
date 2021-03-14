package com.github.steveice10.netty.util.internal;

import com.github.steveice10.netty.util.Recycler;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Promise;

public final class PendingWrite {
  private static final Recycler<PendingWrite> RECYCLER = new Recycler<PendingWrite>() {
      protected PendingWrite newObject(Recycler.Handle<PendingWrite> handle) {
        return new PendingWrite(handle);
      }
    };
  
  private final Recycler.Handle<PendingWrite> handle;
  
  private Object msg;
  
  private Promise<Void> promise;
  
  public static PendingWrite newInstance(Object msg, Promise<Void> promise) {
    PendingWrite pending = (PendingWrite)RECYCLER.get();
    pending.msg = msg;
    pending.promise = promise;
    return pending;
  }
  
  private PendingWrite(Recycler.Handle<PendingWrite> handle) {
    this.handle = handle;
  }
  
  public boolean recycle() {
    this.msg = null;
    this.promise = null;
    this.handle.recycle(this);
    return true;
  }
  
  public boolean failAndRecycle(Throwable cause) {
    ReferenceCountUtil.release(this.msg);
    if (this.promise != null)
      this.promise.setFailure(cause); 
    return recycle();
  }
  
  public boolean successAndRecycle() {
    if (this.promise != null)
      this.promise.setSuccess(null); 
    return recycle();
  }
  
  public Object msg() {
    return this.msg;
  }
  
  public Promise<Void> promise() {
    return this.promise;
  }
  
  public Promise<Void> recycleAndGet() {
    Promise<Void> promise = this.promise;
    recycle();
    return promise;
  }
}
