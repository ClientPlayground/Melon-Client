package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import java.net.URI;

public final class WebSocketClientHandshakerFactory {
  public static WebSocketClientHandshaker newHandshaker(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders) {
    return newHandshaker(webSocketURL, version, subprotocol, allowExtensions, customHeaders, 65536);
  }
  
  public static WebSocketClientHandshaker newHandshaker(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength) {
    return newHandshaker(webSocketURL, version, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, true, false);
  }
  
  public static WebSocketClientHandshaker newHandshaker(URI webSocketURL, WebSocketVersion version, String subprotocol, boolean allowExtensions, HttpHeaders customHeaders, int maxFramePayloadLength, boolean performMasking, boolean allowMaskMismatch) {
    if (version == WebSocketVersion.V13)
      return new WebSocketClientHandshaker13(webSocketURL, WebSocketVersion.V13, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, performMasking, allowMaskMismatch); 
    if (version == WebSocketVersion.V08)
      return new WebSocketClientHandshaker08(webSocketURL, WebSocketVersion.V08, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, performMasking, allowMaskMismatch); 
    if (version == WebSocketVersion.V07)
      return new WebSocketClientHandshaker07(webSocketURL, WebSocketVersion.V07, subprotocol, allowExtensions, customHeaders, maxFramePayloadLength, performMasking, allowMaskMismatch); 
    if (version == WebSocketVersion.V00)
      return new WebSocketClientHandshaker00(webSocketURL, WebSocketVersion.V00, subprotocol, customHeaders, maxFramePayloadLength); 
    throw new WebSocketHandshakeException("Protocol version " + version + " not supported.");
  }
}
