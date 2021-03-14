package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.embedded.EmbeddedChannel;
import com.github.steveice10.netty.handler.codec.compression.ZlibCodecFactory;
import com.github.steveice10.netty.handler.codec.compression.ZlibWrapper;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderNames;
import com.github.steveice10.netty.handler.codec.http.HttpHeaderValues;
import com.github.steveice10.netty.util.AsciiString;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public class DelegatingDecompressorFrameListener extends Http2FrameListenerDecorator {
  private final Http2Connection connection;
  
  private final boolean strict;
  
  private boolean flowControllerInitialized;
  
  private final Http2Connection.PropertyKey propertyKey;
  
  public DelegatingDecompressorFrameListener(Http2Connection connection, Http2FrameListener listener) {
    this(connection, listener, true);
  }
  
  public DelegatingDecompressorFrameListener(Http2Connection connection, Http2FrameListener listener, boolean strict) {
    super(listener);
    this.connection = connection;
    this.strict = strict;
    this.propertyKey = connection.newKey();
    connection.addListener(new Http2ConnectionAdapter() {
          public void onStreamRemoved(Http2Stream stream) {
            DelegatingDecompressorFrameListener.Http2Decompressor decompressor = DelegatingDecompressorFrameListener.this.decompressor(stream);
            if (decompressor != null)
              DelegatingDecompressorFrameListener.cleanup(decompressor); 
          }
        });
  }
  
  public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
    Http2Stream stream = this.connection.stream(streamId);
    Http2Decompressor decompressor = decompressor(stream);
    if (decompressor == null)
      return this.listener.onDataRead(ctx, streamId, data, padding, endOfStream); 
    EmbeddedChannel channel = decompressor.decompressor();
    int compressedBytes = data.readableBytes() + padding;
    decompressor.incrementCompressedBytes(compressedBytes);
    try {
      channel.writeInbound(new Object[] { data.retain() });
      ByteBuf buf = nextReadableBuf(channel);
      if (buf == null && endOfStream && channel.finish())
        buf = nextReadableBuf(channel); 
      if (buf == null) {
        if (endOfStream)
          this.listener.onDataRead(ctx, streamId, Unpooled.EMPTY_BUFFER, padding, true); 
        decompressor.incrementDecompressedBytes(compressedBytes);
        return compressedBytes;
      } 
      try {
        Http2LocalFlowController flowController = this.connection.local().flowController();
        decompressor.incrementDecompressedBytes(padding);
        while (true) {
          ByteBuf nextBuf = nextReadableBuf(channel);
          boolean decompressedEndOfStream = (nextBuf == null && endOfStream);
          if (decompressedEndOfStream && channel.finish()) {
            nextBuf = nextReadableBuf(channel);
            decompressedEndOfStream = (nextBuf == null);
          } 
          decompressor.incrementDecompressedBytes(buf.readableBytes());
          flowController.consumeBytes(stream, this.listener
              .onDataRead(ctx, streamId, buf, padding, decompressedEndOfStream));
          if (nextBuf == null)
            break; 
          padding = 0;
          buf.release();
          buf = nextBuf;
        } 
        return 0;
      } finally {
        buf.release();
      } 
    } catch (Http2Exception e) {
      throw e;
    } catch (Throwable t) {
      throw Http2Exception.streamError(stream.id(), Http2Error.INTERNAL_ERROR, t, "Decompressor error detected while delegating data read on streamId %d", new Object[] { Integer.valueOf(stream.id()) });
    } 
  }
  
  public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream) throws Http2Exception {
    initDecompressor(ctx, streamId, headers, endStream);
    this.listener.onHeadersRead(ctx, streamId, headers, padding, endStream);
  }
  
  public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) throws Http2Exception {
    initDecompressor(ctx, streamId, headers, endStream);
    this.listener.onHeadersRead(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endStream);
  }
  
  protected EmbeddedChannel newContentDecompressor(ChannelHandlerContext ctx, CharSequence contentEncoding) throws Http2Exception {
    if (HttpHeaderValues.GZIP.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_GZIP.contentEqualsIgnoreCase(contentEncoding))
      return new EmbeddedChannel(ctx.channel().id(), ctx.channel().metadata().hasDisconnect(), ctx
          .channel().config(), new ChannelHandler[] { (ChannelHandler)ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP) }); 
    if (HttpHeaderValues.DEFLATE.contentEqualsIgnoreCase(contentEncoding) || HttpHeaderValues.X_DEFLATE.contentEqualsIgnoreCase(contentEncoding)) {
      ZlibWrapper wrapper = this.strict ? ZlibWrapper.ZLIB : ZlibWrapper.ZLIB_OR_NONE;
      return new EmbeddedChannel(ctx.channel().id(), ctx.channel().metadata().hasDisconnect(), ctx
          .channel().config(), new ChannelHandler[] { (ChannelHandler)ZlibCodecFactory.newZlibDecoder(wrapper) });
    } 
    return null;
  }
  
  protected CharSequence getTargetContentEncoding(CharSequence contentEncoding) throws Http2Exception {
    return (CharSequence)HttpHeaderValues.IDENTITY;
  }
  
  private void initDecompressor(ChannelHandlerContext ctx, int streamId, Http2Headers headers, boolean endOfStream) throws Http2Exception {
    Http2Stream stream = this.connection.stream(streamId);
    if (stream == null)
      return; 
    Http2Decompressor decompressor = decompressor(stream);
    if (decompressor == null && !endOfStream) {
      AsciiString asciiString;
      CharSequence contentEncoding = (CharSequence)headers.get(HttpHeaderNames.CONTENT_ENCODING);
      if (contentEncoding == null)
        asciiString = HttpHeaderValues.IDENTITY; 
      EmbeddedChannel channel = newContentDecompressor(ctx, (CharSequence)asciiString);
      if (channel != null) {
        decompressor = new Http2Decompressor(channel);
        stream.setProperty(this.propertyKey, decompressor);
        CharSequence targetContentEncoding = getTargetContentEncoding((CharSequence)asciiString);
        if (HttpHeaderValues.IDENTITY.contentEqualsIgnoreCase(targetContentEncoding)) {
          headers.remove(HttpHeaderNames.CONTENT_ENCODING);
        } else {
          headers.set(HttpHeaderNames.CONTENT_ENCODING, targetContentEncoding);
        } 
      } 
    } 
    if (decompressor != null) {
      headers.remove(HttpHeaderNames.CONTENT_LENGTH);
      if (!this.flowControllerInitialized) {
        this.flowControllerInitialized = true;
        this.connection.local().flowController(new ConsumedBytesConverter(this.connection.local().flowController()));
      } 
    } 
  }
  
  Http2Decompressor decompressor(Http2Stream stream) {
    return (stream == null) ? null : stream.<Http2Decompressor>getProperty(this.propertyKey);
  }
  
  private static void cleanup(Http2Decompressor decompressor) {
    decompressor.decompressor().finishAndReleaseAll();
  }
  
  private static ByteBuf nextReadableBuf(EmbeddedChannel decompressor) {
    ByteBuf buf;
    while (true) {
      buf = (ByteBuf)decompressor.readInbound();
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
  
  private final class ConsumedBytesConverter implements Http2LocalFlowController {
    private final Http2LocalFlowController flowController;
    
    ConsumedBytesConverter(Http2LocalFlowController flowController) {
      this.flowController = (Http2LocalFlowController)ObjectUtil.checkNotNull(flowController, "flowController");
    }
    
    public Http2LocalFlowController frameWriter(Http2FrameWriter frameWriter) {
      return this.flowController.frameWriter(frameWriter);
    }
    
    public void channelHandlerContext(ChannelHandlerContext ctx) throws Http2Exception {
      this.flowController.channelHandlerContext(ctx);
    }
    
    public void initialWindowSize(int newWindowSize) throws Http2Exception {
      this.flowController.initialWindowSize(newWindowSize);
    }
    
    public int initialWindowSize() {
      return this.flowController.initialWindowSize();
    }
    
    public int windowSize(Http2Stream stream) {
      return this.flowController.windowSize(stream);
    }
    
    public void incrementWindowSize(Http2Stream stream, int delta) throws Http2Exception {
      this.flowController.incrementWindowSize(stream, delta);
    }
    
    public void receiveFlowControlledFrame(Http2Stream stream, ByteBuf data, int padding, boolean endOfStream) throws Http2Exception {
      this.flowController.receiveFlowControlledFrame(stream, data, padding, endOfStream);
    }
    
    public boolean consumeBytes(Http2Stream stream, int numBytes) throws Http2Exception {
      DelegatingDecompressorFrameListener.Http2Decompressor decompressor = DelegatingDecompressorFrameListener.this.decompressor(stream);
      if (decompressor != null)
        numBytes = decompressor.consumeBytes(stream.id(), numBytes); 
      try {
        return this.flowController.consumeBytes(stream, numBytes);
      } catch (Http2Exception e) {
        throw e;
      } catch (Throwable t) {
        throw Http2Exception.streamError(stream.id(), Http2Error.INTERNAL_ERROR, t, "Error while returning bytes to flow control window", new Object[0]);
      } 
    }
    
    public int unconsumedBytes(Http2Stream stream) {
      return this.flowController.unconsumedBytes(stream);
    }
    
    public int initialWindowSize(Http2Stream stream) {
      return this.flowController.initialWindowSize(stream);
    }
  }
  
  private static final class Http2Decompressor {
    private final EmbeddedChannel decompressor;
    
    private int compressed;
    
    private int decompressed;
    
    Http2Decompressor(EmbeddedChannel decompressor) {
      this.decompressor = decompressor;
    }
    
    EmbeddedChannel decompressor() {
      return this.decompressor;
    }
    
    void incrementCompressedBytes(int delta) {
      assert delta >= 0;
      this.compressed += delta;
    }
    
    void incrementDecompressedBytes(int delta) {
      assert delta >= 0;
      this.decompressed += delta;
    }
    
    int consumeBytes(int streamId, int decompressedBytes) throws Http2Exception {
      if (decompressedBytes < 0)
        throw new IllegalArgumentException("decompressedBytes must not be negative: " + decompressedBytes); 
      if (this.decompressed - decompressedBytes < 0)
        throw Http2Exception.streamError(streamId, Http2Error.INTERNAL_ERROR, "Attempting to return too many bytes for stream %d. decompressed: %d decompressedBytes: %d", new Object[] { Integer.valueOf(streamId), Integer.valueOf(this.decompressed), Integer.valueOf(decompressedBytes) }); 
      double consumedRatio = decompressedBytes / this.decompressed;
      int consumedCompressed = Math.min(this.compressed, (int)Math.ceil(this.compressed * consumedRatio));
      if (this.compressed - consumedCompressed < 0)
        throw Http2Exception.streamError(streamId, Http2Error.INTERNAL_ERROR, "overflow when converting decompressed bytes to compressed bytes for stream %d.decompressedBytes: %d decompressed: %d compressed: %d consumedCompressed: %d", new Object[] { Integer.valueOf(streamId), Integer.valueOf(decompressedBytes), Integer.valueOf(this.decompressed), Integer.valueOf(this.compressed), Integer.valueOf(consumedCompressed) }); 
      this.decompressed -= decompressedBytes;
      this.compressed -= consumedCompressed;
      return consumedCompressed;
    }
  }
}
