package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.handler.codec.UnsupportedMessageTypeException;
import com.github.steveice10.netty.handler.codec.http.FullHttpMessage;
import com.github.steveice10.netty.handler.codec.http.HttpServerUpgradeHandler;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.logging.InternalLogger;
import com.github.steveice10.netty.util.internal.logging.InternalLoggerFactory;

public class Http2FrameCodec extends Http2ConnectionHandler {
  private static final InternalLogger LOG = InternalLoggerFactory.getInstance(Http2FrameCodec.class);
  
  private final Http2Connection.PropertyKey streamKey;
  
  private final Http2Connection.PropertyKey upgradeKey;
  
  private final Integer initialFlowControlWindowSize;
  
  private ChannelHandlerContext ctx;
  
  private int numBufferedStreams;
  
  private DefaultHttp2FrameStream frameStreamToInitialize;
  
  Http2FrameCodec(Http2ConnectionEncoder encoder, Http2ConnectionDecoder decoder, Http2Settings initialSettings) {
    super(decoder, encoder, initialSettings);
    decoder.frameListener(new FrameListener());
    connection().addListener(new ConnectionListener());
    ((Http2RemoteFlowController)connection().remote().flowController()).listener(new Http2RemoteFlowControllerListener());
    this.streamKey = connection().newKey();
    this.upgradeKey = connection().newKey();
    this.initialFlowControlWindowSize = initialSettings.initialWindowSize();
  }
  
  DefaultHttp2FrameStream newStream() {
    return new DefaultHttp2FrameStream();
  }
  
  final void forEachActiveStream(final Http2FrameStreamVisitor streamVisitor) throws Http2Exception {
    assert this.ctx.executor().inEventLoop();
    connection().forEachActiveStream(new Http2StreamVisitor() {
          public boolean visit(Http2Stream stream) {
            try {
              return streamVisitor.visit(stream.<Http2FrameStream>getProperty(Http2FrameCodec.this.streamKey));
            } catch (Throwable cause) {
              Http2FrameCodec.this.onError(Http2FrameCodec.this.ctx, false, cause);
              return false;
            } 
          }
        });
  }
  
