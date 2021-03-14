package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;

public class HttpServerKeepAliveHandler extends ChannelDuplexHandler {
  private static final String MULTIPART_PREFIX = "multipart";
  
  private boolean persistentConnection = true;
  
  private int pendingResponses;
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof HttpRequest) {
      HttpRequest request = (HttpRequest)msg;
      if (this.persistentConnection) {
        this.pendingResponses++;
        this.persistentConnection = HttpUtil.isKeepAlive(request);
      } 
    } 
    super.channelRead(ctx, msg);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof HttpResponse) {
      HttpResponse response = (HttpResponse)msg;
      trackResponse(response);
      if (!HttpUtil.isKeepAlive(response) || !isSelfDefinedMessageLength(response)) {
        this.pendingResponses = 0;
        this.persistentConnection = false;
      } 
      if (!shouldKeepAlive())
        HttpUtil.setKeepAlive(response, false); 
    } 
    if (msg instanceof LastHttpContent && !shouldKeepAlive())
      promise = promise.unvoid().addListener((GenericFutureListener)ChannelFutureListener.CLOSE); 
    super.write(ctx, msg, promise);
  }
  
  private void trackResponse(HttpResponse response) {
    if (!isInformational(response))
      this.pendingResponses--; 
  }
  
  private boolean shouldKeepAlive() {
    return (this.pendingResponses != 0 || this.persistentConnection);
  }
  
  private static boolean isSelfDefinedMessageLength(HttpResponse response) {
    return (HttpUtil.isContentLengthSet(response) || HttpUtil.isTransferEncodingChunked(response) || isMultipart(response) || 
      isInformational(response) || response.status().code() == HttpResponseStatus.NO_CONTENT.code());
  }
  
  private static boolean isInformational(HttpResponse response) {
    return (response.status().codeClass() == HttpStatusClass.INFORMATIONAL);
  }
  
  private static boolean isMultipart(HttpResponse response) {
    String contentType = response.headers().get((CharSequence)HttpHeaderNames.CONTENT_TYPE);
    return (contentType != null && contentType
      .regionMatches(true, 0, "multipart", 0, "multipart".length()));
  }
}
