package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;

public abstract class ApplicationProtocolNegotiationHandler extends ChannelInboundHandlerAdapter {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ApplicationProtocolNegotiationHandler.class);
  
  private final String fallbackProtocol;
  
  protected ApplicationProtocolNegotiationHandler(String fallbackProtocol) {
    this.fallbackProtocol = (String)ObjectUtil.checkNotNull(fallbackProtocol, "fallbackProtocol");
  }
  
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof SslHandshakeCompletionEvent) {
      ctx.pipeline().remove((ChannelHandler)this);
      SslHandshakeCompletionEvent handshakeEvent = (SslHandshakeCompletionEvent)evt;
      if (handshakeEvent.isSuccess()) {
        SslHandler sslHandler = (SslHandler)ctx.pipeline().get(SslHandler.class);
        if (sslHandler == null)
          throw new IllegalStateException("cannot find a SslHandler in the pipeline (required for application-level protocol negotiation)"); 
        String protocol = sslHandler.applicationProtocol();
        configurePipeline(ctx, (protocol != null) ? protocol : this.fallbackProtocol);
      } else {
        handshakeFailure(ctx, handshakeEvent.cause());
      } 
    } 
    ctx.fireUserEventTriggered(evt);
  }
  
  protected abstract void configurePipeline(ChannelHandlerContext paramChannelHandlerContext, String paramString) throws Exception;
  
  protected void handshakeFailure(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("{} TLS handshake failed:", ctx.channel(), cause);
    ctx.close();
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.warn("{} Failed to select the application-level protocol:", ctx.channel(), cause);
    ctx.close();
  }
}
