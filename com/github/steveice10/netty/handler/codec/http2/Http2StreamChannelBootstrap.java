package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Http2StreamChannelBootstrap {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2StreamChannelBootstrap.class);
  
  private final Map<ChannelOption<?>, Object> options = new LinkedHashMap<ChannelOption<?>, Object>();
  
  private final Map<AttributeKey<?>, Object> attrs = new LinkedHashMap<AttributeKey<?>, Object>();
  
  private final Channel channel;
  
  private volatile ChannelHandler handler;
  
  public Http2StreamChannelBootstrap(Channel channel) {
    this.channel = (Channel)ObjectUtil.checkNotNull(channel, "channel");
  }
  
  public <T> Http2StreamChannelBootstrap option(ChannelOption<T> option, T value) {
    if (option == null)
      throw new NullPointerException("option"); 
    if (value == null) {
      synchronized (this.options) {
        this.options.remove(option);
      } 
    } else {
      synchronized (this.options) {
        this.options.put(option, value);
      } 
    } 
    return this;
  }
  
  public <T> Http2StreamChannelBootstrap attr(AttributeKey<T> key, T value) {
    if (key == null)
      throw new NullPointerException("key"); 
    if (value == null) {
      synchronized (this.attrs) {
        this.attrs.remove(key);
      } 
    } else {
      synchronized (this.attrs) {
        this.attrs.put(key, value);
      } 
    } 
    return this;
  }
  
  public Http2StreamChannelBootstrap handler(ChannelHandler handler) {
    this.handler = (ChannelHandler)ObjectUtil.checkNotNull(handler, "handler");
    return this;
  }
  
  public Future<Http2StreamChannel> open() {
    return open(this.channel.eventLoop().newPromise());
  }
  
  public Future<Http2StreamChannel> open(final Promise<Http2StreamChannel> promise) {
    final ChannelHandlerContext ctx = this.channel.pipeline().context(Http2MultiplexCodec.class);
    if (ctx == null) {
      if (this.channel.isActive()) {
        promise.setFailure(new IllegalStateException(StringUtil.simpleClassName(Http2MultiplexCodec.class) + " must be in the ChannelPipeline of Channel " + this.channel));
      } else {
        promise.setFailure(new ClosedChannelException());
      } 
    } else {
      EventExecutor executor = ctx.executor();
      if (executor.inEventLoop()) {
        open0(ctx, promise);
      } else {
        executor.execute(new Runnable() {
              public void run() {
                Http2StreamChannelBootstrap.this.open0(ctx, promise);
              }
            });
      } 
    } 
    return (Future<Http2StreamChannel>)promise;
  }
  
  public void open0(ChannelHandlerContext ctx, final Promise<Http2StreamChannel> promise) {
    assert ctx.executor().inEventLoop();
    final Http2StreamChannel streamChannel = ((Http2MultiplexCodec)ctx.handler()).newOutboundStream();
    try {
      init(streamChannel);
    } catch (Exception e) {
      streamChannel.unsafe().closeForcibly();
      promise.setFailure(e);
      return;
    } 
    ChannelFuture future = ctx.channel().eventLoop().register(streamChannel);
    future.addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
              promise.setSuccess(streamChannel);
            } else if (future.isCancelled()) {
              promise.cancel(false);
            } else {
              if (streamChannel.isRegistered()) {
                streamChannel.close();
              } else {
                streamChannel.unsafe().closeForcibly();
              } 
              promise.setFailure(future.cause());
            } 
          }
        });
  }
  
  private void init(Channel channel) throws Exception {
    ChannelPipeline p = channel.pipeline();
    ChannelHandler handler = this.handler;
    if (handler != null)
      p.addLast(new ChannelHandler[] { handler }); 
    synchronized (this.options) {
      setChannelOptions(channel, this.options, logger);
    } 
    synchronized (this.attrs) {
      for (Map.Entry<AttributeKey<?>, Object> e : this.attrs.entrySet())
        channel.attr(e.getKey()).set(e.getValue()); 
    } 
  }
  
  private static void setChannelOptions(Channel channel, Map<ChannelOption<?>, Object> options, InternalLogger logger) {
    for (Map.Entry<ChannelOption<?>, Object> e : options.entrySet())
      setChannelOption(channel, e.getKey(), e.getValue(), logger); 
  }
  
  private static void setChannelOption(Channel channel, ChannelOption<?> option, Object value, InternalLogger logger) {
    try {
      if (!channel.config().setOption(option, value))
        logger.warn("Unknown channel option '{}' for channel '{}'", option, channel); 
    } catch (Throwable t) {
      logger.warn("Failed to set channel option '{}' with value '{}' for channel '{}'", new Object[] { option, value, channel, t });
    } 
  }
}
