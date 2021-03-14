package com.github.steveice10.netty.handler.proxy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.base64.Base64;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpClientCodec;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpMethod;
import com.github.steveice10.netty.handler.codec.http.HttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpUtil;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.CharsetUtil;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class HttpProxyHandler extends ProxyHandler {
  private static final String PROTOCOL = "http";
  
  private static final String AUTH_BASIC = "basic";
  
  private final HttpClientCodec codec = new HttpClientCodec();
  
  private final String username;
  
  private final String password;
  
  private final CharSequence authorization;
  
  private final boolean ignoreDefaultPortsInConnectHostHeader;
  
  private HttpResponseStatus status;
  
  private HttpHeaders headers;
  
  public HttpProxyHandler(SocketAddress proxyAddress) {
    this(proxyAddress, (HttpHeaders)null);
  }
  
  public HttpProxyHandler(SocketAddress proxyAddress, HttpHeaders headers) {
    this(proxyAddress, headers, false);
  }
  
  public HttpProxyHandler(SocketAddress proxyAddress, HttpHeaders headers, boolean ignoreDefaultPortsInConnectHostHeader) {
    super(proxyAddress);
    this.username = null;
    this.password = null;
    this.authorization = null;
    this.headers = headers;
    this.ignoreDefaultPortsInConnectHostHeader = ignoreDefaultPortsInConnectHostHeader;
  }
  
  public HttpProxyHandler(SocketAddress proxyAddress, String username, String password) {
    this(proxyAddress, username, password, (HttpHeaders)null);
  }
  
  public HttpProxyHandler(SocketAddress proxyAddress, String username, String password, HttpHeaders headers) {
    this(proxyAddress, username, password, headers, false);
  }
  
  public HttpProxyHandler(SocketAddress proxyAddress, String username, String password, HttpHeaders headers, boolean ignoreDefaultPortsInConnectHostHeader) {
    super(proxyAddress);
    if (username == null)
      throw new NullPointerException("username"); 
    if (password == null)
      throw new NullPointerException("password"); 
    this.username = username;
    this.password = password;
    ByteBuf authz = Unpooled.copiedBuffer(username + ':' + password, CharsetUtil.UTF_8);
    ByteBuf authzBase64 = Base64.encode(authz, false);
    this.authorization = (CharSequence)new AsciiString("Basic " + authzBase64.toString(CharsetUtil.US_ASCII));
    authz.release();
    authzBase64.release();
    this.headers = headers;
    this.ignoreDefaultPortsInConnectHostHeader = ignoreDefaultPortsInConnectHostHeader;
  }
  
  public String protocol() {
    return "http";
  }
  
  public String authScheme() {
    return (this.authorization != null) ? "basic" : "none";
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
    p.addBefore(name, null, (ChannelHandler)this.codec);
  }
  
  protected void removeEncoder(ChannelHandlerContext ctx) throws Exception {
    this.codec.removeOutboundHandler();
  }
  
  protected void removeDecoder(ChannelHandlerContext ctx) throws Exception {
    this.codec.removeInboundHandler();
  }
  
  protected Object newInitialMessage(ChannelHandlerContext ctx) throws Exception {
    InetSocketAddress raddr = destinationAddress();
    String hostString = HttpUtil.formatHostnameForHttp(raddr);
    int port = raddr.getPort();
    String url = hostString + ":" + port;
    String hostHeader = (this.ignoreDefaultPortsInConnectHostHeader && (port == 80 || port == 443)) ? hostString : url;
    DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url, Unpooled.EMPTY_BUFFER, false);
    defaultFullHttpRequest.headers().set((CharSequence)HttpHeaderNames.HOST, hostHeader);
    if (this.authorization != null)
      defaultFullHttpRequest.headers().set((CharSequence)HttpHeaderNames.PROXY_AUTHORIZATION, this.authorization); 
    if (this.headers != null)
      defaultFullHttpRequest.headers().add(this.headers); 
    return defaultFullHttpRequest;
  }
  
  protected boolean handleResponse(ChannelHandlerContext ctx, Object response) throws Exception {
    if (response instanceof HttpResponse) {
      if (this.status != null)
        throw new ProxyConnectException(exceptionMessage("too many responses")); 
      this.status = ((HttpResponse)response).status();
    } 
    boolean finished = response instanceof com.github.steveice10.netty.handler.codec.http.LastHttpContent;
    if (finished) {
      if (this.status == null)
        throw new ProxyConnectException(exceptionMessage("missing response")); 
      if (this.status.code() != 200)
        throw new ProxyConnectException(exceptionMessage("status: " + this.status)); 
    } 
    return finished;
  }
}
