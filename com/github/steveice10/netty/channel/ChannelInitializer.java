package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.util.concurrent.ConcurrentMap;

@Sharable
public abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChannelInitializer.class);
  
  private final ConcurrentMap<ChannelHandlerContext, Boolean> initMap = PlatformDependent.newConcurrentHashMap();
  
  protected abstract void initChannel(C paramC) throws Exception;
  
  public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    if (initChannel(ctx)) {
      ctx.pipeline().fireChannelRegistered();
    } else {
      ctx.fireChannelRegistered();
    } 
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("Failed to initialize a channel. Closing: " + ctx.channel(), cause);
    ctx.close();
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isRegistered())
      initChannel(ctx); 
  }
  
  private boolean initChannel(ChannelHandlerContext ctx) throws Exception {
    if (this.initMap.putIfAbsent(ctx, Boolean.TRUE) == null) {
      try {
        initChannel((C)ctx.channel());
      } catch (Throwable cause) {
        exceptionCaught(ctx, cause);
      } finally {
        remove(ctx);
      } 
      return true;
    } 
    return false;
  }
  
  private void remove(ChannelHandlerContext ctx) {
    try {
      ChannelPipeline pipeline = ctx.pipeline();
      if (pipeline.context(this) != null)
        pipeline.remove(this); 
    } finally {
      this.initMap.remove(ctx);
    } 
  }
}
