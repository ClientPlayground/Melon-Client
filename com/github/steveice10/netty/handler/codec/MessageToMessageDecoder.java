package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.internal.TypeParameterMatcher;
import java.util.List;

public abstract class MessageToMessageDecoder<I> extends ChannelInboundHandlerAdapter {
  private final TypeParameterMatcher matcher;
  
  protected MessageToMessageDecoder() {
    this.matcher = TypeParameterMatcher.find(this, MessageToMessageDecoder.class, "I");
  }
  
  protected MessageToMessageDecoder(Class<? extends I> inboundMessageType) {
    this.matcher = TypeParameterMatcher.get(inboundMessageType);
  }
  
  public boolean acceptInboundMessage(Object msg) throws Exception {
    return this.matcher.match(msg);
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    CodecOutputList out = CodecOutputList.newInstance();
    try {
      if (acceptInboundMessage(msg)) {
        I cast = (I)msg;
        try {
          decode(ctx, cast, out);
        } finally {
          ReferenceCountUtil.release(cast);
        } 
      } else {
        out.add(msg);
      } 
    } catch (DecoderException e) {
      throw e;
    } catch (Exception e) {
      throw new DecoderException(e);
    } finally {
      int size = out.size();
      for (int i = 0; i < size; i++)
        ctx.fireChannelRead(out.getUnsafe(i)); 
      out.recycle();
    } 
  }
  
  protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, I paramI, List<Object> paramList) throws Exception;
}
