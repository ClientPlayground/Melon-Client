package com.github.steveice10.netty.handler.ipfilter;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ConcurrentSet;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

@Sharable
public class UniqueIpFilter extends AbstractRemoteAddressFilter<InetSocketAddress> {
  private final Set<InetAddress> connected = (Set<InetAddress>)new ConcurrentSet();
  
  protected boolean accept(ChannelHandlerContext ctx, InetSocketAddress remoteAddress) throws Exception {
    final InetAddress remoteIp = remoteAddress.getAddress();
    if (this.connected.contains(remoteIp))
      return false; 
    this.connected.add(remoteIp);
    ctx.channel().closeFuture().addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            UniqueIpFilter.this.connected.remove(remoteIp);
          }
        });
    return true;
  }
}
