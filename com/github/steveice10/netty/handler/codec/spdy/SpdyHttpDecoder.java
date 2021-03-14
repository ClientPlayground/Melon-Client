package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToMessageDecoder;
import com.github.steveice10.netty.handler.codec.TooLongFrameException;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.DefaultFullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.FullHttpMessage;
import com.github.steveice10.netty.handler.codec.http.FullHttpRequest;
import com.github.steveice10.netty.handler.codec.http.FullHttpResponse;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpMethod;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.handler.codec.http.HttpUtil;
import com.github.steveice10.netty.handler.codec.http.HttpVersion;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpdyHttpDecoder extends MessageToMessageDecoder<SpdyFrame> {
  private final boolean validateHeaders;
  
  private final int spdyVersion;
  
  private final int maxContentLength;
  
  private final Map<Integer, FullHttpMessage> messageMap;
  
  public SpdyHttpDecoder(SpdyVersion version, int maxContentLength) {
    this(version, maxContentLength, new HashMap<Integer, FullHttpMessage>(), true);
  }
  
  public SpdyHttpDecoder(SpdyVersion version, int maxContentLength, boolean validateHeaders) {
    this(version, maxContentLength, new HashMap<Integer, FullHttpMessage>(), validateHeaders);
  }
  
  protected SpdyHttpDecoder(SpdyVersion version, int maxContentLength, Map<Integer, FullHttpMessage> messageMap) {
    this(version, maxContentLength, messageMap, true);
  }
  
  protected SpdyHttpDecoder(SpdyVersion version, int maxContentLength, Map<Integer, FullHttpMessage> messageMap, boolean validateHeaders) {
    if (version == null)
      throw new NullPointerException("version"); 
    if (maxContentLength <= 0)
      throw new IllegalArgumentException("maxContentLength must be a positive integer: " + maxContentLength); 
    this.spdyVersion = version.getVersion();
    this.maxContentLength = maxContentLength;
    this.messageMap = messageMap;
    this.validateHeaders = validateHeaders;
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    for (Map.Entry<Integer, FullHttpMessage> entry : this.messageMap.entrySet())
      ReferenceCountUtil.safeRelease(entry.getValue()); 
    this.messageMap.clear();
    super.channelInactive(ctx);
  }
  
  protected FullHttpMessage putMessage(int streamId, FullHttpMessage message) {
    return this.messageMap.put(Integer.valueOf(streamId), message);
  }
  
  protected FullHttpMessage getMessage(int streamId) {
    return this.messageMap.get(Integer.valueOf(streamId));
  }
  
  protected FullHttpMessage removeMessage(int streamId) {
    return this.messageMap.remove(Integer.valueOf(streamId));
  }
  
  protected void decode(ChannelHandlerContext ctx, SpdyFrame msg, List<Object> out) throws Exception {
    if (msg instanceof SpdySynStreamFrame) {
      SpdySynStreamFrame spdySynStreamFrame = (SpdySynStreamFrame)msg;
      int streamId = spdySynStreamFrame.streamId();
      if (SpdyCodecUtil.isServerId(streamId)) {
        int associatedToStreamId = spdySynStreamFrame.associatedStreamId();
        if (associatedToStreamId == 0) {
          SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INVALID_STREAM);
          ctx.writeAndFlush(spdyRstStreamFrame);
          return;
        } 
        if (spdySynStreamFrame.isLast()) {
          SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
          ctx.writeAndFlush(spdyRstStreamFrame);
          return;
        } 
        if (spdySynStreamFrame.isTruncated()) {
          SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
          ctx.writeAndFlush(spdyRstStreamFrame);
          return;
        } 
        try {
          FullHttpRequest httpRequestWithEntity = createHttpRequest(spdySynStreamFrame, ctx.alloc());
          httpRequestWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
          httpRequestWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.ASSOCIATED_TO_STREAM_ID, associatedToStreamId);
          httpRequestWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.PRIORITY, spdySynStreamFrame.priority());
          out.add(httpRequestWithEntity);
        } catch (Throwable ignored) {
          SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
          ctx.writeAndFlush(spdyRstStreamFrame);
        } 
      } else {
        if (spdySynStreamFrame.isTruncated()) {
          SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamId);
          spdySynReplyFrame.setLast(true);
          SpdyHeaders frameHeaders = spdySynReplyFrame.headers();
          frameHeaders.setInt(SpdyHeaders.HttpNames.STATUS, HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE.code());
          frameHeaders.setObject(SpdyHeaders.HttpNames.VERSION, HttpVersion.HTTP_1_0);
          ctx.writeAndFlush(spdySynReplyFrame);
          return;
        } 
        try {
          FullHttpRequest httpRequestWithEntity = createHttpRequest(spdySynStreamFrame, ctx.alloc());
          httpRequestWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
          if (spdySynStreamFrame.isLast()) {
            out.add(httpRequestWithEntity);
          } else {
            putMessage(streamId, (FullHttpMessage)httpRequestWithEntity);
          } 
        } catch (Throwable t) {
          SpdySynReplyFrame spdySynReplyFrame = new DefaultSpdySynReplyFrame(streamId);
          spdySynReplyFrame.setLast(true);
          SpdyHeaders frameHeaders = spdySynReplyFrame.headers();
          frameHeaders.setInt(SpdyHeaders.HttpNames.STATUS, HttpResponseStatus.BAD_REQUEST.code());
          frameHeaders.setObject(SpdyHeaders.HttpNames.VERSION, HttpVersion.HTTP_1_0);
          ctx.writeAndFlush(spdySynReplyFrame);
        } 
      } 
    } else if (msg instanceof SpdySynReplyFrame) {
      SpdySynReplyFrame spdySynReplyFrame = (SpdySynReplyFrame)msg;
      int streamId = spdySynReplyFrame.streamId();
      if (spdySynReplyFrame.isTruncated()) {
        SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
        ctx.writeAndFlush(spdyRstStreamFrame);
        return;
      } 
      try {
        FullHttpResponse httpResponseWithEntity = createHttpResponse(spdySynReplyFrame, ctx.alloc(), this.validateHeaders);
        httpResponseWithEntity.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
        if (spdySynReplyFrame.isLast()) {
          HttpUtil.setContentLength((HttpMessage)httpResponseWithEntity, 0L);
          out.add(httpResponseWithEntity);
        } else {
          putMessage(streamId, (FullHttpMessage)httpResponseWithEntity);
        } 
      } catch (Throwable t) {
        SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
        ctx.writeAndFlush(spdyRstStreamFrame);
      } 
    } else if (msg instanceof SpdyHeadersFrame) {
      FullHttpResponse fullHttpResponse;
      SpdyHeadersFrame spdyHeadersFrame = (SpdyHeadersFrame)msg;
      int streamId = spdyHeadersFrame.streamId();
      FullHttpMessage fullHttpMessage = getMessage(streamId);
      if (fullHttpMessage == null) {
        if (SpdyCodecUtil.isServerId(streamId)) {
          if (spdyHeadersFrame.isTruncated()) {
            SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.INTERNAL_ERROR);
            ctx.writeAndFlush(spdyRstStreamFrame);
            return;
          } 
          try {
            fullHttpResponse = createHttpResponse(spdyHeadersFrame, ctx.alloc(), this.validateHeaders);
            fullHttpResponse.headers().setInt((CharSequence)SpdyHttpHeaders.Names.STREAM_ID, streamId);
            if (spdyHeadersFrame.isLast()) {
              HttpUtil.setContentLength((HttpMessage)fullHttpResponse, 0L);
              out.add(fullHttpResponse);
            } else {
              putMessage(streamId, (FullHttpMessage)fullHttpResponse);
            } 
          } catch (Throwable t) {
            SpdyRstStreamFrame spdyRstStreamFrame = new DefaultSpdyRstStreamFrame(streamId, SpdyStreamStatus.PROTOCOL_ERROR);
            ctx.writeAndFlush(spdyRstStreamFrame);
          } 
        } 
        return;
      } 
      if (!spdyHeadersFrame.isTruncated())
        for (Map.Entry<CharSequence, CharSequence> e : (Iterable<Map.Entry<CharSequence, CharSequence>>)spdyHeadersFrame.headers())
          fullHttpResponse.headers().add(e.getKey(), e.getValue());  
      if (spdyHeadersFrame.isLast()) {
        HttpUtil.setContentLength((HttpMessage)fullHttpResponse, fullHttpResponse.content().readableBytes());
        removeMessage(streamId);
        out.add(fullHttpResponse);
      } 
    } else if (msg instanceof SpdyDataFrame) {
      SpdyDataFrame spdyDataFrame = (SpdyDataFrame)msg;
      int streamId = spdyDataFrame.streamId();
      FullHttpMessage fullHttpMessage = getMessage(streamId);
      if (fullHttpMessage == null)
        return; 
      ByteBuf content = fullHttpMessage.content();
      if (content.readableBytes() > this.maxContentLength - spdyDataFrame.content().readableBytes()) {
        removeMessage(streamId);
        throw new TooLongFrameException("HTTP content length exceeded " + this.maxContentLength + " bytes.");
      } 
      ByteBuf spdyDataFrameData = spdyDataFrame.content();
      int spdyDataFrameDataLen = spdyDataFrameData.readableBytes();
      content.writeBytes(spdyDataFrameData, spdyDataFrameData.readerIndex(), spdyDataFrameDataLen);
      if (spdyDataFrame.isLast()) {
        HttpUtil.setContentLength((HttpMessage)fullHttpMessage, content.readableBytes());
        removeMessage(streamId);
        out.add(fullHttpMessage);
      } 
    } else if (msg instanceof SpdyRstStreamFrame) {
      SpdyRstStreamFrame spdyRstStreamFrame = (SpdyRstStreamFrame)msg;
      int streamId = spdyRstStreamFrame.streamId();
      removeMessage(streamId);
    } 
  }
  
  private static FullHttpRequest createHttpRequest(SpdyHeadersFrame requestFrame, ByteBufAllocator alloc) throws Exception {
    SpdyHeaders headers = requestFrame.headers();
    HttpMethod method = HttpMethod.valueOf(headers.getAsString((CharSequence)SpdyHeaders.HttpNames.METHOD));
    String url = headers.getAsString((CharSequence)SpdyHeaders.HttpNames.PATH);
    HttpVersion httpVersion = HttpVersion.valueOf(headers.getAsString((CharSequence)SpdyHeaders.HttpNames.VERSION));
    headers.remove(SpdyHeaders.HttpNames.METHOD);
    headers.remove(SpdyHeaders.HttpNames.PATH);
    headers.remove(SpdyHeaders.HttpNames.VERSION);
    boolean release = true;
    ByteBuf buffer = alloc.buffer();
    try {
      DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(httpVersion, method, url, buffer);
      headers.remove(SpdyHeaders.HttpNames.SCHEME);
      CharSequence host = (CharSequence)headers.get(SpdyHeaders.HttpNames.HOST);
      headers.remove(SpdyHeaders.HttpNames.HOST);
      defaultFullHttpRequest.headers().set((CharSequence)HttpHeaderNames.HOST, host);
      for (Map.Entry<CharSequence, CharSequence> e : (Iterable<Map.Entry<CharSequence, CharSequence>>)requestFrame.headers())
        defaultFullHttpRequest.headers().add(e.getKey(), e.getValue()); 
      HttpUtil.setKeepAlive((HttpMessage)defaultFullHttpRequest, true);
      defaultFullHttpRequest.headers().remove((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
      release = false;
      return (FullHttpRequest)defaultFullHttpRequest;
    } finally {
      if (release)
        buffer.release(); 
    } 
  }
  
  private static FullHttpResponse createHttpResponse(SpdyHeadersFrame responseFrame, ByteBufAllocator alloc, boolean validateHeaders) throws Exception {
    SpdyHeaders headers = responseFrame.headers();
    HttpResponseStatus status = HttpResponseStatus.parseLine((CharSequence)headers.get(SpdyHeaders.HttpNames.STATUS));
    HttpVersion version = HttpVersion.valueOf(headers.getAsString((CharSequence)SpdyHeaders.HttpNames.VERSION));
    headers.remove(SpdyHeaders.HttpNames.STATUS);
    headers.remove(SpdyHeaders.HttpNames.VERSION);
    boolean release = true;
    ByteBuf buffer = alloc.buffer();
    try {
      DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(version, status, buffer, validateHeaders);
      for (Map.Entry<CharSequence, CharSequence> e : (Iterable<Map.Entry<CharSequence, CharSequence>>)responseFrame.headers())
        defaultFullHttpResponse.headers().add(e.getKey(), e.getValue()); 
      HttpUtil.setKeepAlive((HttpMessage)defaultFullHttpResponse, true);
      defaultFullHttpResponse.headers().remove((CharSequence)HttpHeaderNames.TRANSFER_ENCODING);
      defaultFullHttpResponse.headers().remove((CharSequence)HttpHeaderNames.TRAILER);
      release = false;
      return (FullHttpResponse)defaultFullHttpResponse;
    } finally {
      if (release)
        buffer.release(); 
    } 
  }
}
