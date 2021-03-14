package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;

public interface ChannelFuture extends Future<Void> {
  Channel channel();
  
  ChannelFuture addListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  ChannelFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  ChannelFuture removeListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  ChannelFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  ChannelFuture sync() throws InterruptedException;
  
  ChannelFuture syncUninterruptibly();
  
  ChannelFuture await() throws InterruptedException;
  
  ChannelFuture awaitUninterruptibly();
  
  boolean isVoid();
}
