package com.github.steveice10.netty.handler.codec.http.cookie;

public interface Cookie extends Comparable<Cookie> {
  public static final long UNDEFINED_MAX_AGE = -9223372036854775808L;
  
  String name();
  
  String value();
  
  void setValue(String paramString);
  
  boolean wrap();
  
  void setWrap(boolean paramBoolean);
  
  String domain();
  
  void setDomain(String paramString);
  
  String path();
  
  void setPath(String paramString);
  
  long maxAge();
  
  void setMaxAge(long paramLong);
  
  boolean isSecure();
  
  void setSecure(boolean paramBoolean);
  
  boolean isHttpOnly();
  
  void setHttpOnly(boolean paramBoolean);
}
