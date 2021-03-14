package com.github.steveice10.netty.handler.codec.http2;

public final class Http2ConnectionHandlerBuilder extends AbstractHttp2ConnectionHandlerBuilder<Http2ConnectionHandler, Http2ConnectionHandlerBuilder> {
  public Http2ConnectionHandlerBuilder validateHeaders(boolean validateHeaders) {
    return super.validateHeaders(validateHeaders);
  }
  
  public Http2ConnectionHandlerBuilder initialSettings(Http2Settings settings) {
    return super.initialSettings(settings);
  }
  
  public Http2ConnectionHandlerBuilder frameListener(Http2FrameListener frameListener) {
    return super.frameListener(frameListener);
  }
  
  public Http2ConnectionHandlerBuilder gracefulShutdownTimeoutMillis(long gracefulShutdownTimeoutMillis) {
    return super.gracefulShutdownTimeoutMillis(gracefulShutdownTimeoutMillis);
  }
  
  public Http2ConnectionHandlerBuilder server(boolean isServer) {
    return super.server(isServer);
  }
  
  public Http2ConnectionHandlerBuilder connection(Http2Connection connection) {
    return super.connection(connection);
  }
  
  public Http2ConnectionHandlerBuilder maxReservedStreams(int maxReservedStreams) {
    return super.maxReservedStreams(maxReservedStreams);
  }
  
  public Http2ConnectionHandlerBuilder codec(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder) {
    return super.codec(decoder, encoder);
  }
  
  public Http2ConnectionHandlerBuilder frameLogger(Http2FrameLogger frameLogger) {
    return super.frameLogger(frameLogger);
  }
  
  public Http2ConnectionHandlerBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams) {
    return super.encoderEnforceMaxConcurrentStreams(encoderEnforceMaxConcurrentStreams);
  }
  
  public Http2ConnectionHandlerBuilder encoderIgnoreMaxHeaderListSize(boolean encoderIgnoreMaxHeaderListSize) {
    return super.encoderIgnoreMaxHeaderListSize(encoderIgnoreMaxHeaderListSize);
  }
  
  public Http2ConnectionHandlerBuilder headerSensitivityDetector(Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector) {
    return super.headerSensitivityDetector(headerSensitivityDetector);
  }
  
  public Http2ConnectionHandlerBuilder initialHuffmanDecodeCapacity(int initialHuffmanDecodeCapacity) {
    return super.initialHuffmanDecodeCapacity(initialHuffmanDecodeCapacity);
  }
  
  public Http2ConnectionHandler build() {
    return super.build();
  }
  
  protected Http2ConnectionHandler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings) {
    return new Http2ConnectionHandler(decoder, encoder, initialSettings);
  }
}
