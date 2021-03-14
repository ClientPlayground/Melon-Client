package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.util.ReferenceCounted;

public interface InterfaceHttpData extends Comparable<InterfaceHttpData>, ReferenceCounted {
  String getName();
  
  HttpDataType getHttpDataType();
  
  InterfaceHttpData retain();
  
  InterfaceHttpData retain(int paramInt);
  
  InterfaceHttpData touch();
  
  InterfaceHttpData touch(Object paramObject);
  
  public enum HttpDataType {
    Attribute, FileUpload, InternalAttribute;
  }
}
