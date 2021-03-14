package com.github.steveice10.netty.handler.codec.spdy;

public interface SpdyWindowUpdateFrame extends SpdyFrame {
  int streamId();
  
  SpdyWindowUpdateFrame setStreamId(int paramInt);
  
  int deltaWindowSize();
  
  SpdyWindowUpdateFrame setDeltaWindowSize(int paramInt);
}
