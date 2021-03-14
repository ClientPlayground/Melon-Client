package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import org.jboss.marshalling.Unmarshaller;

public interface UnmarshallerProvider {
  Unmarshaller getUnmarshaller(ChannelHandlerContext paramChannelHandlerContext) throws Exception;
}
