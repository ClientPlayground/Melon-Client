package com.github.steveice10.netty.handler.codec.http2;

public interface Http2ResetFrame extends Http2StreamFrame {
  long errorCode();
}
