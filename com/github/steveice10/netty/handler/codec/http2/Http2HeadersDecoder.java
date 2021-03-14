package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;

public interface Http2HeadersDecoder {
  Http2Headers decodeHeaders(int paramInt, ByteBuf paramByteBuf) throws Http2Exception;
  
  Configuration configuration();
  
  public static interface Configuration {
    void maxHeaderTableSize(long param1Long) throws Http2Exception;
    
    long maxHeaderTableSize();
    
    void maxHeaderListSize(long param1Long1, long param1Long2) throws Http2Exception;
    
    long maxHeaderListSize();
    
    long maxHeaderListSizeGoAway();
  }
}
