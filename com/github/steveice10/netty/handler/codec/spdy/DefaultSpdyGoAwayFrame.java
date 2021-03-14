package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSpdyGoAwayFrame implements SpdyGoAwayFrame {
  private int lastGoodStreamId;
  
  private SpdySessionStatus status;
  
  public DefaultSpdyGoAwayFrame(int lastGoodStreamId) {
    this(lastGoodStreamId, 0);
  }
  
  public DefaultSpdyGoAwayFrame(int lastGoodStreamId, int statusCode) {
    this(lastGoodStreamId, SpdySessionStatus.valueOf(statusCode));
  }
  
  public DefaultSpdyGoAwayFrame(int lastGoodStreamId, SpdySessionStatus status) {
    setLastGoodStreamId(lastGoodStreamId);
    setStatus(status);
  }
  
  public int lastGoodStreamId() {
    return this.lastGoodStreamId;
  }
  
  public SpdyGoAwayFrame setLastGoodStreamId(int lastGoodStreamId) {
    if (lastGoodStreamId < 0)
      throw new IllegalArgumentException("Last-good-stream-ID cannot be negative: " + lastGoodStreamId); 
    this.lastGoodStreamId = lastGoodStreamId;
    return this;
  }
  
  public SpdySessionStatus status() {
    return this.status;
  }
  
  public SpdyGoAwayFrame setStatus(SpdySessionStatus status) {
    this.status = status;
    return this;
  }
  
  public String toString() {
    return 
      StringUtil.simpleClassName(this) + StringUtil.NEWLINE + 
      "--> Last-good-stream-ID = " + 
      
      lastGoodStreamId() + StringUtil.NEWLINE + 
      "--> Status: " + 
      
      status();
  }
}
