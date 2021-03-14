package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.Attribute;
import com.github.steveice10.netty.util.AttributeKey;
import com.github.steveice10.netty.util.AttributeMap;
import com.github.steveice10.netty.util.concurrent.EventExecutor;

public interface ChannelHandlerContext extends AttributeMap, ChannelInboundInvoker, ChannelOutboundInvoker {
  Channel channel();
  
  EventExecutor executor();
  
  String name();
  
  ChannelHandler handler();
  
  boolean isRemoved();
  
  ChannelHandlerContext fireChannelRegistered();
  
  ChannelHandlerContext fireChannelUnregistered();
  
  ChannelHandlerContext fireChannelActive();
  
  ChannelHandlerContext fireChannelInactive();
  
  ChannelHandlerContext fireExceptionCaught(Throwable paramThrowable);
  
  ChannelHandlerContext fireUserEventTriggered(Object paramObject);
  
  ChannelHandlerContext fireChannelRead(Object paramObject);
  
  ChannelHandlerContext fireChannelReadComplete();
  
  ChannelHandlerContext fireChannelWritabilityChanged();
  
  ChannelHandlerContext read();
  
  ChannelHandlerContext flush();
  
  ChannelPipeline pipeline();
  
  ByteBufAllocator alloc();
  
  @Deprecated
  <T> Attribute<T> attr(AttributeKey<T> paramAttributeKey);
  
  @Deprecated
  <T> boolean hasAttr(AttributeKey<T> paramAttributeKey);
}
