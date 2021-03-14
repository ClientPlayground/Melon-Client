package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import java.io.Closeable;

public interface Http2FrameReader extends Closeable {
  void readFrame(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, Http2FrameListener paramHttp2FrameListener) throws Http2Exception;
  
  Configuration configuration();
  
  void close();
  
  public static interface Configuration {
    Http2HeadersDecoder.Configuration headersConfiguration();
    
    Http2FrameSizePolicy frameSizePolicy();
  }
}
