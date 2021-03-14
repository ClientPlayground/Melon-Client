package com.github.steveice10.netty.channel.udt;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;

@Deprecated
public interface UdtChannelConfig extends ChannelConfig {
  int getProtocolReceiveBufferSize();
  
  int getProtocolSendBufferSize();
  
  int getReceiveBufferSize();
  
  int getSendBufferSize();
  
  int getSoLinger();
  
  int getSystemReceiveBufferSize();
  
  int getSystemSendBufferSize();
  
  boolean isReuseAddress();
  
  UdtChannelConfig setConnectTimeoutMillis(int paramInt);
  
  @Deprecated
  UdtChannelConfig setMaxMessagesPerRead(int paramInt);
  
  UdtChannelConfig setWriteSpinCount(int paramInt);
  
  UdtChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  UdtChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  UdtChannelConfig setAutoRead(boolean paramBoolean);
  
  UdtChannelConfig setAutoClose(boolean paramBoolean);
  
  UdtChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  UdtChannelConfig setWriteBufferLowWaterMark(int paramInt);
  
  UdtChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
  
  UdtChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
  
  UdtChannelConfig setProtocolReceiveBufferSize(int paramInt);
  
  UdtChannelConfig setProtocolSendBufferSize(int paramInt);
  
  UdtChannelConfig setReceiveBufferSize(int paramInt);
  
  UdtChannelConfig setReuseAddress(boolean paramBoolean);
  
  UdtChannelConfig setSendBufferSize(int paramInt);
  
  UdtChannelConfig setSoLinger(int paramInt);
  
  UdtChannelConfig setSystemReceiveBufferSize(int paramInt);
  
  UdtChannelConfig setSystemSendBufferSize(int paramInt);
}
