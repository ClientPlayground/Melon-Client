package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.util.CharsetUtil;

public class WebSocketServerHandshaker08 extends WebSocketServerHandshaker {
  public static final String WEBSOCKET_08_ACCEPT_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  
  private final boolean allowExtensions;
  
  private final boolean allowMaskMismatch;
  
  public WebSocketServerHandshaker08(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength) {
    this(webSocketURL, subprotocols, allowExtensions, maxFramePayloadLength, false);
  }
  
  public WebSocketServerHandshaker08(String webSocketURL, String subprotocols, boolean allowExtensions, int maxFramePayloadLength, boolean allowMaskMismatch) {
    super(WebSocketVersion.V08, webSocketURL, subprotocols, maxFramePayloadLength);
    this.allowExtensions = allowExtensions;
    this.allowMaskMismatch = allowMaskMismatch;
  }
  
  protected FullHttpResponse newHandshakeResponse(FullHttpRequest req, HttpHeaders headers) {
    DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS);
    if (headers != null)
      defaultFullHttpResponse.headers().add(headers); 
    CharSequence key = req.headers().get((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_KEY);
    if (key == null)
      throw new WebSocketHandshakeException("not a WebSocket request: missing key"); 
    String acceptSeed = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    byte[] sha1 = WebSocketUtil.sha1(acceptSeed.getBytes(CharsetUtil.US_ASCII));
    String accept = WebSocketUtil.base64(sha1);
    if (logger.isDebugEnabled())
      logger.debug("WebSocket version 08 server handshake key: {}, response: {}", key, accept); 
    defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET);
    defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
    defaultFullHttpResponse.headers().add((CharSequence)HttpHeaderNames.SEC_WEBSOCKET_ACCEPT, accept);
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
    return (FullHttpResponse)defaultFullHttpResponse;
  }
  
  protected WebSocketFrameDecoder newWebsocketDecoder() {
    return new WebSocket08FrameDecoder(true, this.allowExtensions, maxFramePayloadLength(), this.allowMaskMismatch);
  }
  
  protected WebSocketFrameEncoder newWebSocketEncoder() {
    return new WebSocket08FrameEncoder(false);
  }
}
