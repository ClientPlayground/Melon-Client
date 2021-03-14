package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.SimpleChannelInboundHandler;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpClientCodec;
import com.github.steveice10.netty.handler.codec.http.HttpContentDecompressor;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpObjectAggregator;
import com.github.steveice10.netty.handler.codec.http.HttpRequestEncoder;
import com.github.steveice10.netty.handler.codec.http.HttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpResponseDecoder;
import com.github.steveice10.netty.handler.codec.http.HttpScheme;
import com.github.steveice10.netty.util.NetUtil;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.net.URI;
import java.nio.channels.ClosedChannelException;
import java.util.Locale;

public abstract class WebSocketClientHandshaker {
  private static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), WebSocketClientHandshaker.class, "processHandshake(...)");
  
  private static final String HTTP_SCHEME_PREFIX = HttpScheme.HTTP + "://";
  
  private static final String HTTPS_SCHEME_PREFIX = HttpScheme.HTTPS + "://";
  
  private final URI uri;
  
  private final WebSocketVersion version;
  
  private volatile boolean handshakeComplete;
  
  private final String expectedSubprotocol;
  
  private volatile String actualSubprotocol;
  
  protected final HttpHeaders customHeaders;
  
  private final int maxFramePayloadLength;
  
  protected WebSocketClientHandshaker(URI uri, WebSocketVersion version, String subprotocol, HttpHeaders customHeaders, int maxFramePayloadLength) {
    this.uri = uri;
    this.version = version;
    this.expectedSubprotocol = subprotocol;
    this.customHeaders = customHeaders;
    this.maxFramePayloadLength = maxFramePayloadLength;
  }
  
  public URI uri() {
    return this.uri;
  }
  
  public WebSocketVersion version() {
    return this.version;
  }
  
  public int maxFramePayloadLength() {
    return this.maxFramePayloadLength;
  }
  
  public boolean isHandshakeComplete() {
    return this.handshakeComplete;
  }
  
  private void setHandshakeComplete() {
    this.handshakeComplete = true;
  }
  
  public String expectedSubprotocol() {
    return this.expectedSubprotocol;
  }
  
  public String actualSubprotocol() {
    return this.actualSubprotocol;
  }
  
  private void setActualSubprotocol(String actualSubprotocol) {
    this.actualSubprotocol = actualSubprotocol;
  }
  
  public ChannelFuture handshake(Channel channel) {
    if (channel == null)
      throw new NullPointerException("channel"); 
    return handshake(channel, channel.newPromise());
  }
  
  public final ChannelFuture handshake(Channel channel, final ChannelPromise promise) {
    FullHttpRequest request = newHandshakeRequest();
    HttpResponseDecoder decoder = (HttpResponseDecoder)channel.pipeline().get(HttpResponseDecoder.class);
    if (decoder == null) {
      HttpClientCodec codec = (HttpClientCodec)channel.pipeline().get(HttpClientCodec.class);
      if (codec == null) {
        promise.setFailure(new IllegalStateException("ChannelPipeline does not contain a HttpResponseDecoder or HttpClientCodec"));
        return (ChannelFuture)promise;
      } 
    } 
    channel.writeAndFlush(request).addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) {
            if (future.isSuccess()) {
              ChannelPipeline p = future.channel().pipeline();
              ChannelHandlerContext ctx = p.context(HttpRequestEncoder.class);
              if (ctx == null)
                ctx = p.context(HttpClientCodec.class); 
              if (ctx == null) {
                promise.setFailure(new IllegalStateException("ChannelPipeline does not contain a HttpRequestEncoder or HttpClientCodec"));
                return;
              } 
              p.addAfter(ctx.name(), "ws-encoder", (ChannelHandler)WebSocketClientHandshaker.this.newWebSocketEncoder());
              promise.setSuccess();
            } else {
              promise.setFailure(future.cause());
            } 
          }
        });
    return (ChannelFuture)promise;
  }
  
  public final void finishHandshake(Channel channel, FullHttpResponse response) {
    verify(response);
    String receivedProtocol = response.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
    receivedProtocol = (receivedProtocol != null) ? receivedProtocol.trim() : null;
    String expectedProtocol = (this.expectedSubprotocol != null) ? this.expectedSubprotocol : "";
    boolean protocolValid = false;
    if (expectedProtocol.isEmpty() && receivedProtocol == null) {
      protocolValid = true;
      setActualSubprotocol(this.expectedSubprotocol);
    } else if (!expectedProtocol.isEmpty() && receivedProtocol != null && !receivedProtocol.isEmpty()) {
      for (String protocol : expectedProtocol.split(",")) {
        if (protocol.trim().equals(receivedProtocol)) {
          protocolValid = true;
          setActualSubprotocol(receivedProtocol);
          break;
        } 
      } 
    } 
    if (!protocolValid)
      throw new WebSocketHandshakeException(String.format("Invalid subprotocol. Actual: %s. Expected one of: %s", new Object[] { receivedProtocol, this.expectedSubprotocol })); 
    setHandshakeComplete();
    final ChannelPipeline p = channel.pipeline();
    HttpContentDecompressor decompressor = (HttpContentDecompressor)p.get(HttpContentDecompressor.class);
    if (decompressor != null)
      p.remove((ChannelHandler)decompressor); 
    HttpObjectAggregator aggregator = (HttpObjectAggregator)p.get(HttpObjectAggregator.class);
    if (aggregator != null)
      p.remove((ChannelHandler)aggregator); 
    ChannelHandlerContext ctx = p.context(HttpResponseDecoder.class);
    if (ctx == null) {
      ctx = p.context(HttpClientCodec.class);
      if (ctx == null)
        throw new IllegalStateException("ChannelPipeline does not contain a HttpRequestEncoder or HttpClientCodec"); 
      final HttpClientCodec codec = (HttpClientCodec)ctx.handler();
      codec.removeOutboundHandler();
      p.addAfter(ctx.name(), "ws-decoder", (ChannelHandler)newWebsocketDecoder());
      channel.eventLoop().execute(new Runnable() {
            public void run() {
              p.remove((ChannelHandler)codec);
            }
          });
    } else {
      if (p.get(HttpRequestEncoder.class) != null)
        p.remove(HttpRequestEncoder.class); 
      final ChannelHandlerContext context = ctx;
      p.addAfter(context.name(), "ws-decoder", (ChannelHandler)newWebsocketDecoder());
      channel.eventLoop().execute(new Runnable() {
            public void run() {
              p.remove(context.handler());
            }
          });
    } 
  }
  
  public final ChannelFuture processHandshake(Channel channel, HttpResponse response) {
    return processHandshake(channel, response, channel.newPromise());
  }
  
  public final ChannelFuture processHandshake(final Channel channel, HttpResponse response, final ChannelPromise promise) {
    if (response instanceof FullHttpResponse) {
      try {
        finishHandshake(channel, (FullHttpResponse)response);
        promise.setSuccess();
      } catch (Throwable cause) {
        promise.setFailure(cause);
      } 
    } else {
      ChannelPipeline p = channel.pipeline();
      ChannelHandlerContext ctx = p.context(HttpResponseDecoder.class);
      if (ctx == null) {
        ctx = p.context(HttpClientCodec.class);
        if (ctx == null)
          return (ChannelFuture)promise.setFailure(new IllegalStateException("ChannelPipeline does not contain a HttpResponseDecoder or HttpClientCodec")); 
      } 
      String aggregatorName = "httpAggregator";
      p.addAfter(ctx.name(), aggregatorName, (ChannelHandler)new HttpObjectAggregator(8192));
      p.addAfter(aggregatorName, "handshaker", (ChannelHandler)new SimpleChannelInboundHandler<FullHttpResponse>() {
            protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse msg) throws Exception {
              ctx.pipeline().remove((ChannelHandler)this);
              try {
                WebSocketClientHandshaker.this.finishHandshake(channel, msg);
                promise.setSuccess();
              } catch (Throwable cause) {
                promise.setFailure(cause);
              } 
            }
            
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
              ctx.pipeline().remove((ChannelHandler)this);
              promise.setFailure(cause);
            }
            
            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
              promise.tryFailure(WebSocketClientHandshaker.CLOSED_CHANNEL_EXCEPTION);
              ctx.fireChannelInactive();
            }
          });
      try {
        ctx.fireChannelRead(ReferenceCountUtil.retain(response));
      } catch (Throwable cause) {
        promise.setFailure(cause);
      } 
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture close(Channel channel, CloseWebSocketFrame frame) {
    if (channel == null)
      throw new NullPointerException("channel"); 
    return close(channel, frame, channel.newPromise());
  }
  
  public ChannelFuture close(Channel channel, CloseWebSocketFrame frame, ChannelPromise promise) {
    if (channel == null)
      throw new NullPointerException("channel"); 
    return channel.writeAndFlush(frame, promise);
  }
  
  static String rawPath(URI wsURL) {
    String path = wsURL.getRawPath();
    String query = wsURL.getRawQuery();
    if (query != null && !query.isEmpty())
      path = path + '?' + query; 
    return (path == null || path.isEmpty()) ? "/" : path;
  }
  
  static CharSequence websocketHostValue(URI wsURL) {
    int port = wsURL.getPort();
    if (port == -1)
      return wsURL.getHost(); 
    String host = wsURL.getHost();
    if (port == HttpScheme.HTTP.port())
      return (HttpScheme.HTTP.name().contentEquals(wsURL.getScheme()) || WebSocketScheme.WS
        .name().contentEquals(wsURL.getScheme())) ? host : 
        NetUtil.toSocketAddressString(host, port); 
    if (port == HttpScheme.HTTPS.port())
      return (HttpScheme.HTTPS.name().contentEquals(wsURL.getScheme()) || WebSocketScheme.WSS
        .name().contentEquals(wsURL.getScheme())) ? host : 
        NetUtil.toSocketAddressString(host, port); 
    return NetUtil.toSocketAddressString(host, port);
  }
  
  static CharSequence websocketOriginValue(URI wsURL) {
    String schemePrefix;
    int defaultPort;
    String scheme = wsURL.getScheme();
    int port = wsURL.getPort();
    if (WebSocketScheme.WSS.name().contentEquals(scheme) || HttpScheme.HTTPS
      .name().contentEquals(scheme) || (scheme == null && port == WebSocketScheme.WSS
      .port())) {
      schemePrefix = HTTPS_SCHEME_PREFIX;
      defaultPort = WebSocketScheme.WSS.port();
    } else {
      schemePrefix = HTTP_SCHEME_PREFIX;
      defaultPort = WebSocketScheme.WS.port();
    } 
    String host = wsURL.getHost().toLowerCase(Locale.US);
    if (port != defaultPort && port != -1)
      return schemePrefix + NetUtil.toSocketAddressString(host, port); 
    return schemePrefix + host;
  }
  
  protected abstract FullHttpRequest newHandshakeRequest();
  
  protected abstract void verify(FullHttpResponse paramFullHttpResponse);
  
  protected abstract WebSocketFrameDecoder newWebsocketDecoder();
  
  protected abstract WebSocketFrameEncoder newWebSocketEncoder();
}
