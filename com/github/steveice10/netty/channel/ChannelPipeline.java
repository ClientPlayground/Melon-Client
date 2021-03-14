package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.concurrent.EventExecutorGroup;
import java.util.List;
import java.util.Map;

public interface ChannelPipeline extends ChannelInboundInvoker, ChannelOutboundInvoker, Iterable<Map.Entry<String, ChannelHandler>> {
  ChannelPipeline addFirst(String paramString, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addFirst(EventExecutorGroup paramEventExecutorGroup, String paramString, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addLast(String paramString, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addLast(EventExecutorGroup paramEventExecutorGroup, String paramString, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addBefore(String paramString1, String paramString2, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addBefore(EventExecutorGroup paramEventExecutorGroup, String paramString1, String paramString2, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addAfter(String paramString1, String paramString2, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addAfter(EventExecutorGroup paramEventExecutorGroup, String paramString1, String paramString2, ChannelHandler paramChannelHandler);
  
  ChannelPipeline addFirst(ChannelHandler... paramVarArgs);
  
  ChannelPipeline addFirst(EventExecutorGroup paramEventExecutorGroup, ChannelHandler... paramVarArgs);
  
  ChannelPipeline addLast(ChannelHandler... paramVarArgs);
  
  ChannelPipeline addLast(EventExecutorGroup paramEventExecutorGroup, ChannelHandler... paramVarArgs);
  
  ChannelPipeline remove(ChannelHandler paramChannelHandler);
  
  ChannelHandler remove(String paramString);
  
  <T extends ChannelHandler> T remove(Class<T> paramClass);
  
  ChannelHandler removeFirst();
  
  ChannelHandler removeLast();
  
  ChannelPipeline replace(ChannelHandler paramChannelHandler1, String paramString, ChannelHandler paramChannelHandler2);
  
  ChannelHandler replace(String paramString1, String paramString2, ChannelHandler paramChannelHandler);
  
  <T extends ChannelHandler> T replace(Class<T> paramClass, String paramString, ChannelHandler paramChannelHandler);
  
  ChannelHandler first();
  
  ChannelHandlerContext firstContext();
  
  ChannelHandler last();
  
  ChannelHandlerContext lastContext();
  
  ChannelHandler get(String paramString);
  
  <T extends ChannelHandler> T get(Class<T> paramClass);
  
  ChannelHandlerContext context(ChannelHandler paramChannelHandler);
  
  ChannelHandlerContext context(String paramString);
  
  ChannelHandlerContext context(Class<? extends ChannelHandler> paramClass);
  
  Channel channel();
  
  List<String> names();
  
  Map<String, ChannelHandler> toMap();
  
  ChannelPipeline fireChannelRegistered();
  
  ChannelPipeline fireChannelUnregistered();
  
  ChannelPipeline fireChannelActive();
  
  ChannelPipeline fireChannelInactive();
  
  ChannelPipeline fireExceptionCaught(Throwable paramThrowable);
  
  ChannelPipeline fireUserEventTriggered(Object paramObject);
  
  ChannelPipeline fireChannelRead(Object paramObject);
  
  ChannelPipeline fireChannelReadComplete();
  
  ChannelPipeline fireChannelWritabilityChanged();
  
  ChannelPipeline flush();
}
