package com.github.steveice10.netty.channel.sctp;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.sun.nio.sctp.SctpStandardSocketOptions;

public interface SctpChannelConfig extends ChannelConfig {
  boolean isSctpNoDelay();
  
  SctpChannelConfig setSctpNoDelay(boolean paramBoolean);
  
  int getSendBufferSize();
  
  SctpChannelConfig setSendBufferSize(int paramInt);
  
  int getReceiveBufferSize();
  
  SctpChannelConfig setReceiveBufferSize(int paramInt);
  
  SctpStandardSocketOptions.InitMaxStreams getInitMaxStreams();
  
  SctpChannelConfig setInitMaxStreams(SctpStandardSocketOptions.InitMaxStreams paramInitMaxStreams);
  
  SctpChannelConfig setConnectTimeoutMillis(int paramInt);
  
  @Deprecated
  SctpChannelConfig setMaxMessagesPerRead(int paramInt);
  
  SctpChannelConfig setWriteSpinCount(int paramInt);
  
  SctpChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  SctpChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  SctpChannelConfig setAutoRead(boolean paramBoolean);
  
  SctpChannelConfig setAutoClose(boolean paramBoolean);
  
  SctpChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  SctpChannelConfig setWriteBufferLowWaterMark(int paramInt);
  
  SctpChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
  
  SctpChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
}
