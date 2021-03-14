package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandler.Sharable;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import org.jboss.marshalling.Marshaller;

@Sharable
public class MarshallingEncoder extends MessageToByteEncoder<Object> {
  private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
  
  private final MarshallerProvider provider;
  
  public MarshallingEncoder(MarshallerProvider provider) {
    this.provider = provider;
  }
  
  protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
    Marshaller marshaller = this.provider.getMarshaller(ctx);
    int lengthPos = out.writerIndex();
    out.writeBytes(LENGTH_PLACEHOLDER);
    ChannelBufferByteOutput output = new ChannelBufferByteOutput(out);
    marshaller.start(output);
    marshaller.writeObject(msg);
    marshaller.finish();
    marshaller.close();
    out.setInt(lengthPos, out.writerIndex() - lengthPos - 4);
  }
}
