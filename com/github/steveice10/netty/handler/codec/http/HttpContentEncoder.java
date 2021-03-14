package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.embedded.EmbeddedChannel;
import com.github.steveice10.netty.handler.codec.MessageToMessageCodec;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public abstract class HttpContentEncoder extends MessageToMessageCodec<HttpRequest, HttpObject> {
  private enum State {
    PASS_THROUGH, AWAIT_HEADERS, AWAIT_CONTENT;
  }
  
  private static final CharSequence ZERO_LENGTH_HEAD = "HEAD";
  
  private static final CharSequence ZERO_LENGTH_CONNECT = "CONNECT";
  
  private static final int CONTINUE_CODE = HttpResponseStatus.CONTINUE.code();
  
  private final Queue<CharSequence> acceptEncodingQueue = new ArrayDeque<CharSequence>();
  
  private EmbeddedChannel encoder;
  
  private State state = State.AWAIT_HEADERS;
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return (msg instanceof HttpContent || msg instanceof HttpResponse);
  }
  
  protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) throws Exception {
    CharSequence acceptedEncoding = msg.headers().get((CharSequence)HttpHeaderNames.ACCEPT_ENCODING);
    if (acceptedEncoding == null)
      acceptedEncoding = HttpContentDecoder.IDENTITY; 
    HttpMethod meth = msg.method();
    if (meth == HttpMethod.HEAD) {
      acceptedEncoding = ZERO_LENGTH_HEAD;
    } else if (meth == HttpMethod.CONNECT) {
      acceptedEncoding = ZERO_LENGTH_CONNECT;
    } 
    this.acceptEncodingQueue.add(acceptedEncoding);
    out.add(ReferenceCountUtil.retain(msg));
  }
  
  protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
    HttpResponse res;
    int code;
    CharSequence acceptEncoding;
    Result result;
    boolean isFull = (msg instanceof HttpResponse && msg instanceof LastHttpContent);
    switch (this.state) {
      case AWAIT_HEADERS:
        ensureHeaders(msg);
        assert this.encoder == null;
        res = (HttpResponse)msg;
        code = res.status().code();
        if (code == CONTINUE_CODE) {
          acceptEncoding = null;
        } else {
          acceptEncoding = this.acceptEncodingQueue.poll();
          if (acceptEncoding == null)
            throw new IllegalStateException("cannot send more responses than requests"); 
        } 
        if (isPassthru(res.protocolVersion(), code, acceptEncoding)) {
          if (isFull) {
            out.add(ReferenceCountUtil.retain(res));
            break;
          } 
          out.add(res);
          this.state = State.PASS_THROUGH;
          break;
        } 
        if (isFull)
          if (!((ByteBufHolder)res).content().isReadable()) {
            out.add(ReferenceCountUtil.retain(res));
            break;
          }  
        result = beginEncode(res, acceptEncoding.toString());
        if (result == null) {
          if (isFull) {
            out.add(ReferenceCountUtil.retain(res));
            break;
          } 
          out.add(res);
          this.state = State.PASS_THROUGH;
          break;
        } 
        this.encoder = result.contentEncoder();
        res.headers().set((CharSequence)HttpHeaderNames.CONTENT_ENCODING, result.targetContentEncoding());
        if (isFull) {
          HttpResponse newRes = new DefaultHttpResponse(res.protocolVersion(), res.status());
          newRes.headers().set(res.headers());
          out.add(newRes);
          ensureContent(res);
          encodeFullResponse(newRes, (HttpContent)res, out);
          break;
        } 
        res.headers().remove((CharSequence)HttpHeaderNames.CONTENT_LENGTH);
        res.headers().set((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        out.add(res);
        this.state = State.AWAIT_CONTENT;
        if (!(msg instanceof HttpContent))
          break; 
      case AWAIT_CONTENT:
        ensureContent(msg);
        if (encodeContent((HttpContent)msg, out))
          this.state = State.AWAIT_HEADERS; 
        break;
      case PASS_THROUGH:
        ensureContent(msg);
        out.add(ReferenceCountUtil.retain(msg));
        if (msg instanceof LastHttpContent)
          this.state = State.AWAIT_HEADERS; 
        break;
    } 
  }
  
  private void encodeFullResponse(HttpResponse newRes, HttpContent content, List<Object> out) {
    int existingMessages = out.size();
    encodeContent(content, out);
    if (HttpUtil.isContentLengthSet(newRes)) {
      int messageSize = 0;
      for (int i = existingMessages; i < out.size(); i++) {
        Object item = out.get(i);
        if (item instanceof HttpContent)
          messageSize += ((HttpContent)item).content().readableBytes(); 
      } 
      HttpUtil.setContentLength(newRes, messageSize);
    } else {
      newRes.headers().set((CharSequence)HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
    } 
  }
  
  private static boolean isPassthru(HttpVersion version, int code, CharSequence httpMethod) {
    return (code < 200 || code == 204 || code == 304 || httpMethod == ZERO_LENGTH_HEAD || (httpMethod == ZERO_LENGTH_CONNECT && code == 200) || version == HttpVersion.HTTP_1_0);
  }
  
  private static void ensureHeaders(HttpObject msg) {
    if (!(msg instanceof HttpResponse))
      throw new IllegalStateException("unexpected message type: " + msg
          
          .getClass().getName() + " (expected: " + HttpResponse.class.getSimpleName() + ')'); 
  }
  
  private static void ensureContent(HttpObject msg) {
    if (!(msg instanceof HttpContent))
      throw new IllegalStateException("unexpected message type: " + msg
          
          .getClass().getName() + " (expected: " + HttpContent.class.getSimpleName() + ')'); 
  }
  
  private boolean encodeContent(HttpContent c, List<Object> out) {
    ByteBuf content = c.content();
    encode(content, out);
    if (c instanceof LastHttpContent) {
      finishEncode(out);
      LastHttpContent last = (LastHttpContent)c;
      HttpHeaders headers = last.trailingHeaders();
      if (headers.isEmpty()) {
        out.add(LastHttpContent.EMPTY_LAST_CONTENT);
      } else {
        out.add(new ComposedLastHttpContent(headers));
      } 
      return true;
    } 
    return false;
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    cleanupSafely(ctx);
    super.handlerRemoved(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    cleanupSafely(ctx);
    super.channelInactive(ctx);
  }
  
  private void cleanup() {
    if (this.encoder != null) {
      this.encoder.finishAndReleaseAll();
      this.encoder = null;
    } 
  }
  
  private void cleanupSafely(ChannelHandlerContext ctx) {
    try {
      cleanup();
    } catch (Throwable cause) {
      ctx.fireExceptionCaught(cause);
    } 
  }
  
  private void encode(ByteBuf in, List<Object> out) {
    this.encoder.writeOutbound(new Object[] { in.retain() });
    fetchEncoderOutput(out);
  }
  
  private void finishEncode(List<Object> out) {
    if (this.encoder.finish())
      fetchEncoderOutput(out); 
    this.encoder = null;
  }
  
  private void fetchEncoderOutput(List<Object> out) {
    while (true) {
      ByteBuf buf = (ByteBuf)this.encoder.readOutbound();
      if (buf == null)
        break; 
      if (!buf.isReadable()) {
        buf.release();
        continue;
      } 
      out.add(new DefaultHttpContent(buf));
    } 
  }
  
  protected abstract Result beginEncode(HttpResponse paramHttpResponse, String paramString) throws Exception;
  
  public static final class Result {
    private final String targetContentEncoding;
    
    private final EmbeddedChannel contentEncoder;
    
    public Result(String targetContentEncoding, EmbeddedChannel contentEncoder) {
      if (targetContentEncoding == null)
        throw new NullPointerException("targetContentEncoding"); 
      if (contentEncoder == null)
        throw new NullPointerException("contentEncoder"); 
      this.targetContentEncoding = targetContentEncoding;
      this.contentEncoder = contentEncoder;
    }
    
    public String targetContentEncoding() {
      return this.targetContentEncoding;
    }
    
    public EmbeddedChannel contentEncoder() {
      return this.contentEncoder;
    }
  }
}
