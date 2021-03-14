package com.github.steveice10.netty.handler.codec.http.websocketx;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.handler.codec.CorruptedFrameException;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;

public class Utf8FrameValidator extends ChannelInboundHandlerAdapter {
  private int fragmentedFramesCount;
  
  private Utf8Validator utf8Validator;
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof WebSocketFrame) {
      WebSocketFrame frame = (WebSocketFrame)msg;
      if (((WebSocketFrame)msg).isFinalFragment()) {
        if (!(frame instanceof PingWebSocketFrame)) {
          this.fragmentedFramesCount = 0;
          if (frame instanceof TextWebSocketFrame || (this.utf8Validator != null && this.utf8Validator
            .isChecking())) {
            checkUTF8String(ctx, frame.content());
            this.utf8Validator.finish();
          } 
        } 
      } else {
        if (this.fragmentedFramesCount == 0) {
          if (frame instanceof TextWebSocketFrame)
            checkUTF8String(ctx, frame.content()); 
        } else if (this.utf8Validator != null && this.utf8Validator.isChecking()) {
          checkUTF8String(ctx, frame.content());
        } 
        this.fragmentedFramesCount++;
      } 
    } 
    super.channelRead(ctx, msg);
  }
  
  private void checkUTF8String(ChannelHandlerContext ctx, ByteBuf buffer) {
    try {
      if (this.utf8Validator == null)
        this.utf8Validator = new Utf8Validator(); 
      this.utf8Validator.check(buffer);
    } catch (CorruptedFrameException ex) {
      if (ctx.channel().isActive())
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener((GenericFutureListener)ChannelFutureListener.CLOSE); 
    } 
  }
}
