package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtension;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandshaker;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionData;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionDecoder;
import com.github.steveice10.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionEncoder;
import java.util.Collections;

public final class DeflateFrameClientExtensionHandshaker implements WebSocketClientExtensionHandshaker {
  private final int compressionLevel;
  
  private final boolean useWebkitExtensionName;
  
  public DeflateFrameClientExtensionHandshaker(boolean useWebkitExtensionName) {
    this(6, useWebkitExtensionName);
  }
  
  public DeflateFrameClientExtensionHandshaker(int compressionLevel, boolean useWebkitExtensionName) {
    if (compressionLevel < 0 || compressionLevel > 9)
      throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)"); 
    this.compressionLevel = compressionLevel;
    this.useWebkitExtensionName = useWebkitExtensionName;
  }
  
  public WebSocketExtensionData newRequestData() {
    return new WebSocketExtensionData(this.useWebkitExtensionName ? "x-webkit-deflate-frame" : "deflate-frame", 
        
        Collections.emptyMap());
  }
  
  public WebSocketClientExtension handshakeExtension(WebSocketExtensionData extensionData) {
    if (!"x-webkit-deflate-frame".equals(extensionData.name()) && 
      !"deflate-frame".equals(extensionData.name()))
      return null; 
    if (extensionData.parameters().isEmpty())
      return new DeflateFrameClientExtension(this.compressionLevel); 
    return null;
  }
  
  private static class DeflateFrameClientExtension implements WebSocketClientExtension {
    private final int compressionLevel;
    
    public DeflateFrameClientExtension(int compressionLevel) {
      this.compressionLevel = compressionLevel;
    }
    
    public int rsv() {
      return 4;
    }
    
    public WebSocketExtensionEncoder newExtensionEncoder() {
      return new PerFrameDeflateEncoder(this.compressionLevel, 15, false);
    }
    
    public WebSocketExtensionDecoder newExtensionDecoder() {
      return new PerFrameDeflateDecoder(false);
    }
  }
}
