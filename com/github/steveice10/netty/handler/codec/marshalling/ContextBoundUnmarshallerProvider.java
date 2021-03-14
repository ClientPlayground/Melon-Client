package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.Attribute;
import com.github.steveice10.netty.util.AttributeKey;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;

public class ContextBoundUnmarshallerProvider extends DefaultUnmarshallerProvider {
  private static final AttributeKey<Unmarshaller> UNMARSHALLER = AttributeKey.valueOf(ContextBoundUnmarshallerProvider.class, "UNMARSHALLER");
  
  public ContextBoundUnmarshallerProvider(MarshallerFactory factory, MarshallingConfiguration config) {
    super(factory, config);
  }
  
  public Unmarshaller getUnmarshaller(ChannelHandlerContext ctx) throws Exception {
    Attribute<Unmarshaller> attr = ctx.channel().attr(UNMARSHALLER);
    Unmarshaller unmarshaller = (Unmarshaller)attr.get();
    if (unmarshaller == null) {
      unmarshaller = super.getUnmarshaller(ctx);
      attr.set(unmarshaller);
    } 
    return unmarshaller;
  }
}
