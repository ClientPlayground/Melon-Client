package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.handler.codec.http.cookie.ClientCookieEncoder;
import com.github.steveice10.netty.handler.codec.http.cookie.Cookie;

@Deprecated
public final class ClientCookieEncoder {
  @Deprecated
  public static String encode(String name, String value) {
    return ClientCookieEncoder.LAX.encode(name, value);
  }
  
  @Deprecated
  public static String encode(Cookie cookie) {
    return ClientCookieEncoder.LAX.encode(cookie);
  }
  
  @Deprecated
  public static String encode(Cookie... cookies) {
    return ClientCookieEncoder.LAX.encode((Cookie[])cookies);
  }
  
  @Deprecated
  public static String encode(Iterable<Cookie> cookies) {
    return ClientCookieEncoder.LAX.encode(cookies);
  }
}
