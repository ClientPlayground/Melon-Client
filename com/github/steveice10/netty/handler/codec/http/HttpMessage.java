package com.github.steveice10.netty.handler.codec.http;

public interface HttpMessage extends HttpObject {
  @Deprecated
  HttpVersion getProtocolVersion();
  
  HttpVersion protocolVersion();
  
  HttpMessage setProtocolVersion(HttpVersion paramHttpVersion);
  
  HttpHeaders headers();
}
