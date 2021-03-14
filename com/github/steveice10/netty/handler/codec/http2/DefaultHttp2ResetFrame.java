package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.StringUtil;

public final class DefaultHttp2ResetFrame extends AbstractHttp2StreamFrame implements Http2ResetFrame {
  private final long errorCode;
  
  public DefaultHttp2ResetFrame(Http2Error error) {
    this.errorCode = ((Http2Error)ObjectUtil.checkNotNull(error, "error")).code();
  }
  
  public DefaultHttp2ResetFrame(long errorCode) {
    this.errorCode = errorCode;
  }
  
  public DefaultHttp2ResetFrame stream(Http2FrameStream stream) {
    super.stream(stream);
    return this;
  }
  
  public String name() {
    return "RST_STREAM";
  }
  
  public long errorCode() {
    return this.errorCode;
  }
  
  public String toString() {
    return StringUtil.simpleClassName(this) + "(stream=" + stream() + ", errorCode=" + this.errorCode + ')';
  }
  
  public boolean equals(Object o) {
    if (!(o instanceof DefaultHttp2ResetFrame))
      return false; 
    DefaultHttp2ResetFrame other = (DefaultHttp2ResetFrame)o;
    return (super.equals(o) && this.errorCode == other.errorCode);
  }
  
  public int hashCode() {
    int hash = super.hashCode();
    hash = hash * 31 + (int)(this.errorCode ^ this.errorCode >>> 32L);
    return hash;
  }
}
