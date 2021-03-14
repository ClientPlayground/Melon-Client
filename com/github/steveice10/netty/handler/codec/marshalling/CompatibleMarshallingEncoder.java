package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import org.jboss.marshalling.Marshaller;

@Sharable
public class CompatibleMarshallingEncoder extends MessageToByteEncoder<Object> {
  private final MarshallerProvider provider;
  
  public CompatibleMarshallingEncoder(MarshallerProvider provider) {
    this.provider = provider;
  }
  
  protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
    Marshaller marshaller = this.provider.getMarshaller(ctx);
    marshaller.start(new ChannelBufferByteOutput(out));
    marshaller.writeObject(msg);
    marshaller.finish();
    marshaller.close();
  }
}
