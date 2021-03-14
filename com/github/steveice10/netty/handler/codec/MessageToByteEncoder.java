package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundHandlerAdapter;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.internal.TypeParameterMatcher;

public abstract class MessageToByteEncoder<I> extends ChannelOutboundHandlerAdapter {
  private final TypeParameterMatcher matcher;
  
  private final boolean preferDirect;
  
  protected MessageToByteEncoder() {
    this(true);
  }
  
  protected MessageToByteEncoder(Class<? extends I> outboundMessageType) {
    this(outboundMessageType, true);
  }
  
  protected MessageToByteEncoder(boolean preferDirect) {
    this.matcher = TypeParameterMatcher.find(this, MessageToByteEncoder.class, "I");
    this.preferDirect = preferDirect;
  }
  
  protected MessageToByteEncoder(Class<? extends I> outboundMessageType, boolean preferDirect) {
    this.matcher = TypeParameterMatcher.get(outboundMessageType);
    this.preferDirect = preferDirect;
  }
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return this.matcher.match(msg);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    ByteBuf buf = null;
    try {
      if (acceptOutboundMessage(msg)) {
        I cast = (I)msg;
        buf = allocateBuffer(ctx, cast, this.preferDirect);
        try {
          encode(ctx, cast, buf);
        } finally {
          ReferenceCountUtil.release(cast);
        } 
        if (buf.isReadable()) {
          ctx.write(buf, promise);
        } else {
          buf.release();
          ctx.write(Unpooled.EMPTY_BUFFER, promise);
        } 
        buf = null;
      } else {
        ctx.write(msg, promise);
      } 
    } catch (EncoderException e) {
      throw e;
    } catch (Throwable e) {
      throw new EncoderException(e);
    } finally {
      if (buf != null)
        buf.release(); 
    } 
  }
  
  protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, I msg, boolean preferDirect) throws Exception {
    if (preferDirect)
      return ctx.alloc().ioBuffer(); 
    return ctx.alloc().heapBuffer();
  }
  
  protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, ByteBuf paramByteBuf) throws Exception;
  
  protected boolean isPreferDirect() {
    return this.preferDirect;
  }
}
