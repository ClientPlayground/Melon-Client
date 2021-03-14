package com.github.steveice10.netty.handler.codec.http.websocketx;

public class WebSocket07FrameDecoder extends WebSocket08FrameDecoder {
  public WebSocket07FrameDecoder(boolean expectMaskedFrames, boolean allowExtensions, int maxFramePayloadLength) {
    this(expectMaskedFrames, allowExtensions, maxFramePayloadLength, false);
  }
  
  public WebSocket07FrameDecoder(boolean expectMaskedFrames, boolean allowExtensions, int maxFramePayloadLength, boolean allowMaskMismatch) {
    super(expectMaskedFrames, allowExtensions, maxFramePayloadLength, allowMaskMismatch);
  }
}
