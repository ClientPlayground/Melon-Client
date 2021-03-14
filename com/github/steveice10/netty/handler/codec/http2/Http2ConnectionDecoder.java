package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.io.Closeable;
import java.util.List;

public interface Http2ConnectionDecoder extends Closeable {
  void lifecycleManager(Http2LifecycleManager paramHttp2LifecycleManager);
  
  Http2Connection connection();
  
  Http2LocalFlowController flowController();
  
  void frameListener(Http2FrameListener paramHttp2FrameListener);
  
  Http2FrameListener frameListener();
  
  void decodeFrame(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, List<Object> paramList) throws Http2Exception;
  
  Http2Settings localSettings();
  
  boolean prefaceReceived();
  
  void close();
}