  public final void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    this.ctx = ctx;
    super.handlerAdded(ctx);
    handlerAdded0(ctx);
    Http2Connection connection = connection();
    if (connection.isServer())
      tryExpandConnectionFlowControlWindow(connection); 
  }
  
  private void tryExpandConnectionFlowControlWindow(Http2Connection connection) throws Http2Exception {
    if (this.initialFlowControlWindowSize != null) {
      Http2Stream connectionStream = connection.connectionStream();
      Http2LocalFlowController localFlowController = connection.local().flowController();
      int delta = this.initialFlowControlWindowSize.intValue() - localFlowController.initialWindowSize(connectionStream);
      if (delta > 0) {
        localFlowController.incrementWindowSize(connectionStream, Math.max(delta << 1, delta));
        flush(this.ctx);
      } 
    } 
  }
  
  void handlerAdded0(ChannelHandlerContext ctx) throws Exception {}
  
  public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt == Http2ConnectionPrefaceAndSettingsFrameWrittenEvent.INSTANCE) {
      tryExpandConnectionFlowControlWindow(connection());
    } else if (evt instanceof HttpServerUpgradeHandler.UpgradeEvent) {
      HttpServerUpgradeHandler.UpgradeEvent upgrade = (HttpServerUpgradeHandler.UpgradeEvent)evt;
      try {
        onUpgradeEvent(ctx, upgrade.retain());
        Http2Stream stream = connection().stream(1);
        if (stream.getProperty(this.streamKey) == null)
          onStreamActive0(stream); 
        upgrade.upgradeRequest().headers().setInt((CharSequence)HttpConversionUtil.ExtensionHeaderNames.STREAM_ID
            .text(), 1);
        stream.setProperty(this.upgradeKey, Boolean.valueOf(true));
        InboundHttpToHttp2Adapter.handle(ctx, 
            connection(), decoder().frameListener(), (FullHttpMessage)upgrade.upgradeRequest().retain());
      } finally {
        upgrade.release();
      } 
      return;
    } 
    super.userEventTriggered(ctx, evt);
  }
  
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    if (msg instanceof Http2DataFrame) {
      Http2DataFrame dataFrame = (Http2DataFrame)msg;
      encoder().writeData(ctx, dataFrame.stream().id(), dataFrame.content(), dataFrame
          .padding(), dataFrame.isEndStream(), promise);
    } else if (msg instanceof Http2HeadersFrame) {
      writeHeadersFrame(ctx, (Http2HeadersFrame)msg, promise);
    } else if (msg instanceof Http2WindowUpdateFrame) {
      Http2WindowUpdateFrame frame = (Http2WindowUpdateFrame)msg;
      Http2FrameStream frameStream = frame.stream();
      try {
        if (frameStream == null) {
          increaseInitialConnectionWindow(frame.windowSizeIncrement());
        } else {
          consumeBytes(frameStream.id(), frame.windowSizeIncrement());
        } 
        promise.setSuccess();
      } catch (Throwable t) {
        promise.setFailure(t);
      } 
    } else if (msg instanceof Http2ResetFrame) {
      Http2ResetFrame rstFrame = (Http2ResetFrame)msg;
      encoder().writeRstStream(ctx, rstFrame.stream().id(), rstFrame.errorCode(), promise);
    } else if (msg instanceof Http2PingFrame) {
      Http2PingFrame frame = (Http2PingFrame)msg;
      encoder().writePing(ctx, frame.ack(), frame.content(), promise);
    } else if (msg instanceof Http2SettingsFrame) {
      encoder().writeSettings(ctx, ((Http2SettingsFrame)msg).settings(), promise);
    } else if (msg instanceof Http2GoAwayFrame) {
      writeGoAwayFrame(ctx, (Http2GoAwayFrame)msg, promise);
    } else if (msg instanceof Http2UnknownFrame) {
      Http2UnknownFrame unknownFrame = (Http2UnknownFrame)msg;
      encoder().writeFrame(ctx, unknownFrame.frameType(), unknownFrame.stream().id(), unknownFrame
          .flags(), unknownFrame.content(), promise);
    } else if (!(msg instanceof Http2Frame)) {
      ctx.write(msg, promise);
    } else {
      ReferenceCountUtil.release(msg);
      throw new UnsupportedMessageTypeException(msg, new Class[0]);
    } 
  }
  
  private void increaseInitialConnectionWindow(int deltaBytes) throws Http2Exception {
    ((Http2LocalFlowController)connection().local().flowController()).incrementWindowSize(connection().connectionStream(), deltaBytes);
  }
  
  final boolean consumeBytes(int streamId, int bytes) throws Http2Exception {
    Http2Stream stream = connection().stream(streamId);
    if (stream != null && streamId == 1) {
      Boolean upgraded = stream.<Boolean>getProperty(this.upgradeKey);
      if (Boolean.TRUE.equals(upgraded))
        return false; 
    } 
    return ((Http2LocalFlowController)connection().local().flowController()).consumeBytes(stream, bytes);
  }
  
  private void writeGoAwayFrame(ChannelHandlerContext ctx, Http2GoAwayFrame frame, ChannelPromise promise) {
    if (frame.lastStreamId() > -1) {
      frame.release();
      throw new IllegalArgumentException("Last stream id must not be set on GOAWAY frame");
    } 
    int lastStreamCreated = connection().remote().lastStreamCreated();
    long lastStreamId = lastStreamCreated + frame.extraStreamIds() * 2L;
    if (lastStreamId > 2147483647L)
      lastStreamId = 2147483647L; 
    goAway(ctx, (int)lastStreamId, frame.errorCode(), frame.content(), promise);
  }
  
  private void writeHeadersFrame(ChannelHandlerContext ctx, Http2HeadersFrame headersFrame, final ChannelPromise promise) {
    if (Http2CodecUtil.isStreamIdValid(headersFrame.stream().id())) {
      encoder().writeHeaders(ctx, headersFrame.stream().id(), headersFrame.headers(), headersFrame.padding(), headersFrame
          .isEndStream(), promise);
    } else {
      DefaultHttp2FrameStream stream = (DefaultHttp2FrameStream)headersFrame.stream();
      Http2Connection connection = connection();
      int streamId = connection.local().incrementAndGetNextStreamId();
      if (streamId < 0) {
        promise.setFailure(new Http2NoMoreStreamIdsException());
        return;
      } 
      stream.id = streamId;
      assert this.frameStreamToInitialize == null;
      this.frameStreamToInitialize = stream;
      ChannelPromise writePromise = ctx.newPromise();
      encoder().writeHeaders(ctx, streamId, headersFrame.headers(), headersFrame.padding(), headersFrame
          .isEndStream(), writePromise);
      if (writePromise.isDone()) {
        notifyHeaderWritePromise((ChannelFuture)writePromise, promise);
      } else {
        this.numBufferedStreams++;
        writePromise.addListener((GenericFutureListener)new ChannelFutureListener() {
              public void operationComplete(ChannelFuture future) throws Exception {
                Http2FrameCodec.this.numBufferedStreams--;
                Http2FrameCodec.notifyHeaderWritePromise(future, promise);
              }
            });
      } 
    } 
  }
  
  private static void notifyHeaderWritePromise(ChannelFuture future, ChannelPromise promise) {
    Throwable cause = future.cause();
    if (cause == null) {
      promise.setSuccess();
    } else {
      promise.setFailure(cause);
    } 
  }
  
  private void onStreamActive0(Http2Stream stream) {
    if (connection().local().isValidStreamId(stream.id()))
      return; 
    DefaultHttp2FrameStream stream2 = newStream().setStreamAndProperty(this.streamKey, stream);
    onHttp2StreamStateChanged(this.ctx, stream2);
  }
  
  private final class ConnectionListener extends Http2ConnectionAdapter {
    private ConnectionListener() {}
    
    public void onStreamAdded(Http2Stream stream) {
      if (Http2FrameCodec.this.frameStreamToInitialize != null && stream.id() == Http2FrameCodec.this.frameStreamToInitialize.id()) {
        Http2FrameCodec.this.frameStreamToInitialize.setStreamAndProperty(Http2FrameCodec.this.streamKey, stream);
        Http2FrameCodec.this.frameStreamToInitialize = null;
      } 
    }
    
    public void onStreamActive(Http2Stream stream) {
      Http2FrameCodec.this.onStreamActive0(stream);
    }
    
    public void onStreamClosed(Http2Stream stream) {
      Http2FrameCodec.DefaultHttp2FrameStream stream2 = stream.<Http2FrameCodec.DefaultHttp2FrameStream>getProperty(Http2FrameCodec.this.streamKey);
      if (stream2 != null)
        Http2FrameCodec.this.onHttp2StreamStateChanged(Http2FrameCodec.this.ctx, stream2); 
    }
    
    public void onStreamHalfClosed(Http2Stream stream) {
      Http2FrameCodec.DefaultHttp2FrameStream stream2 = stream.<Http2FrameCodec.DefaultHttp2FrameStream>getProperty(Http2FrameCodec.this.streamKey);
      if (stream2 != null)
        Http2FrameCodec.this.onHttp2StreamStateChanged(Http2FrameCodec.this.ctx, stream2); 
    }
  }
  
  protected void onConnectionError(ChannelHandlerContext ctx, boolean outbound, Throwable cause, Http2Exception http2Ex) {
    if (!outbound)
      ctx.fireExceptionCaught(cause); 
    super.onConnectionError(ctx, outbound, cause, http2Ex);
  }
  
  protected final void onStreamError(ChannelHandlerContext ctx, boolean outbound, Throwable cause, Http2Exception.StreamException streamException) {
    int streamId = streamException.streamId();
    Http2Stream connectionStream = connection().stream(streamId);
    if (connectionStream == null) {
      onHttp2UnknownStreamError(ctx, cause, streamException);
      super.onStreamError(ctx, outbound, cause, streamException);
      return;
    } 
    Http2FrameStream stream = connectionStream.<Http2FrameStream>getProperty(this.streamKey);
    if (stream == null) {
      LOG.warn("Stream exception thrown without stream object attached.", cause);
      super.onStreamError(ctx, outbound, cause, streamException);
      return;
    } 
    if (!outbound)
      onHttp2FrameStreamException(ctx, new Http2FrameStreamException(stream, streamException.error(), cause)); 
  }
  
  void onHttp2UnknownStreamError(ChannelHandlerContext ctx, Throwable cause, Http2Exception.StreamException streamException) {
    LOG.warn("Stream exception thrown for unkown stream {}.", Integer.valueOf(streamException.streamId()), cause);
  }
  
  protected final boolean isGracefulShutdownComplete() {
    return (super.isGracefulShutdownComplete() && this.numBufferedStreams == 0);
  }
  
  private final class FrameListener implements Http2FrameListener {
    private FrameListener() {}
    
    public void onUnknownFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload) {
      Http2FrameCodec.this.onHttp2Frame(ctx, (new DefaultHttp2UnknownFrame(frameType, flags, payload))
          .stream(requireStream(streamId)).retain());
    }
    
    public void onSettingsRead(ChannelHandlerContext ctx, Http2Settings settings) {
      Http2FrameCodec.this.onHttp2Frame(ctx, new DefaultHttp2SettingsFrame(settings));
    }
    
    public void onPingRead(ChannelHandlerContext ctx, long data) {
      Http2FrameCodec.this.onHttp2Frame(ctx, new DefaultHttp2PingFrame(data, false));
    }
    
    public void onPingAckRead(ChannelHandlerContext ctx, long data) {
      Http2FrameCodec.this.onHttp2Frame(ctx, new DefaultHttp2PingFrame(data, true));
    }
    
    public void onRstStreamRead(ChannelHandlerContext ctx, int streamId, long errorCode) {
      Http2FrameCodec.this.onHttp2Frame(ctx, (new DefaultHttp2ResetFrame(errorCode)).stream(requireStream(streamId)));
    }
    
    public void onWindowUpdateRead(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement) {
      if (streamId == 0)
        return; 
      Http2FrameCodec.this.onHttp2Frame(ctx, (new DefaultHttp2WindowUpdateFrame(windowSizeIncrement)).stream(requireStream(streamId)));
    }
    
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endStream) {
      onHeadersRead(ctx, streamId, headers, padding, endStream);
    }
    
    public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endOfStream) {
      Http2FrameCodec.this.onHttp2Frame(ctx, (new DefaultHttp2HeadersFrame(headers, endOfStream, padding))
          .stream(requireStream(streamId)));
    }
    
    public int onDataRead(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream) {
      Http2FrameCodec.this.onHttp2Frame(ctx, (new DefaultHttp2DataFrame(data, endOfStream, padding))
          .stream(requireStream(streamId)).retain());
      return 0;
    }
    
    public void onGoAwayRead(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData) {
      Http2FrameCodec.this.onHttp2Frame(ctx, (new DefaultHttp2GoAwayFrame(lastStreamId, errorCode, debugData)).retain());
    }
    
    public void onPriorityRead(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive) {}
    
    public void onSettingsAckRead(ChannelHandlerContext ctx) {}
    
    public void onPushPromiseRead(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding) {}
    
    private Http2FrameStream requireStream(int streamId) {
      Http2FrameStream stream = Http2FrameCodec.this.connection().stream(streamId).<Http2FrameStream>getProperty(Http2FrameCodec.this.streamKey);
      if (stream == null)
        throw new IllegalStateException("Stream object required for identifier: " + streamId); 
      return stream;
    }
  }
  
  void onUpgradeEvent(ChannelHandlerContext ctx, HttpServerUpgradeHandler.UpgradeEvent evt) {
    ctx.fireUserEventTriggered(evt);
  }
  
  void onHttp2StreamWritabilityChanged(ChannelHandlerContext ctx, Http2FrameStream stream, boolean writable) {
    ctx.fireUserEventTriggered(Http2FrameStreamEvent.writabilityChanged(stream));
  }
  
  void onHttp2StreamStateChanged(ChannelHandlerContext ctx, Http2FrameStream stream) {
    ctx.fireUserEventTriggered(Http2FrameStreamEvent.stateChanged(stream));
  }
  
  void onHttp2Frame(ChannelHandlerContext ctx, Http2Frame frame) {
    ctx.fireChannelRead(frame);
  }
  
  void onHttp2FrameStreamException(ChannelHandlerContext ctx, Http2FrameStreamException cause) {
    ctx.fireExceptionCaught(cause);
  }
  
  final boolean isWritable(DefaultHttp2FrameStream stream) {
    Http2Stream s = stream.stream;
    return (s != null && ((Http2RemoteFlowController)connection().remote().flowController()).isWritable(s));
  }
  
  private final class Http2RemoteFlowControllerListener implements Http2RemoteFlowController.Listener {
    private Http2RemoteFlowControllerListener() {}
    
    public void writabilityChanged(Http2Stream stream) {
      Http2FrameStream frameStream = stream.<Http2FrameStream>getProperty(Http2FrameCodec.this.streamKey);
      if (frameStream == null)
        return; 
      Http2FrameCodec.this.onHttp2StreamWritabilityChanged(Http2FrameCodec.this
          .ctx, frameStream, ((Http2RemoteFlowController)Http2FrameCodec.this.connection().remote().flowController()).isWritable(stream));
    }
  }
  
  static class DefaultHttp2FrameStream implements Http2FrameStream {
    private volatile int id = -1;
    
    volatile Http2Stream stream;
    
    DefaultHttp2FrameStream setStreamAndProperty(Http2Connection.PropertyKey streamKey, Http2Stream stream) {
      assert this.id == -1 || stream.id() == this.id;
      this.stream = stream;
      stream.setProperty(streamKey, this);
      return this;
    }
    
    public int id() {
      Http2Stream stream = this.stream;
      return (stream == null) ? this.id : stream.id();
    }
    
    public Http2Stream.State state() {
      Http2Stream stream = this.stream;
      return (stream == null) ? Http2Stream.State.IDLE : stream.state();
    }
    
    public String toString() {
      return String.valueOf(id());
    }
  }
}
