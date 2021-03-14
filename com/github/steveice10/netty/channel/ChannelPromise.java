package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;

public interface ChannelPromise extends ChannelFuture, Promise<Void> {
  Channel channel();
  
  ChannelPromise setSuccess(Void paramVoid);
  
  ChannelPromise setSuccess();
  
  boolean trySuccess();
  
  ChannelPromise setFailure(Throwable paramThrowable);
  
  ChannelPromise addListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  ChannelPromise addListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  ChannelPromise removeListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  ChannelPromise removeListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  ChannelPromise sync() throws InterruptedException;
  
  ChannelPromise syncUninterruptibly();
  
  ChannelPromise await() throws InterruptedException;
  
  ChannelPromise awaitUninterruptibly();
  
  ChannelPromise unvoid();
}
