package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpUtil;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;

public class WebSocketServerHandshakerFactory {
  private final String webSocketURL;
  
  private final String subprotocols;
  
  private final boolean allowExtensions;
  
  private final int maxFramePayloadLength;
  
  private final boolean allowMaskMismatch;
  
  public WebSocketServerHandshakerFactory(String webSocketURL, String subprotocols, boolean allowExtensions) {
    this(webSocketURL, subprotocols, allowExtensions, 65536);
  }
  
  public WebSocketServerHandshakerFactory(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength) {
    this(webSocketURL, subprotocols, allowExtensions, maxFramePayloadLength, false);
  }
  
  public WebSocketServerHandshakerFactory(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength, boolean allowMaskMismatch) {
    this.webSocketURL = webSocketURL;
    this.subprotocols = subprotocols;
    this.allowExtensions = allowExtensions;
    this.maxFramePayloadLength = maxFramePayloadLength;
    this.allowMaskMismatch = allowMaskMismatch;
  }
  
  public WebSocketServerHandshaker newHandshaker(HttpRequest req) {
    CharSequence version = req.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_VERSION);
    if (version != null) {
      if (version.equals(WebSocketVersion.V13.toHttpHeaderValue()))
        return new WebSocketServerHandshaker13(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch); 
      if (version.equals(WebSocketVersion.V08.toHttpHeaderValue()))
        return new WebSocketServerHandshaker08(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch); 
      if (version.equals(WebSocketVersion.V07.toHttpHeaderValue()))
        return new WebSocketServerHandshaker07(this.webSocketURL, this.subprotocols, this.allowExtensions, this.maxFramePayloadLength, this.allowMaskMismatch); 
      return null;
    } 
    return new WebSocketServerHandshaker00(this.webSocketURL, this.subprotocols, this.maxFramePayloadLength);
  }
  
  @Deprecated
  public static void sendUnsupportedWebSocketVersionResponse(Channel channel) {
    sendUnsupportedVersionResponse(channel);
  }
  
  public static ChannelFuture sendUnsupportedVersionResponse(Channel channel) {
    return sendUnsupportedVersionResponse(channel, channel.newPromise());
  }
  
  public static ChannelFuture sendUnsupportedVersionResponse(Channel channel, ChannelPromise promise) {
    DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UPGRADE_REQUIRED);
    defaultFullHttpResponse.headers().set((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
    HttpUtil.setContentLength((HttpMessage)defaultFullHttpResponse, 0L);
    return channel.writeAndFlush(defaultFullHttpResponse, promise);
  }
}
