package com.github.steveice10.netty.handler.codec.http2;

public enum Http2Error {
  NO_ERROR(0L),
  PROTOCOL_ERROR(1L),
  INTERNAL_ERROR(2L),
  FLOW_CONTROL_ERROR(3L),
  SETTINGS_TIMEOUT(4L),
  STREAM_CLOSED(5L),
  FRAME_SIZE_ERROR(6L),
  REFUSED_STREAM(7L),
  CANCEL(8L),
  COMPRESSION_ERROR(9L),
  CONNECT_ERROR(10L),
  ENHANCE_YOUR_CALM(11L),
  INADEQUATE_SECURITY(12L),
  HTTP_1_1_REQUIRED(13L);
  
  private final long code;
  
  private static final Http2Error[] INT_TO_ENUM_MAP;
  
  static {
    Http2Error[] errors = values();
    Http2Error[] map = new Http2Error[errors.length];
    for (Http2Error error : errors)
      map[(int)error.code()] = error; 
    INT_TO_ENUM_MAP = map;
  }
  
  Http2Error(long code) {
    this.code = code;
  }
  
  public long code() {
    return this.code;
  }
}
