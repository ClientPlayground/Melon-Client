package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

abstract class WebSocketProtocolHandler extends MessageToMessageDecoder<WebSocketFrame> {
  protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
    if (frame instanceof PingWebSocketFrame) {
      frame.content().retain();
      ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content()));
      return;
    } 
    if (frame instanceof PongWebSocketFrame)
      return; 
    out.add(frame.retain());
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    ctx.fireExceptionCaught(cause);
    ctx.close();
  }
}
