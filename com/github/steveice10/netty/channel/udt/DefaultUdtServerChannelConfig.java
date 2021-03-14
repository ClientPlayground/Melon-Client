package com.github.steveice10.netty.channel.udt;

import com.barchart.udt.nio.ChannelUDT;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import java.io.IOException;
import java.util.Map;

@Deprecated
public class DefaultUdtServerChannelConfig extends DefaultUdtChannelConfig implements UdtServerChannelConfig {
  private volatile int backlog = 64;
  
  public DefaultUdtServerChannelConfig(UdtChannel channel, ChannelUDT channelUDT, boolean apply) throws IOException {
    super(channel, channelUDT, apply);
    if (apply)
      apply(channelUDT); 
  }
  
  protected void apply(ChannelUDT channelUDT) throws IOException {}
  
  public int getBacklog() {
    return this.backlog;
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == ChannelOption.SO_BACKLOG)
      return (T)Integer.valueOf(getBacklog()); 
    return super.getOption(option);
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super.getOptions(), new ChannelOption[] { ChannelOption.SO_BACKLOG });
  }
  
  public UdtServerChannelConfig setBacklog(int backlog) {
    this.backlog = backlog;
    return this;
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == ChannelOption.SO_BACKLOG) {
      setBacklog(((Integer)value).intValue());
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public UdtServerChannelConfig setProtocolReceiveBufferSize(int protocolReceiveBufferSize) {
    super.setProtocolReceiveBufferSize(protocolReceiveBufferSize);
    return this;
  }
  
  public UdtServerChannelConfig setProtocolSendBufferSize(int protocolSendBufferSize) {
    super.setProtocolSendBufferSize(protocolSendBufferSize);
    return this;
  }
  
  public UdtServerChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    super.setReceiveBufferSize(receiveBufferSize);
    return this;
  }
  
  public UdtServerChannelConfig setReuseAddress(boolean reuseAddress) {
    super.setReuseAddress(reuseAddress);
    return this;
  }
  
  public UdtServerChannelConfig setSendBufferSize(int sendBufferSize) {
    super.setSendBufferSize(sendBufferSize);
    return this;
  }
  
  public UdtServerChannelConfig setSoLinger(int soLinger) {
    super.setSoLinger(soLinger);
    return this;
  }
  
  public UdtServerChannelConfig setSystemReceiveBufferSize(int systemSendBufferSize) {
    super.setSystemReceiveBufferSize(systemSendBufferSize);
    return this;
  }
  
  public UdtServerChannelConfig setSystemSendBufferSize(int systemReceiveBufferSize) {
    super.setSystemSendBufferSize(systemReceiveBufferSize);
    return this;
  }
  
  public UdtServerChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public UdtServerChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public UdtServerChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public UdtServerChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public UdtServerChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public UdtServerChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  public UdtServerChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  public UdtServerChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public UdtServerChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  public UdtServerChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public UdtServerChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
}
