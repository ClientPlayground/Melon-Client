package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface Http2HeadersEncoder {
  public static final SensitivityDetector NEVER_SENSITIVE = new SensitivityDetector() {
      public boolean isSensitive(CharSequence name, CharSequence value) {
        return false;
      }
    };
  
  public static final SensitivityDetector ALWAYS_SENSITIVE = new SensitivityDetector() {
      public boolean isSensitive(CharSequence name, CharSequence value) {
        return true;
      }
    };
  
  void encodeHeaders(int paramInt, Http2Headers paramHttp2Headers, ByteBuf paramByteBuf) throws Http2Exception;
  
  Configuration configuration();
  
  public static interface Configuration {
    void maxHeaderTableSize(long param1Long) throws Http2Exception;
    
    long maxHeaderTableSize();
    
    void maxHeaderListSize(long param1Long) throws Http2Exception;
    
    long maxHeaderListSize();
  }
  
  public static interface SensitivityDetector {
    boolean isSensitive(CharSequence param1CharSequence1, CharSequence param1CharSequence2);
  }
}
