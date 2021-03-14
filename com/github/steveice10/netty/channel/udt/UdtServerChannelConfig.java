package com.github.steveice10.netty.channel.udt;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;

@Deprecated
public interface UdtServerChannelConfig extends UdtChannelConfig {
  int getBacklog();
  
  UdtServerChannelConfig setBacklog(int paramInt);
  
  UdtServerChannelConfig setConnectTimeoutMillis(int paramInt);
  
  @Deprecated
  UdtServerChannelConfig setMaxMessagesPerRead(int paramInt);
  
  UdtServerChannelConfig setWriteSpinCount(int paramInt);
  
  UdtServerChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  UdtServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  UdtServerChannelConfig setAutoRead(boolean paramBoolean);
  
  UdtServerChannelConfig setAutoClose(boolean paramBoolean);
  
  UdtServerChannelConfig setProtocolReceiveBufferSize(int paramInt);
  
  UdtServerChannelConfig setProtocolSendBufferSize(int paramInt);
  
  UdtServerChannelConfig setReceiveBufferSize(int paramInt);
  
  UdtServerChannelConfig setReuseAddress(boolean paramBoolean);
  
  UdtServerChannelConfig setSendBufferSize(int paramInt);
  
  UdtServerChannelConfig setSoLinger(int paramInt);
  
  UdtServerChannelConfig setSystemReceiveBufferSize(int paramInt);
  
  UdtServerChannelConfig setSystemSendBufferSize(int paramInt);
  
  UdtServerChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  UdtServerChannelConfig setWriteBufferLowWaterMark(int paramInt);
  
  UdtServerChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
  
  UdtServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
}
