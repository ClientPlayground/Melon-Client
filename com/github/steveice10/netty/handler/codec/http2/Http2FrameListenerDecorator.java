package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class Http2FrameListenerDecorator implements Http2FrameListener {
  protected final Http2FrameListener listener;
  
  public Http2FrameListenerDecorator(Http2FrameListener listener) {
    this.listener = (Http2FrameListener)ObjectUtil.checkNotNull(listener, "listener");
  }
  
  public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
    return this.listener.onDataRead(ctx, streamId, data, padding, endOfStream);
  }
  
  public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream) throws Http2Exception {
    this.listener.onHeadersRead(ctx, streamId, headers, padding, endStream);
  }
  
  public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
    this.listener.onHeadersRead(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endStream);
  }
  
  public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive) throws Http2Exception {
    this.listener.onPriorityRead(ctx, streamId, streamDependency, weight, exclusive);
  }
  
  public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) throws Http2Exception {
    this.listener.onRstStreamRead(ctx, streamId, errorCode);
  }
  
  public void onSettingsAckRead(ChannelHandlerContext ctx) throws Http2Exception {
    this.listener.onSettingsAckRead(ctx);
  }
  
  public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) throws Http2Exception {
    this.listener.onSettingsRead(ctx, settings);
  }
  
  public void onPingRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
    this.listener.onPingRead(ctx, data);
  }
  
  public void onPingAckRead(ChannelHandlerContext ctx, long data) throws Http2Exception {
    this.listener.onPingAckRead(ctx, data);
  }
  
  public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) throws Http2Exception {
    this.listener.onPushPromiseRead(ctx, streamId, promisedStreamId, headers, padding);
  }
  
  public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) throws Http2Exception {
    this.listener.onGoAwayRead(ctx, lastStreamId, errorCode, debugData);
  }
  
  public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) throws Http2Exception {
    this.listener.onWindowUpdateRead(ctx, streamId, windowSizeIncrement);
  }
  
  public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload) throws Http2Exception {
    this.listener.onUnknownFrame(ctx, frameType, streamId, flags, payload);
  }
}
