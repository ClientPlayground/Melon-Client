package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.channel.ServerChannel;
import java.net.InetSocketAddress;

public interface ServerSocketChannel extends ServerChannel {
  ServerSocketChannelConfig config();
  
  InetSocketAddress localAddress();
  
  InetSocketAddress remoteAddress();
}
