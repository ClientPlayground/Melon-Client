package com.github.steveice10.netty.handler.ssl.ocsp;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import com.github.steveice10.netty.handler.ssl.SslHandshakeCompletionEvent;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import javax.net.ssl.SSLHandshakeException;

public abstract class OcspClientHandler extends ChannelInboundHandlerAdapter {
  private static final SSLHandshakeException OCSP_VERIFICATION_EXCEPTION = (SSLHandshakeException)ThrowableUtil.unknownStackTrace(new SSLHandshakeException("Bad OCSP response"), OcspClientHandler.class, "verify(...)");
  
  private final ReferenceCountedOpenSslEngine engine;
  
  protected OcspClientHandler(ReferenceCountedOpenSslEngine engine) {
    this.engine = (ReferenceCountedOpenSslEngine)ObjectUtil.checkNotNull(engine, "engine");
  }
  
  protected abstract boolean verify(ChannelHandlerContext paramChannelHandlerContext, ReferenceCountedOpenSslEngine paramReferenceCountedOpenSslEngine) throws Exception;
  
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof SslHandshakeCompletionEvent) {
      ctx.pipeline().remove((ChannelHandler)this);
      SslHandshakeCompletionEvent event = (SslHandshakeCompletionEvent)evt;
      if (event.isSuccess() && !verify(ctx, this.engine))
        throw OCSP_VERIFICATION_EXCEPTION; 
    } 
    ctx.fireUserEventTriggered(evt);
  }
}
