package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;

public interface ServerSocketChannelConfig extends ChannelConfig {
  int getBacklog();
  
  ServerSocketChannelConfig setBacklog(int paramInt);
  
  boolean isReuseAddress();
  
  ServerSocketChannelConfig setReuseAddress(boolean paramBoolean);
  
  int getReceiveBufferSize();
  
  ServerSocketChannelConfig setReceiveBufferSize(int paramInt);
  
  ServerSocketChannelConfig setPerformancePreferences(int paramInt1, int paramInt2, int paramInt3);
  
  ServerSocketChannelConfig setConnectTimeoutMillis(int paramInt);
  
  @Deprecated
  ServerSocketChannelConfig setMaxMessagesPerRead(int paramInt);
  
  ServerSocketChannelConfig setWriteSpinCount(int paramInt);
  
  ServerSocketChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  ServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  ServerSocketChannelConfig setAutoRead(boolean paramBoolean);
  
  ServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
  
  ServerSocketChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  ServerSocketChannelConfig setWriteBufferLowWaterMark(int paramInt);
  
  ServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
}
