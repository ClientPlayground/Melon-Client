package com.github.steveice10.netty.handler.codec.http.multipart;

import com.github.steveice10.netty.handler.codec.http.HttpContent;
import java.util.List;

public interface InterfaceHttpPostRequestDecoder {
  boolean isMultipart();
  
  void setDiscardThreshold(int paramInt);
  
  int getDiscardThreshold();
  
  List<InterfaceHttpData> getBodyHttpDatas();
  
  List<InterfaceHttpData> getBodyHttpDatas(String paramString);
  
  InterfaceHttpData getBodyHttpData(String paramString);
  
  InterfaceHttpPostRequestDecoder offer(HttpContent paramHttpContent);
  
  boolean hasNext();
  
  InterfaceHttpData next();
  
  InterfaceHttpData currentPartialHttpData();
  
  void destroy();
  
  void cleanFiles();
  
  void removeHttpDataFromClean(InterfaceHttpData paramInterfaceHttpData);
}
