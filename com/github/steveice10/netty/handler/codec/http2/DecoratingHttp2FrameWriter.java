package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class DecoratingHttp2FrameWriter implements Http2FrameWriter {
  private final Http2FrameWriter delegate;
  
  public DecoratingHttp2FrameWriter(Http2FrameWriter delegate) {
    this.delegate = (Http2FrameWriter)ObjectUtil.checkNotNull(delegate, "delegate");
  }
  
  public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endStream, ChannelPromise promise) {
    return this.delegate.writeData(ctx, streamId, data, padding, endStream, promise);
  }
  
  public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream, ChannelPromise promise) {
    return this.delegate.writeHeaders(ctx, streamId, headers, padding, endStream, promise);
  }
  
  public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream, ChannelPromise promise) {
    return this.delegate
      .writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endStream, promise);
  }
  
  public ChannelFuture writePriority(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive, ChannelPromise promise) {
    return this.delegate.writePriority(ctx, streamId, streamDependency, weight, exclusive, promise);
  }
  
  public ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode, ChannelPromise promise) {
    return this.delegate.writeRstStream(ctx, streamId, errorCode, promise);
  }
  
  public ChannelFuture writeSettings(ChannelHandlerContext ctx, Http2Settings settings, ChannelPromise promise) {
    return this.delegate.writeSettings(ctx, settings, promise);
  }
  
  public ChannelFuture writeSettingsAck(ChannelHandlerContext ctx, ChannelPromise promise) {
    return this.delegate.writeSettingsAck(ctx, promise);
  }
  
  public ChannelFuture writePing(ChannelHandlerContext ctx, boolean ack, long data, ChannelPromise promise) {
    return this.delegate.writePing(ctx, ack, data, promise);
  }
  
  public ChannelFuture writePushPromise(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding, ChannelPromise promise) {
    return this.delegate.writePushPromise(ctx, streamId, promisedStreamId, headers, padding, promise);
  }
  
  public ChannelFuture writeGoAway(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData, ChannelPromise promise) {
    return this.delegate.writeGoAway(ctx, lastStreamId, errorCode, debugData, promise);
  }
  
  public ChannelFuture writeWindowUpdate(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement, ChannelPromise promise) {
    return this.delegate.writeWindowUpdate(ctx, streamId, windowSizeIncrement, promise);
  }
  
  public ChannelFuture writeFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload, ChannelPromise promise) {
    return this.delegate.writeFrame(ctx, frameType, streamId, flags, payload, promise);
  }
  
  public Http2FrameWriter.Configuration configuration() {
    return this.delegate.configuration();
  }
  
  public void close() {
    this.delegate.close();
  }
}
