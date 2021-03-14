package com.github.steveice10.netty.handler.codec.compression;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.ChannelPromiseNotifier;
import com.github.steveice10.netty.handler.codec.EncoderException;
import com.github.steveice10.netty.handler.codec.MessageToByteEncoder;
import com.github.steveice10.netty.util.concurrent.EventExecutor;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.zip.Checksum;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.xxhash.XXHashFactory;

public class Lz4FrameEncoder extends MessageToByteEncoder<ByteBuf> {
  private static final EncoderException ENCODE_FINSHED_EXCEPTION = (EncoderException)ThrowableUtil.unknownStackTrace((Throwable)new EncoderException(new IllegalStateException("encode finished and not enough space to write remaining data")), Lz4FrameEncoder.class, "encode");
  
  static final int DEFAULT_MAX_ENCODE_SIZE = 2147483647;
  
  private final int blockSize;
  
  private final LZ4Compressor compressor;
  
  private final ByteBufChecksum checksum;
  
  private final int compressionLevel;
  
  private ByteBuf buffer;
  
  private final int maxEncodeSize;
  
  private volatile boolean finished;
  
  private volatile ChannelHandlerContext ctx;
  
  public Lz4FrameEncoder() {
    this(false);
  }
  
  public Lz4FrameEncoder(boolean highCompressor) {
    this(LZ4Factory.fastestInstance(), highCompressor, 65536, 
        XXHashFactory.fastestInstance().newStreamingHash32(-1756908916).asChecksum());
  }
  
  public Lz4FrameEncoder(LZ4Factory factory, boolean highCompressor, int blockSize, Checksum checksum) {
    this(factory, highCompressor, blockSize, checksum, 2147483647);
  }
  
  public Lz4FrameEncoder(LZ4Factory factory, boolean highCompressor, int blockSize, Checksum checksum, int maxEncodeSize) {
    if (factory == null)
      throw new NullPointerException("factory"); 
    if (checksum == null)
      throw new NullPointerException("checksum"); 
    this.compressor = highCompressor ? factory.highCompressor() : factory.fastCompressor();
    this.checksum = ByteBufChecksum.wrapChecksum(checksum);
    this.compressionLevel = compressionLevel(blockSize);
    this.blockSize = blockSize;
    this.maxEncodeSize = ObjectUtil.checkPositive(maxEncodeSize, "maxEncodeSize");
    this.finished = false;
  }
  
  private static int compressionLevel(int blockSize) {
    if (blockSize < 64 || blockSize > 33554432)
      throw new IllegalArgumentException(String.format("blockSize: %d (expected: %d-%d)", new Object[] { Integer.valueOf(blockSize), Integer.valueOf(64), Integer.valueOf(33554432) })); 
    int compressionLevel = 32 - Integer.numberOfLeadingZeros(blockSize - 1);
    compressionLevel = Math.max(0, compressionLevel - 10);
    return compressionLevel;
  }
  
