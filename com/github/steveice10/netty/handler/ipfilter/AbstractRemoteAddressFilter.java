package com.github.steveice10.netty.handler.ipfilter;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import java.net.SocketAddress;

public abstract class AbstractRemoteAddressFilter<T extends SocketAddress> extends ChannelInboundHandlerAdapter {
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    handleNewChannel(ctx);
    ctx.fireChannelRegistered();
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (!handleNewChannel(ctx))
      throw new IllegalStateException("cannot determine to accept or reject a channel: " + ctx.channel()); 
    ctx.fireChannelActive();
  }
  
  private boolean handleNewChannel(ChannelHandlerContext ctx) throws Exception {
    SocketAddress socketAddress = ctx.channel().remoteAddress();
    if (socketAddress == null)
      return false; 
    ctx.pipeline().remove((ChannelHandler)this);
    if (accept(ctx, (T)socketAddress)) {
      channelAccepted(ctx, (T)socketAddress);
    } else {
      ChannelFuture rejectedFuture = channelRejected(ctx, (T)socketAddress);
      if (rejectedFuture != null) {
        rejectedFuture.addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
      } else {
        ctx.close();
      } 
    } 
    return true;
  }
  
  protected abstract boolean accept(ChannelHandlerContext paramChannelHandlerContext, T paramT) throws Exception;
  
  protected void channelAccepted(ChannelHandlerContext ctx, T remoteAddress) {}
  
  protected ChannelFuture channelRejected(ChannelHandlerContext ctx, T remoteAddress) {
    return null;
  }
}
