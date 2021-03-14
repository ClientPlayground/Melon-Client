package com.github.steveice10.netty.handler.codec.http2;

public interface Http2FrameStreamVisitor {
  boolean visit(Http2FrameStream paramHttp2FrameStream);
}
