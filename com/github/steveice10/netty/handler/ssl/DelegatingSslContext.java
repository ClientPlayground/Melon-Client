package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;

public abstract class DelegatingSslContext extends SslContext {
  private final SslContext ctx;
  
  protected DelegatingSslContext(SslContext ctx) {
    this.ctx = (SslContext)ObjectUtil.checkNotNull(ctx, "ctx");
  }
  
  public final boolean isClient() {
    return this.ctx.isClient();
  }
  
  public final List<String> cipherSuites() {
    return this.ctx.cipherSuites();
  }
  
  public final long sessionCacheSize() {
    return this.ctx.sessionCacheSize();
  }
  
  public final long sessionTimeout() {
    return this.ctx.sessionTimeout();
  }
  
  public final ApplicationProtocolNegotiator applicationProtocolNegotiator() {
    return this.ctx.applicationProtocolNegotiator();
  }
  
  public final SSLEngine newEngine(ByteBufAllocator alloc) {
    SSLEngine engine = this.ctx.newEngine(alloc);
    initEngine(engine);
    return engine;
  }
  
  public final SSLEngine newEngine(ByteBufAllocator alloc, String peerHost, int peerPort) {
    SSLEngine engine = this.ctx.newEngine(alloc, peerHost, peerPort);
    initEngine(engine);
    return engine;
  }
  
  protected final SslHandler newHandler(ByteBufAllocator alloc, boolean startTls) {
    SslHandler handler = this.ctx.newHandler(alloc, startTls);
    initHandler(handler);
    return handler;
  }
  
  protected final SslHandler newHandler(ByteBufAllocator alloc, String peerHost, int peerPort, boolean startTls) {
    SslHandler handler = this.ctx.newHandler(alloc, peerHost, peerPort, startTls);
    initHandler(handler);
    return handler;
  }
  
  public final SSLSessionContext sessionContext() {
    return this.ctx.sessionContext();
  }
  
  protected abstract void initEngine(SSLEngine paramSSLEngine);
  
  protected void initHandler(SslHandler handler) {
    initEngine(handler.engine());
  }
}
