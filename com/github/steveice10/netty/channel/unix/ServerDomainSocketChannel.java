package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.channel.ServerChannel;

public interface ServerDomainSocketChannel extends ServerChannel, UnixChannel {
  DomainSocketAddress remoteAddress();
  
  DomainSocketAddress localAddress();
}
