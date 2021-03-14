package com.github.steveice10.netty.channel.socket;

import java.net.InetSocketAddress;

public interface SocketChannel extends DuplexChannel {
  ServerSocketChannel parent();
  
  SocketChannelConfig config();
  
  InetSocketAddress localAddress();
  
  InetSocketAddress remoteAddress();
}
