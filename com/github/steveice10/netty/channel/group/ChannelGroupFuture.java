package com.github.steveice10.netty.channel.group;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import java.util.Iterator;

public interface ChannelGroupFuture extends Future<Void>, Iterable<ChannelFuture> {
  ChannelGroup group();
  
  ChannelFuture find(Channel paramChannel);
  
  boolean isSuccess();
  
  ChannelGroupException cause();
  
  boolean isPartialSuccess();
  
  boolean isPartialFailure();
  
  ChannelGroupFuture addListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  ChannelGroupFuture addListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  ChannelGroupFuture removeListener(GenericFutureListener<? extends Future<? super Void>> paramGenericFutureListener);
  
  ChannelGroupFuture removeListeners(GenericFutureListener<? extends Future<? super Void>>... paramVarArgs);
  
  ChannelGroupFuture await() throws InterruptedException;
  
  ChannelGroupFuture awaitUninterruptibly();
  
  ChannelGroupFuture syncUninterruptibly();
  
  ChannelGroupFuture sync() throws InterruptedException;
  
  Iterator<ChannelFuture> iterator();
}
