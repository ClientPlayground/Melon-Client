package com.github.steveice10.netty.handler.codec.spdy;

public interface SpdyHeadersFrame extends SpdyStreamFrame {
  boolean isInvalid();
  
  SpdyHeadersFrame setInvalid();
  
  boolean isTruncated();
  
  SpdyHeadersFrame setTruncated();
  
  SpdyHeaders headers();
  
  SpdyHeadersFrame setStreamId(int paramInt);
  
  SpdyHeadersFrame setLast(boolean paramBoolean);
}
