package com.github.steveice10.netty.channel.udt;

import com.github.steveice10.netty.channel.Channel;
import java.net.InetSocketAddress;

@Deprecated
public interface UdtChannel extends Channel {
  UdtChannelConfig config();
  
  InetSocketAddress localAddress();
  
  InetSocketAddress remoteAddress();
}
