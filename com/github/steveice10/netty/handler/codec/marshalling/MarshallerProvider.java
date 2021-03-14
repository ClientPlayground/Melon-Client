package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import org.jboss.marshalling.Marshaller;

public interface MarshallerProvider {
  Marshaller getMarshaller(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
}
