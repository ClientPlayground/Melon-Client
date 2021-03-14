package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import java.io.Closeable;

public interface Http2FrameWriter extends Http2DataWriter, Closeable {
  ChannelFuture writeHeaders(ChannelHandlerContext paramChannelHandlerContext, int paramInt1, Http2Headers paramHttp2Headers, int paramInt2, boolean paramBoolean, ChannelPromise paramChannelPromise);
  
  ChannelFuture writeHeaders(ChannelHandlerContext paramChannelHandlerContext, int paramInt1, Http2Headers paramHttp2Headers, int paramInt2, short paramShort, boolean paramBoolean1, int paramInt3, boolean paramBoolean2, ChannelPromise paramChannelPromise);
  
  ChannelFuture writePriority(ChannelHandlerContext paramChannelHandlerContext, int paramInt1, int paramInt2, short paramShort, boolean paramBoolean, ChannelPromise paramChannelPromise);
  
  ChannelFuture writeRstStream(ChannelHandlerContext paramChannelHandlerContext, int paramInt, long paramLong, ChannelPromise paramChannelPromise);
  
  ChannelFuture writeSettings(ChannelHandlerContext paramChannelHandlerContext, Http2Settings paramHttp2Settings, ChannelPromise paramChannelPromise);
  
  ChannelFuture writeSettingsAck(ChannelHandlerContext paramChannelHandlerContext, ChannelPromise paramChannelPromise);
  
  ChannelFuture writePing(ChannelHandlerContext paramChannelHandlerContext, boolean paramBoolean, long paramLong, ChannelPromise paramChannelPromise);
  
  ChannelFuture writePushPromise(ChannelHandlerContext paramChannelHandlerContext, int paramInt1, int paramInt2, Http2Headers paramHttp2Headers, int paramInt3, ChannelPromise paramChannelPromise);
  
  ChannelFuture writeGoAway(ChannelHandlerContext paramChannelHandlerContext, int paramInt, long paramLong, ByteBuf paramByteBuf, ChannelPromise paramChannelPromise);
  
  ChannelFuture writeWindowUpdate(ChannelHandlerContext paramChannelHandlerContext, int paramInt1, int paramInt2, ChannelPromise paramChannelPromise);
  
  ChannelFuture writeFrame(ChannelHandlerContext paramChannelHandlerContext, byte paramByte, int paramInt, Http2Flags paramHttp2Flags, ByteBuf paramByteBuf, ChannelPromise paramChannelPromise);
  
  Configuration configuration();
  
  void close();
  
  public static interface Configuration {
    Http2HeadersEncoder.Configuration headersConfiguration();
    
    Http2FrameSizePolicy frameSizePolicy();
  }
}
