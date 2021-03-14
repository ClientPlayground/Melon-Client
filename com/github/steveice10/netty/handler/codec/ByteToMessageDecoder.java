package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.CompositeByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelInboundHandlerAdapter;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.List;

public abstract class ByteToMessageDecoder extends ChannelInboundHandlerAdapter {
  public static final Cumulator MERGE_CUMULATOR = new Cumulator() {
      public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
        ByteBuf buffer;
        if (cumulation.writerIndex() > cumulation.maxCapacity() - in.readableBytes() || cumulation
          .refCnt() > 1 || cumulation.isReadOnly()) {
          buffer = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes());
        } else {
          buffer = cumulation;
        } 
        buffer.writeBytes(in);
        in.release();
        return buffer;
      }
    };
  
  public static final Cumulator COMPOSITE_CUMULATOR = new Cumulator() {
      public ByteBuf cumulate(ByteBufAllocator alloc, ByteBuf cumulation, ByteBuf in) {
        CompositeByteBuf compositeByteBuf;
        if (cumulation.refCnt() > 1) {
          ByteBuf buffer = ByteToMessageDecoder.expandCumulation(alloc, cumulation, in.readableBytes());
          buffer.writeBytes(in);
          in.release();
        } else {
          CompositeByteBuf composite;
          if (cumulation instanceof CompositeByteBuf) {
            composite = (CompositeByteBuf)cumulation;
          } else {
            composite = alloc.compositeBuffer(2147483647);
            composite.addComponent(true, cumulation);
          } 
          composite.addComponent(true, in);
          compositeByteBuf = composite;
        } 
        return (ByteBuf)compositeByteBuf;
      }
    };
  
  private static final byte STATE_INIT = 0;
  
  private static final byte STATE_CALLING_CHILD_DECODE = 1;
  
  private static final byte STATE_HANDLER_REMOVED_PENDING = 2;
  
  ByteBuf cumulation;
  
  private Cumulator cumulator = MERGE_CUMULATOR;
  
  private boolean singleDecode;
  
  private boolean decodeWasNull;
  
  private boolean first;
  
  private byte decodeState = 0;
  
  private int discardAfterReads = 16;
  
  private int numReads;
  
  protected ByteToMessageDecoder() {
    ensureNotSharable();
  }
  
  public void setSingleDecode(boolean singleDecode) {
    this.singleDecode = singleDecode;
  }
  
  public boolean isSingleDecode() {
    return this.singleDecode;
  }
  
  public void setCumulator(Cumulator cumulator) {
    if (cumulator == null)
      throw new NullPointerException("cumulator"); 
    this.cumulator = cumulator;
  }
  
  public void setDiscardAfterReads(int discardAfterReads) {
    if (discardAfterReads <= 0)
      throw new IllegalArgumentException("discardAfterReads must be > 0"); 
    this.discardAfterReads = discardAfterReads;
  }
  
  protected int actualReadableBytes() {
    return internalBuffer().readableBytes();
  }
  
  protected ByteBuf internalBuffer() {
    if (this.cumulation != null)
      return this.cumulation; 
    return Unpooled.EMPTY_BUFFER;
  }
  
  public final void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    if (this.decodeState == 1) {
      this.decodeState = 2;
      return;
    } 
    ByteBuf buf = this.cumulation;
    if (buf != null) {
      this.cumulation = null;
      int readable = buf.readableBytes();
      if (readable > 0) {
        ByteBuf bytes = buf.readBytes(readable);
        buf.release();
        ctx.fireChannelRead(bytes);
      } else {
        buf.release();
      } 
      this.numReads = 0;
      ctx.fireChannelReadComplete();
    } 
    handlerRemoved0(ctx);
  }
  
  protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {}
  
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    if (msg instanceof ByteBuf) {
      CodecOutputList out = CodecOutputList.newInstance();
      try {
        ByteBuf data = (ByteBuf)msg;
        this.first = (this.cumulation == null);
        if (this.first) {
          this.cumulation = data;
        } else {
          this.cumulation = this.cumulator.cumulate(ctx.alloc(), this.cumulation, data);
        } 
        callDecode(ctx, this.cumulation, out);
      } catch (DecoderException e) {
        throw e;
      } catch (Exception e) {
        throw new DecoderException(e);
      } finally {
        if (this.cumulation != null && !this.cumulation.isReadable()) {
          this.numReads = 0;
          this.cumulation.release();
          this.cumulation = null;
        } else if (++this.numReads >= this.discardAfterReads) {
          this.numReads = 0;
          discardSomeReadBytes();
        } 
        int size = out.size();
        this.decodeWasNull = !out.insertSinceRecycled();
        fireChannelRead(ctx, out, size);
        out.recycle();
      } 
    } else {
      ctx.fireChannelRead(msg);
    } 
  }
  
  static void fireChannelRead(ChannelHandlerContext ctx, List<Object> msgs, int numElements) {
    if (msgs instanceof CodecOutputList) {
      fireChannelRead(ctx, (CodecOutputList)msgs, numElements);
    } else {
      for (int i = 0; i < numElements; i++)
        ctx.fireChannelRead(msgs.get(i)); 
    } 
  }
  
  static void fireChannelRead(ChannelHandlerContext ctx, CodecOutputList msgs, int numElements) {
    for (int i = 0; i < numElements; i++)
      ctx.fireChannelRead(msgs.getUnsafe(i)); 
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    this.numReads = 0;
    discardSomeReadBytes();
    if (this.decodeWasNull) {
      this.decodeWasNull = false;
      if (!ctx.channel().config().isAutoRead())
        ctx.read(); 
    } 
    ctx.fireChannelReadComplete();
  }
  
  protected final void discardSomeReadBytes() {
    if (this.cumulation != null && !this.first && this.cumulation.refCnt() == 1)
      this.cumulation.discardSomeReadBytes(); 
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    channelInputClosed(ctx, true);
  }
  
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof com.github.steveice10.netty.channel.socket.ChannelInputShutdownEvent)
      channelInputClosed(ctx, false); 
    super.userEventTriggered(ctx, evt);
  }
  
  private void channelInputClosed(ChannelHandlerContext ctx, boolean callChannelInactive) throws Exception {
    CodecOutputList out = CodecOutputList.newInstance();
    try {
      channelInputClosed(ctx, out);
    } catch (DecoderException e) {
      throw e;
    } catch (Exception e) {
      throw new DecoderException(e);
    } finally {
      try {
        if (this.cumulation != null) {
          this.cumulation.release();
          this.cumulation = null;
        } 
        int size = out.size();
        fireChannelRead(ctx, out, size);
        if (size > 0)
          ctx.fireChannelReadComplete(); 
        if (callChannelInactive)
          ctx.fireChannelInactive(); 
      } finally {
        out.recycle();
      } 
    } 
  }
  
  void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception {
    if (this.cumulation != null) {
      callDecode(ctx, this.cumulation, out);
      decodeLast(ctx, this.cumulation, out);
    } else {
      decodeLast(ctx, Unpooled.EMPTY_BUFFER, out);
    } 
  }
  
  protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    try {
      while (in.isReadable()) {
        int outSize = out.size();
        if (outSize > 0) {
          fireChannelRead(ctx, out, outSize);
          out.clear();
          if (ctx.isRemoved())
            break; 
          outSize = 0;
        } 
        int oldInputLength = in.readableBytes();
        decodeRemovalReentryProtection(ctx, in, out);
        if (ctx.isRemoved())
          break; 
        if (outSize == out.size()) {
          if (oldInputLength == in.readableBytes())
            break; 
          continue;
        } 
        if (oldInputLength == in.readableBytes())
          throw new DecoderException(
              StringUtil.simpleClassName(getClass()) + ".decode() did not read anything but decoded a message."); 
        if (isSingleDecode())
          break; 
      } 
    } catch (DecoderException e) {
      throw e;
    } catch (Exception cause) {
      throw new DecoderException(cause);
    } 
  }
  
  protected abstract void decode(ChannelHandlerContext paramChannelHandlerContext, ByteBuf paramByteBuf, List<Object> paramList) throws Exception;
  
  final void decodeRemovalReentryProtection(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    this.decodeState = 1;
    try {
      decode(ctx, in, out);
    } finally {
      boolean removePending = (this.decodeState == 2);
      this.decodeState = 0;
      if (removePending)
        handlerRemoved(ctx); 
    } 
  }
  
  protected void decodeLast(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.isReadable())
      decodeRemovalReentryProtection(ctx, in, out); 
  }
  
  static ByteBuf expandCumulation(ByteBufAllocator alloc, ByteBuf cumulation, int readable) {
    ByteBuf oldCumulation = cumulation;
    cumulation = alloc.buffer(oldCumulation.readableBytes() + readable);
    cumulation.writeBytes(oldCumulation);
    oldCumulation.release();
    return cumulation;
  }
  
  public static interface Cumulator {
    ByteBuf cumulate(ByteBufAllocator param1ByteBufAllocator, ByteBuf param1ByteBuf1, ByteBuf param1ByteBuf2);
  }
}
