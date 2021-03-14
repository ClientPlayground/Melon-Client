package com.github.steveice10.netty.handler.codec.http.websocketx.extensions;

public interface WebSocketExtension {
  public static final int RSV1 = 4;
  
  public static final int RSV2 = 2;
  
  public static final int RSV3 = 1;
  
  int rsv();
  
  WebSocketExtensionEncoder newExtensionEncoder();
  
  WebSocketExtensionDecoder newExtensionDecoder();
}
