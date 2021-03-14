package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandshaker;

@Sharable
public final class WebSocketClientCompressionHandler extends WebSocketClientExtensionHandler {
  public static final WebSocketClientCompressionHandler INSTANCE = new WebSocketClientCompressionHandler();
  
  private WebSocketClientCompressionHandler() {
    super(new WebSocketClientExtensionHandshaker[] { new PerMessageDeflateClientExtensionHandshaker(), new DeflateFrameClientExtensionHandshaker(false), new DeflateFrameClientExtensionHandshaker(true) });
  }
}
