package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.channel.Channel;

public interface UnixChannel extends Channel {
  FileDescriptor fd();
}
