package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HttpClientUpgradeHandler extends HttpObjectAggregator implements ChannelOutboundHandler {
  private final SourceCodec sourceCodec;
  
  private final UpgradeCodec upgradeCodec;
  
  private boolean upgradeRequested;
  
  public static interface UpgradeCodec {
    CharSequence protocol();
    
    Collection<CharSequence> setUpgradeHeaders(ChannelHandlerContext param1ChannelHandlerContext, HttpRequest param1HttpRequest);
    
    void upgradeTo(ChannelHandlerContext param1ChannelHandlerContext, FullHttpResponse param1FullHttpResponse) throws Exception;
  }
  
  public static interface SourceCodec {
    void prepareUpgradeFrom(ChannelHandlerContext param1ChannelHandlerContext);
    
    void upgradeFrom(ChannelHandlerContext param1ChannelHandlerContext);
  }
  
  public enum UpgradeEvent {
    UPGRADE_ISSUED, UPGRADE_SUCCESSFUL, UPGRADE_REJECTED;
  }
  
  public HttpClientUpgradeHandler(SourceCodec sourceCodec, UpgradeCodec upgradeCodec, int maxContentLength) {
    super(maxContentLength);
    if (sourceCodec == null)
      throw new NullPointerException("sourceCodec"); 
    if (upgradeCodec == null)
      throw new NullPointerException("upgradeCodec"); 
    this.sourceCodec = sourceCodec;
    this.upgradeCodec = upgradeCodec;
  }
  
  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    ctx.bind(localAddress, promise);
  }
  
  public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    ctx.connect(remoteAddress, localAddress, promise);
  }
  
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.disconnect(promise);
  }
  
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.close(promise);
  }
  
  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.deregister(promise);
  }
  
  public void read(ChannelHandlerContext ctx) throws Exception {
    ctx.read();
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (!(msg instanceof HttpRequest)) {
      ctx.write(msg, promise);
      return;
    } 
    if (this.upgradeRequested) {
      promise.setFailure(new IllegalStateException("Attempting to write HTTP request with upgrade in progress"));
      return;
    } 
    this.upgradeRequested = true;
    setUpgradeRequestHeaders(ctx, (HttpRequest)msg);
    ctx.write(msg, promise);
    ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_ISSUED);
  }
  
  public void flush(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }
  
  protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
    FullHttpResponse response = null;
    try {
      if (!this.upgradeRequested)
        throw new IllegalStateException("Read HTTP response without requesting protocol switch"); 
      if (msg instanceof HttpResponse) {
        HttpResponse rep = (HttpResponse)msg;
        if (!HttpResponseStatus.SWITCHING_PROTOCOLS.equals(rep.status())) {
          ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_REJECTED);
          removeThisHandler(ctx);
          ctx.fireChannelRead(msg);
          return;
        } 
      } 
      if (msg instanceof FullHttpResponse) {
        response = (FullHttpResponse)msg;
        response.retain();
        out.add(response);
      } else {
        super.decode(ctx, msg, out);
        if (out.isEmpty())
          return; 
        assert out.size() == 1;
        response = (FullHttpResponse)out.get(0);
      } 
      CharSequence upgradeHeader = response.headers().get((CharSequence)HttpHeaderNames.UPGRADE);
      if (upgradeHeader != null && !AsciiString.contentEqualsIgnoreCase(this.upgradeCodec.protocol(), upgradeHeader))
        throw new IllegalStateException("Switching Protocols response with unexpected UPGRADE protocol: " + upgradeHeader); 
      this.sourceCodec.prepareUpgradeFrom(ctx);
      this.upgradeCodec.upgradeTo(ctx, response);
      ctx.fireUserEventTriggered(UpgradeEvent.UPGRADE_SUCCESSFUL);
      this.sourceCodec.upgradeFrom(ctx);
      response.release();
      out.clear();
      removeThisHandler(ctx);
    } catch (Throwable t) {
      ReferenceCountUtil.release(response);
      ctx.fireExceptionCaught(t);
      removeThisHandler(ctx);
    } 
  }
  
  private static void removeThisHandler(ChannelHandlerContext ctx) {
    ctx.pipeline().remove(ctx.name());
  }
  
  private void setUpgradeRequestHeaders(ChannelHandlerContext ctx, HttpRequest request) {
    request.headers().set((CharSequence)HttpHeaderNames.UPGRADE, this.upgradeCodec.protocol());
    Set<CharSequence> connectionParts = new LinkedHashSet<CharSequence>(2);
    connectionParts.addAll(this.upgradeCodec.setUpgradeHeaders(ctx, request));
    StringBuilder builder = new StringBuilder();
    for (CharSequence part : connectionParts) {
      builder.append(part);
      builder.append(',');
    } 
    builder.append((CharSequence)HttpHeaderValues.UPGRADE);
    request.headers().add((CharSequence)HttpHeaderNames.CONNECTION, builder.toString());
  }
}
