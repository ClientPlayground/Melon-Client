package com.github.steveice10.netty.handler.codec.rtsp;

import com.github.steveice10.netty.handler.codec.http.HttpVersion;

public final class RtspVersions {
  public static final HttpVersion RTSP_1_0 = new HttpVersion("RTSP", 1, 0, true);
  
  public static HttpVersion valueOf(String text) {
    if (text == null)
      throw new NullPointerException("text"); 
    text = text.trim().toUpperCase();
    if ("RTSP/1.0".equals(text))
      return RTSP_1_0; 
    return new HttpVersion(text, true);
  }
}
