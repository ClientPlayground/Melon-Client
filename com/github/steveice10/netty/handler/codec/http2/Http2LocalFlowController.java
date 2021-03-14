package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface Http2LocalFlowController extends Http2FlowController {
  Http2LocalFlowController frameWriter(Http2FrameWriter paramHttp2FrameWriter);
  
  void receiveFlowControlledFrame(Http2Stream paramHttp2Stream, ByteBuf paramByteBuf, int paramInt, boolean paramBoolean) throws Http2Exception;
  
  boolean consumeBytes(Http2Stream paramHttp2Stream, int paramInt) throws Http2Exception;
  
  int unconsumedBytes(Http2Stream paramHttp2Stream);
  
  int initialWindowSize(Http2Stream paramHttp2Stream);
}
