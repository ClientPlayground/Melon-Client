package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.handler.codec.Headers;
import com.github.steveice10.netty.util.AsciiString;
import java.util.Iterator;
import java.util.Map;

public interface Http2Headers extends Headers<CharSequence, CharSequence, Http2Headers> {
  Iterator<Map.Entry<CharSequence, CharSequence>> iterator();
  
  Iterator<CharSequence> valueIterator(CharSequence paramCharSequence);
  
  Http2Headers method(CharSequence paramCharSequence);
  
  Http2Headers scheme(CharSequence paramCharSequence);
  
  Http2Headers authority(CharSequence paramCharSequence);
  
  Http2Headers path(CharSequence paramCharSequence);
  
  Http2Headers status(CharSequence paramCharSequence);
  
  CharSequence method();
  
  CharSequence scheme();
  
  CharSequence authority();
  
  CharSequence path();
  
  CharSequence status();
  
  boolean contains(CharSequence paramCharSequence1, CharSequence paramCharSequence2, boolean paramBoolean);
  
  public enum PseudoHeaderName {
    METHOD(":method", true),
    SCHEME(":scheme", true),
    AUTHORITY(":authority", true),
    PATH(":path", true),
    STATUS(":status", false);
    
    private static final char PSEUDO_HEADER_PREFIX = ':';
    
    private static final byte PSEUDO_HEADER_PREFIX_BYTE = 58;
    
    private final AsciiString value;
    
    private final boolean requestOnly;
    
    private static final CharSequenceMap<PseudoHeaderName> PSEUDO_HEADERS = new CharSequenceMap<PseudoHeaderName>();
    
    static {
      for (PseudoHeaderName pseudoHeader : values())
        PSEUDO_HEADERS.add(pseudoHeader.value(), pseudoHeader); 
    }
    
    PseudoHeaderName(String value, boolean requestOnly) {
      this.value = AsciiString.cached(value);
      this.requestOnly = requestOnly;
    }
    
    public AsciiString value() {
      return this.value;
    }
    
    public static boolean hasPseudoHeaderFormat(CharSequence headerName) {
      if (headerName instanceof AsciiString) {
        AsciiString asciiHeaderName = (AsciiString)headerName;
        return (asciiHeaderName.length() > 0 && asciiHeaderName.byteAt(0) == 58);
      } 
      return (headerName.length() > 0 && headerName.charAt(0) == ':');
    }
    
    public static boolean isPseudoHeader(CharSequence header) {
      return PSEUDO_HEADERS.contains(header);
    }
    
    public static PseudoHeaderName getPseudoHeader(CharSequence header) {
      return (PseudoHeaderName)PSEUDO_HEADERS.get(header);
    }
    
    public boolean isRequestOnly() {
      return this.requestOnly;
    }
  }
}
