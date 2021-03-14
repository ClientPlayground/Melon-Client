package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.util.internal.StringUtil;

public class DefaultSpdyRstStreamFrame extends DefaultSpdyStreamFrame implements SpdyRstStreamFrame {
  private SpdyStreamStatus status;
  
  public DefaultSpdyRstStreamFrame(int streamId, int statusCode) {
    this(streamId, SpdyStreamStatus.valueOf(statusCode));
  }
  
  public DefaultSpdyRstStreamFrame(int streamId, SpdyStreamStatus status) {
    super(streamId);
    setStatus(status);
  }
  
  public SpdyRstStreamFrame setStreamId(int streamId) {
    super.setStreamId(streamId);
    return this;
  }
  
  public SpdyRstStreamFrame setLast(boolean last) {
    super.setLast(last);
    return this;
  }
  
  public SpdyStreamStatus status() {
    return this.status;
  }
  
  public SpdyRstStreamFrame setStatus(SpdyStreamStatus status) {
    this.status = status;
    return this;
  }
  
  public String toString() {
    return 
      StringUtil.simpleClassName(this) + StringUtil.NEWLINE + 
      "--> Stream-ID = " + 
      
      streamId() + StringUtil.NEWLINE + 
      "--> Status: " + 
      
      status();
  }
}
