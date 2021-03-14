package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.channel.socket.DuplexChannel;

public interface DomainSocketChannel extends UnixChannel, DuplexChannel {
  DomainSocketAddress remoteAddress();
  
  DomainSocketAddress localAddress();
  
  DomainSocketChannelConfig config();
}
