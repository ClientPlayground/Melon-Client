package com.github.steveice10.netty.handler.codec.http2;

public interface Http2WindowUpdateFrame extends Http2StreamFrame {
  int windowSizeIncrement();
}
