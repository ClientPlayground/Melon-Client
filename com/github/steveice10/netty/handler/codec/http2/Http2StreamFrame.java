package com.github.steveice10.netty.handler.codec.http2;

public interface Http2StreamFrame extends Http2Frame {
  Http2StreamFrame stream(Http2FrameStream paramHttp2FrameStream);
  
  Http2FrameStream stream();
}
