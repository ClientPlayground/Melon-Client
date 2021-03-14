package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.channel.ChannelOption;

public class UnixChannelOption<T> extends ChannelOption<T> {
  public static final ChannelOption<Boolean> SO_REUSEPORT = valueOf(UnixChannelOption.class, "SO_REUSEPORT");
  
  public static final ChannelOption<DomainSocketReadMode> DOMAIN_SOCKET_READ_MODE = ChannelOption.valueOf(UnixChannelOption.class, "DOMAIN_SOCKET_READ_MODE");
  
  protected UnixChannelOption() {
    super(null);
  }
}
