package com.github.steveice10.netty.handler.codec.spdy;

public interface SpdySynStreamFrame extends SpdyHeadersFrame {
  int associatedStreamId();
  
  SpdySynStreamFrame setAssociatedStreamId(int paramInt);
  
  byte priority();
  
  SpdySynStreamFrame setPriority(byte paramByte);
  
  boolean isUnidirectional();
  
  SpdySynStreamFrame setUnidirectional(boolean paramBoolean);
  
  SpdySynStreamFrame setStreamId(int paramInt);
  
  SpdySynStreamFrame setLast(boolean paramBoolean);
  
  SpdySynStreamFrame setInvalid();
}
