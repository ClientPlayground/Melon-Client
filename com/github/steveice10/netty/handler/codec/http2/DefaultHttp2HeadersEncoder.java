package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class DefaultHttp2HeadersEncoder implements Http2HeadersEncoder, Http2HeadersEncoder.Configuration {
  private final HpackEncoder hpackEncoder;
  
  private final Http2HeadersEncoder.SensitivityDetector sensitivityDetector;
  
  private final ByteBuf tableSizeChangeOutput = Unpooled.buffer();
  
  public DefaultHttp2HeadersEncoder() {
    this(NEVER_SENSITIVE);
  }
  
  public DefaultHttp2HeadersEncoder(Http2HeadersEncoder.SensitivityDetector sensitivityDetector) {
    this(sensitivityDetector, new HpackEncoder());
  }
  
  public DefaultHttp2HeadersEncoder(Http2HeadersEncoder.SensitivityDetector sensitivityDetector, boolean ignoreMaxHeaderListSize) {
    this(sensitivityDetector, new HpackEncoder(ignoreMaxHeaderListSize));
  }
  
  public DefaultHttp2HeadersEncoder(Http2HeadersEncoder.SensitivityDetector sensitivityDetector, boolean ignoreMaxHeaderListSize, int dynamicTableArraySizeHint) {
    this(sensitivityDetector, new HpackEncoder(ignoreMaxHeaderListSize, dynamicTableArraySizeHint));
  }
  
  DefaultHttp2HeadersEncoder(Http2HeadersEncoder.SensitivityDetector sensitivityDetector, HpackEncoder hpackEncoder) {
    this.sensitivityDetector = (Http2HeadersEncoder.SensitivityDetector)ObjectUtil.checkNotNull(sensitivityDetector, "sensitiveDetector");
    this.hpackEncoder = (HpackEncoder)ObjectUtil.checkNotNull(hpackEncoder, "hpackEncoder");
  }
  
  public void encodeHeaders(int streamId, Http2Headers headers, ByteBuf buffer) throws Http2Exception {
    try {
      if (this.tableSizeChangeOutput.isReadable()) {
        buffer.writeBytes(this.tableSizeChangeOutput);
        this.tableSizeChangeOutput.clear();
      } 
      this.hpackEncoder.encodeHeaders(streamId, buffer, headers, this.sensitivityDetector);
    } catch (Http2Exception e) {
      throw e;
    } catch (Throwable t) {
      throw Http2Exception.connectionError(Http2Error.COMPRESSION_ERROR, t, "Failed encoding headers block: %s", new Object[] { t.getMessage() });
    } 
  }
  
  public void maxHeaderTableSize(long max) throws Http2Exception {
    this.hpackEncoder.setMaxHeaderTableSize(this.tableSizeChangeOutput, max);
  }
  
  public long maxHeaderTableSize() {
    return this.hpackEncoder.getMaxHeaderTableSize();
  }
  
  public void maxHeaderListSize(long max) throws Http2Exception {
    this.hpackEncoder.setMaxHeaderListSize(max);
  }
  
  public long maxHeaderListSize() {
    return this.hpackEncoder.getMaxHeaderListSize();
  }
  
  public Http2HeadersEncoder.Configuration configuration() {
    return this;
  }
}
