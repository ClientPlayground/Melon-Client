package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.channel.ChannelHandlerContext;

public interface Http2FlowController {
  void channelHandlerContext(ChannelHandlerContext paramChannelHandlerContext) throws Http2Exception;
  
  void initialWindowSize(int paramInt) throws Http2Exception;
  
  int initialWindowSize();
  
  int windowSize(Http2Stream paramHttp2Stream);
  
  void incrementWindowSize(Http2Stream paramHttp2Stream, int paramInt) throws Http2Exception;
}
