package com.github.steveice10.netty.handler.codec;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.util.Signal;
import com.github.steveice10.netty.util.internal.StringUtil;
import java.util.List;

public abstract class ReplayingDecoder<S> extends ByteToMessageDecoder {
  static final Signal REPLAY = Signal.valueOf(ReplayingDecoder.class, "REPLAY");
  
  private final ReplayingDecoderByteBuf replayable = new ReplayingDecoderByteBuf();
  
  private S state;
  
  private int checkpoint = -1;
  
  protected ReplayingDecoder() {
    this((S)null);
  }
  
  protected ReplayingDecoder(S initialState) {
    this.state = initialState;
  }
  
  protected void checkpoint() {
    this.checkpoint = internalBuffer().readerIndex();
  }
  
  protected void checkpoint(S state) {
    checkpoint();
    state(state);
  }
  
  protected S state() {
    return this.state;
  }
  
  protected S state(S newState) {
    S oldState = this.state;
    this.state = newState;
    return oldState;
  }
  
  final void channelInputClosed(ChannelHandlerContext ctx, List<Object> out) throws Exception {
    try {
      this.replayable.terminate();
      if (this.cumulation != null) {
        callDecode(ctx, internalBuffer(), out);
      } else {
        this.replayable.setCumulation(Unpooled.EMPTY_BUFFER);
      } 
      decodeLast(ctx, this.replayable, out);
    } catch (Signal replay) {
      replay.expect(REPLAY);
    } 
  }
  
  protected void callDecode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
    this.replayable.setCumulation(in);
    try {
      while (in.isReadable()) {
        int oldReaderIndex = this.checkpoint = in.readerIndex();
        int outSize = out.size();
        if (outSize > 0) {
          fireChannelRead(ctx, out, outSize);
          out.clear();
          if (ctx.isRemoved())
            break; 
          outSize = 0;
        } 
        S oldState = this.state;
        int oldInputLength = in.readableBytes();
        try {
          decodeRemovalReentryProtection(ctx, this.replayable, out);
          if (ctx.isRemoved())
            break; 
          if (outSize == out.size()) {
            if (oldInputLength == in.readableBytes() && oldState == this.state)
              throw new DecoderException(
                  StringUtil.simpleClassName(getClass()) + ".decode() must consume the inbound data or change its state if it did not decode anything."); 
            continue;
          } 
        } catch (Signal replay) {
          replay.expect(REPLAY);
          if (ctx.isRemoved())
            break; 
          int checkpoint = this.checkpoint;
          if (checkpoint >= 0)
            in.readerIndex(checkpoint); 
          break;
        } 
        if (oldReaderIndex == in.readerIndex() && oldState == this.state)
          throw new DecoderException(
              StringUtil.simpleClassName(getClass()) + ".decode() method must consume the inbound data or change its state if it decoded something."); 
        if (isSingleDecode())
          break; 
      } 
    } catch (DecoderException e) {
      throw e;
    } catch (Exception cause) {
      throw new DecoderException(cause);
    } 
  }
}
