package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.internal.StringUtil;

public abstract class Http2ChannelDuplexHandler extends ChannelDuplexHandler {
  private volatile Http2FrameCodec frameCodec;
  
  public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.frameCodec = requireHttp2FrameCodec(ctx);
    handlerAdded0(ctx);
  }
  
  protected void handlerAdded0(ChannelHandlerContext ctx) throws Exception {}
  
  public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    try {
      handlerRemoved0(ctx);
    } finally {
      this.frameCodec = null;
    } 
  }
  
  protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {}
  
  public final Http2FrameStream newStream() {
    Http2FrameCodec codec = this.frameCodec;
    if (codec == null)
      throw new IllegalStateException(StringUtil.simpleClassName(Http2FrameCodec.class) + " not found. Has the handler been added to a pipeline?"); 
    return codec.newStream();
  }
  
  protected final void forEachActiveStream(Http2FrameStreamVisitor streamVisitor) throws Http2Exception {
    this.frameCodec.forEachActiveStream(streamVisitor);
  }
  
  private static Http2FrameCodec requireHttp2FrameCodec(ChannelHandlerContext ctx) {
    ChannelHandlerContext frameCodecCtx = ctx.pipeline().context(Http2FrameCodec.class);
    if (frameCodecCtx == null)
      throw new IllegalArgumentException(Http2FrameCodec.class.getSimpleName() + " was not found in the channel pipeline."); 
    return (Http2FrameCodec)frameCodecCtx.handler();
  }
}
