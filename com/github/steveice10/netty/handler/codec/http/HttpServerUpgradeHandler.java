package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.ReferenceCounted;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HttpServerUpgradeHandler extends HttpObjectAggregator {
  private final SourceCodec sourceCodec;
  
  private final UpgradeCodecFactory upgradeCodecFactory;
  
  private boolean handlingUpgrade;
  
  public static final class UpgradeEvent implements ReferenceCounted {
    private final CharSequence protocol;
    
    private final FullHttpRequest upgradeRequest;
    
    UpgradeEvent(CharSequence protocol, FullHttpRequest upgradeRequest) {
      this.protocol = protocol;
      this.upgradeRequest = upgradeRequest;
    }
    
    public CharSequence protocol() {
      return this.protocol;
    }
    
    public FullHttpRequest upgradeRequest() {
      return this.upgradeRequest;
    }
    
    public int refCnt() {
      return this.upgradeRequest.refCnt();
    }
    
    public UpgradeEvent retain() {
      this.upgradeRequest.retain();
      return this;
    }
    
    public UpgradeEvent retain(int increment) {
      this.upgradeRequest.retain(increment);
      return this;
    }
    
    public UpgradeEvent touch() {
      this.upgradeRequest.touch();
      return this;
    }
    
    public UpgradeEvent touch(Object hint) {
      this.upgradeRequest.touch(hint);
      return this;
    }
    
    public boolean release() {
      return this.upgradeRequest.release();
    }
    
    public boolean release(int decrement) {
      return this.upgradeRequest.release(decrement);
    }
    
    public String toString() {
      return "UpgradeEvent [protocol=" + this.protocol + ", upgradeRequest=" + this.upgradeRequest + ']';
    }
  }
  
  public HttpServerUpgradeHandler(SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory) {
    this(sourceCodec, upgradeCodecFactory, 0);
  }
  
  public HttpServerUpgradeHandler(SourceCodec sourceCodec, UpgradeCodecFactory upgradeCodecFactory, int maxContentLength) {
    super(maxContentLength);
    this.sourceCodec = (SourceCodec)ObjectUtil.checkNotNull(sourceCodec, "sourceCodec");
    this.upgradeCodecFactory = (UpgradeCodecFactory)ObjectUtil.checkNotNull(upgradeCodecFactory, "upgradeCodecFactory");
  }
  
  protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
    FullHttpRequest fullRequest;
    this.handlingUpgrade |= isUpgradeRequest(msg);
    if (!this.handlingUpgrade) {
      ReferenceCountUtil.retain(msg);
      out.add(msg);
      return;
    } 
    if (msg instanceof FullHttpRequest) {
      fullRequest = (FullHttpRequest)msg;
      ReferenceCountUtil.retain(msg);
      out.add(msg);
    } else {
      super.decode(ctx, msg, out);
      if (out.isEmpty())
        return; 
      assert out.size() == 1;
      this.handlingUpgrade = false;
      fullRequest = (FullHttpRequest)out.get(0);
    } 
    if (upgrade(ctx, fullRequest))
      out.clear(); 
  }
  
  private static boolean isUpgradeRequest(HttpObject msg) {
    return (msg instanceof HttpRequest && ((HttpRequest)msg).headers().get((CharSequence)HttpHeaderNames.UPGRADE) != null);
  }
  
  private boolean upgrade(ChannelHandlerContext ctx, FullHttpRequest request) {
    List<CharSequence> requestedProtocols = splitHeader(request.headers().get((CharSequence)HttpHeaderNames.UPGRADE));
    int numRequestedProtocols = requestedProtocols.size();
    UpgradeCodec upgradeCodec = null;
    CharSequence upgradeProtocol = null;
    for (int i = 0; i < numRequestedProtocols; i++) {
      CharSequence p = requestedProtocols.get(i);
      UpgradeCodec c = this.upgradeCodecFactory.newUpgradeCodec(p);
      if (c != null) {
        upgradeProtocol = p;
        upgradeCodec = c;
        break;
      } 
    } 
    if (upgradeCodec == null)
      return false; 
    CharSequence connectionHeader = request.headers().get((CharSequence)HttpHeaderNames.CONNECTION);
    if (connectionHeader == null)
      return false; 
    Collection<CharSequence> requiredHeaders = upgradeCodec.requiredUpgradeHeaders();
    List<CharSequence> values = splitHeader(connectionHeader);
    if (!AsciiString.containsContentEqualsIgnoreCase(values, (CharSequence)HttpHeaderNames.UPGRADE) || 
      !AsciiString.containsAllContentEqualsIgnoreCase(values, requiredHeaders))
      return false; 
    for (CharSequence requiredHeader : requiredHeaders) {
      if (!request.headers().contains(requiredHeader))
        return false; 
    } 
    FullHttpResponse upgradeResponse = createUpgradeResponse(upgradeProtocol);
    if (!upgradeCodec.prepareUpgradeResponse(ctx, request, upgradeResponse.headers()))
      return false; 
    UpgradeEvent event = new UpgradeEvent(upgradeProtocol, request);
    try {
      ChannelFuture writeComplete = ctx.writeAndFlush(upgradeResponse);
      this.sourceCodec.upgradeFrom(ctx);
      upgradeCodec.upgradeTo(ctx, request);
      ctx.pipeline().remove((ChannelHandler)this);
      ctx.fireUserEventTriggered(event.retain());
      writeComplete.addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE);
    } finally {
      event.release();
    } 
    return true;
  }
  
  private static FullHttpResponse createUpgradeResponse(CharSequence upgradeProtocol) {
    DefaultFullHttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SWITCHING_PROTOCOLS, Unpooled.EMPTY_BUFFER, false);
    res.headers().add((CharSequence)HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE);
    res.headers().add((CharSequence)HttpHeaderNames.UPGRADE, upgradeProtocol);
    return res;
  }
  
  private static List<CharSequence> splitHeader(CharSequence header) {
    StringBuilder builder = new StringBuilder(header.length());
    List<CharSequence> protocols = new ArrayList<CharSequence>(4);
    for (int i = 0; i < header.length(); i++) {
      char c = header.charAt(i);
      if (!Character.isWhitespace(c))
        if (c == ',') {
          protocols.add(builder.toString());
          builder.setLength(0);
        } else {
          builder.append(c);
        }  
    } 
    if (builder.length() > 0)
      protocols.add(builder.toString()); 
    return protocols;
  }
  
  public static interface UpgradeCodecFactory {
    HttpServerUpgradeHandler.UpgradeCodec newUpgradeCodec(CharSequence param1CharSequence);
  }
  
  public static interface UpgradeCodec {
    Collection<CharSequence> requiredUpgradeHeaders();
    
    boolean prepareUpgradeResponse(ChannelHandlerContext param1ChannelHandlerContext, FullHttpRequest param1FullHttpRequest, HttpHeaders param1HttpHeaders);
    
    void upgradeTo(ChannelHandlerContext param1ChannelHandlerContext, FullHttpRequest param1FullHttpRequest);
  }
  
  public static interface SourceCodec {
    void upgradeFrom(ChannelHandlerContext param1ChannelHandlerContext);
  }
}
