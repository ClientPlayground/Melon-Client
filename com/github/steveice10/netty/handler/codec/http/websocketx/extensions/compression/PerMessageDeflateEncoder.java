package com.github.steveice10.netty.handler.codec.http.websocketx.extensions.compression;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.http.websocketx.WebSocketFrame;
import java.util.List;

class PerMessageDeflateEncoder extends DeflateEncoder {
  private boolean compressing;
  
  public PerMessageDeflateEncoder(int compressionLevel, int windowSize, boolean noContext) {
    super(compressionLevel, windowSize, noContext);
  }
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return (((msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.TextWebSocketFrame || msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.BinaryWebSocketFrame) && (((WebSocketFrame)msg)
      
      .rsv() & 0x4) == 0) || (msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame && this.compressing));
  }
  
  protected int rsv(WebSocketFrame msg) {
    return (msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.TextWebSocketFrame || msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.BinaryWebSocketFrame) ? (msg
      .rsv() | 0x4) : msg.rsv();
  }
  
  protected boolean removeFrameTail(WebSocketFrame msg) {
    return msg.isFinalFragment();
  }
  
  protected void encode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
    super.encode(ctx, msg, out);
    if (msg.isFinalFragment()) {
      this.compressing = false;
    } else if (msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.TextWebSocketFrame || msg instanceof com.github.steveice10.netty.handler.codec.http.websocketx.BinaryWebSocketFrame) {
      this.compressing = true;
    } 
  }
}
