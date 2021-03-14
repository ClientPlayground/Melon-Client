package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.handler.codec.http.cookie.Cookie;
import com.github.steveice10.netty.handler.codec.http.cookie.ServerCookieEncoder;
import java.util.Collection;
import java.util.List;

@Deprecated
public final class ServerCookieEncoder {
  @Deprecated
  public static String encode(String name, String value) {
    return ServerCookieEncoder.LAX.encode(name, value);
  }
  
  @Deprecated
  public static String encode(Cookie cookie) {
    return ServerCookieEncoder.LAX.encode(cookie);
  }
  
  @Deprecated
  public static List<String> encode(Cookie... cookies) {
    return ServerCookieEncoder.LAX.encode((Cookie[])cookies);
  }
  
  @Deprecated
  public static List<String> encode(Collection<Cookie> cookies) {
    return ServerCookieEncoder.LAX.encode(cookies);
  }
  
  @Deprecated
  public static List<String> encode(Iterable<Cookie> cookies) {
    return ServerCookieEncoder.LAX.encode(cookies);
  }
}
