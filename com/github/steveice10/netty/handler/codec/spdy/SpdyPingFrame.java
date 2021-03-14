package com.github.steveice10.netty.handler.codec.spdy;

public interface SpdyPingFrame extends SpdyFrame {
  int id();
  
  SpdyPingFrame setId(int paramInt);
}
