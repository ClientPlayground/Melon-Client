package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;

public interface SocketChannelConfig extends ChannelConfig {
  boolean isTcpNoDelay();
  
  SocketChannelConfig setTcpNoDelay(boolean paramBoolean);
  
  int getSoLinger();
  
  SocketChannelConfig setSoLinger(int paramInt);
  
  int getSendBufferSize();
  
  SocketChannelConfig setSendBufferSize(int paramInt);
  
  int getReceiveBufferSize();
  
  SocketChannelConfig setReceiveBufferSize(int paramInt);
  
  boolean isKeepAlive();
  
  SocketChannelConfig setKeepAlive(boolean paramBoolean);
  
  int getTrafficClass();
  
  SocketChannelConfig setTrafficClass(int paramInt);
  
  boolean isReuseAddress();
  
  SocketChannelConfig setReuseAddress(boolean paramBoolean);
  
  SocketChannelConfig setPerformancePreferences(int paramInt1, int paramInt2, int paramInt3);
  
  boolean isAllowHalfClosure();
  
  SocketChannelConfig setAllowHalfClosure(boolean paramBoolean);
  
  SocketChannelConfig setConnectTimeoutMillis(int paramInt);
  
  @Deprecated
  SocketChannelConfig setMaxMessagesPerRead(int paramInt);
  
  SocketChannelConfig setWriteSpinCount(int paramInt);
  
  SocketChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  SocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  SocketChannelConfig setAutoRead(boolean paramBoolean);
  
  SocketChannelConfig setAutoClose(boolean paramBoolean);
  
  SocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
  
  SocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
}
