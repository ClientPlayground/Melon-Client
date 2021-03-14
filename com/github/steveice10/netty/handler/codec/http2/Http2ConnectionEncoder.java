package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;

public interface Http2ConnectionEncoder extends Http2FrameWriter {
  void lifecycleManager(Http2LifecycleManager paramHttp2LifecycleManager);
  
  Http2Connection connection();
  
  Http2RemoteFlowController flowController();
  
  Http2FrameWriter frameWriter();
  
  Http2Settings pollSentSettings();
  
  void remoteSettings(Http2Settings paramHttp2Settings) throws Http2Exception;
  
  ChannelFuture writeFrame(ChannelHandlerContext paramChannelHandlerContext, byte paramByte, int paramInt, Http2Flags paramHttp2Flags, ByteBuf paramByteBuf, ChannelPromise paramChannelPromise);
}
