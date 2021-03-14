package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.util.internal.ObjectUtil;

public final class Http2FrameStreamException extends Exception {
  private static final long serialVersionUID = -4407186173493887044L;
  
  private final Http2Error error;
  
  private final Http2FrameStream stream;
  
  public Http2FrameStreamException(Http2FrameStream stream, Http2Error error, Throwable cause) {
    super(cause.getMessage(), cause);
    this.stream = (Http2FrameStream)ObjectUtil.checkNotNull(stream, "stream");
    this.error = (Http2Error)ObjectUtil.checkNotNull(error, "error");
  }
  
  public Http2Error error() {
    return this.error;
  }
  
  public Http2FrameStream stream() {
    return this.stream;
  }
}
