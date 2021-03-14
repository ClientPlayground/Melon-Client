package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSpdyWindowUpdateFrame implements SpdyWindowUpdateFrame {
  private int streamId;
  
  private int deltaWindowSize;
  
  public DefaultSpdyWindowUpdateFrame(int streamId, int deltaWindowSize) {
    setStreamId(streamId);
    setDeltaWindowSize(deltaWindowSize);
  }
  
  public int streamId() {
    return this.streamId;
  }
  
  public SpdyWindowUpdateFrame setStreamId(int streamId) {
    if (streamId < 0)
      throw new IllegalArgumentException("Stream-ID cannot be negative: " + streamId); 
    this.streamId = streamId;
    return this;
  }
  
  public int deltaWindowSize() {
    return this.deltaWindowSize;
  }
  
  public SpdyWindowUpdateFrame setDeltaWindowSize(int deltaWindowSize) {
    if (deltaWindowSize <= 0)
      throw new IllegalArgumentException("Delta-Window-Size must be positive: " + deltaWindowSize); 
    this.deltaWindowSize = deltaWindowSize;
    return this;
  }
  
  public String toString() {
    return 
      StringUtil.simpleClassName(this) + StringUtil.NEWLINE + 
      "--> Stream-ID = " + 
      
      streamId() + StringUtil.NEWLINE + 
      "--> Delta-Window-Size = " + 
      
      deltaWindowSize();
  }
}
