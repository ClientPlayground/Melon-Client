package com.github.steveice10.netty.handler.ssl;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.DecoderException;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.FutureListener;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.PlatformDependent;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.util.List;
import java.util.Locale;

public abstract class AbstractSniHandler<T> extends ByteToMessageDecoder implements ChannelOutboundHandler {
  private static final int MAX_SSL_RECORDS = 4;
  
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(AbstractSniHandler.class);
  
  private boolean handshakeFailed;
  
  private boolean suppressRead;
  
  private boolean readPending;
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (!this.suppressRead && !this.handshakeFailed) {
      int writerIndex = in.writerIndex();
      try {
        for (int i = 0; i < 4; i++) {
          int len, majorVersion, readerIndex = in.readerIndex();
          int readableBytes = writerIndex - readerIndex;
          if (readableBytes < 5)
            return; 
          int command = in.getUnsignedByte(readerIndex);
          switch (command) {
            case 20:
            case 21:
              len = SslUtils.getEncryptedPacketLength(in, readerIndex);
              if (len == -2) {
                this.handshakeFailed = true;
                NotSslRecordException e = new NotSslRecordException("not an SSL/TLS record: " + ByteBufUtil.hexDump(in));
                in.skipBytes(in.readableBytes());
                ctx.fireUserEventTriggered(new SniCompletionEvent(e));
                SslUtils.handleHandshakeFailure(ctx, e, true);
                throw e;
              } 
              if (len == -1 || writerIndex - readerIndex - 5 < len)
                return; 
              in.skipBytes(len);
              break;
            case 22:
              majorVersion = in.getUnsignedByte(readerIndex + 1);
              if (majorVersion == 3) {
                int packetLength = in.getUnsignedShort(readerIndex + 3) + 5;
                if (readableBytes < packetLength)
                  return; 
                int endOffset = readerIndex + packetLength;
                int offset = readerIndex + 43;
                if (endOffset - offset < 6)
                  break; 
                int sessionIdLength = in.getUnsignedByte(offset);
                offset += sessionIdLength + 1;
                int cipherSuitesLength = in.getUnsignedShort(offset);
                offset += cipherSuitesLength + 2;
                int compressionMethodLength = in.getUnsignedByte(offset);
                offset += compressionMethodLength + 1;
                int extensionsLength = in.getUnsignedShort(offset);
                offset += 2;
                int extensionsLimit = offset + extensionsLength;
                if (extensionsLimit > endOffset)
                  break; 
                while (extensionsLimit - offset >= 4) {
                  int extensionType = in.getUnsignedShort(offset);
                  offset += 2;
                  int extensionLength = in.getUnsignedShort(offset);
                  offset += 2;
                  if (extensionsLimit - offset < extensionLength)
                    break; 
                  if (extensionType == 0) {
                    offset += 2;
                    if (extensionsLimit - offset < 3)
                      break; 
                    int serverNameType = in.getUnsignedByte(offset);
                    offset++;
                    if (serverNameType == 0) {
                      int serverNameLength = in.getUnsignedShort(offset);
                      offset += 2;
                      if (extensionsLimit - offset < serverNameLength)
                        break; 
                      String hostname = in.toString(offset, serverNameLength, CharsetUtil.US_ASCII);
                      try {
                        select(ctx, hostname.toLowerCase(Locale.US));
                      } catch (Throwable t) {
                        PlatformDependent.throwException(t);
                      } 
                      return;
                    } 
                    break;
                  } 
                  offset += extensionLength;
                } 
              } 
              break;
            default:
              break;
          } 
        } 
      } catch (NotSslRecordException e) {
        throw e;
      } catch (Exception e) {
        if (logger.isDebugEnabled())
          logger.debug("Unexpected client hello packet: " + ByteBufUtil.hexDump(in), e); 
      } 
      select(ctx, null);
    } 
  }
  
  private void select(final ChannelHandlerContext ctx, final String hostname) throws Exception {
    Future<T> future = lookup(ctx, hostname);
    if (future.isDone()) {
      fireSniCompletionEvent(ctx, hostname, future);
      onLookupComplete(ctx, hostname, future);
    } else {
      this.suppressRead = true;
      future.addListener((GenericFutureListener)new FutureListener<T>() {
            public void operationComplete(Future<T> future) throws Exception {
              try {
                AbstractSniHandler.this.suppressRead = false;
                try {
                  AbstractSniHandler.this.fireSniCompletionEvent(ctx, hostname, future);
                  AbstractSniHandler.this.onLookupComplete(ctx, hostname, future);
                } catch (DecoderException err) {
                  ctx.fireExceptionCaught((Throwable)err);
                } catch (Exception cause) {
                  ctx.fireExceptionCaught((Throwable)new DecoderException(cause));
                } catch (Throwable cause) {
                  ctx.fireExceptionCaught(cause);
                } 
              } finally {
                if (AbstractSniHandler.this.readPending) {
                  AbstractSniHandler.this.readPending = false;
                  ctx.read();
                } 
              } 
            }
          });
    } 
  }
  
  private void fireSniCompletionEvent(ChannelHandlerContext ctx, String hostname, Future<T> future) {
    Throwable cause = future.cause();
    if (cause == null) {
      ctx.fireUserEventTriggered(new SniCompletionEvent(hostname));
    } else {
      ctx.fireUserEventTriggered(new SniCompletionEvent(hostname, cause));
    } 
  }
  
  public void read(ChannelHandlerContext ctx) throws Exception {
    if (this.suppressRead) {
      this.readPending = true;
    } else {
      ctx.read();
    } 
  }
  
  public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    ctx.bind(localAddress, promise);
  }
  
  public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
    ctx.connect(remoteAddress, localAddress, promise);
  }
  
  public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.disconnect(promise);
  }
  
  public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.close(promise);
  }
  
  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.deregister(promise);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    ctx.write(msg, promise);
  }
  
  public void flush(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }
  
  protected abstract Future<T> lookup(ChannelHandlerContext paramChannelHandlerContext, String paramString) throws Exception;
  
  protected abstract void onLookupComplete(ChannelHandlerContext paramChannelHandlerContext, String paramString, Future<T> paramFuture) throws Exception;
}
