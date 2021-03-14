package com.github.steveice10.netty.handler.codec.http2;

public interface Http2StreamVisitor {
  boolean visit(Http2Stream paramHttp2Stream) throws Http2Exception;
}
