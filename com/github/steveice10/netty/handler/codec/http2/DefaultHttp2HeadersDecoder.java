package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class DefaultHttp2HeadersDecoder implements Http2HeadersDecoder, Http2HeadersDecoder.Configuration {
  private static final float HEADERS_COUNT_WEIGHT_NEW = 0.2F;
  
  private static final float HEADERS_COUNT_WEIGHT_HISTORICAL = 0.8F;
  
  private final HpackDecoder hpackDecoder;
  
  private final boolean validateHeaders;
  
  private float headerArraySizeAccumulator = 8.0F;
  
  public DefaultHttp2HeadersDecoder() {
    this(true);
  }
  
  public DefaultHttp2HeadersDecoder(boolean validateHeaders) {
    this(validateHeaders, 8192L);
  }
  
  public DefaultHttp2HeadersDecoder(boolean validateHeaders, long maxHeaderListSize) {
    this(validateHeaders, maxHeaderListSize, 32);
  }
  
  public DefaultHttp2HeadersDecoder(boolean validateHeaders, long maxHeaderListSize, int initialHuffmanDecodeCapacity) {
    this(validateHeaders, new HpackDecoder(maxHeaderListSize, initialHuffmanDecodeCapacity));
  }
  
  DefaultHttp2HeadersDecoder(boolean validateHeaders, HpackDecoder hpackDecoder) {
    this.hpackDecoder = (HpackDecoder)ObjectUtil.checkNotNull(hpackDecoder, "hpackDecoder");
    this.validateHeaders = validateHeaders;
  }
  
  public void maxHeaderTableSize(long max) throws Http2Exception {
    this.hpackDecoder.setMaxHeaderTableSize(max);
  }
  
  public long maxHeaderTableSize() {
    return this.hpackDecoder.getMaxHeaderTableSize();
  }
  
  public void maxHeaderListSize(long max, long goAwayMax) throws Http2Exception {
    this.hpackDecoder.setMaxHeaderListSize(max, goAwayMax);
  }
  
  public long maxHeaderListSize() {
    return this.hpackDecoder.getMaxHeaderListSize();
  }
  
  public long maxHeaderListSizeGoAway() {
    return this.hpackDecoder.getMaxHeaderListSizeGoAway();
  }
  
  public Http2HeadersDecoder.Configuration configuration() {
    return this;
  }
  
  public Http2Headers decodeHeaders(int streamId, ByteBuf headerBlock) throws Http2Exception {
    try {
      Http2Headers headers = newHeaders();
      this.hpackDecoder.decode(streamId, headerBlock, headers, this.validateHeaders);
      this.headerArraySizeAccumulator = 0.2F * headers.size() + 0.8F * this.headerArraySizeAccumulator;
      return headers;
    } catch (Http2Exception e) {
      throw e;
    } catch (Throwable e) {
      throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, e, e.getMessage(), new Object[0]);
    } 
  }
  
  protected final int numberOfHeadersGuess() {
    return (int)this.headerArraySizeAccumulator;
  }
  
  protected final boolean validateHeaders() {
    return this.validateHeaders;
  }
  
  protected Http2Headers newHeaders() {
    return new DefaultHttp2Headers(this.validateHeaders, (int)this.headerArraySizeAccumulator);
  }
}
