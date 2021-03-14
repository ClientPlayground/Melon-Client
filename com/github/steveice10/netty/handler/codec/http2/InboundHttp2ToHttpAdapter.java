package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.http.FullHttpMessage;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpStatusClass;
import com.github.steveice10.netty.handler.codec.http.HttpUtil;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class InboundHttp2ToHttpAdapter extends Http2EventAdapter {
  private static final ImmediateSendDetector DEFAULT_SEND_DETECTOR = new ImmediateSendDetector() {
      public boolean mustSendImmediately(FullHttpMessage msg) {
        if (msg instanceof FullHttpResponse)
          return (((FullHttpResponse)msg).status().codeClass() == HttpStatusClass.INFORMATIONAL); 
        if (msg instanceof FullHttpRequest)
          return msg.headers().contains((CharSequence)HttpHeaderNames.EXPECT); 
        return false;
      }
      
      public FullHttpMessage copyIfNeeded(FullHttpMessage msg) {
        if (msg instanceof FullHttpRequest) {
          FullHttpRequest copy = ((FullHttpRequest)msg).replace(Unpooled.buffer(0));
          copy.headers().remove((CharSequence)HttpHeaderNames.EXPECT);
          return (FullHttpMessage)copy;
        } 
        return null;
      }
    };
  
  private final int maxContentLength;
  
  private final ImmediateSendDetector sendDetector;
  
  private final Http2Connection.PropertyKey messageKey;
  
  private final boolean propagateSettings;
  
  protected final Http2Connection connection;
  
  protected final boolean validateHttpHeaders;
  
  private static interface ImmediateSendDetector {
    boolean mustSendImmediately(FullHttpMessage param1FullHttpMessage);
    
    FullHttpMessage copyIfNeeded(FullHttpMessage param1FullHttpMessage);
  }
  
  protected InboundHttp2ToHttpAdapter(Http2Connection connection, int maxContentLength, boolean validateHttpHeaders, boolean propagateSettings) {
    ObjectUtil.checkNotNull(connection, "connection");
    if (maxContentLength <= 0)
      throw new IllegalArgumentException("maxContentLength: " + maxContentLength + " (expected: > 0)"); 
    this.connection = connection;
    this.maxContentLength = maxContentLength;
    this.validateHttpHeaders = validateHttpHeaders;
    this.propagateSettings = propagateSettings;
    this.sendDetector = DEFAULT_SEND_DETECTOR;
    this.messageKey = connection.newKey();
  }
  
  protected final void removeMessage(Http2Stream stream, boolean release) {
    FullHttpMessage msg = stream.<FullHttpMessage>removeProperty(this.messageKey);
    if (release && msg != null)
      msg.release(); 
  }
  
  protected final FullHttpMessage getMessage(Http2Stream stream) {
    return stream.<FullHttpMessage>getProperty(this.messageKey);
  }
  
  protected final void putMessage(Http2Stream stream, FullHttpMessage message) {
    FullHttpMessage previous = stream.<FullHttpMessage>setProperty(this.messageKey, message);
    if (previous != message && previous != null)
      previous.release(); 
  }
  
  public void onStreamRemoved(Http2Stream stream) {
    removeMessage(stream, true);
  }
  
  protected void fireChannelRead(ChannelHandlerContext ctx, FullHttpMessage msg, boolean release, Http2Stream stream) {
    removeMessage(stream, release);
    HttpUtil.setContentLength((HttpMessage)msg, msg.content().readableBytes());
    ctx.fireChannelRead(msg);
  }
  
  protected FullHttpMessage newMessage(Http2Stream stream, Http2Headers headers, boolean validateHttpHeaders, ByteBufAllocator alloc) throws Http2Exception {
    return this.connection.isServer() ? (FullHttpMessage)HttpConversionUtil.toFullHttpRequest(stream.id(), headers, alloc, validateHttpHeaders) : 
      (FullHttpMessage)HttpConversionUtil.toFullHttpResponse(stream.id(), headers, alloc, validateHttpHeaders);
  }
  
  protected FullHttpMessage processHeadersBegin(ChannelHandlerContext ctx, Http2Stream stream, Http2Headers headers, boolean endOfStream, boolean allowAppend, boolean appendToTrailer) throws Http2Exception {
    FullHttpMessage msg = getMessage(stream);
    boolean release = true;
    if (msg == null) {
      msg = newMessage(stream, headers, this.validateHttpHeaders, ctx.alloc());
    } else if (allowAppend) {
      release = false;
      HttpConversionUtil.addHttp2ToHttpHeaders(stream.id(), headers, msg, appendToTrailer);
    } else {
      release = false;
      msg = null;
    } 
    if (this.sendDetector.mustSendImmediately(msg)) {
      FullHttpMessage copy = endOfStream ? null : this.sendDetector.copyIfNeeded(msg);
      fireChannelRead(ctx, msg, release, stream);
      return copy;
    } 
    return msg;
  }
  
  private void processHeadersEnd(ChannelHandlerContext ctx, Http2Stream stream, FullHttpMessage msg, boolean endOfStream) {
    if (endOfStream) {
      fireChannelRead(ctx, msg, (getMessage(stream) != msg), stream);
    } else {
      putMessage(stream, msg);
    } 
  }
  
  public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
    Http2Stream stream = this.connection.stream(streamId);
    FullHttpMessage msg = getMessage(stream);
    if (msg == null)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Data Frame received for unknown stream id %d", new Object[] { Integer.valueOf(streamId) }); 
    ByteBuf content = msg.content();
    int dataReadableBytes = data.readableBytes();
    if (content.readableBytes() > this.maxContentLength - dataReadableBytes)
      throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "Content length exceeded max of %d for stream id %d", new Object[] { Integer.valueOf(this.maxContentLength), Integer.valueOf(streamId) }); 
    content.writeBytes(data, data.readerIndex(), dataReadableBytes);
    if (endOfStream)
      fireChannelRead(ctx, msg, false, stream); 
    return dataReadableBytes + padding;
  }
  
  public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endOfStream) throws Http2Exception {
    Http2Stream stream = this.connection.stream(streamId);
    FullHttpMessage msg = processHeadersBegin(ctx, stream, headers, endOfStream, true, true);
    if (msg != null)
      processHeadersEnd(ctx, stream, msg, endOfStream); 
  }
  
  public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream) throws Http2Exception {
    Http2Stream stream = this.connection.stream(streamId);
    FullHttpMessage msg = processHeadersBegin(ctx, stream, headers, endOfStream, true, true);
    if (msg != null) {
      if (streamDependency != 0)
        msg.headers().setInt((CharSequence)HttpConversionUtil.ExtensionHeaderNames.STREAM_DEPENDENCY_ID.text(), streamDependency); 
      msg.headers().setShort((CharSequence)HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), weight);
      processHeadersEnd(ctx, stream, msg, endOfStream);
    } 
  }
  
  public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
    Http2Stream stream = this.connection.stream(streamId);
    FullHttpMessage msg = getMessage(stream);
    if (msg != null)
      onRstStreamRead(stream, msg); 
    ctx.fireExceptionCaught(Http2Exception.streamError(streamId, Http2Error.valueOf(errorCode), "HTTP/2 to HTTP layer caught stream reset", new Object[0]));
  }
  
  public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) throws Http2Exception {
    Http2Stream promisedStream = this.connection.stream(promisedStreamId);
    if (headers.status() == null)
      headers.status((CharSequence)HttpResponseStatus.OK.codeAsText()); 
    FullHttpMessage msg = processHeadersBegin(ctx, promisedStream, headers, false, false, false);
    if (msg == null)
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Push Promise Frame received for pre-existing stream id %d", new Object[] { Integer.valueOf(promisedStreamId) }); 
    msg.headers().setInt((CharSequence)HttpConversionUtil.ExtensionHeaderNames.STREAM_PROMISE_ID.text(), streamId);
    msg.headers().setShort((CharSequence)HttpConversionUtil.ExtensionHeaderNames.STREAM_WEIGHT.text(), (short)16);
    processHeadersEnd(ctx, promisedStream, msg, false);
  }
  
  public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
    if (this.propagateSettings)
      ctx.fireChannelRead(settings); 
  }
  
  protected void onRstStreamRead(Http2Stream stream, FullHttpMessage msg) {
    removeMessage(stream, true);
  }
}
