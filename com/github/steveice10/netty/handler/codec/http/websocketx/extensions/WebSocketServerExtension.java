package com.github.steveice10.netty.handler.codec.http.websocketx.extensions;

public interface WebSocketServerExtension extends WebSocketExtension {
  WebSocketExtensionData newReponseData();
}
