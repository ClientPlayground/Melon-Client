package com.github.steveice10.netty.channel.socket.oio;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.PreferHeapByteBufAllocator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.socket.DefaultServerSocketChannelConfig;
import com.github.steveice10.netty.channel.socket.ServerSocketChannel;
import com.github.steveice10.netty.channel.socket.ServerSocketChannelConfig;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

public class DefaultOioServerSocketChannelConfig extends DefaultServerSocketChannelConfig implements OioServerSocketChannelConfig {
  @Deprecated
  public DefaultOioServerSocketChannelConfig(ServerSocketChannel channel, ServerSocket javaSocket) {
    super(channel, javaSocket);
    setAllocator((ByteBufAllocator)new PreferHeapByteBufAllocator(getAllocator()));
  }
  
  DefaultOioServerSocketChannelConfig(OioServerSocketChannel channel, ServerSocket javaSocket) {
    super(channel, javaSocket);
    setAllocator((ByteBufAllocator)new PreferHeapByteBufAllocator(getAllocator()));
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super
        .getOptions(), new ChannelOption[] { ChannelOption.SO_TIMEOUT });
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
  
  public OioServerSocketChannelConfig setSoTimeout(int timeout) {
    try {
      this.javaSocket.setSoTimeout(timeout);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
    return this;
  }
  
  public int getSoTimeout() {
    try {
      return this.javaSocket.getSoTimeout();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public OioServerSocketChannelConfig setBacklog(int backlog) {
    super.setBacklog(backlog);
    return this;
  }
  
  public OioServerSocketChannelConfig setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }
  
  public OioServerSocketChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }
  
  public OioServerSocketChannelConfig setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    super.setPerformancePreferences(connectionTime, latency, bandwidth);
    return this;
  }
  
  public OioServerSocketChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public OioServerSocketChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public OioServerSocketChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public OioServerSocketChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public OioServerSocketChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public OioServerSocketChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  protected void autoReadCleared() {
    if (this.channel instanceof OioServerSocketChannel)
      ((OioServerSocketChannel)this.channel).clearReadPending0(); 
  }
  
  public OioServerSocketChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  public OioServerSocketChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  public OioServerSocketChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public OioServerSocketChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public OioServerSocketChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
}
