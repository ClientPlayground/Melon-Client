package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.handler.codec.http.HttpRequest;
import java.nio.charset.Charset;

public interface HttpDataFactory {
  void setMaxLimit(long paramLong);
  
  Attribute createAttribute(HttpRequest paramHttpRequest, String paramString);
  
  Attribute createAttribute(HttpRequest paramHttpRequest, String paramString, long paramLong);
  
  Attribute createAttribute(HttpRequest paramHttpRequest, String paramString1, String paramString2);
  
  FileUpload createFileUpload(HttpRequest paramHttpRequest, String paramString1, String paramString2, String paramString3, String paramString4, Charset paramCharset, long paramLong);
  
  void removeHttpDataFromClean(HttpRequest paramHttpRequest, InterfaceHttpData paramInterfaceHttpData);
  
  void cleanRequestHttpData(HttpRequest paramHttpRequest);
  
  void cleanAllHttpData();
  
  @Deprecated
  void cleanRequestHttpDatas(HttpRequest paramHttpRequest);
  
  @Deprecated
  void cleanAllHttpDatas();
}
