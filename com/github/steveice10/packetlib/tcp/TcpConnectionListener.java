package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.netty.bootstrap.ServerBootstrap;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelInitializer;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.nio.NioEventLoopGroup;
import com.github.steveice10.netty.channel.socket.nio.NioServerSocketChannel;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.packetlib.ConnectionListener;
import com.github.steveice10.packetlib.Server;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import java.net.InetSocketAddress;

public class TcpConnectionListener implements ConnectionListener {
  private String host;
  
  private int port;
  
  private Server server;
  
  private EventLoopGroup group;
  
  private Channel channel;
  
  public TcpConnectionListener(String host, int port, Server server) {
    this.host = host;
    this.port = port;
    this.server = server;
  }
  
  public String getHost() {
    return this.host;
  }
  
  public int getPort() {
    return this.port;
  }
  
  public boolean isListening() {
    return (this.channel != null && this.channel.isOpen());
  }
  
  public void bind() {
    bind(true);
  }
  
  public void bind(boolean wait) {
    bind(wait, null);
  }
  
  public void bind(boolean wait, final Runnable callback) {
    if (this.group != null || this.channel != null)
      return; 
    this.group = (EventLoopGroup)new NioEventLoopGroup();
    ChannelFuture future = ((ServerBootstrap)((ServerBootstrap)(new ServerBootstrap()).channel(NioServerSocketChannel.class)).childHandler((ChannelHandler)new ChannelInitializer<Channel>() {
          public void initChannel(Channel channel) throws Exception {
            InetSocketAddress address = (InetSocketAddress)channel.remoteAddress();
            PacketProtocol protocol = TcpConnectionListener.this.server.createPacketProtocol();
            TcpSession session = new TcpServerSession(address.getHostName(), address.getPort(), protocol, TcpConnectionListener.this.server);
            session.getPacketProtocol().newServerSession(TcpConnectionListener.this.server, session);
            channel.config().setOption(ChannelOption.IP_TOS, Integer.valueOf(24));
            channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(false));
            ChannelPipeline pipeline = channel.pipeline();
            session.refreshReadTimeoutHandler(channel);
            session.refreshWriteTimeoutHandler(channel);
            pipeline.addLast("encryption", (ChannelHandler)new TcpPacketEncryptor(session));
            pipeline.addLast("sizer", (ChannelHandler)new TcpPacketSizer(session));
            pipeline.addLast("codec", (ChannelHandler)new TcpPacketCodec(session));
            pipeline.addLast("manager", (ChannelHandler)session);
          }
        }).group(this.group).localAddress(this.host, this.port)).bind();
    if (wait) {
      try {
        future.sync();
      } catch (InterruptedException interruptedException) {}
      this.channel = future.channel();
      if (callback != null)
        callback.run(); 
    } else {
      future.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
              if (future.isSuccess()) {
                TcpConnectionListener.this.channel = future.channel();
                if (callback != null)
                  callback.run(); 
              } else {
                System.err.println("[ERROR] Failed to asynchronously bind connection listener.");
                if (future.cause() != null)
                  future.cause().printStackTrace(); 
              } 
            }
          });
    } 
  }
  
  public void close() {
    close(false);
  }
  
  public void close(boolean wait) {
    close(wait, null);
  }
  
  public void close(boolean wait, final Runnable callback) {
    if (this.channel != null) {
      if (this.channel.isOpen()) {
        ChannelFuture future = this.channel.close();
        if (wait) {
          try {
            future.sync();
          } catch (InterruptedException interruptedException) {}
          if (callback != null)
            callback.run(); 
        } else {
          future.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  if (future.isSuccess()) {
                    if (callback != null)
                      callback.run(); 
                  } else {
                    System.err.println("[ERROR] Failed to asynchronously close connection listener.");
                    if (future.cause() != null)
                      future.cause().printStackTrace(); 
                  } 
                }
              });
        } 
      } 
      this.channel = null;
    } 
    if (this.group != null) {
      Future<?> future = this.group.shutdownGracefully();
      if (wait) {
        try {
          future.sync();
        } catch (InterruptedException interruptedException) {}
      } else {
        future.addListener(new GenericFutureListener() {
              public void operationComplete(Future future) throws Exception {
                if (!future.isSuccess()) {
                  System.err.println("[ERROR] Failed to asynchronously close connection listener.");
                  if (future.cause() != null)
                    future.cause().printStackTrace(); 
                } 
              }
            });
      } 
      this.group = null;
    } 
  }
}
