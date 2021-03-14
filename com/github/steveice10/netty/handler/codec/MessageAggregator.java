package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;
import com.github.steveice10.netty.buffer.CompositeByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import java.util.List;

public abstract class MessageAggregator<I, S, C extends ByteBufHolder, O extends ByteBufHolder> extends MessageToMessageDecoder<I> {
  private static final int DEFAULT_MAX_COMPOSITEBUFFER_COMPONENTS = 1024;
  
  private final int maxContentLength;
  
  private O currentMessage;
  
  private boolean handlingOversizedMessage;
  
  private int maxCumulationBufferComponents = 1024;
  
  private ChannelHandlerContext ctx;
  
  private ChannelFutureListener continueResponseWriteListener;
  
  protected MessageAggregator(int maxContentLength) {
    validateMaxContentLength(maxContentLength);
    this.maxContentLength = maxContentLength;
  }
  
  protected MessageAggregator(int maxContentLength, Class<? extends I> inboundMessageType) {
    super(inboundMessageType);
    validateMaxContentLength(maxContentLength);
    this.maxContentLength = maxContentLength;
  }
  
  private static void validateMaxContentLength(int maxContentLength) {
    if (maxContentLength < 0)
      throw new IllegalArgumentException("maxContentLength: " + maxContentLength + " (expected: >= 0)"); 
  }
  
  public boolean acceptInboundMessage(Object msg) throws Exception {
    if (!super.acceptInboundMessage(msg))
      return false; 
    I in = (I)msg;
    return ((isContentMessage(in) || isStartMessage(in)) && !isAggregated(in));
  }
  
  protected abstract boolean isStartMessage(I paramI) throws Exception;
  
  protected abstract boolean isContentMessage(I paramI) throws Exception;
  
  protected abstract boolean isLastContentMessage(C paramC) throws Exception;
  
  protected abstract boolean isAggregated(I paramI) throws Exception;
  
  public final int maxContentLength() {
    return this.maxContentLength;
  }
  
  public final int maxCumulationBufferComponents() {
    return this.maxCumulationBufferComponents;
  }
  
  public final void setMaxCumulationBufferComponents(int maxCumulationBufferComponents) {
    if (maxCumulationBufferComponents < 2)
      throw new IllegalArgumentException("maxCumulationBufferComponents: " + maxCumulationBufferComponents + " (expected: >= 2)"); 
    if (this.ctx == null) {
      this.maxCumulationBufferComponents = maxCumulationBufferComponents;
    } else {
      throw new IllegalStateException("decoder properties cannot be changed once the decoder is added to a pipeline.");
    } 
  }
  
  @Deprecated
  public final boolean isHandlingOversizedMessage() {
    return this.handlingOversizedMessage;
  }
  
  protected final ChannelHandlerContext ctx() {
    if (this.ctx == null)
      throw new IllegalStateException("not added to a pipeline yet"); 
    return this.ctx;
  }
  
