package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtensionHandler;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtensionHandshaker;

public class WebSocketServerCompressionHandler extends WebSocketServerExtensionHandler {
  public WebSocketServerCompressionHandler() {
    super(new WebSocketServerExtensionHandshaker[] { new PerMessageDeflateServerExtensionHandshaker(), new DeflateFrameServerExtensionHandshaker() });
  }
}
