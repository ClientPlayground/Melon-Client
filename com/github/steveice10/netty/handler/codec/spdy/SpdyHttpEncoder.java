package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageEncoder;
import com.github.steveice10.netty.handler.codec.UnsupportedMessageTypeException;
import com.github.steveice10.netty.handler.codec.http.FullHttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpContent;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaders;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpObject;
import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import com.github.steveice10.netty.handler.codec.http.HttpResponse;
import com.github.steveice10.netty.handler.codec.http.LastHttpContent;
import com.github.steveice10.netty.util.AsciiString;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SpdyHttpEncoder extends MessageToMessageEncoder<HttpObject> {
  private int currentStreamId;
  
  private final boolean validateHeaders;
  
  private final boolean headersToLowerCase;
  
  public SpdyHttpEncoder(SpdyVersion version) {
    this(version, true, true);
  }
  
  public SpdyHttpEncoder(SpdyVersion version, boolean headersToLowerCase, boolean validateHeaders) {
    if (version == null)
      throw new NullPointerException("version"); 
    this.headersToLowerCase = headersToLowerCase;
    this.validateHeaders = validateHeaders;
  }
  
  protected void encode(ChannelHandlerContext ctx, HttpObject msg, List<Object> out) throws Exception {
    boolean valid = false;
    boolean last = false;
    if (msg instanceof HttpRequest) {
      HttpRequest httpRequest = (HttpRequest)msg;
      SpdySynStreamFrame spdySynStreamFrame = createSynStreamFrame(httpRequest);
      out.add(spdySynStreamFrame);
      last = (spdySynStreamFrame.isLast() || spdySynStreamFrame.isUnidirectional());
      valid = true;
    } 
    if (msg instanceof HttpResponse) {
      HttpResponse httpResponse = (HttpResponse)msg;
      SpdyHeadersFrame spdyHeadersFrame = createHeadersFrame(httpResponse);
      out.add(spdyHeadersFrame);
      last = spdyHeadersFrame.isLast();
      valid = true;
    } 
    if (msg instanceof HttpContent && !last) {
      HttpContent chunk = (HttpContent)msg;
      chunk.content().retain();
      SpdyDataFrame spdyDataFrame = new DefaultSpdyDataFrame(this.currentStreamId, chunk.content());
      if (chunk instanceof LastHttpContent) {
        LastHttpContent trailer = (LastHttpContent)chunk;
        HttpHeaders trailers = trailer.trailingHeaders();
        if (trailers.isEmpty()) {
          spdyDataFrame.setLast(true);
          out.add(spdyDataFrame);
        } else {
          SpdyHeadersFrame spdyHeadersFrame = new DefaultSpdyHeadersFrame(this.currentStreamId, this.validateHeaders);
          spdyHeadersFrame.setLast(true);
          Iterator<Map.Entry<CharSequence, CharSequence>> itr = trailers.iteratorCharSequence();
          while (itr.hasNext()) {
            Map.Entry<CharSequence, CharSequence> entry = itr.next();
            CharSequence headerName = this.headersToLowerCase ? (CharSequence)AsciiString.of(entry.getKey()).toLowerCase() : entry.getKey();
            spdyHeadersFrame.headers().add(headerName, entry.getValue());
          } 
          out.add(spdyDataFrame);
          out.add(spdyHeadersFrame);
        } 
      } else {
        out.add(spdyDataFrame);
      } 
      valid = true;
    } 
    if (!valid)
      throw new UnsupportedMessageTypeException(msg, new Class[0]); 
  }
  
  private SpdySynStreamFrame createSynStreamFrame(HttpRequest httpRequest) throws Exception {
    HttpHeaders httpHeaders = httpRequest.headers();
    int streamId = httpHeaders.getInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID).intValue();
    int associatedToStreamId = httpHeaders.getInt((CharSequence)SpdyHttpHeaders.Names.ASSOCIATED_TO_STREAM_ID, 0);
    byte priority = (byte)httpHeaders.getInt((CharSequence)SpdyHttpHeaders.Names.PRIORITY, 0);
    CharSequence scheme = httpHeaders.get((CharSequence)SpdyHttpHeaders.Names.SCHEME);
    httpHeaders.remove((CharSequence)SpdyHttpHeaders.Names.STREAM_ID);
    httpHeaders.remove((CharSequence)SpdyHttpHeaders.Names.ASSOCIATED_TO_STREAM_ID);
    httpHeaders.remove((CharSequence)SpdyHttpHeaders.Names.PRIORITY);
    httpHeaders.remove((CharSequence)SpdyHttpHeaders.Names.SCHEME);
    httpHeaders.remove((CharSequence)HttpHeaderNames.CONNECTION);
    httpHeaders.remove("Keep-Alive");
    httpHeaders.remove("Proxy-Connection");
    httpHeaders.remove((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
    SpdySynStreamFrame spdySynStreamFrame = new DefaultSpdySynStreamFrame(streamId, associatedToStreamId, priority, this.validateHeaders);
    SpdyHeaders frameHeaders = spdySynStreamFrame.headers();
    frameHeaders.set(SpdyHeaders.HttpNames.METHOD, httpRequest.method().name());
    frameHeaders.set(SpdyHeaders.HttpNames.PATH, httpRequest.uri());
    frameHeaders.set(SpdyHeaders.HttpNames.VERSION, httpRequest.protocolVersion().text());
    CharSequence host = httpHeaders.get((CharSequence)HttpHeaderNames.HOST);
    httpHeaders.remove((CharSequence)HttpHeaderNames.HOST);
    frameHeaders.set(SpdyHeaders.HttpNames.HOST, host);
    if (scheme == null)
      scheme = "https"; 
    frameHeaders.set(SpdyHeaders.HttpNames.SCHEME, scheme);
    Iterator<Map.Entry<CharSequence, CharSequence>> itr = httpHeaders.iteratorCharSequence();
    while (itr.hasNext()) {
      Map.Entry<CharSequence, CharSequence> entry = itr.next();
      CharSequence headerName = this.headersToLowerCase ? (CharSequence)AsciiString.of(entry.getKey()).toLowerCase() : entry.getKey();
      frameHeaders.add(headerName, entry.getValue());
    } 
    this.currentStreamId = spdySynStreamFrame.streamId();
    if (associatedToStreamId == 0) {
      spdySynStreamFrame.setLast(isLast((HttpMessage)httpRequest));
    } else {
      spdySynStreamFrame.setUnidirectional(true);
    } 
    return spdySynStreamFrame;
  }
  
  private SpdyHeadersFrame createHeadersFrame(HttpResponse httpResponse) throws Exception {
    SpdyHeadersFrame spdyHeadersFrame;
    HttpHeaders httpHeaders = httpResponse.headers();
    int streamId = httpHeaders.getInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID).intValue();
    httpHeaders.remove((CharSequence)SpdyHttpHeaders.Names.STREAM_ID);
    httpHeaders.remove((CharSequence)HttpHeaderNames.CONNECTION);
    httpHeaders.remove("Keep-Alive");
    httpHeaders.remove("Proxy-Connection");
    httpHeaders.remove((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
    if (SpdyCodecUtil.isServerId(streamId)) {
      spdyHeadersFrame = new DefaultSpdyHeadersFrame(streamId, this.validateHeaders);
    } else {
      spdyHeadersFrame = new DefaultSpdySynReplyFrame(streamId, this.validateHeaders);
    } 
    SpdyHeaders frameHeaders = spdyHeadersFrame.headers();
    frameHeaders.set(SpdyHeaders.HttpNames.STATUS, httpResponse.status().codeAsText());
    frameHeaders.set(SpdyHeaders.HttpNames.VERSION, httpResponse.protocolVersion().text());
    Iterator<Map.Entry<CharSequence, CharSequence>> itr = httpHeaders.iteratorCharSequence();
    while (itr.hasNext()) {
      Map.Entry<CharSequence, CharSequence> entry = itr.next();
      CharSequence headerName = this.headersToLowerCase ? (CharSequence)AsciiString.of(entry.getKey()).toLowerCase() : entry.getKey();
      spdyHeadersFrame.headers().add(headerName, entry.getValue());
    } 
    this.currentStreamId = streamId;
    spdyHeadersFrame.setLast(isLast((HttpMessage)httpResponse));
    return spdyHeadersFrame;
  }
  
  private static boolean isLast(HttpMessage httpMessage) {
    if (httpMessage instanceof FullHttpMessage) {
      FullHttpMessage fullMessage = (FullHttpMessage)httpMessage;
      if (fullMessage.trailingHeaders().isEmpty() && !fullMessage.content().isReadable())
        return true; 
    } 
    return false;
  }
}