  protected void decode(final ChannelHandlerContext ctx, I msg, List<Object> out) throws Exception {
    if (isStartMessage(msg)) {
      this.handlingOversizedMessage = false;
      if (this.currentMessage != null) {
        this.currentMessage.release();
        this.currentMessage = null;
        throw new MessageAggregationException();
      } 
      I i = msg;
      Object continueResponse = newContinueResponse((S)i, this.maxContentLength, ctx.pipeline());
      if (continueResponse != null) {
        ChannelFutureListener listener = this.continueResponseWriteListener;
        if (listener == null)
          this.continueResponseWriteListener = listener = new ChannelFutureListener() {
              public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess())
                  ctx.fireExceptionCaught(future.cause()); 
              }
            }; 
        boolean closeAfterWrite = closeAfterContinueResponse(continueResponse);
        this.handlingOversizedMessage = ignoreContentAfterContinueResponse(continueResponse);
        ChannelFuture future = ctx.writeAndFlush(continueResponse).addListener((GenericFutureListener)listener);
        if (closeAfterWrite) {
          future.addListener((GenericFutureListener)ChannelFutureListener.CLOSE);
          return;
        } 
        if (this.handlingOversizedMessage)
          return; 
      } else if (isContentLengthInvalid((S)i, this.maxContentLength)) {
        invokeHandleOversizedMessage(ctx, (S)i);
        return;
      } 
      if (i instanceof DecoderResultProvider && !((DecoderResultProvider)i).decoderResult().isSuccess()) {
        O aggregated;
        if (i instanceof ByteBufHolder) {
          aggregated = beginAggregation((S)i, ((ByteBufHolder)i).content().retain());
        } else {
          aggregated = beginAggregation((S)i, Unpooled.EMPTY_BUFFER);
        } 
        finishAggregation(aggregated);
        out.add(aggregated);
        return;
      } 
      CompositeByteBuf content = ctx.alloc().compositeBuffer(this.maxCumulationBufferComponents);
      if (i instanceof ByteBufHolder)
        appendPartialContent(content, ((ByteBufHolder)i).content()); 
      this.currentMessage = beginAggregation((S)i, (ByteBuf)content);
    } else if (isContentMessage(msg)) {
      boolean last;
      if (this.currentMessage == null)
        return; 
      CompositeByteBuf content = (CompositeByteBuf)this.currentMessage.content();
      ByteBufHolder byteBufHolder = (ByteBufHolder)msg;
      if (content.readableBytes() > this.maxContentLength - byteBufHolder.content().readableBytes()) {
        O o = this.currentMessage;
        invokeHandleOversizedMessage(ctx, (S)o);
        return;
      } 
      appendPartialContent(content, byteBufHolder.content());
      aggregate(this.currentMessage, (C)byteBufHolder);
      if (byteBufHolder instanceof DecoderResultProvider) {
        DecoderResult decoderResult = ((DecoderResultProvider)byteBufHolder).decoderResult();
        if (!decoderResult.isSuccess()) {
          if (this.currentMessage instanceof DecoderResultProvider)
            ((DecoderResultProvider)this.currentMessage).setDecoderResult(
                DecoderResult.failure(decoderResult.cause())); 
          last = true;
        } else {
          last = isLastContentMessage((C)byteBufHolder);
        } 
      } else {
        last = isLastContentMessage((C)byteBufHolder);
      } 
      if (last) {
        finishAggregation(this.currentMessage);
        out.add(this.currentMessage);
        this.currentMessage = null;
      } 
    } else {
      throw new MessageAggregationException();
    } 
  }
  
  private static void appendPartialContent(CompositeByteBuf content, ByteBuf partialContent) {
    if (partialContent.isReadable())
      content.addComponent(true, partialContent.retain()); 
  }
  
  protected abstract boolean isContentLengthInvalid(S paramS, int paramInt) throws Exception;
  
  protected abstract Object newContinueResponse(S paramS, int paramInt, ChannelPipeline paramChannelPipeline) throws Exception;
  
  protected abstract boolean closeAfterContinueResponse(Object paramObject) throws Exception;
  
  protected abstract boolean ignoreContentAfterContinueResponse(Object paramObject) throws Exception;
  
  protected abstract O beginAggregation(S paramS, ByteBuf paramByteBuf) throws Exception;
  
  protected void aggregate(O aggregated, C content) throws Exception {}
  
  protected void finishAggregation(O aggregated) throws Exception {}
  
  private void invokeHandleOversizedMessage(ChannelHandlerContext ctx, S oversized) throws Exception {
    this.handlingOversizedMessage = true;
    this.currentMessage = null;
    try {
      handleOversizedMessage(ctx, oversized);
    } finally {
      ReferenceCountUtil.release(oversized);
    } 
  }
  
  protected void handleOversizedMessage(ChannelHandlerContext ctx, S oversized) throws Exception {
    ctx.fireExceptionCaught(new TooLongFrameException("content length exceeded " + 
          maxContentLength() + " bytes."));
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    if (this.currentMessage != null && !ctx.channel().config().isAutoRead())
      ctx.read(); 
    ctx.fireChannelReadComplete();
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    try {
      super.channelInactive(ctx);
    } finally {
      releaseCurrentMessage();
    } 
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    try {
      super.handlerRemoved(ctx);
    } finally {
      releaseCurrentMessage();
    } 
  }
  
  private void releaseCurrentMessage() {
    if (this.currentMessage != null) {
      this.currentMessage.release();
      this.currentMessage = null;
      this.handlingOversizedMessage = false;
    } 
  }
}
