package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.bootstrap.ChannelFactory;

public interface ChannelFactory<T extends Channel> extends ChannelFactory<T> {
  T newChannel();
}
