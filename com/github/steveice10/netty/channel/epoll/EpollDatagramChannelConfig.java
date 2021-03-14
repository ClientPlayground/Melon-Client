package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.FixedRecvByteBufAllocator;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.channel.socket.DatagramChannelConfig;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Map;

public final class EpollDatagramChannelConfig extends EpollChannelConfig implements DatagramChannelConfig {
  private static final RecvByteBufAllocator DEFAULT_RCVBUF_ALLOCATOR = (RecvByteBufAllocator)new FixedRecvByteBufAllocator(2048);
  
  private final EpollDatagramChannel datagramChannel;
  
  private boolean activeOnOpen;
  
  EpollDatagramChannelConfig(EpollDatagramChannel channel) {
    super(channel);
    this.datagramChannel = channel;
    setRecvByteBufAllocator(DEFAULT_RCVBUF_ALLOCATOR);
  }
  
  public Map<ChannelOption<?>, Object> getOptions() {
    return getOptions(super
        .getOptions(), new ChannelOption[] { 
          ChannelOption.SO_BROADCAST, ChannelOption.SO_RCVBUF, ChannelOption.SO_SNDBUF, ChannelOption.SO_REUSEADDR, ChannelOption.IP_MULTICAST_LOOP_DISABLED, ChannelOption.IP_MULTICAST_ADDR, ChannelOption.IP_MULTICAST_IF, ChannelOption.IP_MULTICAST_TTL, ChannelOption.IP_TOS, ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION, 
          EpollChannelOption.SO_REUSEPORT, EpollChannelOption.IP_TRANSPARENT, EpollChannelOption.IP_RECVORIGDSTADDR });
  }
  
