package com.github.steveice10.netty.handler.codec.http.websocketx.extensions;

public interface WebSocketServerExtensionHandshaker {
  WebSocketServerExtension handshakeExtension(WebSocketExtensionData paramWebSocketExtensionData);
}
