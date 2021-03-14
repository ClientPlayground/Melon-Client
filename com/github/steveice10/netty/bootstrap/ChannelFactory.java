package com.github.steveice10.netty.bootstrap;

@Deprecated
public interface ChannelFactory<T extends com.github.steveice10.netty.channel.Channel> {
  T newChannel();
}
