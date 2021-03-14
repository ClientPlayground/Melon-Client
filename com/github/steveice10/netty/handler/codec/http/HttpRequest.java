package com.github.steveice10.netty.handler.codec.http;

public interface HttpRequest extends HttpMessage {
  @Deprecated
  HttpMethod getMethod();
  
  HttpMethod method();
  
  HttpRequest setMethod(HttpMethod paramHttpMethod);
  
  @Deprecated
  String getUri();
  
  String uri();
  
  HttpRequest setUri(String paramString);
  
  HttpRequest setProtocolVersion(HttpVersion paramHttpVersion);
}
