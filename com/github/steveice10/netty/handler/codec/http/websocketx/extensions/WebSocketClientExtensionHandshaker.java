package com.github.steveice10.netty.handler.codec.http.websocketx.extensions;

public interface WebSocketClientExtensionHandshaker {
  WebSocketExtensionData newRequestData();
  
  WebSocketClientExtension handshakeExtension(WebSocketExtensionData paramWebSocketExtensionData);
}
