package com.github.steveice10.netty.handler.stream;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelDuplexHandler;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelProgressivePromise;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;

public class ChunkedWriteHandler extends ChannelDuplexHandler {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ChunkedWriteHandler.class);
  
  private final Queue<PendingWrite> queue = new ArrayDeque<PendingWrite>();
  
  private volatile ChannelHandlerContext ctx;
  
  private PendingWrite currentWrite;
  
  @Deprecated
  public ChunkedWriteHandler(int maxPendingWrites) {
    if (maxPendingWrites <= 0)
      throw new IllegalArgumentException("maxPendingWrites: " + maxPendingWrites + " (expected: > 0)"); 
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
  }
  
  public void resumeTransfer() {
    final ChannelHandlerContext ctx = this.ctx;
    if (ctx == null)
      return; 
    if (ctx.executor().inEventLoop()) {
      resumeTransfer0(ctx);
    } else {
      ctx.executor().execute(new Runnable() {
            public void run() {
              ChunkedWriteHandler.this.resumeTransfer0(ctx);
            }
          });
    } 
  }
  
  private void resumeTransfer0(ChannelHandlerContext ctx) {
    try {
      doFlush(ctx);
    } catch (Exception e) {
      if (logger.isWarnEnabled())
        logger.warn("Unexpected exception while sending chunks.", e); 
    } 
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    this.queue.add(new PendingWrite(msg, promise));
  }
  
  public void flush(ChannelHandlerContext ctx) throws Exception {
    doFlush(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    doFlush(ctx);
    ctx.fireChannelInactive();
  }
  
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().isWritable())
      doFlush(ctx); 
    ctx.fireChannelWritabilityChanged();
  }
  
  private void discard(Throwable cause) {
    while (true) {
      PendingWrite currentWrite = this.currentWrite;
      if (this.currentWrite == null) {
        currentWrite = this.queue.poll();
      } else {
        this.currentWrite = null;
      } 
      if (currentWrite == null)
        break; 
      Object message = currentWrite.msg;
      if (message instanceof ChunkedInput) {
        ChunkedInput<?> in = (ChunkedInput)message;
        try {
          if (!in.isEndOfInput()) {
            if (cause == null)
              cause = new ClosedChannelException(); 
            currentWrite.fail(cause);
          } else {
            currentWrite.success(in.length());
          } 
          closeInput(in);
        } catch (Exception e) {
          currentWrite.fail(e);
          logger.warn(ChunkedInput.class.getSimpleName() + ".isEndOfInput() failed", e);
          closeInput(in);
        } 
        continue;
      } 
      if (cause == null)
        cause = new ClosedChannelException(); 
      currentWrite.fail(cause);
    } 
  }
  
  private void doFlush(ChannelHandlerContext ctx) {
    final Channel channel = ctx.channel();
    if (!channel.isActive()) {
      discard(null);
      return;
    } 
    boolean requiresFlush = true;
    ByteBufAllocator allocator = ctx.alloc();
    while (channel.isWritable()) {
      if (this.currentWrite == null)
        this.currentWrite = this.queue.poll(); 
      if (this.currentWrite == null)
        break; 
      final PendingWrite currentWrite = this.currentWrite;
      final Object pendingMessage = currentWrite.msg;
      if (pendingMessage instanceof ChunkedInput) {
        boolean endOfInput, suspend;
        final ChunkedInput<?> chunks = (ChunkedInput)pendingMessage;
        Object message = null;
        try {
          message = chunks.readChunk(allocator);
          endOfInput = chunks.isEndOfInput();
          if (message == null) {
            suspend = !endOfInput;
          } else {
            suspend = false;
          } 
        } catch (Throwable t) {
          this.currentWrite = null;
          if (message != null)
            ReferenceCountUtil.release(message); 
          currentWrite.fail(t);
          closeInput(chunks);
          break;
        } 
        if (suspend)
          break; 
        if (message == null)
          message = Unpooled.EMPTY_BUFFER; 
        ChannelFuture f = ctx.write(message);
        if (endOfInput) {
          this.currentWrite = null;
          f.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  currentWrite.progress(chunks.progress(), chunks.length());
                  currentWrite.success(chunks.length());
                  ChunkedWriteHandler.closeInput(chunks);
                }
              });
        } else if (channel.isWritable()) {
          f.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  if (!future.isSuccess()) {
                    ChunkedWriteHandler.closeInput((ChunkedInput)pendingMessage);
                    currentWrite.fail(future.cause());
                  } else {
                    currentWrite.progress(chunks.progress(), chunks.length());
                  } 
                }
              });
        } else {
          f.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  if (!future.isSuccess()) {
                    ChunkedWriteHandler.closeInput((ChunkedInput)pendingMessage);
                    currentWrite.fail(future.cause());
                  } else {
                    currentWrite.progress(chunks.progress(), chunks.length());
                    if (channel.isWritable())
                      ChunkedWriteHandler.this.resumeTransfer(); 
                  } 
                }
              });
        } 
        ctx.flush();
        requiresFlush = false;
      } else {
        this.currentWrite = null;
        ctx.write(pendingMessage, currentWrite.promise);
        requiresFlush = true;
      } 
      if (!channel.isActive()) {
        discard(new ClosedChannelException());
        break;
      } 
    } 
    if (requiresFlush)
      ctx.flush(); 
  }
  
  private static void closeInput(ChunkedInput<?> chunks) {
    try {
      chunks.close();
    } catch (Throwable t) {
      if (logger.isWarnEnabled())
        logger.warn("Failed to close a chunked input.", t); 
    } 
  }
  
  public ChunkedWriteHandler() {}
  
  private static final class PendingWrite {
    final Object msg;
    
    final ChannelPromise promise;
    
    PendingWrite(Object msg, ChannelPromise promise) {
      this.msg = msg;
      this.promise = promise;
    }
    
    void fail(Throwable cause) {
      ReferenceCountUtil.release(this.msg);
      this.promise.tryFailure(cause);
    }
    
    void success(long total) {
      if (this.promise.isDone())
        return; 
      progress(total, total);
      this.promise.trySuccess();
    }
    
    void progress(long progress, long total) {
      if (this.promise instanceof ChannelProgressivePromise)
        ((ChannelProgressivePromise)this.promise).tryProgress(progress, total); 
    }
  }
}
