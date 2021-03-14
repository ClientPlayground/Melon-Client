package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.util.AsciiString;

public final class SpdyHttpHeaders {
  public static final class Names {
    public static final AsciiString STREAM_ID = AsciiString.cached("x-spdy-stream-id");
    
    public static final AsciiString ASSOCIATED_TO_STREAM_ID = AsciiString.cached("x-spdy-associated-to-stream-id");
    
    public static final AsciiString PRIORITY = AsciiString.cached("x-spdy-priority");
    
    public static final AsciiString SCHEME = AsciiString.cached("x-spdy-scheme");
  }
}
