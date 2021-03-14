package com.github.steveice10.netty.handler.codec.marshalling;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.Unmarshaller;

public class MarshallingDecoder extends LengthFieldBasedFrameDecoder {
  private final UnmarshallerProvider provider;
  
  public MarshallingDecoder(UnmarshallerProvider provider) {
    this(provider, 1048576);
  }
  
  public MarshallingDecoder(UnmarshallerProvider provider, int maxObjectSize) {
    super(maxObjectSize, 0, 4, 0, 4);
    this.provider = provider;
  }
  
  protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
    ByteBuf frame = (ByteBuf)super.decode(ctx, in);
    if (frame == null)
      return null; 
    Unmarshaller unmarshaller = this.provider.getUnmarshaller(ctx);
    ByteInput input = new ChannelBufferByteInput(frame);
    try {
      unmarshaller.start(input);
      Object obj = unmarshaller.readObject();
      unmarshaller.finish();
      return obj;
    } finally {
      unmarshaller.close();
    } 
  }
  
  protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
    return buffer.slice(index, length);
  }
}
