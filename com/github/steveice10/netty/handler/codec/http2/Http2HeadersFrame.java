package com.github.steveice10.netty.handler.codec.http2;

public interface Http2HeadersFrame extends Http2StreamFrame {
  Http2Headers headers();
  
  int padding();
  
  boolean isEndStream();
}
