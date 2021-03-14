package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import java.util.regex.Pattern;

public class WebSocketServerHandshaker00 extends WebSocketServerHandshaker {
  private static final Pattern BEGINNING_DIGIT = Pattern.compile("[^0-9]");
  
  private static final Pattern BEGINNING_SPACE = Pattern.compile("[^ ]");
  
  public WebSocketServerHandshaker00(String webSocketURL, String subprotocols, int maxFramePayloadLength) {
    super(WebSocketVersion.V00, webSocketURL, subprotocols, maxFramePayloadLength);
  }
  
  protected FullHttpResponse newHandshakeResponse(FullHttpRequest req, HttpHeaders headers) {
    if (!req.headers().containsValue((CharSequence)HttpHeaderNames.CONNECTION, (CharSequence)HttpHeaderValues.UPGRADE, true) || 
      !HttpHeaderValues.WEBSOCKET.contentEqualsIgnoreCase(req.headers().get((CharSequence)HttpHeaderNames.UPGRADE)))
      throw new WebSocketHandshakeException("not a WebSocket handshake request: missing upgrade"); 
    boolean isHixie76 = (req.headers().contains((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY1) && req.headers().contains((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY2));
    DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(101, isHixie76 ? "WebSocket Protocol Handshake" : "Web Socket Protocol Handshake"));
    if (headers != null)
      defaultFullHttpResponse.headers().add(headers); 
    defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET);
    defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
    if (isHixie76) {
      defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_ORIGIN, req.headers().get((CharSequence)HttpHeaderNames.ORIGIN));
      defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_LOCATION, uri());
      String subprotocols = req.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
      if (subprotocols != null) {
        String selectedSubprotocol = selectSubprotocol(subprotocols);
        if (selectedSubprotocol == null) {
          if (logger.isDebugEnabled())
            logger.debug("Requested subprotocol(s) not supported: {}", subprotocols); 
        } else {
          defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL, selectedSubprotocol);
        } 
      } 
      String key1 = req.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY1);
      String key2 = req.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY2);
      int a = (int)(Long.parseLong(BEGINNING_DIGIT.matcher(key1).replaceAll("")) / BEGINNING_SPACE.matcher(key1).replaceAll("").length());
      int b = (int)(Long.parseLong(BEGINNING_DIGIT.matcher(key2).replaceAll("")) / BEGINNING_SPACE.matcher(key2).replaceAll("").length());
      long c = req.content().readLong();
      ByteBuf input = Unpooled.buffer(16);
      input.writeInt(a);
      input.writeInt(b);
      input.writeLong(c);
      defaultFullHttpResponse.content().writeBytes(WebSocketUtil.md5(input.array()));
    } else {
      defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.WEBSOCKET_ORIGIN, req.headers().get((CharSequence)HttpHeaderNames.ORIGIN));
      defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.WEBSOCKET_LOCATION, uri());
      String protocol = req.headers().get((CharSequence)HttpHeaderNames.WEBSOCKET_PROTOCOL);
      if (protocol != null)
        defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.WEBSOCKET_PROTOCOL, selectSubprotocol(protocol)); 
    } 
    return (FullHttpResponse)defaultFullHttpResponse;
  }
  
  public ChannelFuture close(Channel channel, CloseWebSocketFrame frame, ChannelPromise promise) {
    return channel.writeAndFlush(frame, promise);
  }
  
  protected WebSocketFrameDecoder newWebsocketDecoder() {
    return new WebSocket00FrameDecoder(maxFramePayloadLength());
  }
  
  protected WebSocketFrameEncoder newWebSocketEncoder() {
    return new WebSocket00FrameEncoder();
  }
}
