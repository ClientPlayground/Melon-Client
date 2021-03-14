package com.github.steveice10.netty.handler.codec.rtsp;

import com.github.steveice10.netty.handler.codec.http.HttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpObjectDecoder;

@Deprecated
public abstract class RtspObjectDecoder extends HttpObjectDecoder {
  protected RtspObjectDecoder() {
    this(4096, 8192, 8192);
  }
  
  protected RtspObjectDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength) {
    super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false);
  }
  
  protected RtspObjectDecoder(int maxInitialLineLength, int maxHeaderSize, int maxContentLength, boolean validateHeaders) {
    super(maxInitialLineLength, maxHeaderSize, maxContentLength * 2, false, validateHeaders);
  }
  
  protected boolean isContentAlwaysEmpty(HttpMessage msg) {
    boolean empty = super.isContentAlwaysEmpty(msg);
    if (empty)
      return true; 
    if (!msg.headers().contains((CharSequence)RtspHeaderNames.CONTENT_LENGTH))
      return true; 
    return empty;
  }
}
