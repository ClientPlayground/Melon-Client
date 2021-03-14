package com.github.steveice10.netty.handler.codec.http;

import com.github.steveice10.netty.handler.codec.http.cookie.Cookie;
import java.util.Set;

@Deprecated
public interface Cookie extends Cookie {
  @Deprecated
  String getName();
  
  @Deprecated
  String getValue();
  
  @Deprecated
  String getDomain();
  
  @Deprecated
  String getPath();
  
  @Deprecated
  String getComment();
  
  @Deprecated
  String comment();
  
  @Deprecated
  void setComment(String paramString);
  
  @Deprecated
  long getMaxAge();
  
  @Deprecated
  long maxAge();
  
  @Deprecated
  void setMaxAge(long paramLong);
  
  @Deprecated
  int getVersion();
  
  @Deprecated
  int version();
  
  @Deprecated
  void setVersion(int paramInt);
  
  @Deprecated
  String getCommentUrl();
  
  @Deprecated
  String commentUrl();
  
  @Deprecated
  void setCommentUrl(String paramString);
  
  @Deprecated
  boolean isDiscard();
  
  @Deprecated
  void setDiscard(boolean paramBoolean);
  
  @Deprecated
  Set<Integer> getPorts();
  
  @Deprecated
  Set<Integer> ports();
  
  @Deprecated
  void setPorts(int... paramVarArgs);
  
  @Deprecated
  void setPorts(Iterable<Integer> paramIterable);
}