  public <T> T getOption(ChannelOption<T> option) {
    if (option == ChannelOption.SO_BROADCAST)
      return (T)Boolean.valueOf(isBroadcast()); 
    if (option == ChannelOption.SO_RCVBUF)
      return (T)Integer.valueOf(getReceiveBufferSize()); 
    if (option == ChannelOption.SO_SNDBUF)
      return (T)Integer.valueOf(getSendBufferSize()); 
    if (option == ChannelOption.SO_REUSEADDR)
      return (T)Boolean.valueOf(isReuseAddress()); 
    if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED)
      return (T)Boolean.valueOf(isLoopbackModeDisabled()); 
    if (option == ChannelOption.IP_MULTICAST_ADDR)
      return (T)getInterface(); 
    if (option == ChannelOption.IP_MULTICAST_IF)
      return (T)getNetworkInterface(); 
    if (option == ChannelOption.IP_MULTICAST_TTL)
      return (T)Integer.valueOf(getTimeToLive()); 
    if (option == ChannelOption.IP_TOS)
      return (T)Integer.valueOf(getTrafficClass()); 
    if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION)
      return (T)Boolean.valueOf(this.activeOnOpen); 
    if (option == EpollChannelOption.SO_REUSEPORT)
      return (T)Boolean.valueOf(isReusePort()); 
    if (option == EpollChannelOption.IP_TRANSPARENT)
      return (T)Boolean.valueOf(isIpTransparent()); 
    if (option == EpollChannelOption.IP_RECVORIGDSTADDR)
      return (T)Boolean.valueOf(isIpRecvOrigDestAddr()); 
    return super.getOption(option);
  }
  
  public <T> boolean setOption(ChannelOption<T> option, T value) {
    validate(option, value);
    if (option == ChannelOption.SO_BROADCAST) {
      setBroadcast(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.SO_RCVBUF) {
      setReceiveBufferSize(((Integer)value).intValue());
    } else if (option == ChannelOption.SO_SNDBUF) {
      setSendBufferSize(((Integer)value).intValue());
    } else if (option == ChannelOption.SO_REUSEADDR) {
      setReuseAddress(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.IP_MULTICAST_LOOP_DISABLED) {
      setLoopbackModeDisabled(((Boolean)value).booleanValue());
    } else if (option == ChannelOption.IP_MULTICAST_ADDR) {
      setInterface((InetAddress)value);
    } else if (option == ChannelOption.IP_MULTICAST_IF) {
      setNetworkInterface((NetworkInterface)value);
    } else if (option == ChannelOption.IP_MULTICAST_TTL) {
      setTimeToLive(((Integer)value).intValue());
    } else if (option == ChannelOption.IP_TOS) {
      setTrafficClass(((Integer)value).intValue());
    } else if (option == ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION) {
      setActiveOnOpen(((Boolean)value).booleanValue());
    } else if (option == EpollChannelOption.SO_REUSEPORT) {
      setReusePort(((Boolean)value).booleanValue());
    } else if (option == EpollChannelOption.IP_TRANSPARENT) {
      setIpTransparent(((Boolean)value).booleanValue());
    } else if (option == EpollChannelOption.IP_RECVORIGDSTADDR) {
      setIpRecvOrigDestAddr(((Boolean)value).booleanValue());
    } else {
      return super.setOption(option, value);
    } 
    return true;
  }
  
  private void setActiveOnOpen(boolean activeOnOpen) {
    if (this.channel.isRegistered())
      throw new IllegalStateException("Can only changed before channel was registered"); 
    this.activeOnOpen = activeOnOpen;
  }
  
  boolean getActiveOnOpen() {
    return this.activeOnOpen;
  }
  
  public EpollDatagramChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
    super.setMessageSizeEstimator(estimator);
    return this;
  }
  
  @Deprecated
  public EpollDatagramChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
    super.setWriteBufferLowWaterMark(writeBufferLowWaterMark);
    return this;
  }
  
  @Deprecated
  public EpollDatagramChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
    super.setWriteBufferHighWaterMark(writeBufferHighWaterMark);
    return this;
  }
  
  public EpollDatagramChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
    super.setWriteBufferWaterMark(writeBufferWaterMark);
    return this;
  }
  
  public EpollDatagramChannelConfig setAutoClose(boolean autoClose) {
    super.setAutoClose(autoClose);
    return this;
  }
  
  public EpollDatagramChannelConfig setAutoRead(boolean autoRead) {
    super.setAutoRead(autoRead);
    return this;
  }
  
  public EpollDatagramChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
    super.setRecvByteBufAllocator(allocator);
    return this;
  }
  
  public EpollDatagramChannelConfig setWriteSpinCount(int writeSpinCount) {
    super.setWriteSpinCount(writeSpinCount);
    return this;
  }
  
  public EpollDatagramChannelConfig setAllocator(ByteBufAllocator allocator) {
    super.setAllocator(allocator);
    return this;
  }
  
  public EpollDatagramChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
    super.setConnectTimeoutMillis(connectTimeoutMillis);
    return this;
  }
  
  @Deprecated
  public EpollDatagramChannelConfig setMaxMessagesPerRead(int maxMessagesPerRead) {
    super.setMaxMessagesPerRead(maxMessagesPerRead);
    return this;
  }
  
  public int getSendBufferSize() {
    try {
      return this.datagramChannel.socket.getSendBufferSize();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setSendBufferSize(int sendBufferSize) {
    try {
      this.datagramChannel.socket.setSendBufferSize(sendBufferSize);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getReceiveBufferSize() {
    try {
      return this.datagramChannel.socket.getReceiveBufferSize();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setReceiveBufferSize(int receiveBufferSize) {
    try {
      this.datagramChannel.socket.setReceiveBufferSize(receiveBufferSize);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public int getTrafficClass() {
    try {
      return this.datagramChannel.socket.getTrafficClass();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setTrafficClass(int trafficClass) {
    try {
      this.datagramChannel.socket.setTrafficClass(trafficClass);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isReuseAddress() {
    try {
      return this.datagramChannel.socket.isReuseAddress();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setReuseAddress(boolean reuseAddress) {
    try {
      this.datagramChannel.socket.setReuseAddress(reuseAddress);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isBroadcast() {
    try {
      return this.datagramChannel.socket.isBroadcast();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setBroadcast(boolean broadcast) {
    try {
      this.datagramChannel.socket.setBroadcast(broadcast);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isLoopbackModeDisabled() {
    return false;
  }
  
  public DatagramChannelConfig setLoopbackModeDisabled(boolean loopbackModeDisabled) {
    throw new UnsupportedOperationException("Multicast not supported");
  }
  
  public int getTimeToLive() {
    return -1;
  }
  
  public EpollDatagramChannelConfig setTimeToLive(int ttl) {
    throw new UnsupportedOperationException("Multicast not supported");
  }
  
  public InetAddress getInterface() {
    return null;
  }
  
  public EpollDatagramChannelConfig setInterface(InetAddress interfaceAddress) {
    throw new UnsupportedOperationException("Multicast not supported");
  }
  
  public NetworkInterface getNetworkInterface() {
    return null;
  }
  
  public EpollDatagramChannelConfig setNetworkInterface(NetworkInterface networkInterface) {
    throw new UnsupportedOperationException("Multicast not supported");
  }
  
  public EpollDatagramChannelConfig setEpollMode(EpollMode mode) {
    super.setEpollMode(mode);
    return this;
  }
  
  public boolean isReusePort() {
    try {
      return this.datagramChannel.socket.isReusePort();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setReusePort(boolean reusePort) {
    try {
      this.datagramChannel.socket.setReusePort(reusePort);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isIpTransparent() {
    try {
      return this.datagramChannel.socket.isIpTransparent();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setIpTransparent(boolean ipTransparent) {
    try {
      this.datagramChannel.socket.setIpTransparent(ipTransparent);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public boolean isIpRecvOrigDestAddr() {
    try {
      return this.datagramChannel.socket.isIpRecvOrigDestAddr();
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
  
  public EpollDatagramChannelConfig setIpRecvOrigDestAddr(boolean ipTransparent) {
    try {
      this.datagramChannel.socket.setIpRecvOrigDestAddr(ipTransparent);
      return this;
    } catch (IOException e) {
      throw new ChannelException(e);
    } 
  }
}