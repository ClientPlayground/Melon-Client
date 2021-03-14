package com.github.steveice10.netty.channel.socket;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import java.net.InetAddress;
import java.net.NetworkInterface;

public interface DatagramChannelConfig extends ChannelConfig {
  int getSendBufferSize();
  
  DatagramChannelConfig setSendBufferSize(int paramInt);
  
  int getReceiveBufferSize();
  
  DatagramChannelConfig setReceiveBufferSize(int paramInt);
  
  int getTrafficClass();
  
  DatagramChannelConfig setTrafficClass(int paramInt);
  
  boolean isReuseAddress();
  
  DatagramChannelConfig setReuseAddress(boolean paramBoolean);
  
  boolean isBroadcast();
  
  DatagramChannelConfig setBroadcast(boolean paramBoolean);
  
  boolean isLoopbackModeDisabled();
  
  DatagramChannelConfig setLoopbackModeDisabled(boolean paramBoolean);
  
  int getTimeToLive();
  
  DatagramChannelConfig setTimeToLive(int paramInt);
  
  InetAddress getInterface();
  
  DatagramChannelConfig setInterface(InetAddress paramInetAddress);
  
  NetworkInterface getNetworkInterface();
  
  DatagramChannelConfig setNetworkInterface(NetworkInterface paramNetworkInterface);
  
  @Deprecated
  DatagramChannelConfig setMaxMessagesPerRead(int paramInt);
  
  DatagramChannelConfig setWriteSpinCount(int paramInt);
  
  DatagramChannelConfig setConnectTimeoutMillis(int paramInt);
  
  DatagramChannelConfig setAllocator(ByteBufAllocator paramByteBufAllocator);
  
  DatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator paramRecvByteBufAllocator);
  
  DatagramChannelConfig setAutoRead(boolean paramBoolean);
  
  DatagramChannelConfig setAutoClose(boolean paramBoolean);
  
  DatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator paramMessageSizeEstimator);
  
  DatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark paramWriteBufferWaterMark);
}
