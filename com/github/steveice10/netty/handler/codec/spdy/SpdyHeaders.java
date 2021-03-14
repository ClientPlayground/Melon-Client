package com.github.steveice10.netty.handler.codec.spdy;

import com.github.steveice10.netty.handler.codec.Headers;
import com.github.steveice10.netty.util.AsciiString;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface SpdyHeaders extends Headers<CharSequence, CharSequence, SpdyHeaders> {
  String getAsString(CharSequence paramCharSequence);
  
  List<String> getAllAsString(CharSequence paramCharSequence);
  
  Iterator<Map.Entry<String, String>> iteratorAsString();
  
  boolean contains(CharSequence paramCharSequence1, CharSequence paramCharSequence2, boolean paramBoolean);
  
  public static final class HttpNames {
    public static final AsciiString HOST = AsciiString.cached(":host");
    
    public static final AsciiString METHOD = AsciiString.cached(":method");
    
    public static final AsciiString PATH = AsciiString.cached(":path");
    
    public static final AsciiString SCHEME = AsciiString.cached(":scheme");
    
    public static final AsciiString STATUS = AsciiString.cached(":status");
    
    public static final AsciiString VERSION = AsciiString.cached(":version");
  }
}
