package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.handler.codec.http.websocketx.WebSocketFrame;

class PerFrameDeflateEncoder extends DeflateEncoder {
  public PerFrameDeflateEncoder(int compressionLevel, int windowSize, boolean noContext) {
    super(compressionLevel, windowSize, noContext);
  }
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return ((msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.TextWebSocketFrame || msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.BinaryWebSocketFrame || msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame) && ((WebSocketFrame)msg)
      
      .content().readableBytes() > 0 && (((WebSocketFrame)msg)
      .rsv() & 0x4) == 0);
  }
  
  protected int rsv(WebSocketFrame msg) {
    return msg.rsv() | 0x4;
  }
  
  protected boolean removeFrameTail(WebSocketFrame msg) {
    return true;
  }
}
