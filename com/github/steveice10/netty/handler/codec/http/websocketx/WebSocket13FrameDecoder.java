package com.github.steveice10.netty.handler.codec.http.websocketx;

public class WebSocket13FrameDecoder extends WebSocket08FrameDecoder {
  public WebSocket13FrameDecoder(boolean expectMaskedFrames, boolean allowExtensions, int maxFramePayloadLength) {
    this(expectMaskedFrames, allowExtensions, maxFramePayloadLength, false);
  }
  
  public WebSocket13FrameDecoder(boolean expectMaskedFrames, boolean allowExtensions, int maxFramePayloadLength, boolean allowMaskMismatch) {
    super(expectMaskedFrames, allowExtensions, maxFramePayloadLength, allowMaskMismatch);
  }
}
