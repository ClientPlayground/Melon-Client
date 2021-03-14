package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.PromiseAggregator;

@Deprecated
public final class ChannelPromiseAggregator extends PromiseAggregator<Void, ChannelFuture> implements ChannelFutureListener {
  public ChannelPromiseAggregator(ChannelPromise aggregatePromise) {
    super(aggregatePromise);
  }
}
