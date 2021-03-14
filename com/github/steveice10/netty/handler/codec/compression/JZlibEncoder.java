package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.ChannelPromiseNotifier;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.EmptyArrays;
import com.jcraft.jzlib.Deflater;
import com.jcraft.jzlib.JZlib;
import java.util.concurrent.TimeUnit;

public class JZlibEncoder extends ZlibEncoder {
  private final int wrapperOverhead;
  
  private final Deflater z = new Deflater();
  
  private volatile boolean finished;
  
  private volatile ChannelHandlerContext ctx;
  
  public JZlibEncoder() {
    this(6);
  }
  
  public JZlibEncoder(int compressionLevel) {
    this(ZlibWrapper.ZLIB, compressionLevel);
  }
  
  public JZlibEncoder(ZlibWrapper wrapper) {
    this(wrapper, 6);
  }
  
  public JZlibEncoder(ZlibWrapper wrapper, int compressionLevel) {
    this(wrapper, compressionLevel, 15, 8);
  }
  
  public JZlibEncoder(ZlibWrapper wrapper, int compressionLevel, int windowBits, int memLevel) {
    if (compressionLevel < 0 || compressionLevel > 9)
      throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)"); 
    if (windowBits < 9 || windowBits > 15)
      throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)"); 
    if (memLevel < 1 || memLevel > 9)
      throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)"); 
    if (wrapper == null)
      throw new NullPointerException("wrapper"); 
    if (wrapper == ZlibWrapper.ZLIB_OR_NONE)
      throw new IllegalArgumentException("wrapper '" + ZlibWrapper.ZLIB_OR_NONE + "' is not allowed for compression."); 
    int resultCode = this.z.init(compressionLevel, windowBits, memLevel, 
        
        ZlibUtil.convertWrapperType(wrapper));
    if (resultCode != 0)
      ZlibUtil.fail(this.z, "initialization failure", resultCode); 
    this.wrapperOverhead = ZlibUtil.wrapperOverhead(wrapper);
  }
  
  public JZlibEncoder(byte[] dictionary) {
    this(6, dictionary);
  }
  
  public JZlibEncoder(int compressionLevel, byte[] dictionary) {
    this(compressionLevel, 15, 8, dictionary);
  }
  
  public JZlibEncoder(int compressionLevel, int windowBits, int memLevel, byte[] dictionary) {
    if (compressionLevel < 0 || compressionLevel > 9)
      throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)"); 
    if (windowBits < 9 || windowBits > 15)
      throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)"); 
    if (memLevel < 1 || memLevel > 9)
      throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)"); 
    if (dictionary == null)
      throw new NullPointerException("dictionary"); 
    int resultCode = this.z.deflateInit(compressionLevel, windowBits, memLevel, JZlib.W_ZLIB);
    if (resultCode != 0) {
      ZlibUtil.fail(this.z, "initialization failure", resultCode);
    } else {
      resultCode = this.z.deflateSetDictionary(dictionary, dictionary.length);
      if (resultCode != 0)
        ZlibUtil.fail(this.z, "failed to set the dictionary", resultCode); 
    } 
    this.wrapperOverhead = ZlibUtil.wrapperOverhead(ZlibWrapper.ZLIB);
  }
  
  public ChannelFuture close() {
    return close(ctx().channel().newPromise());
  }
  
  public ChannelFuture close(final ChannelPromise promise) {
    ChannelHandlerContext ctx = ctx();
    EventExecutor executor = ctx.executor();
    if (executor.inEventLoop())
      return finishEncode(ctx, promise); 
    final ChannelPromise p = ctx.newPromise();
    executor.execute(new Runnable() {
          public void run() {
            ChannelFuture f = JZlibEncoder.this.finishEncode(JZlibEncoder.this.ctx(), p);
            f.addListener((GenericFutureListener)new ChannelPromiseNotifier(new ChannelPromise[] { this.val$promise }));
          }
        });
    return (ChannelFuture)p;
  }
  
  private ChannelHandlerContext ctx() {
    ChannelHandlerContext ctx = this.ctx;
    if (ctx == null)
      throw new IllegalStateException("not added to a pipeline"); 
    return ctx;
  }
  
  public boolean isClosed() {
    return this.finished;
  }
  
  protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
    if (this.finished) {
      out.writeBytes(in);
      return;
    } 
    int inputLength = in.readableBytes();
    if (inputLength == 0)
      return; 
    try {
      int resultCode;
      boolean inHasArray = in.hasArray();
      this.z.avail_in = inputLength;
      if (inHasArray) {
        this.z.next_in = in.array();
        this.z.next_in_index = in.arrayOffset() + in.readerIndex();
      } else {
        byte[] array = new byte[inputLength];
        in.getBytes(in.readerIndex(), array);
        this.z.next_in = array;
        this.z.next_in_index = 0;
      } 
      int oldNextInIndex = this.z.next_in_index;
      int maxOutputLength = (int)Math.ceil(inputLength * 1.001D) + 12 + this.wrapperOverhead;
      out.ensureWritable(maxOutputLength);
      this.z.avail_out = maxOutputLength;
      this.z.next_out = out.array();
      this.z.next_out_index = out.arrayOffset() + out.writerIndex();
      int oldNextOutIndex = this.z.next_out_index;
      try {
        resultCode = this.z.deflate(2);
      } finally {
        in.skipBytes(this.z.next_in_index - oldNextInIndex);
      } 
      if (resultCode != 0)
        ZlibUtil.fail(this.z, "compression failure", resultCode); 
      int outputLength = this.z.next_out_index - oldNextOutIndex;
      if (outputLength > 0)
        out.writerIndex(out.writerIndex() + outputLength); 
    } finally {
      this.z.next_in = null;
      this.z.next_out = null;
    } 
  }
  
  public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) {
    ChannelFuture f = finishEncode(ctx, ctx.newPromise());
    f.addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture f) throws Exception {
            ctx.close(promise);
          }
        });
    if (!f.isDone())
      ctx.executor().schedule(new Runnable() {
            public void run() {
              ctx.close(promise);
            }
          },  10L, TimeUnit.SECONDS); 
  }
  
  private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise) {
    ByteBuf footer;
    if (this.finished) {
      promise.setSuccess();
      return (ChannelFuture)promise;
    } 
    this.finished = true;
    try {
      this.z.next_in = EmptyArrays.EMPTY_BYTES;
      this.z.next_in_index = 0;
      this.z.avail_in = 0;
      byte[] out = new byte[32];
      this.z.next_out = out;
      this.z.next_out_index = 0;
      this.z.avail_out = out.length;
      int resultCode = this.z.deflate(4);
      if (resultCode != 0 && resultCode != 1) {
        promise.setFailure((Throwable)ZlibUtil.deflaterException(this.z, "compression failure", resultCode));
        return (ChannelFuture)promise;
      } 
      if (this.z.next_out_index != 0) {
        footer = Unpooled.wrappedBuffer(out, 0, this.z.next_out_index);
      } else {
        footer = Unpooled.EMPTY_BUFFER;
      } 
    } finally {
      this.z.deflateEnd();
      this.z.next_in = null;
      this.z.next_out = null;
    } 
    return ctx.writeAndFlush(footer, promise);
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
  }
}
