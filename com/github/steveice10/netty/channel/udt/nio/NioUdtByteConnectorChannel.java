package com.github.steveice10.netty.channel.udt.nio;

import com.barchart.udt.StatusUDT;
import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ChannelUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.nio.AbstractNioByteChannel;
import com.github.steveice10.netty.channel.udt.DefaultUdtChannelConfig;
import com.github.steveice10.netty.channel.udt.UdtChannel;
import com.github.steveice10.netty.channel.udt.UdtChannelConfig;
import com.github.steveice10.netty.util.internal.SocketUtils;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

@Deprecated
public class NioUdtByteConnectorChannel extends AbstractNioByteChannel implements UdtChannel {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(NioUdtByteConnectorChannel.class);
  
  private final UdtChannelConfig config;
  
  public NioUdtByteConnectorChannel() {
    this(TypeUDT.STREAM);
  }
  
  public NioUdtByteConnectorChannel(Channel parent, SocketChannelUDT channelUDT) {
    super(parent, (SelectableChannel)channelUDT);
    try {
      channelUDT.configureBlocking(false);
      switch (channelUDT.socketUDT().status()) {
        case INIT:
        case OPENED:
          this.config = (UdtChannelConfig)new DefaultUdtChannelConfig(this, (ChannelUDT)channelUDT, true);
          return;
      } 
      this.config = (UdtChannelConfig)new DefaultUdtChannelConfig(this, (ChannelUDT)channelUDT, false);
    } catch (Exception e) {
      try {
        channelUDT.close();
      } catch (Exception e2) {
        if (logger.isWarnEnabled())
          logger.warn("Failed to close channel.", e2); 
      } 
      throw new ChannelException("Failed to configure channel.", e);
    } 
  }
  
  public NioUdtByteConnectorChannel(SocketChannelUDT channelUDT) {
    this((Channel)null, channelUDT);
  }
  
  public NioUdtByteConnectorChannel(TypeUDT type) {
    this(NioUdtProvider.newConnectorChannelUDT(type));
  }
  
  public UdtChannelConfig config() {
    return this.config;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    privilegedBind(javaChannel(), localAddress);
  }
  
  protected void doClose() throws Exception {
    javaChannel().close();
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    doBind((localAddress != null) ? localAddress : new InetSocketAddress(0));
    boolean success = false;
    try {
      boolean connected = SocketUtils.connect((SocketChannel)javaChannel(), remoteAddress);
      if (!connected)
        selectionKey().interestOps(
            selectionKey().interestOps() | 0x8); 
      success = true;
      return connected;
    } finally {
      if (!success)
        doClose(); 
    } 
  }
  
  protected void doDisconnect() throws Exception {
    doClose();
  }
  
  protected void doFinishConnect() throws Exception {
    if (javaChannel().finishConnect()) {
      selectionKey().interestOps(
          selectionKey().interestOps() & 0xFFFFFFF7);
    } else {
      throw new Error("Provider error: failed to finish connect. Provider library should be upgraded.");
    } 
  }
  
  protected int doReadBytes(ByteBuf byteBuf) throws Exception {
    RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
    allocHandle.attemptedBytesRead(byteBuf.writableBytes());
    return byteBuf.writeBytes((ScatteringByteChannel)javaChannel(), allocHandle.attemptedBytesRead());
  }
  
  protected int doWriteBytes(ByteBuf byteBuf) throws Exception {
    int expectedWrittenBytes = byteBuf.readableBytes();
    return byteBuf.readBytes((GatheringByteChannel)javaChannel(), expectedWrittenBytes);
  }
  
  protected ChannelFuture shutdownInput() {
    return newFailedFuture(new UnsupportedOperationException("shutdownInput"));
  }
  
  protected long doWriteFileRegion(FileRegion region) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  public boolean isActive() {
    SocketChannelUDT channelUDT = javaChannel();
    return (channelUDT.isOpen() && channelUDT.isConnectFinished());
  }
  
  protected SocketChannelUDT javaChannel() {
    return (SocketChannelUDT)super.javaChannel();
  }
  
  protected SocketAddress localAddress0() {
    return javaChannel().socket().getLocalSocketAddress();
  }
  
  protected SocketAddress remoteAddress0() {
    return javaChannel().socket().getRemoteSocketAddress();
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public InetSocketAddress remoteAddress() {
    return (InetSocketAddress)super.remoteAddress();
  }
  
  private static void privilegedBind(final SocketChannelUDT socketChannel, final SocketAddress localAddress) throws IOException {
    try {
      AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
            public Void run() throws IOException {
              socketChannel.bind(localAddress);
              return null;
            }
          });
    } catch (PrivilegedActionException e) {
      throw (IOException)e.getCause();
    } 
  }
}
