package com.github.steveice10.netty.handler.codec.spdy;

public interface SpdyGoAwayFrame extends SpdyFrame {
  int lastGoodStreamId();
  
  SpdyGoAwayFrame setLastGoodStreamId(int paramInt);
  
  SpdySessionStatus status();
  
  SpdyGoAwayFrame setStatus(SpdySessionStatus paramSpdySessionStatus);
}
