package com.github.steveice10.netty.handler.codec.http2;

public interface Http2FrameStream {
  int id();
  
  Http2Stream.State state();
}
