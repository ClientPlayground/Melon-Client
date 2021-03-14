package com.github.steveice10.netty.handler.codec.http2;

public final class Http2FrameStreamEvent {
  private final Http2FrameStream stream;
  
  private final Type type;
  
  enum Type {
    State, Writability;
  }
  
  private Http2FrameStreamEvent(Http2FrameStream stream, Type type) {
    this.stream = stream;
    this.type = type;
  }
  
  public Http2FrameStream stream() {
    return this.stream;
  }
  
  public Type type() {
    return this.type;
  }
  
  static Http2FrameStreamEvent stateChanged(Http2FrameStream stream) {
    return new Http2FrameStreamEvent(stream, Type.State);
  }
  
  static Http2FrameStreamEvent writabilityChanged(Http2FrameStream stream) {
    return new Http2FrameStreamEvent(stream, Type.Writability);
  }
}
