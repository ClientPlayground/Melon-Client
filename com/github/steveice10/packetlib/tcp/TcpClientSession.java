package com.github.steveice10.packetlib.tcp;

import com.github.steveice10.netty.bootstrap.Bootstrap;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelInitializer;
import com.github.steveice10.netty.channel.ChannelOption;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.EventLoopGroup;
import com.github.steveice10.netty.channel.nio.NioEventLoopGroup;
import com.github.steveice10.netty.channel.oio.OioEventLoopGroup;
import com.github.steveice10.netty.channel.socket.nio.NioSocketChannel;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.packet.PacketProtocol;
import java.net.Proxy;
import java.util.Hashtable;
import javax.naming.directory.InitialDirContext;

public class TcpClientSession extends TcpSession {
  private Client client;
  
  private Proxy proxy;
  
  private EventLoopGroup group;
  
  public TcpClientSession(String host, int port, PacketProtocol protocol, Client client, Proxy proxy) {
    super(host, port, protocol);
    this.client = client;
    this.proxy = proxy;
  }
  
  public void connect(boolean wait) {
    if (this.disconnected)
      throw new IllegalStateException("Session has already been disconnected."); 
    if (this.group != null)
      return; 
    try {
      final Bootstrap bootstrap = new Bootstrap();
      if (this.proxy != null) {
        this.group = (EventLoopGroup)new OioEventLoopGroup();
        bootstrap.channelFactory(new ProxyOioChannelFactory(this.proxy));
      } else {
        this.group = (EventLoopGroup)new NioEventLoopGroup();
        bootstrap.channel(NioSocketChannel.class);
      } 
      ((Bootstrap)((Bootstrap)bootstrap.handler((ChannelHandler)new ChannelInitializer<Channel>() {
            public void initChannel(Channel channel) throws Exception {
              TcpClientSession.this.getPacketProtocol().newClientSession(TcpClientSession.this.client, TcpClientSession.this);
              channel.config().setOption(ChannelOption.IP_TOS, Integer.valueOf(24));
              channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(false));
              ChannelPipeline pipeline = channel.pipeline();
              TcpClientSession.this.refreshReadTimeoutHandler(channel);
              TcpClientSession.this.refreshWriteTimeoutHandler(channel);
              pipeline.addLast("encryption", (ChannelHandler)new TcpPacketEncryptor(TcpClientSession.this));
              pipeline.addLast("sizer", (ChannelHandler)new TcpPacketSizer(TcpClientSession.this));
              pipeline.addLast("codec", (ChannelHandler)new TcpPacketCodec(TcpClientSession.this));
              pipeline.addLast("manager", (ChannelHandler)TcpClientSession.this);
            }
          })).group(this.group)).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.valueOf(getConnectTimeout() * 1000));
      Runnable connectTask = new Runnable() {
          public void run() {
            try {
              String host = TcpClientSession.this.getHost();
              int port = TcpClientSession.this.getPort();
              try {
                Hashtable<String, String> environment = new Hashtable<>();
                environment.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
                environment.put("java.naming.provider.url", "dns:");
                String[] result = (new InitialDirContext(environment)).getAttributes(TcpClientSession.this.getPacketProtocol().getSRVRecordPrefix() + "._tcp." + host, new String[] { "SRV" }).get("srv").get().toString().split(" ", 4);
                host = result[3];
                port = Integer.parseInt(result[2]);
              } catch (Throwable throwable) {}
              bootstrap.remoteAddress(host, port);
              ChannelFuture future = bootstrap.connect().sync();
              if (future.isSuccess())
                while (!TcpClientSession.this.isConnected() && !TcpClientSession.this.disconnected) {
                  try {
                    Thread.sleep(5L);
                  } catch (InterruptedException interruptedException) {}
                }  
            } catch (Throwable t) {
              TcpClientSession.this.exceptionCaught(null, t);
            } 
          }
        };
      if (wait) {
        connectTask.run();
      } else {
        (new Thread(connectTask)).start();
      } 
    } catch (Throwable t) {
      exceptionCaught(null, t);
    } 
  }
  
  public void disconnect(String reason, Throwable cause, boolean wait) {
    super.disconnect(reason, cause, wait);
    if (this.group != null) {
      Future<?> future = this.group.shutdownGracefully();
      if (wait)
        try {
          future.await();
        } catch (InterruptedException interruptedException) {} 
      this.group = null;
    } 
  }
}
