package com.github.steveice10.netty.channel.group;

import com.github.steveice10.netty.channel.Channel;

public interface ChannelMatcher {
  boolean matches(Channel paramChannel);
}
