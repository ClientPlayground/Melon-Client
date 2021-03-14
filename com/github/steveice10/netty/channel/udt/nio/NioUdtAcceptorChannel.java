package com.github.steveice10.netty.channel.udt.nio;

import com.barchart.udt.TypeUDT;
import com.barchart.udt.nio.ChannelUDT;
import com.barchart.udt.nio.ServerSocketChannelUDT;
import com.barchart.udt.nio.SocketChannelUDT;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelException;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.nio.AbstractNioMessageChannel;
import com.github.steveice10.netty.channel.udt.DefaultUdtServerChannelConfig;
import com.github.steveice10.netty.channel.udt.UdtChannel;
import com.github.steveice10.netty.channel.udt.UdtChannelConfig;
import com.github.steveice10.netty.channel.udt.UdtServerChannel;
import com.github.steveice10.netty.channel.udt.UdtServerChannelConfig;
import com.github.steveice10.netty.util.internal.SocketUtils;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.List;

@Deprecated
public abstract class NioUdtAcceptorChannel extends AbstractNioMessageChannel implements UdtServerChannel {
  protected static final InternalLogger logger = InternalLoggerFactory.getInstance(NioUdtAcceptorChannel.class);
  
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  private final UdtServerChannelConfig config;
  
  protected NioUdtAcceptorChannel(ServerSocketChannelUDT channelUDT) {
    super(null, (SelectableChannel)channelUDT, 16);
    try {
      channelUDT.configureBlocking(false);
      this.config = (UdtServerChannelConfig)new DefaultUdtServerChannelConfig((UdtChannel)this, (ChannelUDT)channelUDT, true);
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
  
  protected NioUdtAcceptorChannel(TypeUDT type) {
    this(NioUdtProvider.newAcceptorChannelUDT(type));
  }
  
  public UdtServerChannelConfig config() {
    return this.config;
  }
  
  protected void doBind(SocketAddress localAddress) throws Exception {
    javaChannel().socket().bind(localAddress, this.config.getBacklog());
  }
  
  protected void doClose() throws Exception {
    javaChannel().close();
  }
  
  protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected void doDisconnect() throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected void doFinishConnect() throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected boolean doWriteMessage(Object msg, ChannelOutboundBuffer in) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  protected final Object filterOutboundMessage(Object msg) throws Exception {
    throw new UnsupportedOperationException();
  }
  
  public boolean isActive() {
    return javaChannel().socket().isBound();
  }
  
  protected ServerSocketChannelUDT javaChannel() {
    return (ServerSocketChannelUDT)super.javaChannel();
  }
  
  protected SocketAddress localAddress0() {
    return SocketUtils.localSocketAddress((ServerSocket)javaChannel().socket());
  }
  
  public InetSocketAddress localAddress() {
    return (InetSocketAddress)super.localAddress();
  }
  
  public InetSocketAddress remoteAddress() {
    return null;
  }
  
  protected SocketAddress remoteAddress0() {
    return null;
  }
  
  public ChannelMetadata metadata() {
    return METADATA;
  }
  
  protected int doReadMessages(List<Object> buf) throws Exception {
    SocketChannelUDT channelUDT = (SocketChannelUDT)SocketUtils.accept((ServerSocketChannel)javaChannel());
    if (channelUDT == null)
      return 0; 
    buf.add(newConnectorChannel(channelUDT));
    return 1;
  }
  
  protected abstract UdtChannel newConnectorChannel(SocketChannelUDT paramSocketChannelUDT);
}
