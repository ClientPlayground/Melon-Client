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
import com.github.steveice10.netty.handler.codec.http.HttpContentCompressor;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpObjectAggregator;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpRequestDecoder;
import com.github.steveice10.netty.handler.codec.http.HttpResponseEncoder;
import com.github.steveice10.netty.handler.codec.http.HttpServerCodec;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class WebSocketServerHandshaker {
  protected static final InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketServerHandshaker.class);
  
  private static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), WebSocketServerHandshaker.class, "handshake(...)");
  
  private final String uri;
  
  private final String[] subprotocols;
  
  private final WebSocketVersion version;
  
  private final int maxFramePayloadLength;
  
  private String selectedSubprotocol;
  
  public static final String SUB_PROTOCOL_WILDCARD = "*";
  
  protected WebSocketServerHandshaker(WebSocketVersion version, String uri, String subprotocols, int maxFramePayloadLength) {
    this.version = version;
    this.uri = uri;
    if (subprotocols != null) {
      String[] subprotocolArray = subprotocols.split(",");
      for (int i = 0; i < subprotocolArray.length; i++)
        subprotocolArray[i] = subprotocolArray[i].trim(); 
      this.subprotocols = subprotocolArray;
    } else {
      this.subprotocols = EmptyArrays.EMPTY_STRINGS;
    } 
    this.maxFramePayloadLength = maxFramePayloadLength;
  }
  
  public String uri() {
    return this.uri;
  }
  
  public Set<String> subprotocols() {
    Set<String> ret = new LinkedHashSet<String>();
    Collections.addAll(ret, this.subprotocols);
    return ret;
  }
  
  public WebSocketVersion version() {
    return this.version;
  }
  
  public int maxFramePayloadLength() {
    return this.maxFramePayloadLength;
  }
  
  public ChannelFuture handshake(Channel channel, FullHttpRequest req) {
    return handshake(channel, req, (HttpHeaders)null, channel.newPromise());
  }
  
  public final ChannelFuture handshake(Channel channel, FullHttpRequest req, HttpHeaders responseHeaders, final ChannelPromise promise) {
    final String encoderName;
    if (logger.isDebugEnabled())
      logger.debug("{} WebSocket version {} server handshake", channel, version()); 
    FullHttpResponse response = newHandshakeResponse(req, responseHeaders);
    ChannelPipeline p = channel.pipeline();
    if (p.get(HttpObjectAggregator.class) != null)
      p.remove(HttpObjectAggregator.class); 
    if (p.get(HttpContentCompressor.class) != null)
      p.remove(HttpContentCompressor.class); 
    ChannelHandlerContext ctx = p.context(HttpRequestDecoder.class);
    if (ctx == null) {
      ctx = p.context(HttpServerCodec.class);
      if (ctx == null) {
        promise.setFailure(new IllegalStateException("No HttpDecoder and no HttpServerCodec in the pipeline"));
        return (ChannelFuture)promise;
      } 
      p.addBefore(ctx.name(), "wsdecoder", (ChannelHandler)newWebsocketDecoder());
      p.addBefore(ctx.name(), "wsencoder", (ChannelHandler)newWebSocketEncoder());
      encoderName = ctx.name();
    } else {
      p.replace(ctx.name(), "wsdecoder", (ChannelHandler)newWebsocketDecoder());
      encoderName = p.context(HttpResponseEncoder.class).name();
      p.addBefore(encoderName, "wsencoder", (ChannelHandler)newWebSocketEncoder());
    } 
    channel.writeAndFlush(response).addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            if (future.isSuccess()) {
              ChannelPipeline p = future.channel().pipeline();
              p.remove(encoderName);
              promise.setSuccess();
            } else {
              promise.setFailure(future.cause());
            } 
          }
        });
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture handshake(Channel channel, HttpRequest req) {
    return handshake(channel, req, (HttpHeaders)null, channel.newPromise());
  }
  
  public final ChannelFuture handshake(final Channel channel, HttpRequest req, final HttpHeaders responseHeaders, final ChannelPromise promise) {
    if (req instanceof FullHttpRequest)
      return handshake(channel, (FullHttpRequest)req, responseHeaders, promise); 
    if (logger.isDebugEnabled())
      logger.debug("{} WebSocket version {} server handshake", channel, version()); 
    ChannelPipeline p = channel.pipeline();
    ChannelHandlerContext ctx = p.context(HttpRequestDecoder.class);
    if (ctx == null) {
      ctx = p.context(HttpServerCodec.class);
      if (ctx == null) {
        promise.setFailure(new IllegalStateException("No HttpDecoder and no HttpServerCodec in the pipeline"));
        return (ChannelFuture)promise;
      } 
    } 
    String aggregatorName = "httpAggregator";
    p.addAfter(ctx.name(), aggregatorName, (ChannelHandler)new HttpObjectAggregator(8192));
    p.addAfter(aggregatorName, "handshaker", (ChannelHandler)new SimpleChannelInboundHandler<FullHttpRequest>() {
          protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
            ctx.pipeline().remove((ChannelHandler)this);
            WebSocketServerHandshaker.this.handshake(channel, msg, responseHeaders, promise);
          }
          
          public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.pipeline().remove((ChannelHandler)this);
            promise.tryFailure(cause);
            ctx.fireExceptionCaught(cause);
          }
          
          public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            promise.tryFailure(WebSocketServerHandshaker.CLOSED_CHANNEL_EXCEPTION);
            ctx.fireChannelInactive();
          }
        });
    try {
      ctx.fireChannelRead(ReferenceCountUtil.retain(req));
    } catch (Throwable cause) {
      promise.setFailure(cause);
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
    return channel.writeAndFlush(frame, promise).addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
  }
  
  protected String selectSubprotocol(String requestedSubprotocols) {
    if (requestedSubprotocols == null || this.subprotocols.length == 0)
      return null; 
    String[] requestedSubprotocolArray = requestedSubprotocols.split(",");
    for (String p : requestedSubprotocolArray) {
      String requestedSubprotocol = p.trim();
      for (String supportedSubprotocol : this.subprotocols) {
        if ("*".equals(supportedSubprotocol) || requestedSubprotocol
          .equals(supportedSubprotocol)) {
          this.selectedSubprotocol = requestedSubprotocol;
          return requestedSubprotocol;
        } 
      } 
    } 
    return null;
  }
  
  public String selectedSubprotocol() {
    return this.selectedSubprotocol;
  }
  
  protected abstract FullHttpResponse newHandshakeResponse(FullHttpRequest paramFullHttpRequest, HttpHeaders paramHttpHeaders);
  
  protected abstract WebSocketFrameDecoder newWebsocketDecoder();
  
  protected abstract WebSocketFrameEncoder newWebSocketEncoder();
}
