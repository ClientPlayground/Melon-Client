package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;

class WebSocketClientProtocolHandshakeHandler extends ChannelInboundHandlerAdapter {
  private final WebSocketClientHandshaker handshaker;
  
  WebSocketClientProtocolHandshakeHandler(WebSocketClientHandshaker handshaker) {
    this.handshaker = handshaker;
  }
  
  public void channelActive(final ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    this.handshaker.handshake(ctx.channel()).addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            if (!future.isSuccess()) {
              ctx.fireExceptionCaught(future.cause());
            } else {
              ctx.fireUserEventTriggered(WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_ISSUED);
            } 
          }
        });
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (!(msg instanceof FullHttpResponse)) {
      ctx.fireChannelRead(msg);
      return;
    } 
    FullHttpResponse response = (FullHttpResponse)msg;
    try {
      if (!this.handshaker.isHandshakeComplete()) {
        this.handshaker.finishHandshake(ctx.channel(), response);
        ctx.fireUserEventTriggered(WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE);
        ctx.pipeline().remove((ChannelHandler)this);
        return;
      } 
      throw new IllegalStateException("WebSocketClientHandshaker should have been non finished yet");
    } finally {
      response.release();
    } 
  }
}
