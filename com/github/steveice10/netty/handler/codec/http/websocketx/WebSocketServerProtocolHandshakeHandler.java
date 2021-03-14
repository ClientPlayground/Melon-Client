package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpMethod;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpUtil;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.handler.ssl.SslHandler;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;

class WebSocketServerProtocolHandshakeHandler extends ChannelInboundHandlerAdapter {
  private final String websocketPath;
  
  private final String subprotocols;
  
  private final boolean allowExtensions;
  
  private final int maxFramePayloadSize;
  
  private final boolean allowMaskMismatch;
  
  private final boolean checkStartsWith;
  
  WebSocketServerProtocolHandshakeHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch) {
    this(websocketPath, subprotocols, allowExtensions, maxFrameSize, allowMaskMismatch, false);
  }
  
  WebSocketServerProtocolHandshakeHandler(String websocketPath, String subprotocols, boolean allowExtensions, int maxFrameSize, boolean allowMaskMismatch, boolean checkStartsWith) {
    this.websocketPath = websocketPath;
    this.subprotocols = subprotocols;
    this.allowExtensions = allowExtensions;
    this.maxFramePayloadSize = maxFrameSize;
    this.allowMaskMismatch = allowMaskMismatch;
    this.checkStartsWith = checkStartsWith;
  }
  
  public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
    final FullHttpRequest req = (FullHttpRequest)msg;
    if (isNotWebSocketPath(req)) {
      ctx.fireChannelRead(msg);
      return;
    } 
    try {
      if (req.method() != HttpMethod.GET) {
        sendHttpResponse(ctx, (HttpRequest)req, (HttpResponse)new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
        return;
      } 
      WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(ctx.pipeline(), (HttpRequest)req, this.websocketPath), this.subprotocols, this.allowExtensions, this.maxFramePayloadSize, this.allowMaskMismatch);
      final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker((HttpRequest)req);
      if (handshaker == null) {
        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
      } else {
        ChannelFuture handshakeFuture = handshaker.handshake(ctx.channel(), req);
        handshakeFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
              public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                  ctx.fireExceptionCaught(future.cause());
                } else {
                  ctx.fireUserEventTriggered(WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE);
                  ctx.fireUserEventTriggered(new WebSocketServerProtocolHandler.HandshakeComplete(req
                        
                        .uri(), req.headers(), handshaker.selectedSubprotocol()));
                } 
              }
            });
        WebSocketServerProtocolHandler.setHandshaker(ctx.channel(), handshaker);
        ctx.pipeline().replace((ChannelHandler)this, "WS403Responder", 
            WebSocketServerProtocolHandler.forbiddenHttpRequestResponder());
      } 
    } finally {
      req.release();
    } 
  }
  
  private boolean isNotWebSocketPath(FullHttpRequest req) {
    return this.checkStartsWith ? (!req.uri().startsWith(this.websocketPath)) : (!req.uri().equals(this.websocketPath));
  }
  
  private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
    ChannelFuture f = ctx.channel().writeAndFlush(res);
    if (!HttpUtil.isKeepAlive((HttpMessage)req) || res.status().code() != 200)
      f.addListener((GenericFutureListener)ChannelFutureListener.CLOSE); 
  }
  
  private static String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path) {
    String protocol = "ws";
    if (cp.get(SslHandler.class) != null)
      protocol = "wss"; 
    String host = req.headers().get((CharSequence)HttpHeaderNames.HOST);
    return protocol + "://" + host + path;
  }
}
