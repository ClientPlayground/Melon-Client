package com.github.steveice10.netty.handler.codec.http.cookie;

public abstract class CookieEncoder {
  protected final boolean strict;
  
  protected CookieEncoder(boolean strict) {
    this.strict = strict;
  }
  
  protected void validateCookie(String name, String value) {
    if (this.strict) {
      int pos;
      if ((pos = CookieUtil.firstInvalidCookieNameOctet(name)) >= 0)
        throw new IllegalArgumentException("Cookie name contains an invalid char: " + name.charAt(pos)); 
      CharSequence unwrappedValue = CookieUtil.unwrapValue(value);
      if (unwrappedValue == null)
        throw new IllegalArgumentException("Cookie value wrapping quotes are not balanced: " + value); 
      if ((pos = CookieUtil.firstInvalidCookieValueOctet(unwrappedValue)) >= 0)
        throw new IllegalArgumentException("Cookie value contains an invalid char: " + value.charAt(pos)); 
    } 
  }
}
