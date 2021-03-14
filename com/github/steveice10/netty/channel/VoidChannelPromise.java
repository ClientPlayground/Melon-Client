package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.AbstractFuture;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import java.util.concurrent.TimeUnit;

public final class VoidChannelPromise extends AbstractFuture<Void> implements ChannelPromise {
  private final Channel channel;
  
  private final ChannelFutureListener fireExceptionListener;
  
  public VoidChannelPromise(Channel channel, boolean fireException) {
    if (channel == null)
      throw new NullPointerException("channel"); 
    this.channel = channel;
    if (fireException) {
      this.fireExceptionListener = new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            Throwable cause = future.cause();
            if (cause != null)
              VoidChannelPromise.this.fireException0(cause); 
          }
        };
    } else {
      this.fireExceptionListener = null;
    } 
  }
  
  public VoidChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> listener) {
    fail();
    return this;
  }
  
  public VoidChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
    fail();
    return this;
  }
  
  public VoidChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> listener) {
    return this;
  }
  
  public VoidChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... listeners) {
    return this;
  }
  
  public VoidChannelPromise await() throws InterruptedException {
    if (Thread.interrupted())
      throw new InterruptedException(); 
    return this;
  }
  
  public boolean await(long timeout, TimeUnit unit) {
    fail();
    return false;
  }
  
  public boolean await(long timeoutMillis) {
    fail();
    return false;
  }
  
  public VoidChannelPromise awaitUninterruptibly() {
    fail();
    return this;
  }
  
  public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
    fail();
    return false;
  }
  
  public boolean awaitUninterruptibly(long timeoutMillis) {
    fail();
    return false;
  }
  
  public Channel channel() {
    return this.channel;
  }
  
  public boolean isDone() {
    return false;
  }
  
  public boolean isSuccess() {
    return false;
  }
  
  public boolean setUncancellable() {
    return true;
  }
  
  public boolean isCancellable() {
    return false;
  }
  
  public boolean isCancelled() {
    return false;
  }
  
  public Throwable cause() {
    return null;
  }
  
  public VoidChannelPromise sync() {
    fail();
    return this;
  }
  
  public VoidChannelPromise syncUninterruptibly() {
    fail();
    return this;
  }
  
  public VoidChannelPromise setFailure(Throwable cause) {
    fireException0(cause);
    return this;
  }
  
  public VoidChannelPromise setSuccess() {
    return this;
  }
  
  public boolean tryFailure(Throwable cause) {
    fireException0(cause);
    return false;
  }
  
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }
  
  public boolean trySuccess() {
    return false;
  }
  
  private static void fail() {
    throw new IllegalStateException("void future");
  }
  
  public VoidChannelPromise setSuccess(Void result) {
    return this;
  }
  
  public boolean trySuccess(Void result) {
    return false;
  }
  
  public Void getNow() {
    return null;
  }
  
  public ChannelPromise unvoid() {
    ChannelPromise promise = new DefaultChannelPromise(this.channel);
    if (this.fireExceptionListener != null)
      promise.addListener(this.fireExceptionListener); 
    return promise;
  }
  
  public boolean isVoid() {
    return true;
  }
  
  private void fireException0(Throwable cause) {
    if (this.fireExceptionListener != null && this.channel.isRegistered())
      this.channel.pipeline().fireExceptionCaught(cause); 
  }
}
