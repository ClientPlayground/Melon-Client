package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.concurrent.PromiseNotifier;

public final class ChannelPromiseNotifier extends PromiseNotifier<Void, ChannelFuture> implements ChannelFutureListener {
  public ChannelPromiseNotifier(ChannelPromise... promises) {
    super((Promise[])promises);
  }
  
  public ChannelPromiseNotifier(boolean logNotifyFailure, ChannelPromise... promises) {
    super(logNotifyFailure, (Promise[])promises);
  }
}
