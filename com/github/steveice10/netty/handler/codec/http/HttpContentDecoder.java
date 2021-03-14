package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.embedded.EmbeddedChannel;
import com.github.steveice10.netty.handler.codec.CodecException;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import java.util.List;

public abstract class HttpContentDecoder extends MessageToMessageDecoder<HttpObject> {
  static final String IDENTITY = HttpHeaderValues.IDENTITY.toString();
  
  protected ChannelHandlerContext ctx;
  
  private EmbeddedChannel decoder;
  
  private boolean continueResponse;
  
  protected void decode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
    if (msg instanceof HttpResponse && ((HttpResponse)msg).status().code() == 100) {
      if (!(msg instanceof LastHttpContent))
        this.continueResponse = true; 
      out.add(ReferenceCountUtil.retain(msg));
      return;
    } 
    if (this.continueResponse) {
      if (msg instanceof LastHttpContent)
        this.continueResponse = false; 
      out.add(ReferenceCountUtil.retain(msg));
      return;
    } 
    if (msg instanceof HttpMessage) {
      cleanup();
      HttpMessage message = (HttpMessage)msg;
      HttpHeaders headers = message.headers();
      String contentEncoding = headers.get((CharSequence)HttpHeaderNames.CONTENT_ENCODING);
      if (contentEncoding != null) {
        contentEncoding = contentEncoding.trim();
      } else {
        contentEncoding = IDENTITY;
      } 
      this.decoder = newContentDecoder(contentEncoding);
      if (this.decoder == null) {
        if (message instanceof HttpContent)
          ((HttpContent)message).retain(); 
        out.add(message);
        return;
      } 
      if (headers.contains((CharSequence)HttpHeaderNames.CONTENT_LENGTH)) {
        headers.remove((CharSequence)HttpHeaderNames.CONTENT_LENGTH);
        headers.set((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
      } 
      CharSequence targetContentEncoding = getTargetContentEncoding(contentEncoding);
      if (HttpHeaderValues.IDENTITY.contentEquals(targetContentEncoding)) {
        headers.remove((CharSequence)HttpHeaderNames.CONTENT_ENCODING);
      } else {
        headers.set((CharSequence)HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
      } 
      if (message instanceof HttpContent) {
        HttpMessage copy;
        if (message instanceof HttpRequest) {
          HttpRequest r = (HttpRequest)message;
          copy = new DefaultHttpRequest(r.protocolVersion(), r.method(), r.uri());
        } else if (message instanceof HttpResponse) {
          HttpResponse r = (HttpResponse)message;
          copy = new DefaultHttpResponse(r.protocolVersion(), r.status());
        } else {
          throw new CodecException("Object of class " + message.getClass().getName() + " is not a HttpRequest or HttpResponse");
        } 
        copy.headers().set(message.headers());
        copy.setDecoderResult(message.decoderResult());
        out.add(copy);
      } else {
        out.add(message);
      } 
    } 
    if (msg instanceof HttpContent) {
      HttpContent c = (HttpContent)msg;
      if (this.decoder == null) {
        out.add(c.retain());
      } else {
        decodeContent(c, out);
      } 
    } 
  }
  
  private void decodeContent(HttpContent c, List<Object> out) {
    ByteBuf content = c.content();
    decode(content, out);
    if (c instanceof LastHttpContent) {
      finishDecode(out);
      LastHttpContent last = (LastHttpContent)c;
      HttpHeaders headers = last.trailingHeaders();
      if (headers.isEmpty()) {
        out.add(LastHttpContent.EMPTY_LAST_CONTENT);
      } else {
        out.add(new ComposedLastHttpContent(headers));
      } 
    } 
  }
  
  protected String getTargetContentEncoding(String contentEncoding) throws Exception {
    return IDENTITY;
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    cleanupSafely(ctx);
    super.handlerRemoved(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    cleanupSafely(ctx);
    super.channelInactive(ctx);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
    super.handlerAdded(ctx);
  }
  
  private void cleanup() {
    if (this.decoder != null) {
      this.decoder.finishAndReleaseAll();
      this.decoder = null;
    } 
  }
  
  private void cleanupSafely(ChannelHandlerContext ctx) {
    try {
      cleanup();
    } catch (Throwable cause) {
      ctx.fireExceptionCaught(cause);
    } 
  }
  
  private void decode(ByteBuf in, List<Object> out) {
    this.decoder.writeInbound(new Object[] { in.retain() });
    fetchDecoderOutput(out);
  }
  
  private void finishDecode(List<Object> out) {
    if (this.decoder.finish())
      fetchDecoderOutput(out); 
    this.decoder = null;
  }
  
  private void fetchDecoderOutput(List<Object> out) {
    while (true) {
      ByteBuf buf = (ByteBuf)this.decoder.readInbound();
      if (buf == null)
        break; 
      if (!buf.isReadable()) {
        buf.release();
        continue;
      } 
      out.add(new DefaultHttpContent(buf));
    } 
  }
  
  protected abstract EmbeddedChannel newContentDecoder(String paramString) throws Exception;
}
