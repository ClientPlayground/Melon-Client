package com.github.steveice10.netty.handler.codec.http.cookie;

import com.github.steveice10.netty.handler.codec.DateFormatter;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.Date;

public final class ClientCookieDecoder extends CookieDecoder {
  public static final ClientCookieDecoder STRICT = new ClientCookieDecoder(true);
  
  public static final ClientCookieDecoder LAX = new ClientCookieDecoder(false);
  
  private ClientCookieDecoder(boolean strict) {
    super(strict);
  }
  
  public Cookie decode(String header) {
    int headerLen = ((String)ObjectUtil.checkNotNull(header, "header")).length();
    if (headerLen == 0)
      return null; 
    CookieBuilder cookieBuilder = null;
    int i = 0;
    while (i != headerLen) {
      int nameEnd, valueEnd, valueBegin;
      char c = header.charAt(i);
      if (c == ',')
        break; 
      if (c == '\t' || c == '\n' || c == '\013' || c == '\f' || c == '\r' || c == ' ' || c == ';') {
        i++;
        continue;
      } 
      int nameBegin = i;
      while (true) {
        char curChar = header.charAt(i);
        if (curChar == ';') {
          nameEnd = i;
          valueBegin = valueEnd = -1;
          break;
        } 
        if (curChar == '=') {
          nameEnd = i;
          i++;
          if (i == headerLen) {
            int j = 0, k = j;
            break;
          } 
          valueBegin = i;
          int semiPos = header.indexOf(';', i);
          valueEnd = i = (semiPos > 0) ? semiPos : headerLen;
          break;
        } 
        i++;
        if (i == headerLen) {
          nameEnd = headerLen;
          valueBegin = valueEnd = -1;
          break;
        } 
      } 
      if (valueEnd > 0 && header.charAt(valueEnd - 1) == ',')
        valueEnd--; 
      if (cookieBuilder == null) {
        DefaultCookie cookie = initCookie(header, nameBegin, nameEnd, valueBegin, valueEnd);
        if (cookie == null)
          return null; 
        cookieBuilder = new CookieBuilder(cookie, header);
        continue;
      } 
      cookieBuilder.appendAttribute(nameBegin, nameEnd, valueBegin, valueEnd);
    } 
    return (cookieBuilder != null) ? cookieBuilder.cookie() : null;
  }
  
  private static class CookieBuilder {
    private final String header;
    
    private final DefaultCookie cookie;
    
    private String domain;
    
    private String path;
    
    private long maxAge = Long.MIN_VALUE;
    
    private int expiresStart;
    
    private int expiresEnd;
    
    private boolean secure;
    
    private boolean httpOnly;
    
    CookieBuilder(DefaultCookie cookie, String header) {
      this.cookie = cookie;
      this.header = header;
    }
    
    private long mergeMaxAgeAndExpires() {
      if (this.maxAge != Long.MIN_VALUE)
        return this.maxAge; 
      if (isValueDefined(this.expiresStart, this.expiresEnd)) {
        Date expiresDate = DateFormatter.parseHttpDate(this.header, this.expiresStart, this.expiresEnd);
        if (expiresDate != null) {
          long maxAgeMillis = expiresDate.getTime() - System.currentTimeMillis();
          return maxAgeMillis / 1000L + ((maxAgeMillis % 1000L != 0L) ? 1L : 0L);
        } 
      } 
      return Long.MIN_VALUE;
    }
    
    Cookie cookie() {
      this.cookie.setDomain(this.domain);
      this.cookie.setPath(this.path);
      this.cookie.setMaxAge(mergeMaxAgeAndExpires());
      this.cookie.setSecure(this.secure);
      this.cookie.setHttpOnly(this.httpOnly);
      return this.cookie;
    }
    
    void appendAttribute(int keyStart, int keyEnd, int valueStart, int valueEnd) {
      int length = keyEnd - keyStart;
      if (length == 4) {
        parse4(keyStart, valueStart, valueEnd);
      } else if (length == 6) {
        parse6(keyStart, valueStart, valueEnd);
      } else if (length == 7) {
        parse7(keyStart, valueStart, valueEnd);
      } else if (length == 8) {
        parse8(keyStart);
      } 
    }
    
    private void parse4(int nameStart, int valueStart, int valueEnd) {
      if (this.header.regionMatches(true, nameStart, "Path", 0, 4))
        this.path = computeValue(valueStart, valueEnd); 
    }
    
    private void parse6(int nameStart, int valueStart, int valueEnd) {
      if (this.header.regionMatches(true, nameStart, "Domain", 0, 5)) {
        this.domain = computeValue(valueStart, valueEnd);
      } else if (this.header.regionMatches(true, nameStart, "Secure", 0, 5)) {
        this.secure = true;
      } 
    }
    
    private void setMaxAge(String value) {
      try {
        this.maxAge = Math.max(Long.parseLong(value), 0L);
      } catch (NumberFormatException numberFormatException) {}
    }
    
    private void parse7(int nameStart, int valueStart, int valueEnd) {
      if (this.header.regionMatches(true, nameStart, "Expires", 0, 7)) {
        this.expiresStart = valueStart;
        this.expiresEnd = valueEnd;
      } else if (this.header.regionMatches(true, nameStart, "Max-Age", 0, 7)) {
        setMaxAge(computeValue(valueStart, valueEnd));
      } 
    }
    
    private void parse8(int nameStart) {
      if (this.header.regionMatches(true, nameStart, "HTTPOnly", 0, 8))
        this.httpOnly = true; 
    }
    
    private static boolean isValueDefined(int valueStart, int valueEnd) {
      return (valueStart != -1 && valueStart != valueEnd);
    }
    
    private String computeValue(int valueStart, int valueEnd) {
      return isValueDefined(valueStart, valueEnd) ? this.header.substring(valueStart, valueEnd) : null;
    }
  }
}
