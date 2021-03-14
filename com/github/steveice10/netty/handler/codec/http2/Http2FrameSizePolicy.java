package com.github.steveice10.netty.handler.codec.http2;

public interface Http2FrameSizePolicy {
  void maxFrameSize(int paramInt) throws Http2Exception;
  
  int maxFrameSize();
}
