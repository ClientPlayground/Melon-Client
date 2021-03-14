package com.github.steveice10.netty.channel.pool;

import com.github.steveice10.netty.bootstrap.Bootstrap;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelInitializer;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.util.Deque;

public class SimpleChannelPool implements ChannelPool {
  private static final AttributeKey<SimpleChannelPool> POOL_KEY = AttributeKey.newInstance("channelPool");
  
  private static final IllegalStateException FULL_EXCEPTION = (IllegalStateException)ThrowableUtil.unknownStackTrace(new IllegalStateException("ChannelPool full"), SimpleChannelPool.class, "releaseAndOffer(...)");
  
  private final Deque<Channel> deque = PlatformDependent.newConcurrentDeque();
  
  private final ChannelPoolHandler handler;
  
  private final ChannelHealthChecker healthCheck;
  
  private final Bootstrap bootstrap;
  
  private final boolean releaseHealthCheck;
  
  private final boolean lastRecentUsed;
  
  public SimpleChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler) {
    this(bootstrap, handler, ChannelHealthChecker.ACTIVE);
  }
  
  public SimpleChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck) {
    this(bootstrap, handler, healthCheck, true);
  }
  
  public SimpleChannelPool(Bootstrap bootstrap, ChannelPoolHandler handler, ChannelHealthChecker healthCheck, boolean releaseHealthCheck) {
    this(bootstrap, handler, healthCheck, releaseHealthCheck, true);
  }
  
  public SimpleChannelPool(Bootstrap bootstrap, final ChannelPoolHandler handler, ChannelHealthChecker healthCheck, boolean releaseHealthCheck, boolean lastRecentUsed) {
    this.handler = (ChannelPoolHandler)ObjectUtil.checkNotNull(handler, "handler");
    this.healthCheck = (ChannelHealthChecker)ObjectUtil.checkNotNull(healthCheck, "healthCheck");
    this.releaseHealthCheck = releaseHealthCheck;
    this.bootstrap = ((Bootstrap)ObjectUtil.checkNotNull(bootstrap, "bootstrap")).clone();
    this.bootstrap.handler((ChannelHandler)new ChannelInitializer<Channel>() {
          protected void initChannel(Channel ch) throws Exception {
            assert ch.eventLoop().inEventLoop();
            handler.channelCreated(ch);
          }
        });
    this.lastRecentUsed = lastRecentUsed;
  }
  
  protected Bootstrap bootstrap() {
    return this.bootstrap;
  }
  
  protected ChannelPoolHandler handler() {
    return this.handler;
  }
  
  protected ChannelHealthChecker healthChecker() {
    return this.healthCheck;
  }
  
  protected boolean releaseHealthCheck() {
    return this.releaseHealthCheck;
  }
  
  public final Future<Channel> acquire() {
    return acquire(this.bootstrap.config().group().next().newPromise());
  }
  
  public Future<Channel> acquire(Promise<Channel> promise) {
    ObjectUtil.checkNotNull(promise, "promise");
    return acquireHealthyFromPoolOrNew(promise);
  }
  
  private Future<Channel> acquireHealthyFromPoolOrNew(final Promise<Channel> promise) {
    try {
      final Channel ch = pollChannel();
      if (ch == null) {
        Bootstrap bs = this.bootstrap.clone();
        bs.attr(POOL_KEY, this);
        ChannelFuture f = connectChannel(bs);
        if (f.isDone()) {
          notifyConnect(f, promise);
        } else {
          f.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  SimpleChannelPool.this.notifyConnect(future, promise);
                }
              });
        } 
        return (Future<Channel>)promise;
      } 
      EventLoop loop = ch.eventLoop();
      if (loop.inEventLoop()) {
        doHealthCheck(ch, promise);
      } else {
        loop.execute(new Runnable() {
              public void run() {
                SimpleChannelPool.this.doHealthCheck(ch, promise);
              }
            });
      } 
    } catch (Throwable cause) {
      promise.tryFailure(cause);
    } 
    return (Future<Channel>)promise;
  }
  
  private void notifyConnect(ChannelFuture future, Promise<Channel> promise) {
    if (future.isSuccess()) {
      Channel channel = future.channel();
      if (!promise.trySuccess(channel))
        release(channel); 
    } else {
      promise.tryFailure(future.cause());
    } 
  }
  
  private void doHealthCheck(final Channel ch, final Promise<Channel> promise) {
    assert ch.eventLoop().inEventLoop();
    Future<Boolean> f = this.healthCheck.isHealthy(ch);
    if (f.isDone()) {
      notifyHealthCheck(f, ch, promise);
    } else {
      f.addListener((GenericFutureListener)new FutureListener<Boolean>() {
            public void operationComplete(Future<Boolean> future) throws Exception {
              SimpleChannelPool.this.notifyHealthCheck(future, ch, promise);
            }
          });
    } 
  }
  
  private void notifyHealthCheck(Future<Boolean> future, Channel ch, Promise<Channel> promise) {
    assert ch.eventLoop().inEventLoop();
    if (future.isSuccess()) {
      if (((Boolean)future.getNow()).booleanValue()) {
        try {
          ch.attr(POOL_KEY).set(this);
          this.handler.channelAcquired(ch);
          promise.setSuccess(ch);
        } catch (Throwable cause) {
          closeAndFail(ch, cause, promise);
        } 
      } else {
        closeChannel(ch);
        acquireHealthyFromPoolOrNew(promise);
      } 
    } else {
      closeChannel(ch);
      acquireHealthyFromPoolOrNew(promise);
    } 
  }
  
  protected ChannelFuture connectChannel(Bootstrap bs) {
    return bs.connect();
  }
  
  public final Future<Void> release(Channel channel) {
    return release(channel, channel.eventLoop().newPromise());
  }
  
  public Future<Void> release(final Channel channel, final Promise<Void> promise) {
    ObjectUtil.checkNotNull(channel, "channel");
    ObjectUtil.checkNotNull(promise, "promise");
    try {
      EventLoop loop = channel.eventLoop();
      if (loop.inEventLoop()) {
        doReleaseChannel(channel, promise);
      } else {
        loop.execute(new Runnable() {
              public void run() {
                SimpleChannelPool.this.doReleaseChannel(channel, promise);
              }
            });
      } 
    } catch (Throwable cause) {
      closeAndFail(channel, cause, promise);
    } 
    return (Future<Void>)promise;
  }
  
  private void doReleaseChannel(Channel channel, Promise<Void> promise) {
    assert channel.eventLoop().inEventLoop();
    if (channel.attr(POOL_KEY).getAndSet(null) != this) {
      closeAndFail(channel, new IllegalArgumentException("Channel " + channel + " was not acquired from this ChannelPool"), promise);
    } else {
      try {
        if (this.releaseHealthCheck) {
          doHealthCheckOnRelease(channel, promise);
        } else {
          releaseAndOffer(channel, promise);
        } 
      } catch (Throwable cause) {
        closeAndFail(channel, cause, promise);
      } 
    } 
  }
  
  private void doHealthCheckOnRelease(final Channel channel, final Promise<Void> promise) throws Exception {
    final Future<Boolean> f = this.healthCheck.isHealthy(channel);
    if (f.isDone()) {
      releaseAndOfferIfHealthy(channel, promise, f);
    } else {
      f.addListener((GenericFutureListener)new FutureListener<Boolean>() {
            public void operationComplete(Future<Boolean> future) throws Exception {
              SimpleChannelPool.this.releaseAndOfferIfHealthy(channel, promise, f);
            }
          });
    } 
  }
  
  private void releaseAndOfferIfHealthy(Channel channel, Promise<Void> promise, Future<Boolean> future) throws Exception {
    if (((Boolean)future.getNow()).booleanValue()) {
      releaseAndOffer(channel, promise);
    } else {
      this.handler.channelReleased(channel);
      promise.setSuccess(null);
    } 
  }
  
  private void releaseAndOffer(Channel channel, Promise<Void> promise) throws Exception {
    if (offerChannel(channel)) {
      this.handler.channelReleased(channel);
      promise.setSuccess(null);
    } else {
      closeAndFail(channel, FULL_EXCEPTION, promise);
    } 
  }
  
  private static void closeChannel(Channel channel) {
    channel.attr(POOL_KEY).getAndSet(null);
    channel.close();
  }
  
  private static void closeAndFail(Channel channel, Throwable cause, Promise<?> promise) {
    closeChannel(channel);
    promise.tryFailure(cause);
  }
  
  protected Channel pollChannel() {
    return this.lastRecentUsed ? this.deque.pollLast() : this.deque.pollFirst();
  }
  
  protected boolean offerChannel(Channel channel) {
    return this.deque.offer(channel);
  }
  
  public void close() {
    while (true) {
      Channel channel = pollChannel();
      if (channel == null)
        break; 
      channel.close();
    } 
  }
}
