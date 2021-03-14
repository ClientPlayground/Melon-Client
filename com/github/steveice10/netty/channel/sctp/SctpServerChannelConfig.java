package com.github.steveice10.netty.channel.sctp;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.sun.nio.sctp.SctpStandardSocketOptions;

public interface SctpServerChannelConfig extends ChannelConfig {
  int getBacklog();
  
  SctpServerChannelConfig setBacklog(int paramInt);
  
  int getSendBufferSize();
  
  SctpServerChannelConfig setSendBufferSize(int paramInt);
  
  int getReceiveBufferSize();
  
  SctpServerChannelConfig setReceiveBufferSize(int paramInt);
  
  SctpStandardSocketOptions.InitMaxStreams getInitMaxStreams();
  
  SctpServerChannelConfig setInitMaxStreams(SctpStandardSocketOptions.InitMaxStreams paramInitMaxStreams);
  
  @Deprecated
  SctpServerChannelConfig setMaxMessagesPerRead(int paramInt);
  
  SctpServerChannelConfig setWriteSpinCount(int paramInt);
  
  SctpServerChannelConfig setConnectTimeoutMillis(int paramInt);
  
  SctpServerChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  SctpServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  SctpServerChannelConfig setAutoRead(boolean paramBoolean);
  
  SctpServerChannelConfig setAutoClose(boolean paramBoolean);
  
  SctpServerChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  SctpServerChannelConfig setWriteBufferLowWaterMark(int paramInt);
  
  SctpServerChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
  
  SctpServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
}
