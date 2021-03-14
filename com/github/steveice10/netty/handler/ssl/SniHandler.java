package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.util.AsyncMapping;
import com.github.steveice10.netty.util.DomainNameMapping;
import com.github.steveice10.netty.util.Mapping;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.PlatformDependent;

public class SniHandler extends AbstractSniHandler<SslContext> {
  private static final Selection EMPTY_SELECTION = new Selection(null, null);
  
  protected final AsyncMapping<String, SslContext> mapping;
  
  private volatile Selection selection = EMPTY_SELECTION;
  
  public SniHandler(Mapping<? super String, ? extends SslContext> mapping) {
    this(new AsyncMappingAdapter(mapping, null));
  }
  
  public SniHandler(DomainNameMapping<? extends SslContext> mapping) {
    this((Mapping)mapping);
  }
  
  public SniHandler(AsyncMapping<? super String, ? extends SslContext> mapping) {
    this.mapping = (AsyncMapping<String, SslContext>)ObjectUtil.checkNotNull(mapping, "mapping");
  }
  
  public String hostname() {
    return this.selection.hostname;
  }
  
  public SslContext sslContext() {
    return this.selection.context;
  }
  
  protected Future<SslContext> lookup(ChannelHandlerContext ctx, String hostname) throws Exception {
    return this.mapping.map(hostname, ctx.executor().newPromise());
  }
  
  protected final void onLookupComplete(ChannelHandlerContext ctx, String hostname, Future<SslContext> future) throws Exception {
    if (!future.isSuccess()) {
      Throwable cause = future.cause();
      if (cause instanceof Error)
        throw (Error)cause; 
      throw new DecoderException("failed to get the SslContext for " + hostname, cause);
    } 
    SslContext sslContext = (SslContext)future.getNow();
    this.selection = new Selection(sslContext, hostname);
    try {
      replaceHandler(ctx, hostname, sslContext);
    } catch (Throwable cause) {
      this.selection = EMPTY_SELECTION;
      PlatformDependent.throwException(cause);
    } 
  }
  
  protected void replaceHandler(ChannelHandlerContext ctx, String hostname, SslContext sslContext) throws Exception {
    SslHandler sslHandler = null;
    try {
      sslHandler = sslContext.newHandler(ctx.alloc());
      ctx.pipeline().replace((ChannelHandler)this, SslHandler.class.getName(), (ChannelHandler)sslHandler);
      sslHandler = null;
    } finally {
      if (sslHandler != null)
        ReferenceCountUtil.safeRelease(sslHandler.engine()); 
    } 
  }
  
  private static final class AsyncMappingAdapter implements AsyncMapping<String, SslContext> {
    private final Mapping<? super String, ? extends SslContext> mapping;
    
    private AsyncMappingAdapter(Mapping<? super String, ? extends SslContext> mapping) {
      this.mapping = (Mapping<? super String, ? extends SslContext>)ObjectUtil.checkNotNull(mapping, "mapping");
    }
    
    public Future<SslContext> map(String input, Promise<SslContext> promise) {
      SslContext context;
      try {
        context = (SslContext)this.mapping.map(input);
      } catch (Throwable cause) {
        return (Future<SslContext>)promise.setFailure(cause);
      } 
      return (Future<SslContext>)promise.setSuccess(context);
    }
  }
  
  private static final class Selection {
    final SslContext context;
    
    final String hostname;
    
    Selection(SslContext context, String hostname) {
      this.context = context;
      this.hostname = hostname;
    }
  }
}
