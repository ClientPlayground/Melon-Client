package com.github.steveice10.netty.channel.socket.oio;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.socket.DatagramChannelConfig;
import java.net.InetAddress;
import java.net.NetworkInterface;

public interface OioDatagramChannelConfig extends DatagramChannelConfig {
  OioDatagramChannelConfig setSoTimeout(int paramInt);
  
  int getSoTimeout();
  
  OioDatagramChannelConfig setSendBufferSize(int paramInt);
  
  OioDatagramChannelConfig setReceiveBufferSize(int paramInt);
  
  OioDatagramChannelConfig setTrafficClass(int paramInt);
  
  OioDatagramChannelConfig setReuseAddress(boolean paramBoolean);
  
  OioDatagramChannelConfig setBroadcast(boolean paramBoolean);
  
  OioDatagramChannelConfig setLoopbackModeDisabled(boolean paramBoolean);
  
  OioDatagramChannelConfig setTimeToLive(int paramInt);
  
  OioDatagramChannelConfig setInterface(InetAddress paramInetAddress);
  
  OioDatagramChannelConfig setNetworkInterface(NetworkInterface paramNetworkInterface);
  
  OioDatagramChannelConfig setMaxMessagesPerRead(int paramInt);
  
  OioDatagramChannelConfig setWriteSpinCount(int paramInt);
  
  OioDatagramChannelConfig setConnectTimeoutMillis(int paramInt);
  
  OioDatagramChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  OioDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  OioDatagramChannelConfig setAutoRead(boolean paramBoolean);
  
  OioDatagramChannelConfig setAutoClose(boolean paramBoolean);
  
  OioDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
  
  OioDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
  
  OioDatagramChannelConfig setWriteBufferHighWaterMark(int paramInt);
  
  OioDatagramChannelConfig setWriteBufferLowWaterMark(int paramInt);
}
