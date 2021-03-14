package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

public interface DatagramChannel extends Channel {
  DatagramChannelConfig config();
  
  InetSocketAddress localAddress();
  
  InetSocketAddress remoteAddress();
  
  boolean isConnected();
  
  ChannelFuture joinGroup(InetAddress paramInetAddress);
  
  ChannelFuture joinGroup(InetAddress paramInetAddress, ChannelPromise paramChannelPromise);
  
  ChannelFuture joinGroup(InetSocketAddress paramInetSocketAddress, NetworkInterface paramNetworkInterface);
  
  ChannelFuture joinGroup(InetSocketAddress paramInetSocketAddress, NetworkInterface paramNetworkInterface, ChannelPromise paramChannelPromise);
  
  ChannelFuture joinGroup(InetAddress paramInetAddress1, NetworkInterface paramNetworkInterface, InetAddress paramInetAddress2);
  
  ChannelFuture joinGroup(InetAddress paramInetAddress1, NetworkInterface paramNetworkInterface, InetAddress paramInetAddress2, ChannelPromise paramChannelPromise);
  
  ChannelFuture leaveGroup(InetAddress paramInetAddress);
  
  ChannelFuture leaveGroup(InetAddress paramInetAddress, ChannelPromise paramChannelPromise);
  
  ChannelFuture leaveGroup(InetSocketAddress paramInetSocketAddress, NetworkInterface paramNetworkInterface);
  
  ChannelFuture leaveGroup(InetSocketAddress paramInetSocketAddress, NetworkInterface paramNetworkInterface, ChannelPromise paramChannelPromise);
  
  ChannelFuture leaveGroup(InetAddress paramInetAddress1, NetworkInterface paramNetworkInterface, InetAddress paramInetAddress2);
  
  ChannelFuture leaveGroup(InetAddress paramInetAddress1, NetworkInterface paramNetworkInterface, InetAddress paramInetAddress2, ChannelPromise paramChannelPromise);
  
  ChannelFuture block(InetAddress paramInetAddress1, NetworkInterface paramNetworkInterface, InetAddress paramInetAddress2);
  
  ChannelFuture block(InetAddress paramInetAddress1, NetworkInterface paramNetworkInterface, InetAddress paramInetAddress2, ChannelPromise paramChannelPromise);
  
  ChannelFuture block(InetAddress paramInetAddress1, InetAddress paramInetAddress2);
  
  ChannelFuture block(InetAddress paramInetAddress1, InetAddress paramInetAddress2, ChannelPromise paramChannelPromise);
}
