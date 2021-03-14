package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;

public class DefaultUnmarshallerProvider implements UnmarshallerProvider {
  private final MarshallerFactory factory;
  
  private final MarshallingConfiguration config;
  
  public DefaultUnmarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config) {
    this.factory = factory;
    this.config = config;
  }
  
  public Unmarshaller getUnmarshaller(ChannelHandlerContext ctx) throws Exception {
    return this.factory.createUnmarshaller(this.config);
  }
}
