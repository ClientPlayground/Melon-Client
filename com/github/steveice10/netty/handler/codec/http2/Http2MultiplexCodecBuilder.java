package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerAdapter;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class Http2MultiplexCodecBuilder extends AbstractHttp2ConnectionHandlerBuilder<Http2MultiplexCodec, Http2MultiplexCodecBuilder> {
  final ChannelHandler childHandler;
  
  Http2MultiplexCodecBuilder(boolean server, ChannelHandler childHandler) {
    server(server);
    this.childHandler = checkSharable((ChannelHandler)ObjectUtil.checkNotNull(childHandler, "childHandler"));
  }
  
  private static ChannelHandler checkSharable(ChannelHandler handler) {
    if (handler instanceof ChannelHandlerAdapter && !((ChannelHandlerAdapter)handler).isSharable() && 
      !handler.getClass().isAnnotationPresent((Class)ChannelHandler.Sharable.class))
      throw new IllegalArgumentException("The handler must be Sharable"); 
    return handler;
  }
  
  public static Http2MultiplexCodecBuilder forClient(ChannelHandler childHandler) {
    return new Http2MultiplexCodecBuilder(false, childHandler);
  }
  
  public static Http2MultiplexCodecBuilder forServer(ChannelHandler childHandler) {
    return new Http2MultiplexCodecBuilder(true, childHandler);
  }
  
  public Http2Settings initialSettings() {
    return super.initialSettings();
  }
  
  public Http2MultiplexCodecBuilder initialSettings(Http2Settings settings) {
    return super.initialSettings(settings);
  }
  
  public long gracefulShutdownTimeoutMillis() {
    return super.gracefulShutdownTimeoutMillis();
  }
  
  public Http2MultiplexCodecBuilder gracefulShutdownTimeoutMillis(long gracefulShutdownTimeoutMillis) {
    return super.gracefulShutdownTimeoutMillis(gracefulShutdownTimeoutMillis);
  }
  
  public boolean isServer() {
    return super.isServer();
  }
  
  public int maxReservedStreams() {
    return super.maxReservedStreams();
  }
  
  public Http2MultiplexCodecBuilder maxReservedStreams(int maxReservedStreams) {
    return super.maxReservedStreams(maxReservedStreams);
  }
  
  public boolean isValidateHeaders() {
    return super.isValidateHeaders();
  }
  
  public Http2MultiplexCodecBuilder validateHeaders(boolean validateHeaders) {
    return super.validateHeaders(validateHeaders);
  }
  
  public Http2FrameLogger frameLogger() {
    return super.frameLogger();
  }
  
  public Http2MultiplexCodecBuilder frameLogger(Http2FrameLogger frameLogger) {
    return super.frameLogger(frameLogger);
  }
  
  public boolean encoderEnforceMaxConcurrentStreams() {
    return super.encoderEnforceMaxConcurrentStreams();
  }
  
  public Http2MultiplexCodecBuilder encoderEnforceMaxConcurrentStreams(boolean encoderEnforceMaxConcurrentStreams) {
    return super.encoderEnforceMaxConcurrentStreams(encoderEnforceMaxConcurrentStreams);
  }
  
  public Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector() {
    return super.headerSensitivityDetector();
  }
  
  public Http2MultiplexCodecBuilder headerSensitivityDetector(Http2HeadersEncoder.SensitivityDetector headerSensitivityDetector) {
    return super.headerSensitivityDetector(headerSensitivityDetector);
  }
  
  public Http2MultiplexCodecBuilder encoderIgnoreMaxHeaderListSize(boolean ignoreMaxHeaderListSize) {
    return super.encoderIgnoreMaxHeaderListSize(ignoreMaxHeaderListSize);
  }
  
  public Http2MultiplexCodecBuilder initialHuffmanDecodeCapacity(int initialHuffmanDecodeCapacity) {
    return super.initialHuffmanDecodeCapacity(initialHuffmanDecodeCapacity);
  }
  
  public Http2MultiplexCodec build() {
    return super.build();
  }
  
  protected Http2MultiplexCodec build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings) {
    return new Http2MultiplexCodec(encoder, decoder, initialSettings, this.childHandler);
  }
}
