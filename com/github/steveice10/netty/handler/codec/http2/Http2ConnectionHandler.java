package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufUtil;
import com.github.steveice10.netty.buffer.Unpooled;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelOutboundHandler;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.ByteToMessageDecoder;
import com.github.steveice10.netty.handler.codec.http.HttpResponseStatus;
import com.github.steveice10.netty.util.CharsetUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.concurrent.Promise;
import com.github.steveice10.netty.util.concurrent.ScheduledFuture;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Http2ConnectionHandler extends ByteToMessageDecoder implements Http2LifecycleManager, ChannelOutboundHandler {
  private static final InternalLogger logger = InternalLoggerFactory.getInstance(Http2ConnectionHandler.class);
  
  private static final Http2Headers HEADERS_TOO_LARGE_HEADERS = ReadOnlyHttp2Headers.serverHeaders(false, HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE
      .codeAsText(), new com.github.steveice10.netty.util.AsciiString[0]);
  
  private static final ByteBuf HTTP_1_X_BUF = Unpooled.unreleasableBuffer(
      Unpooled.wrappedBuffer(new byte[] { 72, 84, 84, 80, 47, 49, 46 })).asReadOnly();
  
  private final Http2ConnectionDecoder decoder;
  
  private final Http2ConnectionEncoder encoder;
  
  private final Http2Settings initialSettings;
  
  private ChannelFutureListener closeListener;
  
  private BaseDecoder byteDecoder;
  
  private long gracefulShutdownTimeoutMillis;
  
  protected Http2ConnectionHandler(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder, Http2Settings initialSettings) {
    this.initialSettings = (Http2Settings)ObjectUtil.checkNotNull(initialSettings, "initialSettings");
    this.decoder = (Http2ConnectionDecoder)ObjectUtil.checkNotNull(decoder, "decoder");
    this.encoder = (Http2ConnectionEncoder)ObjectUtil.checkNotNull(encoder, "encoder");
    if (encoder.connection() != decoder.connection())
      throw new IllegalArgumentException("Encoder and Decoder do not share the same connection object"); 
  }
  
  Http2ConnectionHandler(boolean server, Http2FrameWriter frameWriter, Http2FrameLogger frameLogger, Http2Settings initialSettings) {
    this.initialSettings = (Http2Settings)ObjectUtil.checkNotNull(initialSettings, "initialSettings");
    Http2Connection connection = new DefaultHttp2Connection(server);
    Long maxHeaderListSize = initialSettings.maxHeaderListSize();
    Http2FrameReader frameReader = new DefaultHttp2FrameReader((maxHeaderListSize == null) ? new DefaultHttp2HeadersDecoder(true) : new DefaultHttp2HeadersDecoder(true, maxHeaderListSize.longValue()));
    if (frameLogger != null) {
      frameWriter = new Http2OutboundFrameLogger(frameWriter, frameLogger);
      frameReader = new Http2InboundFrameLogger(frameReader, frameLogger);
    } 
    this.encoder = new DefaultHttp2ConnectionEncoder(connection, frameWriter);
    this.decoder = new DefaultHttp2ConnectionDecoder(connection, this.encoder, frameReader);
  }
  
  public long gracefulShutdownTimeoutMillis() {
    return this.gracefulShutdownTimeoutMillis;
  }
  
  public void gracefulShutdownTimeoutMillis(long gracefulShutdownTimeoutMillis) {
    if (gracefulShutdownTimeoutMillis < -1L)
      throw new IllegalArgumentException("gracefulShutdownTimeoutMillis: " + gracefulShutdownTimeoutMillis + " (expected: -1 for indefinite or >= 0)"); 
    this.gracefulShutdownTimeoutMillis = gracefulShutdownTimeoutMillis;
  }
  
  public Http2Connection connection() {
    return this.encoder.connection();
  }
  
  public Http2ConnectionDecoder decoder() {
    return this.decoder;
  }
  
  public Http2ConnectionEncoder encoder() {
    return this.encoder;
  }
  
  private boolean prefaceSent() {
    return (this.byteDecoder != null && this.byteDecoder.prefaceSent());
  }
  
  public void onHttpClientUpgrade() throws Http2Exception {
    if (connection().isServer())
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Client-side HTTP upgrade requested for a server", new Object[0]); 
    if (!prefaceSent())
      throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "HTTP upgrade must occur after preface was sent", new Object[0]); 
    if (this.decoder.prefaceReceived())
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "HTTP upgrade must occur before HTTP/2 preface is received", new Object[0]); 
    connection().local().createStream(1, true);
  }
  
  public void onHttpServerUpgrade(Http2Settings settings) throws Http2Exception {
    if (!connection().isServer())
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server-side HTTP upgrade requested for a client", new Object[0]); 
    if (!prefaceSent())
      throw Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, "HTTP upgrade must occur after preface was sent", new Object[0]); 
    if (this.decoder.prefaceReceived())
      throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "HTTP upgrade must occur before HTTP/2 preface is received", new Object[0]); 
    this.encoder.remoteSettings(settings);
    connection().remote().createStream(1, true);
  }
  
  public void flush(ChannelHandlerContext ctx) {
    try {
      this.encoder.flowController().writePendingBytes();
      ctx.flush();
    } catch (Http2Exception e) {
      onError(ctx, true, e);
    } catch (Throwable cause) {
      onError(ctx, true, Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, cause, "Error flushing", new Object[0]));
    } 
  }
  
  private abstract class BaseDecoder {
    private BaseDecoder() {}
    
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {}
    
    public void channelActive(ChannelHandlerContext ctx) throws Exception {}
    
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      Http2ConnectionHandler.this.encoder().close();
      Http2ConnectionHandler.this.decoder().close();
      Http2ConnectionHandler.this.connection().close((Promise<Void>)ctx.voidPromise());
    }
    
    public boolean prefaceSent() {
      return true;
    }
    
    public abstract void decode(ChannelHandlerContext param1ChannelHandlerContext, ByteBuf param1ByteBuf, List<Object> param1List) throws Exception;
  }
  
  private final class PrefaceDecoder extends BaseDecoder {
    private ByteBuf clientPrefaceString;
    
    private boolean prefaceSent;
    
    public PrefaceDecoder(ChannelHandlerContext ctx) throws Exception {
      this.clientPrefaceString = Http2ConnectionHandler.clientPrefaceString(Http2ConnectionHandler.this.encoder.connection());
      sendPreface(ctx);
    }
    
    public boolean prefaceSent() {
      return this.prefaceSent;
    }
    
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      try {
        if (ctx.channel().isActive() && readClientPrefaceString(in) && verifyFirstFrameIsSettings(in)) {
          Http2ConnectionHandler.this.byteDecoder = new Http2ConnectionHandler.FrameDecoder();
          Http2ConnectionHandler.this.byteDecoder.decode(ctx, in, out);
        } 
      } catch (Throwable e) {
        Http2ConnectionHandler.this.onError(ctx, false, e);
      } 
    }
    
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
      sendPreface(ctx);
    }
    
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
      cleanup();
      super.channelInactive(ctx);
    }
    
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      cleanup();
    }
    
    private void cleanup() {
      if (this.clientPrefaceString != null) {
        this.clientPrefaceString.release();
        this.clientPrefaceString = null;
      } 
    }
    
    private boolean readClientPrefaceString(ByteBuf in) throws Http2Exception {
      if (this.clientPrefaceString == null)
        return true; 
      int prefaceRemaining = this.clientPrefaceString.readableBytes();
      int bytesRead = Math.min(in.readableBytes(), prefaceRemaining);
      if (bytesRead == 0 || !ByteBufUtil.equals(in, in.readerIndex(), this.clientPrefaceString, this.clientPrefaceString
          .readerIndex(), bytesRead)) {
        int maxSearch = 1024;
        int http1Index = ByteBufUtil.indexOf(Http2ConnectionHandler.HTTP_1_X_BUF, in.slice(in.readerIndex(), Math.min(in.readableBytes(), maxSearch)));
        if (http1Index != -1) {
          String chunk = in.toString(in.readerIndex(), http1Index - in.readerIndex(), CharsetUtil.US_ASCII);
          throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Unexpected HTTP/1.x request: %s", new Object[] { chunk });
        } 
        String receivedBytes = ByteBufUtil.hexDump(in, in.readerIndex(), 
            Math.min(in.readableBytes(), this.clientPrefaceString.readableBytes()));
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "HTTP/2 client preface string missing or corrupt. Hex dump for received bytes: %s", new Object[] { receivedBytes });
      } 
      in.skipBytes(bytesRead);
      this.clientPrefaceString.skipBytes(bytesRead);
      if (!this.clientPrefaceString.isReadable()) {
        this.clientPrefaceString.release();
        this.clientPrefaceString = null;
        return true;
      } 
      return false;
    }
    
    private boolean verifyFirstFrameIsSettings(ByteBuf in) throws Http2Exception {
      if (in.readableBytes() < 5)
        return false; 
      short frameType = in.getUnsignedByte(in.readerIndex() + 3);
      short flags = in.getUnsignedByte(in.readerIndex() + 4);
      if (frameType != 4 || (flags & 0x1) != 0)
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "First received frame was not SETTINGS. Hex dump for first 5 bytes: %s", new Object[] { ByteBufUtil.hexDump(in, in.readerIndex(), 5) }); 
      return true;
    }
    
    private void sendPreface(ChannelHandlerContext ctx) throws Exception {
      if (this.prefaceSent || !ctx.channel().isActive())
        return; 
      this.prefaceSent = true;
      boolean isClient = !Http2ConnectionHandler.this.connection().isServer();
      if (isClient)
        ctx.write(Http2CodecUtil.connectionPrefaceBuf()).addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE); 
      Http2ConnectionHandler.this.encoder.writeSettings(ctx, Http2ConnectionHandler.this.initialSettings, ctx.newPromise()).addListener((GenericFutureListener)ChannelFutureListener.CLOSE_ON_FAILURE);
      if (isClient)
        Http2ConnectionHandler.this.userEventTriggered(ctx, Http2ConnectionPrefaceAndSettingsFrameWrittenEvent.INSTANCE); 
    }
  }
  
  private final class FrameDecoder extends BaseDecoder {
    private FrameDecoder() {}
    
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      try {
        Http2ConnectionHandler.this.decoder.decodeFrame(ctx, in, out);
      } catch (Throwable e) {
        Http2ConnectionHandler.this.onError(ctx, false, e);
      } 
    }
  }
  
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.encoder.lifecycleManager(this);
    this.decoder.lifecycleManager(this);
    this.encoder.flowController().channelHandlerContext(ctx);
    this.decoder.flowController().channelHandlerContext(ctx);
    this.byteDecoder = new PrefaceDecoder(ctx);
  }
  
  protected void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
    if (this.byteDecoder != null) {
      this.byteDecoder.handlerRemoved(ctx);
      this.byteDecoder = null;
    } 
  }
  
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    if (this.byteDecoder == null)
      this.byteDecoder = new PrefaceDecoder(ctx); 
    this.byteDecoder.channelActive(ctx);
    super.channelActive(ctx);
  }
  
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    if (this.byteDecoder != null) {
      this.byteDecoder.channelInactive(ctx);
      this.byteDecoder = null;
    } 
  }
  
  public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
    try {
      if (ctx.channel().isWritable())
        flush(ctx); 
      this.encoder.flowController().channelWritabilityChanged();
    } finally {
      super.channelWritabilityChanged(ctx);
    } 
  }
  
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    this.byteDecoder.decode(ctx, in, out);
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
    promise = promise.unvoid();
    if (!ctx.channel().isActive()) {
      ctx.close(promise);
      return;
    } 
    ChannelFuture future = connection().goAwaySent() ? ctx.write(Unpooled.EMPTY_BUFFER) : goAway(ctx, (Http2Exception)null);
    ctx.flush();
    doGracefulShutdown(ctx, future, promise);
  }
  
  private void doGracefulShutdown(ChannelHandlerContext ctx, ChannelFuture future, ChannelPromise promise) {
    if (isGracefulShutdownComplete()) {
      future.addListener((GenericFutureListener)new ClosingChannelFutureListener(ctx, promise));
    } else if (this.gracefulShutdownTimeoutMillis < 0L) {
      this.closeListener = new ClosingChannelFutureListener(ctx, promise);
    } else {
      this.closeListener = new ClosingChannelFutureListener(ctx, promise, this.gracefulShutdownTimeoutMillis, TimeUnit.MILLISECONDS);
    } 
  }
  
  public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
    ctx.deregister(promise);
  }
  
  public void read(ChannelHandlerContext ctx) throws Exception {
    ctx.read();
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    ctx.write(msg, promise);
  }
  
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    try {
      channelReadComplete0(ctx);
    } finally {
      flush(ctx);
    } 
  }
  
  void channelReadComplete0(ChannelHandlerContext ctx) throws Exception {
    super.channelReadComplete(ctx);
  }
  
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    if (Http2CodecUtil.getEmbeddedHttp2Exception(cause) != null) {
      onError(ctx, false, cause);
    } else {
      super.exceptionCaught(ctx, cause);
    } 
  }
  
  public void closeStreamLocal(Http2Stream stream, ChannelFuture future) {
    switch (stream.state()) {
      case GRACEFUL_SHUTDOWN:
      case null:
        stream.closeLocalSide();
        return;
    } 
    closeStream(stream, future);
  }
  
  public void closeStreamRemote(Http2Stream stream, ChannelFuture future) {
    switch (stream.state()) {
      case null:
      case null:
        stream.closeRemoteSide();
        return;
    } 
    closeStream(stream, future);
  }
  
  public void closeStream(Http2Stream stream, ChannelFuture future) {
    stream.close();
    if (future.isDone()) {
      checkCloseConnection(future);
    } else {
      future.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
              Http2ConnectionHandler.this.checkCloseConnection(future);
            }
          });
    } 
  }
  
  public void onError(ChannelHandlerContext ctx, boolean outbound, Throwable cause) {
    Http2Exception embedded = Http2CodecUtil.getEmbeddedHttp2Exception(cause);
    if (Http2Exception.isStreamError(embedded)) {
      onStreamError(ctx, outbound, cause, (Http2Exception.StreamException)embedded);
    } else if (embedded instanceof Http2Exception.CompositeStreamException) {
      Http2Exception.CompositeStreamException compositException = (Http2Exception.CompositeStreamException)embedded;
      for (Http2Exception.StreamException streamException : compositException)
        onStreamError(ctx, outbound, cause, streamException); 
    } else {
      onConnectionError(ctx, outbound, cause, embedded);
    } 
    ctx.flush();
  }
  
  protected boolean isGracefulShutdownComplete() {
    return (connection().numActiveStreams() == 0);
  }
  
  protected void onConnectionError(ChannelHandlerContext ctx, boolean outbound, Throwable cause, Http2Exception http2Ex) {
    if (http2Ex == null)
      http2Ex = new Http2Exception(Http2Error.INTERNAL_ERROR, cause.getMessage(), cause); 
    ChannelPromise promise = ctx.newPromise();
    ChannelFuture future = goAway(ctx, http2Ex);
    switch (http2Ex.shutdownHint()) {
      case GRACEFUL_SHUTDOWN:
        doGracefulShutdown(ctx, future, promise);
        return;
    } 
    future.addListener((GenericFutureListener)new ClosingChannelFutureListener(ctx, promise));
  }
  
  protected void onStreamError(ChannelHandlerContext ctx, boolean outbound, Throwable cause, Http2Exception.StreamException http2Ex) {
    int streamId = http2Ex.streamId();
    Http2Stream stream = connection().stream(streamId);
    if (http2Ex instanceof Http2Exception.HeaderListSizeException && ((Http2Exception.HeaderListSizeException)http2Ex)
      .duringDecode() && 
      connection().isServer()) {
      if (stream == null)
        try {
          stream = this.encoder.connection().remote().createStream(streamId, true);
        } catch (Http2Exception e) {
          resetUnknownStream(ctx, streamId, http2Ex.error().code(), ctx.newPromise());
          return;
        }  
      if (stream != null && !stream.isHeadersSent())
        try {
          handleServerHeaderDecodeSizeError(ctx, stream);
        } catch (Throwable cause2) {
          onError(ctx, outbound, Http2Exception.connectionError(Http2Error.INTERNAL_ERROR, cause2, "Error DecodeSizeError", new Object[0]));
        }  
    } 
    if (stream == null) {
      resetUnknownStream(ctx, streamId, http2Ex.error().code(), ctx.newPromise());
    } else {
      resetStream(ctx, stream, http2Ex.error().code(), ctx.newPromise());
    } 
  }
  
  protected void handleServerHeaderDecodeSizeError(ChannelHandlerContext ctx, Http2Stream stream) {
    encoder().writeHeaders(ctx, stream.id(), HEADERS_TOO_LARGE_HEADERS, 0, true, ctx.newPromise());
  }
  
  protected Http2FrameWriter frameWriter() {
    return encoder().frameWriter();
  }
  
  private ChannelFuture resetUnknownStream(final ChannelHandlerContext ctx, int streamId, long errorCode, ChannelPromise promise) {
    ChannelFuture future = frameWriter().writeRstStream(ctx, streamId, errorCode, promise);
    if (future.isDone()) {
      closeConnectionOnError(ctx, future);
    } else {
      future.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
              Http2ConnectionHandler.this.closeConnectionOnError(ctx, future);
            }
          });
    } 
    return future;
  }
  
  public ChannelFuture resetStream(ChannelHandlerContext ctx, int streamId, long errorCode, ChannelPromise promise) {
    Http2Stream stream = connection().stream(streamId);
    if (stream == null)
      return resetUnknownStream(ctx, streamId, errorCode, promise.unvoid()); 
    return resetStream(ctx, stream, errorCode, promise);
  }
  
  private ChannelFuture resetStream(final ChannelHandlerContext ctx, final Http2Stream stream, long errorCode, ChannelPromise promise) {
    ChannelFuture future;
    promise = promise.unvoid();
    if (stream.isResetSent())
      return (ChannelFuture)promise.setSuccess(); 
    if (stream.state() == Http2Stream.State.IDLE || (
      connection().local().created(stream) && !stream.isHeadersSent() && !stream.isPushPromiseSent())) {
      ChannelPromise channelPromise = promise.setSuccess();
    } else {
      future = frameWriter().writeRstStream(ctx, stream.id(), errorCode, promise);
    } 
    stream.resetSent();
    if (future.isDone()) {
      processRstStreamWriteResult(ctx, stream, future);
    } else {
      future.addListener((GenericFutureListener)new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
              Http2ConnectionHandler.this.processRstStreamWriteResult(ctx, stream, future);
            }
          });
    } 
    return future;
  }
  
  public ChannelFuture goAway(final ChannelHandlerContext ctx, final int lastStreamId, final long errorCode, final ByteBuf debugData, ChannelPromise promise) {
    try {
      promise = promise.unvoid();
      Http2Connection connection = connection();
      if (connection().goAwaySent()) {
        if (lastStreamId == connection().remote().lastStreamKnownByPeer()) {
          debugData.release();
          return (ChannelFuture)promise.setSuccess();
        } 
        if (lastStreamId > connection.remote().lastStreamKnownByPeer())
          throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Last stream identifier must not increase between sending multiple GOAWAY frames (was '%d', is '%d').", new Object[] { Integer.valueOf(connection.remote().lastStreamKnownByPeer()), Integer.valueOf(lastStreamId) }); 
      } 
      connection.goAwaySent(lastStreamId, errorCode, debugData);
      debugData.retain();
      ChannelFuture future = frameWriter().writeGoAway(ctx, lastStreamId, errorCode, debugData, promise);
      if (future.isDone()) {
        processGoAwayWriteResult(ctx, lastStreamId, errorCode, debugData, future);
      } else {
        future.addListener((GenericFutureListener)new ChannelFutureListener() {
              public void operationComplete(ChannelFuture future) throws Exception {
                Http2ConnectionHandler.processGoAwayWriteResult(ctx, lastStreamId, errorCode, debugData, future);
              }
            });
      } 
      return future;
    } catch (Throwable cause) {
      debugData.release();
      return (ChannelFuture)promise.setFailure(cause);
    } 
  }
  
  private void checkCloseConnection(ChannelFuture future) {
    if (this.closeListener != null && isGracefulShutdownComplete()) {
      ChannelFutureListener closeListener = this.closeListener;
      this.closeListener = null;
      try {
        closeListener.operationComplete((Future)future);
      } catch (Exception e) {
        throw new IllegalStateException("Close listener threw an unexpected exception", e);
      } 
    } 
  }
  
  private ChannelFuture goAway(ChannelHandlerContext ctx, Http2Exception cause) {
    long errorCode = (cause != null) ? cause.error().code() : Http2Error.NO_ERROR.code();
    int lastKnownStream = connection().remote().lastStreamCreated();
    return goAway(ctx, lastKnownStream, errorCode, Http2CodecUtil.toByteBuf(ctx, cause), ctx.newPromise());
  }
  
  private void processRstStreamWriteResult(ChannelHandlerContext ctx, Http2Stream stream, ChannelFuture future) {
    if (future.isSuccess()) {
      closeStream(stream, future);
    } else {
      onConnectionError(ctx, true, future.cause(), (Http2Exception)null);
    } 
  }
  
  private void closeConnectionOnError(ChannelHandlerContext ctx, ChannelFuture future) {
    if (!future.isSuccess())
      onConnectionError(ctx, true, future.cause(), (Http2Exception)null); 
  }
  
  private static ByteBuf clientPrefaceString(Http2Connection connection) {
    return connection.isServer() ? Http2CodecUtil.connectionPrefaceBuf() : null;
  }
  
  private static void processGoAwayWriteResult(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData, ChannelFuture future) {
    try {
      if (future.isSuccess()) {
        if (errorCode != Http2Error.NO_ERROR.code()) {
          if (logger.isDebugEnabled())
            logger.debug("{} Sent GOAWAY: lastStreamId '{}', errorCode '{}', debugData '{}'. Forcing shutdown of the connection.", new Object[] { ctx
                  
                  .channel(), Integer.valueOf(lastStreamId), Long.valueOf(errorCode), debugData.toString(CharsetUtil.UTF_8), future.cause() }); 
          ctx.close();
        } 
      } else {
        if (logger.isDebugEnabled())
          logger.debug("{} Sending GOAWAY failed: lastStreamId '{}', errorCode '{}', debugData '{}'. Forcing shutdown of the connection.", new Object[] { ctx
                
                .channel(), Integer.valueOf(lastStreamId), Long.valueOf(errorCode), debugData.toString(CharsetUtil.UTF_8), future.cause() }); 
        ctx.close();
      } 
    } finally {
      debugData.release();
    } 
  }
  
  private static final class ClosingChannelFutureListener implements ChannelFutureListener {
    private final ChannelHandlerContext ctx;
    
    private final ChannelPromise promise;
    
    private final ScheduledFuture<?> timeoutTask;
    
    ClosingChannelFutureListener(ChannelHandlerContext ctx, ChannelPromise promise) {
      this.ctx = ctx;
      this.promise = promise;
      this.timeoutTask = null;
    }
    
    ClosingChannelFutureListener(final ChannelHandlerContext ctx, final ChannelPromise promise, long timeout, TimeUnit unit) {
      this.ctx = ctx;
      this.promise = promise;
      this.timeoutTask = ctx.executor().schedule(new Runnable() {
            public void run() {
              ctx.close(promise);
            }
          },  timeout, unit);
    }
    
    public void operationComplete(ChannelFuture sentGoAwayFuture) throws Exception {
      if (this.timeoutTask != null)
        this.timeoutTask.cancel(false); 
      this.ctx.close(this.promise);
    }
  }
}
