package com.github.steveice10.netty.channel;

public interface ChannelInboundInvoker {
  ChannelInboundInvoker fireChannelRegistered();
  
  ChannelInboundInvoker fireChannelUnregistered();
  
  ChannelInboundInvoker fireChannelActive();
  
  ChannelInboundInvoker fireChannelInactive();
  
  ChannelInboundInvoker fireExceptionCaught(Throwable paramThrowable);
  
  ChannelInboundInvoker fireUserEventTriggered(Object paramObject);
  
  ChannelInboundInvoker fireChannelRead(Object paramObject);
  
  ChannelInboundInvoker fireChannelReadComplete();
  
  ChannelInboundInvoker fireChannelWritabilityChanged();
}
