package com.github.steveice10.netty.channel.socket.oio;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.AddressedEnvelope;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.oio.AbstractOioMessageChannel;
import com.github.steveice10.netty.channel.socket.DatagramChannel;
import com.github.steveice10.netty.channel.socket.DatagramChannelConfig;
import com.github.steveice10.netty.channel.socket.DatagramPacket;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.channels.NotYetConnectedException;
import java.util.List;
import java.util.Locale;

public class OioDatagramChannel extends AbstractOioMessageChannel implements DatagramChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(OioDatagramChannel.class);
  
  private static final ChannelMetadata METADATA = new ChannelMetadata(true);
  
  private static final String EXPECTED_TYPES = " (expected: " + 
    StringUtil.simpleClassName(DatagramPacket.class) + ", " + 
    StringUtil.simpleClassName(AddressedEnvelope.class) + '<' + 
    StringUtil.simpleClassName(ByteBuf.class) + ", " + 
    StringUtil.simpleClassName(SocketAddress.class) + ">, " + 
    StringUtil.simpleClassName(ByteBuf.class) + ')';
  
  private final MulticastSocket socket;
  
  private final OioDatagramChannelConfig config;
  
  private final DatagramPacket tmpPacket = new DatagramPacket(EmptyArrays.EMPTY_BYTES, 0);
  
  private static MulticastSocket newSocket() {
    try {
      return new MulticastSocket(null);
    } catch (Exception e) {
      throw new ChannelException("failed to create a new socket", e);
    } 
  }
  
  public OioDatagramChannel() {
    this(newSocket());
  }
  
  public OioDatagramChannel(MulticastSocket socket) {
    super(null);
    boolean success = false;
    try {
      socket.setSoTimeout(1000);
      socket.setBroadcast(false);
      success = true;
    } catch (SocketException e) {
      throw new ChannelException("Failed to configure the datagram socket timeout.", e);
    } finally {
      if (!success)
        socket.close(); 
    } 
    this.socket = socket;
    this.config = new DefaultOioDatagramChannelConfig(this, socket);
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public DatagramChannelConfig config() {
    return this.config;
  }
  
  public boolean isOpen() {
    return !this.socket.isClosed();
  }
  
  public boolean isActive() {
    return (isOpen() && ((((Boolean)this.config
      .getOption(ChannelOption.DATAGRAM_CHANNEL_ACTIVE_ON_REGISTRATION)).booleanValue() && isRegistered()) || this.socket
      .isBound()));
  }
  
  public boolean isConnected() {
    return this.socket.isConnected();
  }
  
  protected SocketAddress localAddress0() {
    return this.socket.getLocalSocketAddress();
  }
  
  protected SocketAddress remoteAddress0() {
    return this.socket.getRemoteSocketAddress();
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    this.socket.bind(localAddress);
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (localAddress != null)
      this.socket.bind(localAddress); 
    boolean success = false;
    try {
      this.socket.connect(remoteAddress);
      success = true;
    } finally {
      if (!success)
        try {
          this.socket.close();
        } catch (Throwable t) {
          logger.warn("Failed to close a socket.", t);
        }  
    } 
  }
  
  protected void doDisconnect() throws Exception {
    this.socket.disconnect();
  }
  
  protected void doClose() throws Exception {
    this.socket.close();
  }
  
  protected int doReadMessages(List<Object> buf) throws Exception {
    DatagramChannelConfig config = config();
    RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    ByteBuf data = config.getAllocator().heapBuffer(allocHandle.guess());
    boolean free = true;
    try {
      this.tmpPacket.setAddress(null);
      this.tmpPacket.setData(data.array(), data.arrayOffset(), data.capacity());
      this.socket.receive(this.tmpPacket);
      InetSocketAddress remoteAddr = (InetSocketAddress)this.tmpPacket.getSocketAddress();
      allocHandle.lastBytesRead(this.tmpPacket.getLength());
      buf.add(new DatagramPacket(data.writerIndex(allocHandle.lastBytesRead()), localAddress(), remoteAddr));
      free = false;
      return 1;
    } catch (SocketTimeoutException e) {
      return 0;
    } catch (SocketException e) {
      if (!e.getMessage().toLowerCase(Locale.US).contains("socket closed"))
        throw e; 
      return -1;
    } catch (Throwable cause) {
      PlatformDependent.throwException(cause);
      return -1;
    } finally {
      if (free)
        data.release(); 
    } 
  }
  
  protected void doWrite(ChannelOutboundBuffer in) throws Exception {
    while (true) {
      SocketAddress remoteAddress;
      ByteBuf data;
      Object o = in.current();
      if (o == null)
        break; 
      if (o instanceof AddressedEnvelope) {
        AddressedEnvelope<ByteBuf, SocketAddress> envelope = (AddressedEnvelope<ByteBuf, SocketAddress>)o;
        remoteAddress = envelope.recipient();
        data = (ByteBuf)envelope.content();
      } else {
        data = (ByteBuf)o;
        remoteAddress = null;
      } 
      int length = data.readableBytes();
      try {
        if (remoteAddress != null) {
          this.tmpPacket.setSocketAddress(remoteAddress);
        } else {
          if (!isConnected())
            throw new NotYetConnectedException(); 
          this.tmpPacket.setAddress(null);
        } 
        if (data.hasArray()) {
          this.tmpPacket.setData(data.array(), data.arrayOffset() + data.readerIndex(), length);
        } else {
          byte[] tmp = new byte[length];
          data.getBytes(data.readerIndex(), tmp);
          this.tmpPacket.setData(tmp);
        } 
        this.socket.send(this.tmpPacket);
        in.remove();
      } catch (Exception e) {
        in.remove(e);
      } 
    } 
  }
  
  protected Object filterOutboundMessage(Object msg) {
    if (msg instanceof DatagramPacket || msg instanceof ByteBuf)
      return msg; 
    if (msg instanceof AddressedEnvelope) {
      AddressedEnvelope<Object, SocketAddress> e = (AddressedEnvelope<Object, SocketAddress>)msg;
      if (e.content() instanceof ByteBuf)
        return msg; 
    } 
    throw new UnsupportedOperationException("unsupported message type: " + 
        StringUtil.simpleClassName(msg) + EXPECTED_TYPES);
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress) {
    return joinGroup(multicastAddress, newPromise());
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress, ChannelPromise promise) {
    ensureBound();
    try {
      this.socket.joinGroup(multicastAddress);
      promise.setSuccess();
    } catch (IOException e) {
      promise.setFailure(e);
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    return joinGroup(multicastAddress, networkInterface, newPromise());
  }
  
  public ChannelFuture joinGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise) {
    ensureBound();
    try {
      this.socket.joinGroup(multicastAddress, networkInterface);
      promise.setSuccess();
    } catch (IOException e) {
      promise.setFailure(e);
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source) {
    return newFailedFuture(new UnsupportedOperationException());
  }
  
  public ChannelFuture joinGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise) {
    promise.setFailure(new UnsupportedOperationException());
    return (ChannelFuture)promise;
  }
  
  private void ensureBound() {
    if (!isActive())
      throw new IllegalStateException(DatagramChannel.class
          .getName() + " must be bound to join a group."); 
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress) {
    return leaveGroup(multicastAddress, newPromise());
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress, ChannelPromise promise) {
    try {
      this.socket.leaveGroup(multicastAddress);
      promise.setSuccess();
    } catch (IOException e) {
      promise.setFailure(e);
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface) {
    return leaveGroup(multicastAddress, networkInterface, newPromise());
  }
  
  public ChannelFuture leaveGroup(InetSocketAddress multicastAddress, NetworkInterface networkInterface, ChannelPromise promise) {
    try {
      this.socket.leaveGroup(multicastAddress, networkInterface);
      promise.setSuccess();
    } catch (IOException e) {
      promise.setFailure(e);
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source) {
    return newFailedFuture(new UnsupportedOperationException());
  }
  
  public ChannelFuture leaveGroup(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress source, ChannelPromise promise) {
    promise.setFailure(new UnsupportedOperationException());
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock) {
    return newFailedFuture(new UnsupportedOperationException());
  }
  
  public ChannelFuture block(InetAddress multicastAddress, NetworkInterface networkInterface, InetAddress sourceToBlock, ChannelPromise promise) {
    promise.setFailure(new UnsupportedOperationException());
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock) {
    return newFailedFuture(new UnsupportedOperationException());
  }
  
  public ChannelFuture block(InetAddress multicastAddress, InetAddress sourceToBlock, ChannelPromise promise) {
    promise.setFailure(new UnsupportedOperationException());
    return (ChannelFuture)promise;
  }
}
