package com.github.steveice10.netty.handler.codec.spdy;

public interface SpdyRstStreamFrame extends SpdyStreamFrame {
  SpdyStreamStatus status();
  
  SpdyRstStreamFrame setStatus(SpdyStreamStatus paramSpdyStreamStatus);
  
  SpdyRstStreamFrame setStreamId(int paramInt);
  
  SpdyRstStreamFrame setLast(boolean paramBoolean);
}
