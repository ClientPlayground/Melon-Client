package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.handler.codec.http.websocketx.WebSocketFrame;

class PerFrameDeflateDecoder extends DeflateDecoder {
  public PerFrameDeflateDecoder(boolean noContext) {
    super(noContext);
  }
  
  public boolean acceptInboundMessage(Object msg) throws Exception {
    return ((msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.TextWebSocketFrame || msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.BinaryWebSocketFrame || msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame) && (((WebSocketFrame)msg)
      
      .rsv() & 0x4) > 0);
  }
  
  protected int newRsv(WebSocketFrame msg) {
    return msg.rsv() ^ 0x4;
  }
  
  protected boolean appendFrameTail(WebSocketFrame msg) {
    return true;
  }
}
