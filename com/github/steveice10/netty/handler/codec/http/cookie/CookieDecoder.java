package com.github.steveice10.netty.handler.codec.http.cookie;

import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.CharBuffer;

public abstract class CookieDecoder {
  private final InternalLogger logger = InternalLoggerFactory.getInstance(getClass());
  
  private final boolean strict;
  
  protected CookieDecoder(boolean strict) {
    this.strict = strict;
  }
  
  protected DefaultCookie initCookie(String header, int nameBegin, int nameEnd, int valueBegin, int valueEnd) {
    if (nameBegin == -1 || nameBegin == nameEnd) {
      this.logger.debug("Skipping cookie with null name");
      return null;
    } 
    if (valueBegin == -1) {
      this.logger.debug("Skipping cookie with null value");
      return null;
    } 
    CharSequence wrappedValue = CharBuffer.wrap(header, valueBegin, valueEnd);
    CharSequence unwrappedValue = CookieUtil.unwrapValue(wrappedValue);
    if (unwrappedValue == null) {
      this.logger.debug("Skipping cookie because starting quotes are not properly balanced in '{}'", wrappedValue);
      return null;
    } 
    String name = header.substring(nameBegin, nameEnd);
    int invalidOctetPos;
    if (this.strict && (invalidOctetPos = CookieUtil.firstInvalidCookieNameOctet(name)) >= 0) {
      if (this.logger.isDebugEnabled())
        this.logger.debug("Skipping cookie because name '{}' contains invalid char '{}'", name, 
            Character.valueOf(name.charAt(invalidOctetPos))); 
      return null;
    } 
    boolean wrap = (unwrappedValue.length() != valueEnd - valueBegin);
    if (this.strict && (invalidOctetPos = CookieUtil.firstInvalidCookieValueOctet(unwrappedValue)) >= 0) {
      if (this.logger.isDebugEnabled())
        this.logger.debug("Skipping cookie because value '{}' contains invalid char '{}'", unwrappedValue, 
            Character.valueOf(unwrappedValue.charAt(invalidOctetPos))); 
      return null;
    } 
    DefaultCookie cookie = new DefaultCookie(name, unwrappedValue.toString());
    cookie.setWrap(wrap);
    return cookie;
  }
}
