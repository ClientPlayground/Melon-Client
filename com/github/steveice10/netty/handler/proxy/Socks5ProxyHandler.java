package com.github.steveice10.netty.handler.proxy;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.socksx.v5.DefaultSocks5CommandRequest;
import com.github.steveice10.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import com.github.steveice10.netty.handler.codec.socksx.v5.DefaultSocks5PasswordAuthRequest;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5AddressType;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5ClientEncoder;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5CommandResponse;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5CommandResponseDecoder;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5CommandType;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5InitialRequest;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5InitialResponse;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5InitialResponseDecoder;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponse;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5PasswordAuthResponseDecoder;
import com.github.steveice10.netty.handler.codec.socksx.v5.Socks5PasswordAuthStatus;
import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.Collections;

public final class Socks5ProxyHandler extends ProxyHandler {
  private static final String PROTOCOL = "socks5";
  
  private static final String AUTH_PASSWORD = "password";
  
  private static final Socks5InitialRequest INIT_REQUEST_NO_AUTH = (Socks5InitialRequest)new DefaultSocks5InitialRequest(
      Collections.singletonList(Socks5AuthMethod.NO_AUTH));
  
  private static final Socks5InitialRequest INIT_REQUEST_PASSWORD = (Socks5InitialRequest)new DefaultSocks5InitialRequest(
      Arrays.asList(new Socks5AuthMethod[] { Socks5AuthMethod.NO_AUTH, Socks5AuthMethod.PASSWORD }));
  
  private final String username;
  
  private final String password;
  
  private String decoderName;
  
  private String encoderName;
  
  public Socks5ProxyHandler(SocketAddress proxyAddress) {
    this(proxyAddress, (String)null, (String)null);
  }
  
  public Socks5ProxyHandler(SocketAddress proxyAddress, String username, String password) {
    super(proxyAddress);
    if (username != null && username.isEmpty())
      username = null; 
    if (password != null && password.isEmpty())
      password = null; 
    this.username = username;
    this.password = password;
  }
  
  public String protocol() {
    return "socks5";
  }
  
  public String authScheme() {
    return (socksAuthMethod() == Socks5AuthMethod.PASSWORD) ? "password" : "none";
  }
  
  public String username() {
    return this.username;
  }
  
  public String password() {
    return this.password;
  }
  
  protected void addCodec(ChannelHandlerContext ctx) throws Exception {
    ChannelPipeline p = ctx.pipeline();
    String name = ctx.name();
    Socks5InitialResponseDecoder decoder = new Socks5InitialResponseDecoder();
    p.addBefore(name, null, (ChannelHandler)decoder);
    this.decoderName = p.context((ChannelHandler)decoder).name();
    this.encoderName = this.decoderName + ".encoder";
    p.addBefore(name, this.encoderName, (ChannelHandler)Socks5ClientEncoder.DEFAULT);
  }
  
  protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
    ctx.pipeline().remove(this.encoderName);
  }
  
  protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
    ChannelPipeline p = ctx.pipeline();
    if (p.context(this.decoderName) != null)
      p.remove(this.decoderName); 
  }
  
  protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
    return (socksAuthMethod() == Socks5AuthMethod.PASSWORD) ? INIT_REQUEST_PASSWORD : INIT_REQUEST_NO_AUTH;
  }
  
  protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
    if (response instanceof Socks5InitialResponse) {
      Socks5InitialResponse socks5InitialResponse = (Socks5InitialResponse)response;
      Socks5AuthMethod authMethod = socksAuthMethod();
      if (socks5InitialResponse.authMethod() != Socks5AuthMethod.NO_AUTH && socks5InitialResponse.authMethod() != authMethod)
        throw new ProxyConnectException(exceptionMessage("unexpected authMethod: " + socks5InitialResponse.authMethod())); 
      if (authMethod == Socks5AuthMethod.NO_AUTH) {
        sendConnectCommand(ctx);
      } else if (authMethod == Socks5AuthMethod.PASSWORD) {
        ctx.pipeline().replace(this.decoderName, this.decoderName, (ChannelHandler)new Socks5PasswordAuthResponseDecoder());
        sendToProxyServer(new DefaultSocks5PasswordAuthRequest((this.username != null) ? this.username : "", (this.password != null) ? this.password : ""));
      } else {
        throw new Error();
      } 
      return false;
    } 
    if (response instanceof Socks5PasswordAuthResponse) {
      Socks5PasswordAuthResponse socks5PasswordAuthResponse = (Socks5PasswordAuthResponse)response;
      if (socks5PasswordAuthResponse.status() != Socks5PasswordAuthStatus.SUCCESS)
        throw new ProxyConnectException(exceptionMessage("authStatus: " + socks5PasswordAuthResponse.status())); 
      sendConnectCommand(ctx);
      return false;
    } 
    Socks5CommandResponse res = (Socks5CommandResponse)response;
    if (res.status() != Socks5CommandStatus.SUCCESS)
      throw new ProxyConnectException(exceptionMessage("status: " + res.status())); 
    return true;
  }
  
  private Socks5AuthMethod socksAuthMethod() {
    Socks5AuthMethod authMethod;
    if (this.username == null && this.password == null) {
      authMethod = Socks5AuthMethod.NO_AUTH;
    } else {
      authMethod = Socks5AuthMethod.PASSWORD;
    } 
    return authMethod;
  }
  
  private void sendConnectCommand(ChannelHandlerContext ctx) throws Exception {
    Socks5AddressType addrType;
    String rhost;
    InetSocketAddress raddr = destinationAddress();
    if (raddr.isUnresolved()) {
      addrType = Socks5AddressType.DOMAIN;
      rhost = raddr.getHostString();
    } else {
      rhost = raddr.getAddress().getHostAddress();
      if (NetUtil.isValidIpV4Address(rhost)) {
        addrType = Socks5AddressType.IPv4;
      } else if (NetUtil.isValidIpV6Address(rhost)) {
        addrType = Socks5AddressType.IPv6;
      } else {
        throw new ProxyConnectException(
            exceptionMessage("unknown address type: " + StringUtil.simpleClassName(rhost)));
      } 
    } 
    ctx.pipeline().replace(this.decoderName, this.decoderName, (ChannelHandler)new Socks5CommandResponseDecoder());
    sendToProxyServer(new DefaultSocks5CommandRequest(Socks5CommandType.CONNECT, addrType, rhost, raddr.getPort()));
  }
}
