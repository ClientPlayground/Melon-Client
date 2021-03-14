package com.github.steveice10.netty.channel.sctp.nio;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.nio.AbstractNioMessageChannel;
import com.github.steveice10.netty.channel.sctp.DefaultSctpChannelConfig;
import com.github.steveice10.netty.channel.sctp.SctpChannel;
import com.github.steveice10.netty.channel.sctp.SctpChannelConfig;
import com.github.steveice10.netty.channel.sctp.SctpMessage;
import com.github.steveice10.netty.channel.sctp.SctpNotificationHandler;
import com.github.steveice10.netty.channel.sctp.SctpServerChannel;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import com.sun.nio.sctp.Association;
import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.NotificationHandler;
import com.sun.nio.sctp.SctpChannel;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NioSctpChannel extends AbstractNioMessageChannel implements SctpChannel {
  private static final ChannelMetadata METADATA = new ChannelMetadata(false);
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioSctpChannel.class);
  
  private final SctpChannelConfig config;
  
  private final NotificationHandler<?> notificationHandler;
  
  private static SctpChannel newSctpChannel() {
    try {
      return SctpChannel.open();
    } catch (IOException e) {
      throw new ChannelException("Failed to open a sctp channel.", e);
    } 
  }
  
  public NioSctpChannel() {
    this(newSctpChannel());
  }
  
  public NioSctpChannel(SctpChannel sctpChannel) {
    this((Channel)null, sctpChannel);
  }
  
  public NioSctpChannel(Channel parent, SctpChannel sctpChannel) {
    super(parent, sctpChannel, 1);
    try {
      sctpChannel.configureBlocking(false);
      this.config = (SctpChannelConfig)new NioSctpChannelConfig(this, sctpChannel);
      this.notificationHandler = (NotificationHandler<?>)new SctpNotificationHandler(this);
    } catch (IOException e) {
      try {
        sctpChannel.close();
      } catch (IOException e2) {
        if (logger.isWarnEnabled())
          logger.warn("Failed to close a partially initialized sctp channel.", e2); 
      } 
      throw new ChannelException("Failed to enter non-blocking mode.", e);
    } 
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  public SctpServerChannel parent() {
    return (SctpServerChannel)super.parent();
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  public Association association() {
    try {
      return javaChannel().association();
    } catch (IOException ignored) {
      return null;
    } 
  }
  
  public Set<InetSocketAddress> allLocalAddresses() {
    try {
      Set<SocketAddress> allLocalAddresses = javaChannel().getAllLocalAddresses();
      Set<InetSocketAddress> addresses = new LinkedHashSet<InetSocketAddress>(allLocalAddresses.size());
      for (SocketAddress socketAddress : allLocalAddresses)
        addresses.add((InetSocketAddress)socketAddress); 
      return addresses;
    } catch (Throwable ignored) {
      return Collections.emptySet();
    } 
  }
  
  public SctpChannelConfig config() {
    return this.config;
  }
  
  public Set<InetSocketAddress> allRemoteAddresses() {
    try {
      Set<SocketAddress> allLocalAddresses = javaChannel().getRemoteAddresses();
      Set<InetSocketAddress> addresses = new HashSet<InetSocketAddress>(allLocalAddresses.size());
      for (SocketAddress socketAddress : allLocalAddresses)
        addresses.add((InetSocketAddress)socketAddress); 
      return addresses;
    } catch (Throwable ignored) {
      return Collections.emptySet();
    } 
  }
  
  protected SctpChannel javaChannel() {
    return (SctpChannel)super.javaChannel();
  }
  
  public boolean isActive() {
    SctpChannel ch = javaChannel();
    return (ch.isOpen() && association() != null);
  }
  
  protected SocketAddress localAddress0() {
    try {
      Iterator<SocketAddress> i = javaChannel().getAllLocalAddresses().iterator();
      if (i.hasNext())
        return i.next(); 
    } catch (IOException iOException) {}
    return null;
  }
  
  protected SocketAddress remoteAddress0() {
    try {
      Iterator<SocketAddress> i = javaChannel().getRemoteAddresses().iterator();
      if (i.hasNext())
        return i.next(); 
    } catch (IOException iOException) {}
    return null;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    javaChannel().bind(localAddress);
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    if (localAddress != null)
      javaChannel().bind(localAddress); 
    boolean success = false;
    try {
      boolean connected = javaChannel().connect(remoteAddress);
      if (!connected)
        selectionKey().interestOps(8); 
      success = true;
      return connected;
    } finally {
      if (!success)
        doClose(); 
    } 
  }
  
  protected void doFinishConnect() throws Exception {
    if (!javaChannel().finishConnect())
      throw new Error(); 
  }
  
  protected void doDisconnect() throws Exception {
    doClose();
  }
  
  protected void doClose() throws Exception {
    javaChannel().close();
  }
  
  protected int doReadMessages(List<Object> buf) throws Exception {
    SctpChannel ch = javaChannel();
    RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    ByteBuf buffer = allocHandle.allocate(config().getAllocator());
    boolean free = true;
    try {
      ByteBuffer data = buffer.internalNioBuffer(buffer.writerIndex(), buffer.writableBytes());
      int pos = data.position();
      MessageInfo messageInfo = ch.receive(data, (Object)null, this.notificationHandler);
      if (messageInfo == null)
        return 0; 
      allocHandle.lastBytesRead(data.position() - pos);
      buf.add(new SctpMessage(messageInfo, buffer
            .writerIndex(buffer.writerIndex() + allocHandle.lastBytesRead())));
      free = false;
      return 1;
    } catch (Throwable cause) {
      PlatformDependent.throwException(cause);
      return -1;
    } finally {
      if (free)
        buffer.release(); 
    } 
  }
  
  protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception {
    SctpMessage packet = (SctpMessage)msg;
    ByteBuf data = packet.content();
    int dataLen = data.readableBytes();
    if (dataLen == 0)
      return true; 
    ByteBufAllocator alloc = alloc();
    boolean needsCopy = (data.nioBufferCount() != 1);
    if (!needsCopy && 
      !data.isDirect() && alloc.isDirectBufferPooled())
      needsCopy = true; 
    if (needsCopy)
      data = alloc.directBuffer(dataLen).writeBytes(data); 
    ByteBuffer nioData = data.nioBuffer();
    MessageInfo mi = MessageInfo.createOutgoing(association(), null, packet.streamIdentifier());
    mi.payloadProtocolID(packet.protocolIdentifier());
    mi.streamNumber(packet.streamIdentifier());
    mi.unordered(packet.isUnordered());
    int writtenBytes = javaChannel().send(nioData, mi);
    return (writtenBytes > 0);
  }
  
  protected final Object filterOutboundMessage(Object msg) throws Exception {
    if (msg instanceof SctpMessage) {
      SctpMessage m = (SctpMessage)msg;
      ByteBuf buf = m.content();
      if (buf.isDirect() && buf.nioBufferCount() == 1)
        return m; 
      return new SctpMessage(m.protocolIdentifier(), m.streamIdentifier(), m.isUnordered(), 
          newDirectBuffer((ReferenceCounted)m, buf));
    } 
    throw new UnsupportedOperationException("unsupported message type: " + 
        StringUtil.simpleClassName(msg) + " (expected: " + 
        StringUtil.simpleClassName(SctpMessage.class));
  }
  
  public ChannelFuture bindAddress(InetAddress localAddress) {
    return bindAddress(localAddress, newPromise());
  }
  
  public ChannelFuture bindAddress(final InetAddress localAddress, final ChannelPromise promise) {
    if (eventLoop().inEventLoop()) {
      try {
        javaChannel().bindAddress(localAddress);
        promise.setSuccess();
      } catch (Throwable t) {
        promise.setFailure(t);
      } 
    } else {
      eventLoop().execute(new Runnable() {
            public void run() {
              NioSctpChannel.this.bindAddress(localAddress, promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture unbindAddress(InetAddress localAddress) {
    return unbindAddress(localAddress, newPromise());
  }
  
  public ChannelFuture unbindAddress(final InetAddress localAddress, final ChannelPromise promise) {
    if (eventLoop().inEventLoop()) {
      try {
        javaChannel().unbindAddress(localAddress);
        promise.setSuccess();
      } catch (Throwable t) {
        promise.setFailure(t);
      } 
    } else {
      eventLoop().execute(new Runnable() {
            public void run() {
              NioSctpChannel.this.unbindAddress(localAddress, promise);
            }
          });
    } 
    return (ChannelFuture)promise;
  }
  
  private final class NioSctpChannelConfig extends DefaultSctpChannelConfig {
    private NioSctpChannelConfig(NioSctpChannel channel, SctpChannel javaChannel) {
      super(channel, javaChannel);
    }
    
    protected void autoReadCleared() {
      NioSctpChannel.this.clearReadPending();
    }
  }
}
