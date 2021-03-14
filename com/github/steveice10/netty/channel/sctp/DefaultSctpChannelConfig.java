package com.github.steveice10.netty.channel.sctp;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.DefaultChannelConfig;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpStandardSocketOptions;
import java.io.IOException;
import java.util.Map;

public class DefaultSctpChannelConfig extends DefaultChannelConfig implements SctpChannelConfig {
  private final SctpChannel javaChannel;
  
  public DefaultSctpChannelConfig(SctpChannel channel, SctpChannel javaChannel) {
    super(channel);
    if (javaChannel == null)
      throw new NullPointerException("javaChannel"); 
    this.javaChannel = javaChannel;
    if (PlatformDependent.canEnableTcpNoDelayByDefault())
      try {
        setSctpNoDelay(true);
      } catch (Exception exception) {} 
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super
        .getOptions(), new ChannelOption[] { ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, SctpChannelOption.SCTP_NODELAY, SctpChannelOption.SCTP_INIT_MAXSTREAMS });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == ChannelOption.SO_RCVBUF)
      return (T)Integer.valueOf(getReceiveBufferSize()); 
    if (option == ChannelOption.SO_SNDBUF)
      return (T)Integer.valueOf(getSendBufferSize()); 
    if (option == SctpChannelOption.SCTP_NODELAY)
      return (T)Boolean.valueOf(isSctpNoDelay()); 
    if (option == SctpChannelOption.SCTP_INIT_MAXSTREAMS)
      return (T)getInitMaxStreams(); 
    return (T)super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == ChannelOption.SO_RCVBUF) {
      setReceiveBufferSize(((Integer)value).intValue());
    } else if (option == ChannelOption.SO_SNDBUF) {
      setSendBufferSize(((Integer)value).intValue());
    } else if (option == SctpChannelOption.SCTP_NODELAY) {
      setSctpNoDelay(((Boolean)value).booleanValue());
    } else if (option == SctpChannelOption.SCTP_INIT_MAXSTREAMS) {
      setInitMaxStreams((SctpStandardSocketOptions.InitMaxStreams)value);
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  public boolean isSctpNoDelay() {
    try {
      return ((Boolean)this.javaChannel.<Boolean>getOption(SctpStandardSocketOptions.SCTP_NODELAY)).booleanValue();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public SctpChannelConfig setSctpNoDelay(boolean sctpNoDelay) {
    try {
      this.javaChannel.setOption(SctpStandardSocketOptions.SCTP_NODELAY, Boolean.valueOf(sctpNoDelay));
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
    return this;
  }
  
  public int getSendBufferSize() {
    try {
      return ((Integer)this.javaChannel.<Integer>getOption(SctpStandardSocketOptions.SO_SNDBUF)).intValue();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public SctpChannelConfig setSendBufferSize(int sendBufferSize) {
    try {
      this.javaChannel.setOption(SctpStandardSocketOptions.SO_SNDBUF, Integer.valueOf(sendBufferSize));
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
    return this;
  }
  
  public int getReceiveBufferSize() {
    try {
      return ((Integer)this.javaChannel.<Integer>getOption(SctpStandardSocketOptions.SO_RCVBUF)).intValue();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public SctpChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    try {
      this.javaChannel.setOption(SctpStandardSocketOptions.SO_RCVBUF, Integer.valueOf(receiveBufferSize));
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
    return this;
  }
  
  public SctpStandardSocketOptions.InitMaxStreams getInitMaxStreams() {
    try {
      return this.javaChannel.<SctpStandardSocketOptions.InitMaxStreams>getOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public SctpChannelConfig setInitMaxStreams(SctpStandardSocketOptions.InitMaxStreams initMaxStreams) {
    try {
      this.javaChannel.setOption(SctpStandardSocketOptions.SCTP_INIT_MAXSTREAMS, initMaxStreams);
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
    return this;
  }
  
  public SctpChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public SctpChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public SctpChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public SctpChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public SctpChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public SctpChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  public SctpChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  public SctpChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  public SctpChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  public SctpChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public SctpChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
}
