package com.github.steveice10.netty.channel.unix;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;

public interface DomainSocketChannelConfig extends ChannelConfig {
  @Deprecated
  DomainSocketChannelConfig setMaxMessagesPerRead(int paramInt);
  
  DomainSocketChannelConfig setConnectTimeoutMillis(int paramInt);
  
  DomainSocketChannelConfig setWriteSpinCount(int paramInt);
  
  DomainSocketChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  DomainSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  DomainSocketChannelConfig setAutoRead(boolean paramBoolean);
  
  DomainSocketChannelConfig setAutoClose(boolean paramBoolean);
  
  @Deprecated
  DomainSocketChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  @Deprecated
  DomainSocketChannelConfig setWriteBufferLowWaterMark(int paramInt);
  
  DomainSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
  
  DomainSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
  
  DomainSocketChannelConfig setReadMode(DomainSocketReadMode paramDomainSocketReadMode);
  
  DomainSocketReadMode getReadMode();
}
