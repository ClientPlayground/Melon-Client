package com.github.steveice10.netty.channel.sctp;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.sun.nio.sctp.Association;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;

public interface SctpChannel extends Channel {
  SctpServerChannel parent();
  
  Association association();
  
  InetSocketAddress localAddress();
  
  Set<InetSocketAddress> allLocalAddresses();
  
  SctpChannelConfig config();
  
  InetSocketAddress remoteAddress();
  
  Set<InetSocketAddress> allRemoteAddresses();
  
  ChannelFuture bindAddress(InetAddress paramInetAddress);
  
  ChannelFuture bindAddress(InetAddress paramInetAddress, ChannelPromise paramChannelPromise);
  
  ChannelFuture unbindAddress(InetAddress paramInetAddress);
  
  ChannelFuture unbindAddress(InetAddress paramInetAddress, ChannelPromise paramChannelPromise);
}
