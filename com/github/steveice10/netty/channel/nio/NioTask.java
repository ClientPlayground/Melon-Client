package com.github.steveice10.netty.channel.nio;

import java.nio.channels.SelectionKey;

public interface NioTask<C extends java.nio.channels.SelectableChannel> {
  void channelReady(C paramC, SelectionKey paramSelectionKey) throws Exception;
  
  void channelUnregistered(C paramC, Throwable paramThrowable) throws Exception;
}
