package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.FileRegion;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class HttpObjectEncoder<H extends HttpMessage> extends MessageToMessageEncoder<Object> {
  static final int CRLF_SHORT = 3338;
  
  private static final int ZERO_CRLF_MEDIUM = 3149066;
  
  private static final byte[] ZERO_CRLF_CRLF = new byte[] { 48, 13, 10, 13, 10 };
  
  private static final ByteBuf CRLF_BUF = Unpooled.unreleasableBuffer(Unpooled.directBuffer(2).writeByte(13).writeByte(10));
  
  private static final ByteBuf ZERO_CRLF_CRLF_BUF = Unpooled.unreleasableBuffer(Unpooled.directBuffer(ZERO_CRLF_CRLF.length)
      .writeBytes(ZERO_CRLF_CRLF));
  
  private static final float HEADERS_WEIGHT_NEW = 0.2F;
  
  private static final float HEADERS_WEIGHT_HISTORICAL = 0.8F;
  
  private static final float TRAILERS_WEIGHT_NEW = 0.2F;
  
  private static final float TRAILERS_WEIGHT_HISTORICAL = 0.8F;
  
  private static final int ST_INIT = 0;
  
  private static final int ST_CONTENT_NON_CHUNK = 1;
  
  private static final int ST_CONTENT_CHUNK = 2;
  
  private static final int ST_CONTENT_ALWAYS_EMPTY = 3;
  
  private int state = 0;
  
  private float headersEncodedSizeAccumulator = 256.0F;
  
  private float trailersEncodedSizeAccumulator = 256.0F;
  
  protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
    ByteBuf buf = null;
    if (msg instanceof HttpMessage) {
      if (this.state != 0)
        throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg)); 
      HttpMessage httpMessage = (HttpMessage)msg;
      buf = ctx.alloc().buffer((int)this.headersEncodedSizeAccumulator);
      encodeInitialLine(buf, (H)httpMessage);
      this
        .state = isContentAlwaysEmpty((H)httpMessage) ? 3 : (HttpUtil.isTransferEncodingChunked(httpMessage) ? 2 : 1);
      sanitizeHeadersBeforeEncode((H)httpMessage, (this.state == 3));
      encodeHeaders(httpMessage.headers(), buf);
      ByteBufUtil.writeShortBE(buf, 3338);
      this.headersEncodedSizeAccumulator = 0.2F * padSizeForAccumulation(buf.readableBytes()) + 0.8F * this.headersEncodedSizeAccumulator;
    } 
    if (msg instanceof ByteBuf) {
      ByteBuf potentialEmptyBuf = (ByteBuf)msg;
      if (!potentialEmptyBuf.isReadable()) {
        out.add(potentialEmptyBuf.retain());
        return;
      } 
    } 
    if (msg instanceof HttpContent || msg instanceof ByteBuf || msg instanceof FileRegion) {
      long contentLength;
      switch (this.state) {
        case 0:
          throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
        case 1:
          contentLength = contentLength(msg);
          if (contentLength > 0L) {
            if (buf != null && buf.writableBytes() >= contentLength && msg instanceof HttpContent) {
              buf.writeBytes(((HttpContent)msg).content());
              out.add(buf);
            } else {
              if (buf != null)
                out.add(buf); 
              out.add(encodeAndRetain(msg));
            } 
            if (msg instanceof LastHttpContent)
              this.state = 0; 
            break;
          } 
        case 3:
          if (buf != null) {
            out.add(buf);
            break;
          } 
          out.add(Unpooled.EMPTY_BUFFER);
          break;
        case 2:
          if (buf != null)
            out.add(buf); 
          encodeChunkedContent(ctx, msg, contentLength(msg), out);
          break;
        default:
          throw new Error();
      } 
      if (msg instanceof LastHttpContent)
        this.state = 0; 
    } else if (buf != null) {
      out.add(buf);
    } 
  }
  
  protected void encodeHeaders(HttpHeaders headers, ByteBuf buf) {
    Iterator<Map.Entry<CharSequence, CharSequence>> iter = headers.iteratorCharSequence();
    while (iter.hasNext()) {
      Map.Entry<CharSequence, CharSequence> header = iter.next();
      HttpHeadersEncoder.encoderHeader(header.getKey(), header.getValue(), buf);
    } 
  }
  
  private void encodeChunkedContent(ChannelHandlerContext ctx, Object msg, long contentLength, List<Object> out) {
    if (contentLength > 0L) {
      String lengthHex = Long.toHexString(contentLength);
      ByteBuf buf = ctx.alloc().buffer(lengthHex.length() + 2);
      buf.writeCharSequence(lengthHex, CharsetUtil.US_ASCII);
      ByteBufUtil.writeShortBE(buf, 3338);
      out.add(buf);
      out.add(encodeAndRetain(msg));
      out.add(CRLF_BUF.duplicate());
    } 
    if (msg instanceof LastHttpContent) {
      HttpHeaders headers = ((LastHttpContent)msg).trailingHeaders();
      if (headers.isEmpty()) {
        out.add(ZERO_CRLF_CRLF_BUF.duplicate());
      } else {
        ByteBuf buf = ctx.alloc().buffer((int)this.trailersEncodedSizeAccumulator);
        ByteBufUtil.writeMediumBE(buf, 3149066);
        encodeHeaders(headers, buf);
        ByteBufUtil.writeShortBE(buf, 3338);
        this.trailersEncodedSizeAccumulator = 0.2F * padSizeForAccumulation(buf.readableBytes()) + 0.8F * this.trailersEncodedSizeAccumulator;
        out.add(buf);
      } 
    } else if (contentLength == 0L) {
      out.add(encodeAndRetain(msg));
    } 
  }
  
  protected void sanitizeHeadersBeforeEncode(H msg, boolean isAlwaysEmpty) {}
  
  protected boolean isContentAlwaysEmpty(H msg) {
    return false;
  }
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return (msg instanceof HttpObject || msg instanceof ByteBuf || msg instanceof FileRegion);
  }
  
  private static Object encodeAndRetain(Object msg) {
    if (msg instanceof ByteBuf)
      return ((ByteBuf)msg).retain(); 
    if (msg instanceof HttpContent)
      return ((HttpContent)msg).content().retain(); 
    if (msg instanceof FileRegion)
      return ((FileRegion)msg).retain(); 
    throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
  }
  
  private static long contentLength(Object msg) {
    if (msg instanceof HttpContent)
      return ((HttpContent)msg).content().readableBytes(); 
    if (msg instanceof ByteBuf)
      return ((ByteBuf)msg).readableBytes(); 
    if (msg instanceof FileRegion)
      return ((FileRegion)msg).count(); 
    throw new IllegalStateException("unexpected message type: " + StringUtil.simpleClassName(msg));
  }
  
  private static int padSizeForAccumulation(int readableBytes) {
    return (readableBytes << 2) / 3;
  }
  
  @Deprecated
  protected static void encodeAscii(String s, ByteBuf buf) {
    buf.writeCharSequence(s, CharsetUtil.US_ASCII);
  }
  
  protected abstract void encodeInitialLine(ByteBuf paramByteBuf, H paramH) throws Exception;
}
