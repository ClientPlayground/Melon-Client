package com.github.steveice10.netty.channel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface ChannelHandler {
  void handlerAdded(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
  
  void handlerRemoved(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
  
  @Deprecated
  void exceptionCaught(ChannelHandlerContext paramChannelHandlerContext, Throwable paramThrowable) throws Exception;
  
  @Inherited
  @Documented
  @Target({ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Sharable {}
}