  protected ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect) {
    return allocateBuffer(ctx, msg, preferDirect, true);
  }
  
  private ByteBuf allocateBuffer(ChannelHandlerContext ctx, ByteBuf msg, boolean preferDirect, boolean allowEmptyReturn) {
    int targetBufSize = 0;
    int remaining = msg.readableBytes() + this.buffer.readableBytes();
    if (remaining < 0)
      throw new EncoderException("too much data to allocate a buffer for compression"); 
    while (remaining > 0) {
      int curSize = Math.min(this.blockSize, remaining);
      remaining -= curSize;
      targetBufSize += this.compressor.maxCompressedLength(curSize) + 21;
    } 
    if (targetBufSize > this.maxEncodeSize || 0 > targetBufSize)
      throw new EncoderException(String.format("requested encode buffer size (%d bytes) exceeds the maximum allowable size (%d bytes)", new Object[] { Integer.valueOf(targetBufSize), Integer.valueOf(this.maxEncodeSize) })); 
    if (allowEmptyReturn && targetBufSize < this.blockSize)
      return Unpooled.EMPTY_BUFFER; 
    if (preferDirect)
      return ctx.alloc().ioBuffer(targetBufSize, targetBufSize); 
    return ctx.alloc().heapBuffer(targetBufSize, targetBufSize);
  }
  
  protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
    if (this.finished) {
      if (!out.isWritable(in.readableBytes()))
        throw ENCODE_FINSHED_EXCEPTION; 
      out.writeBytes(in);
      return;
    } 
    ByteBuf buffer = this.buffer;
    int length;
    while ((length = in.readableBytes()) > 0) {
      int nextChunkSize = Math.min(length, buffer.writableBytes());
      in.readBytes(buffer, nextChunkSize);
      if (!buffer.isWritable())
        flushBufferedData(out); 
    } 
  }
  
  private void flushBufferedData(ByteBuf out) {
    int blockType, compressedLength, flushableBytes = this.buffer.readableBytes();
    if (flushableBytes == 0)
      return; 
    this.checksum.reset();
    this.checksum.update(this.buffer, this.buffer.readerIndex(), flushableBytes);
    int check = (int)this.checksum.getValue();
    int bufSize = this.compressor.maxCompressedLength(flushableBytes) + 21;
    out.ensureWritable(bufSize);
    int idx = out.writerIndex();
    try {
      ByteBuffer outNioBuffer = out.internalNioBuffer(idx + 21, out.writableBytes() - 21);
      int pos = outNioBuffer.position();
      this.compressor.compress(this.buffer.internalNioBuffer(this.buffer.readerIndex(), flushableBytes), outNioBuffer);
      compressedLength = outNioBuffer.position() - pos;
    } catch (LZ4Exception e) {
      throw new CompressionException(e);
    } 
    if (compressedLength >= flushableBytes) {
      blockType = 16;
      compressedLength = flushableBytes;
      out.setBytes(idx + 21, this.buffer, 0, flushableBytes);
    } else {
      blockType = 32;
    } 
    out.setLong(idx, 5501767354678207339L);
    out.setByte(idx + 8, (byte)(blockType | this.compressionLevel));
    out.setIntLE(idx + 9, compressedLength);
    out.setIntLE(idx + 13, flushableBytes);
    out.setIntLE(idx + 17, check);
    out.writerIndex(idx + 21 + compressedLength);
    this.buffer.clear();
  }
  
  public void flush(ChannelHandlerContext ctx) throws Exception {
    if (this.buffer != null && this.buffer.isReadable()) {
      ByteBuf buf = allocateBuffer(ctx, Unpooled.EMPTY_BUFFER, isPreferDirect(), false);
      flushBufferedData(buf);
      ctx.write(buf);
    } 
    ctx.flush();
  }
  
  private ChannelFuture finishEncode(ChannelHandlerContext ctx, ChannelPromise promise) {
    if (this.finished) {
      promise.setSuccess();
      return (ChannelFuture)promise;
    } 
    this.finished = true;
    ByteBuf footer = ctx.alloc().heapBuffer(this.compressor
        .maxCompressedLength(this.buffer.readableBytes()) + 21);
    flushBufferedData(footer);
    int idx = footer.writerIndex();
    footer.setLong(idx, 5501767354678207339L);
    footer.setByte(idx + 8, (byte)(0x10 | this.compressionLevel));
    footer.setInt(idx + 9, 0);
    footer.setInt(idx + 13, 0);
    footer.setInt(idx + 17, 0);
    footer.writerIndex(idx + 21);
    return ctx.writeAndFlush(footer, promise);
  }
  
  public boolean isClosed() {
    return this.finished;
  }
  
  public ChannelFuture close() {
    return close(ctx().newPromise());
  }
  
  public ChannelFuture close(final ChannelPromise promise) {
    ChannelHandlerContext ctx = ctx();
    EventExecutor executor = ctx.executor();
    if (executor.inEventLoop())
      return finishEncode(ctx, promise); 
    executor.execute(new Runnable() {
          public void run() {
            ChannelFuture f = Lz4FrameEncoder.this.finishEncode(Lz4FrameEncoder.this.ctx(), promise);
            f.addListener((GenericFutureListener)new ChannelPromiseNotifier(new ChannelPromise[] { this.val$promise }));
          }
        });
    return (ChannelFuture)promise;
  }
  
  public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
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
  
  private ChannelHandlerContext ctx() {
    ChannelHandlerContext ctx = this.ctx;
    if (ctx == null)
      throw new IllegalStateException("not added to a pipeline"); 
    return ctx;
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) {
    this.ctx = ctx;
    this.buffer = Unpooled.wrappedBuffer(new byte[this.blockSize]);
    this.buffer.clear();
  }
  
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    super.handlerRemoved(ctx);
    if (this.buffer != null) {
      this.buffer.release();
      this.buffer = null;
    } 
  }
  
  final ByteBuf getBackingBuffer() {
    return this.buffer;
  }
}
