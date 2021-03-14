package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;

public class HttpServerExpectContinueHandler extends ChannelInboundHandlerAdapter {
  private static final FullHttpResponse EXPECTATION_FAILED = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.EXPECTATION_FAILED, Unpooled.EMPTY_BUFFER);
  
  private static final FullHttpResponse ACCEPT = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE, Unpooled.EMPTY_BUFFER);
  
  static {
    EXPECTATION_FAILED.headers().set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, Integer.valueOf(0));
    ACCEPT.headers().set((CharSequence)HttpHeaderNames.CONTENT_LENGTH, Integer.valueOf(0));
  }
  
  protected HttpResponse acceptMessage(HttpRequest request) {
    return ACCEPT.retainedDuplicate();
  }
  
  protected HttpResponse rejectResponse(HttpRequest request) {
    return EXPECTATION_FAILED.retainedDuplicate();
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      HttpRequest req = (HttpRequest)msg;
      if (HttpUtil.is100ContinueExpected(req)) {
        HttpResponse accept = acceptMessage(req);
        if (accept == null) {
          HttpResponse rejection = rejectResponse(req);
          ReferenceCountUtil.release(msg);
          ctx.writeAndFlush(rejection).addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE);
          return;
        } 
        ctx.writeAndFlush(accept).addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE);
        req.headers().remove((CharSequence)HttpHeaderNames.EXPECT);
      } 
    } 
    super.channelRead(ctx, msg);
  }
}
