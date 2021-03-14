package com.github.steveice10.netty.handler.proxy;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.socksx.v4.DefaultSocks4CommandRequest;
import com.github.steveice10.netty.handler.codec.socksx.v4.Socks4ClientDecoder;
import com.github.steveice10.netty.handler.codec.socksx.v4.Socks4ClientEncoder;
import com.github.steveice10.netty.handler.codec.socksx.v4.Socks4CommandResponse;
import com.github.steveice10.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import com.github.steveice10.netty.handler.codec.socksx.v4.Socks4CommandType;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class Socks4ProxyHandler extends ProxyHandler {
  private static final String PROTOCOL = "socks4";
  
  private static final String AUTH_USERNAME = "username";
  
  private final String username;
  
  private String decoderName;
  
  private String encoderName;
  
  public Socks4ProxyHandler(SocketAddress proxyAddress) {
    this(proxyAddress, (String)null);
  }
  
  public Socks4ProxyHandler(SocketAddress proxyAddress, String username) {
    super(proxyAddress);
    if (username != null && username.isEmpty())
      username = null; 
    this.username = username;
  }
  
  public String protocol() {
    return "socks4";
  }
  
  public String authScheme() {
    return (this.username != null) ? "username" : "none";
  }
  
  public String username() {
    return this.username;
  }
  
  protected void addCodec(ChannelHandlerContext ctx) throws Exception {
    ChannelPipeline p = ctx.pipeline();
    String name = ctx.name();
    Socks4ClientDecoder decoder = new Socks4ClientDecoder();
    p.addBefore(name, null, (ChannelHandler)decoder);
    this.decoderName = p.context((ChannelHandler)decoder).name();
    this.encoderName = this.decoderName + ".encoder";
    p.addBefore(name, this.encoderName, (ChannelHandler)Socks4ClientEncoder.INSTANCE);
  }
  
  protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
    ChannelPipeline p = ctx.pipeline();
    p.remove(this.encoderName);
  }
  
  protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
    ChannelPipeline p = ctx.pipeline();
    p.remove(this.decoderName);
  }
  
  protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
    String rhost;
    InetSocketAddress raddr = destinationAddress();
    if (raddr.isUnresolved()) {
      rhost = raddr.getHostString();
    } else {
      rhost = raddr.getAddress().getHostAddress();
    } 
    return new DefaultSocks4CommandRequest(Socks4CommandType.CONNECT, rhost, raddr
        .getPort(), (this.username != null) ? this.username : "");
  }
  
  protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
    Socks4CommandResponse res = (Socks4CommandResponse)response;
    Socks4CommandStatus status = res.status();
    if (status == Socks4CommandStatus.SUCCESS)
      return true; 
    throw new ProxyConnectException(exceptionMessage("status: " + status));
  }
}
