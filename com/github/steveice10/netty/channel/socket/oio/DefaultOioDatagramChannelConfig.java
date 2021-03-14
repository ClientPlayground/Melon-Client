package com.github.steveice10.netty.channel.socket.oio;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.PreferHeapByteBufAllocator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.socket.DatagramChannel;
import com.github.steveice10.netty.channel.socket.DatagramChannelConfig;
import com.github.steveice10.netty.channel.socket.DefaultDatagramChannelConfig;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Map;

final class DefaultOioDatagramChannelConfig extends DefaultDatagramChannelConfig implements OioDatagramChannelConfig {
  DefaultOioDatagramChannelConfig(DatagramChannel channel, DatagramSocket javaSocket) {
    super(channel, javaSocket);
    setAllocator((ByteBufAllocator)new PreferHeapByteBufAllocator(getAllocator()));
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_TIMEOUT });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == ChannelOption.SO_TIMEOUT)
      return (T)Integer.valueOf(getSoTimeout()); 
    return (T)super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == ChannelOption.SO_TIMEOUT) {
      setSoTimeout(((Integer)value).intValue());
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public OioDatagramChannelConfig setSoTimeout(int timeout) {
    try {
      javaSocket().setSoTimeout(timeout);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
    return this;
  }
  
  public int getSoTimeout() {
    try {
      return javaSocket().getSoTimeout();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public OioDatagramChannelConfig setBroadcast(boolean broadcast) {
    super.setBroadcast(broadcast);
    return this;
  }
  
  public OioDatagramChannelConfig setInterface(InetAddress interfaceAddress) {
    super.setInterface(interfaceAddress);
    return this;
  }
  
  public OioDatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled) {
    super.setLoopbackModeDisabled(loopbackModeDisabled);
    return this;
  }
  
  public OioDatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface) {
    super.setNetworkInterface(networkInterface);
    return this;
  }
  
  public OioDatagramChannelConfig setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }
  
  public OioDatagramChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }
  
  public OioDatagramChannelConfig setSendBufferSize(int sendBufferSize) {
    super.setSendBufferSize(sendBufferSize);
    return this;
  }
  
  public OioDatagramChannelConfig setTimeToLive(int ttl) {
    super.setTimeToLive(ttl);
    return this;
  }
  
  public OioDatagramChannelConfig setTrafficClass(int trafficClass) {
    super.setTrafficClass(trafficClass);
    return this;
  }
  
  public OioDatagramChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public OioDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  public OioDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public OioDatagramChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public OioDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public OioDatagramChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  public OioDatagramChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  public OioDatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  public OioDatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public OioDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public OioDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
}
