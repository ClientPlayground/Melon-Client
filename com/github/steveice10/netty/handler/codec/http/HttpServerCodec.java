package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandler;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.CombinedChannelDuplexHandler;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public final class HttpServerCodec extends CombinedChannelDuplexHandler<HttpRequestDecoder, HttpResponseEncoder> implements HttpServerUpgradeHandler.SourceCodec {
  private final Queue<HttpMethod> queue = new ArrayDeque<HttpMethod>();
  
  public HttpServerCodec() {
    this(4096, 8192, 8192);
  }
  
  public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
    init((ChannelInboundHandler)new HttpServerRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize), (ChannelOutboundHandler)new HttpServerResponseEncoder());
  }
  
  public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
    init((ChannelInboundHandler)new HttpServerRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders), (ChannelOutboundHandler)new HttpServerResponseEncoder());
  }
  
  public HttpServerCodec(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders, int initialBufferSize) {
    init((ChannelInboundHandler)new HttpServerRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize), (ChannelOutboundHandler)new HttpServerResponseEncoder());
  }
  
  public void upgradeFrom(ChannelHandlerContext ctx) {
    ctx.pipeline().remove((ChannelHandler)this);
  }
  
  private final class HttpServerRequestDecoder extends HttpRequestDecoder {
    public HttpServerRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
      super(maxInitialLineLength, maxHeaderSize, maxChunkSize);
    }
    
    public HttpServerRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders) {
      super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders);
    }
    
    public HttpServerRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize, boolean validateHeaders, int initialBufferSize) {
      super(maxInitialLineLength, maxHeaderSize, maxChunkSize, validateHeaders, initialBufferSize);
    }
    
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
      int oldSize = out.size();
      super.decode(ctx, buffer, out);
      int size = out.size();
      for (int i = oldSize; i < size; i++) {
        Object obj = out.get(i);
        if (obj instanceof HttpRequest)
          HttpServerCodec.this.queue.add(((HttpRequest)obj).method()); 
      } 
    }
  }
  
  private final class HttpServerResponseEncoder extends HttpResponseEncoder {
    private HttpMethod method;
    
    private HttpServerResponseEncoder() {}
    
    protected void sanitizeHeadersBeforeEncode(HttpResponse msg, boolean isAlwaysEmpty) {
      if (!isAlwaysEmpty && this.method == HttpMethod.CONNECT && msg.status().codeClass() == HttpStatusClass.SUCCESS) {
        msg.headers().remove((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
        return;
      } 
      super.sanitizeHeadersBeforeEncode(msg, isAlwaysEmpty);
    }
    
    protected boolean isContentAlwaysEmpty(HttpResponse msg) {
      this.method = HttpServerCodec.this.queue.poll();
      return (HttpMethod.HEAD.equals(this.method) || super.isContentAlwaysEmpty(msg));
    }
  }
}
