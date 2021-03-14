package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;

public interface DuplexChannel extends Channel {
  boolean isInputShutdown();
  
  ChannelFuture shutdownInput();
  
  ChannelFuture shutdownInput(ChannelPromise paramChannelPromise);
  
  boolean isOutputShutdown();
  
  ChannelFuture shutdownOutput();
  
  ChannelFuture shutdownOutput(ChannelPromise paramChannelPromise);
  
  boolean isShutdown();
  
  ChannelFuture shutdown();
  
  ChannelFuture shutdown(ChannelPromise paramChannelPromise);
}
