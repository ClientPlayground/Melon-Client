package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.TypeParameterMatcher;
import java.util.List;

public abstract class MessageToMessageEncoder<I> extends ChannelOutboundHandlerAdapter {
  private final TypeParameterMatcher matcher;
  
  protected MessageToMessageEncoder() {
    this.matcher = TypeParameterMatcher.find(this, MessageToMessageEncoder.class, "I");
  }
  
  protected MessageToMessageEncoder(Class<? extends I> outboundMessageType) {
    this.matcher = TypeParameterMatcher.get(outboundMessageType);
  }
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return this.matcher.match(msg);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    CodecOutputList out = null;
    try {
      if (acceptOutboundMessage(msg)) {
        out = CodecOutputList.newInstance();
        I cast = (I)msg;
        try {
          encode(ctx, cast, out);
        } finally {
          ReferenceCountUtil.release(cast);
        } 
        if (out.isEmpty()) {
          out.recycle();
          out = null;
          throw new EncoderException(
              StringUtil.simpleClassName(this) + " must produce at least one message.");
        } 
      } else {
        ctx.write(msg, promise);
      } 
    } catch (EncoderException e) {
      throw e;
    } catch (Throwable t) {
      throw new EncoderException(t);
    } finally {
      if (out != null) {
        int sizeMinusOne = out.size() - 1;
        if (sizeMinusOne == 0) {
          ctx.write(out.get(0), promise);
        } else if (sizeMinusOne > 0) {
          ChannelPromise voidPromise = ctx.voidPromise();
          boolean isVoidPromise = (promise == voidPromise);
          for (int i = 0; i < sizeMinusOne; i++) {
            ChannelPromise p;
            if (isVoidPromise) {
              p = voidPromise;
            } else {
              p = ctx.newPromise();
            } 
            ctx.write(out.getUnsafe(i), p);
          } 
          ctx.write(out.getUnsafe(sizeMinusOne), promise);
        } 
        out.recycle();
      } 
    } 
  }
  
  protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, List<Object> paramList) throws Exception;
}
