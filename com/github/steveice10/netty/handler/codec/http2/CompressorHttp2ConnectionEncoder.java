package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.embedded.EmbeddedChannel;
import com.github.steveice10.netty.handler.codec.compression.ZlibCodecFactory;
import com.github.steveice10.netty.handler.codec.compression.ZlibWrapper;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.concurrent.PromiseCombiner;

public class CompressorHttp2ConnectionEncoder extends DecoratingHttp2ConnectionEncoder {
  public static final int DEFAULT_COMPRESSION_LEVEL = 6;
  
  public static final int DEFAULT_WINDOW_BITS = 15;
  
  public static final int DEFAULT_MEM_LEVEL = 8;
  
  private final int compressionLevel;
  
  private final int windowBits;
  
  private final int memLevel;
  
  private final Http2Connection.PropertyKey propertyKey;
  
  public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate) {
    this(delegate, 6, 15, 8);
  }
  
  public CompressorHttp2ConnectionEncoder(Http2ConnectionEncoder delegate, int compressionLevel, int windowBits, int memLevel) {
    super(delegate);
    if (compressionLevel < 0 || compressionLevel > 9)
      throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 0-9)"); 
    if (windowBits < 9 || windowBits > 15)
      throw new IllegalArgumentException("windowBits: " + windowBits + " (expected: 9-15)"); 
    if (memLevel < 1 || memLevel > 9)
      throw new IllegalArgumentException("memLevel: " + memLevel + " (expected: 1-9)"); 
    this.compressionLevel = compressionLevel;
    this.windowBits = windowBits;
    this.memLevel = memLevel;
    this.propertyKey = connection().newKey();
    connection().addListener(new Http2ConnectionAdapter() {
          public void onStreamRemoved(Http2Stream stream) {
            EmbeddedChannel compressor = stream.<EmbeddedChannel>getProperty(CompressorHttp2ConnectionEncoder.this.propertyKey);
            if (compressor != null)
              CompressorHttp2ConnectionEncoder.this.cleanup(stream, compressor); 
          }
        });
  }
  
  public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream, ChannelPromise promise) {
    Http2Stream stream = connection().stream(streamId);
    EmbeddedChannel channel = (stream == null) ? null : stream.<EmbeddedChannel>getProperty(this.propertyKey);
    if (channel == null)
      return super.writeData(ctx, streamId, data, padding, endOfStream, promise); 
    try {
      channel.writeOutbound(new Object[] { data });
      ByteBuf buf = nextReadableBuf(channel);
      if (buf == null) {
        if (endOfStream) {
          if (channel.finish())
            buf = nextReadableBuf(channel); 
          return super.writeData(ctx, streamId, (buf == null) ? Unpooled.EMPTY_BUFFER : buf, padding, true, promise);
        } 
        promise.setSuccess();
        return (ChannelFuture)promise;
      } 
      PromiseCombiner combiner = new PromiseCombiner();
      while (true) {
        ByteBuf nextBuf = nextReadableBuf(channel);
        boolean compressedEndOfStream = (nextBuf == null && endOfStream);
        if (compressedEndOfStream && channel.finish()) {
          nextBuf = nextReadableBuf(channel);
          compressedEndOfStream = (nextBuf == null);
        } 
        ChannelPromise bufPromise = ctx.newPromise();
        combiner.add((Promise)bufPromise);
        super.writeData(ctx, streamId, buf, padding, compressedEndOfStream, bufPromise);
        if (nextBuf == null)
          break; 
        padding = 0;
        buf = nextBuf;
      } 
      combiner.finish((Promise)promise);
    } catch (Throwable cause) {
      promise.tryFailure(cause);
    } finally {
      if (endOfStream)
        cleanup(stream, channel); 
    } 
    return (ChannelFuture)promise;
  }
  
  public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream, ChannelPromise promise) {
    try {
      EmbeddedChannel compressor = newCompressor(ctx, headers, endStream);
      ChannelFuture future = super.writeHeaders(ctx, streamId, headers, padding, endStream, promise);
      bindCompressorToStream(compressor, streamId);
      return future;
    } catch (Throwable e) {
      promise.tryFailure(e);
      return (ChannelFuture)promise;
    } 
  }
  
  public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream, ChannelPromise promise) {
    try {
      EmbeddedChannel compressor = newCompressor(ctx, headers, endOfStream);
      ChannelFuture future = super.writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endOfStream, promise);
      bindCompressorToStream(compressor, streamId);
      return future;
    } catch (Throwable e) {
      promise.tryFailure(e);
      return (ChannelFuture)promise;
    } 
  }
  
  protected EmbeddedChannel newContentCompressor(ChannelHandlerContext ctx, CharSequence contentEncoding) throws Http2Exception {
    if (HttpHeaderValues.GZIP.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_GZIP.contentEqualsIgnoreCase(contentEncoding))
      return newCompressionChannel(ctx, ZlibWrapper.GZIP); 
    if (HttpHeaderValues.DEFLATE.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_DEFLATE.contentEqualsIgnoreCase(contentEncoding))
      return newCompressionChannel(ctx, ZlibWrapper.ZLIB); 
    return null;
  }
  
  protected CharSequence getTargetContentEncoding(CharSequence contentEncoding) throws Http2Exception {
    return contentEncoding;
  }
  
  private EmbeddedChannel newCompressionChannel(ChannelHandlerContext ctx, ZlibWrapper wrapper) {
    return new EmbeddedChannel(ctx.channel().id(), ctx.channel().metadata().hasDisconnect(), ctx
        .channel().config(), new ChannelHandler[] { (ChannelHandler)ZlibCodecFactory.newZlibEncoder(wrapper, this.compressionLevel, this.windowBits, this.memLevel) });
  }
  
  private EmbeddedChannel newCompressor(ChannelHandlerContext ctx, Http2Headers headers, boolean endOfStream) throws Http2Exception {
    AsciiString asciiString;
    if (endOfStream)
      return null; 
    CharSequence encoding = (CharSequence)headers.get(HttpHeaderNames.CONTENT_ENCODING);
    if (encoding == null)
      asciiString = HttpHeaderValues.IDENTITY; 
    EmbeddedChannel compressor = newContentCompressor(ctx, (CharSequence)asciiString);
    if (compressor != null) {
      CharSequence targetContentEncoding = getTargetContentEncoding((CharSequence)asciiString);
      if (HttpHeaderValues.IDENTITY.contentEqualsIgnoreCase(targetContentEncoding)) {
        headers.remove(HttpHeaderNames.CONTENT_ENCODING);
      } else {
        headers.set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
      } 
      headers.remove(HttpHeaderNames.CONTENT_LENGTH);
    } 
    return compressor;
  }
  
  private void bindCompressorToStream(EmbeddedChannel compressor, int streamId) {
    if (compressor != null) {
      Http2Stream stream = connection().stream(streamId);
      if (stream != null)
        stream.setProperty(this.propertyKey, compressor); 
    } 
  }
  
  void cleanup(Http2Stream stream, EmbeddedChannel compressor) {
    if (compressor.finish())
      while (true) {
        ByteBuf buf = (ByteBuf)compressor.readOutbound();
        if (buf == null)
          break; 
        buf.release();
      }  
    stream.removeProperty(this.propertyKey);
  }
  
  private static ByteBuf nextReadableBuf(EmbeddedChannel compressor) {
    ByteBuf buf;
    while (true) {
      buf = (ByteBuf)compressor.readOutbound();
      if (buf == null)
        return null; 
      if (!buf.isReadable()) {
        buf.release();
        continue;
      } 
      break;
    } 
    return buf;
  }
}
