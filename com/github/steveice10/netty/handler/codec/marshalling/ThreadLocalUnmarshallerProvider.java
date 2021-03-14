package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.concurrent.FastThreadLocal;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;

public class ThreadLocalUnmarshallerProvider implements UnmarshallerProvider {
  private final FastThreadLocal<Unmarshaller> unmarshallers = new FastThreadLocal();
  
  private final MarshallerFactory factory;
  
  private final MarshallingConfiguration config;
  
  public ThreadLocalUnmarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config) {
    this.factory = factory;
    this.config = config;
  }
  
  public Unmarshaller getUnmarshaller(ChannelHandlerContext ctx) throws Exception {
    Unmarshaller unmarshaller = (Unmarshaller)this.unmarshallers.get();
    if (unmarshaller == null) {
      unmarshaller = this.factory.createUnmarshaller(this.config);
      this.unmarshallers.set(unmarshaller);
    } 
    return unmarshaller;
  }
}
