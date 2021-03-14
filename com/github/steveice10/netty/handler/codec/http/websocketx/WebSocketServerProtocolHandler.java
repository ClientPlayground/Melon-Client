package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import java.util.List;

public class WebSocketServerProtocolHandler extends WebSocketProtocolHandler {
  public enum ServerHandshakeStateEvent {
    HANDSHAKE_COMPLETE;
  }
  
  public static final class HandshakeComplete {
    private final String requestUri;
    
    private final HttpHeaders requestHeaders;
    
    private final String selectedSubprotocol;
    
    HandshakeComplete(String requestUri, HttpHeaders requestHeaders, String selectedSubprotocol) {
      this.requestUri = requestUri;
      this.requestHeaders = requestHeaders;
      this.selectedSubprotocol = selectedSubprotocol;
    }
    
    public String requestUri() {
      return this.requestUri;
    }
    
    public HttpHeaders requestHeaders() {
      return this.requestHeaders;
    }
    
    public String selectedSubprotocol() {
      return this.selectedSubprotocol;
    }
  }
  
  private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY = AttributeKey.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");
  
  private final String websocketPath;
  
  private final String subprotocols;
  
  private final boolean allowExtensions;
  
  private final int maxFramePayloadLength;
  
  private final boolean allowMaskMismatch;
  
  private final boolean checkStartsWith;
  
  public WebSocketServerProtocolHandler(String websocketPath) {
    this(websocketPath, null, false);
  }
  
  public WebSocketServerProtocolHandler(String websocketPath, boolean checkStartsWith) {
    this(websocketPath, null, false, 65536, false, checkStartsWith);
  }
  
  public WebSocketServerProtocolHandler(String websocketPath, String subprotocols) {
    this(websocketPath, subprotocols, false);
  }
  
  public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions) {
    this(websocketPath, subprotocols, allowExtensions, 65536);
  }
  
  public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize) {
    this(websocketPath, subprotocols, allowExtensions, maxFrameSize, false);
  }
  
  public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch) {
    this(websocketPath, subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch, false);
  }
  
  public WebSocketServerProtocolHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch, boolean checkStartsWith) {
    this.websocketPath = websocketPath;
    this.subprotocols = subprotocols;
    this.allowExtensions = allowExtensions;
    this.maxFramePayloadLength = maxFrameSize;
    this.allowMaskMismatch = allowMaskMismatch;
    this.checkStartsWith = checkStartsWith;
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) {
    ChannelPipeline cp = ctx.pipeline();
    if (cp.get(WebSocketServerProtocolHandshakeHandler.class) == null)
      ctx.pipeline().addBefore(ctx.name(), WebSocketServerProtocolHandshakeHandler.class.getName(), (ChannelHandler)new WebSocketServerProtocolHandshakeHandler(this.websocketPath, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch, this.checkStartsWith)); 
    if (cp.get(Utf8FrameValidator.class) == null)
      ctx.pipeline().addBefore(ctx.name(), Utf8FrameValidator.class.getName(), (ChannelHandler)new Utf8FrameValidator()); 
  }
  
  protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
    if (frame instanceof CloseWebSocketFrame) {
      WebSocketServerHandshaker handshaker = getHandshaker(ctx.channel());
      if (handshaker != null) {
        frame.retain();
        handshaker.close(ctx.channel(), (CloseWebSocketFrame)frame);
      } else {
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
      } 
      return;
    } 
    super.decode(ctx, frame, out);
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (cause instanceof WebSocketHandshakeException) {
      DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.wrappedBuffer(cause.getMessage().getBytes()));
      ctx.channel().writeAndFlush(defaultFullHttpResponse).addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
    } else {
      ctx.fireExceptionCaught(cause);
      ctx.close();
    } 
  }
  
  static WebSocketServerHandshaker getHandshaker(Channel channel) {
    return (WebSocketServerHandshaker)channel.attr(HANDSHAKER_ATTR_KEY).get();
  }
  
  static void setHandshaker(Channel channel, WebSocketServerHandshaker handshaker) {
    channel.attr(HANDSHAKER_ATTR_KEY).set(handshaker);
  }
  
  static ChannelHandler forbiddenHttpRequestResponder() {
    return (ChannelHandler)new ChannelInboundHandlerAdapter() {
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
          if (msg instanceof FullHttpRequest) {
            ((FullHttpRequest)msg).release();
            DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN);
            ctx.channel().writeAndFlush(defaultFullHttpResponse);
          } else {
            ctx.fireChannelRead(msg);
          } 
        }
      };
  }
}
