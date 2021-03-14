package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.internal.TypeParameterMatcher;
import java.util.List;

public abstract class ByteToMessageCodec<I> extends ChannelDuplexHandler {
  private final TypeParameterMatcher outboundMsgMatcher;
  
  private final MessageToByteEncoder<I> encoder;
  
  private final ByteToMessageDecoder decoder = new ByteToMessageDecoder() {
      public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteToMessageCodec.this.decode(ctx, in, out);
      }
      
      protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteToMessageCodec.this.decodeLast(ctx, in, out);
      }
    };
  
  protected ByteToMessageCodec() {
    this(true);
  }
  
  protected ByteToMessageCodec(Class<? extends I> outboundMessageType) {
    this(outboundMessageType, true);
  }
  
  protected ByteToMessageCodec(boolean preferDirect) {
    ensureNotSharable();
    this.outboundMsgMatcher = TypeParameterMatcher.find(this, ByteToMessageCodec.class, "I");
    this.encoder = new Encoder(preferDirect);
  }
  
  protected ByteToMessageCodec(Class<? extends I> outboundMessageType, boolean preferDirect) {
    ensureNotSharable();
    this.outboundMsgMatcher = TypeParameterMatcher.get(outboundMessageType);
    this.encoder = new Encoder(preferDirect);
  }
  
  public boolean acceptOutboundMessage(Object msg) throws Exception {
    return this.outboundMsgMatcher.match(msg);
  }
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    this.decoder.channelRead(ctx, msg);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    this.encoder.write(ctx, msg, promise);
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelReadComplete(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    this.decoder.channelInactive(ctx);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    try {
      this.decoder.handlerAdded(ctx);
    } finally {
      this.encoder.handlerAdded(ctx);
    } 
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    try {
      this.decoder.handlerRemoved(ctx);
    } finally {
      this.encoder.handlerRemoved(ctx);
    } 
  }
  
  protected abstract void encode(ChannelHandlerContext paramChannelHandlerContext, I paramI, ByteBuf paramByteBuf) throws Exception;
  
  protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, List<Object> paramList) throws Exception;
  
  protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.isReadable())
      decode(ctx, in, out); 
  }
  
  private final class Encoder extends MessageToByteEncoder<I> {
    Encoder(boolean preferDirect) {
      super(preferDirect);
    }
    
    public boolean acceptOutboundMessage(Object msg) throws Exception {
      return ByteToMessageCodec.this.acceptOutboundMessage(msg);
    }
    
    protected void encode(ChannelHandlerContext ctx, I msg, ByteBuf out) throws Exception {
      ByteToMessageCodec.this.encode(ctx, msg, out);
    }
  }
}
