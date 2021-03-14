package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.channel.AbstractCoalescingBufferQueue;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.CoalescingBufferQueue;
import com.github.steveice10.netty.handler.codec.http.HttpStatusClass;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.ObjectUtil;
import java.util.ArrayDeque;

public class DefaultHttp2ConnectionEncoder implements Http2ConnectionEncoder {
  private final Http2FrameWriter frameWriter;
  
  private final Http2Connection connection;
  
  private Http2LifecycleManager lifecycleManager;
  
  private final ArrayDeque<Http2Settings> outstandingLocalSettingsQueue = new ArrayDeque<Http2Settings>(4);
  
  public DefaultHttp2ConnectionEncoder(Http2Connection connection, Http2FrameWriter frameWriter) {
    this.connection = (Http2Connection)ObjectUtil.checkNotNull(connection, "connection");
    this.frameWriter = (Http2FrameWriter)ObjectUtil.checkNotNull(frameWriter, "frameWriter");
    if (connection.remote().flowController() == null)
      connection.remote().flowController(new DefaultHttp2RemoteFlowController(connection)); 
  }
  
  public void lifecycleManager(Http2LifecycleManager lifecycleManager) {
    this.lifecycleManager = (Http2LifecycleManager)ObjectUtil.checkNotNull(lifecycleManager, "lifecycleManager");
  }
  
  public Http2FrameWriter frameWriter() {
    return this.frameWriter;
  }
  
  public Http2Connection connection() {
    return this.connection;
  }
  
  public final Http2RemoteFlowController flowController() {
    return connection().remote().flowController();
  }
  
  public void remoteSettings(Http2Settings settings) throws Http2Exception {
    Boolean pushEnabled = settings.pushEnabled();
    Http2FrameWriter.Configuration config = configuration();
    Http2HeadersEncoder.Configuration outboundHeaderConfig = config.headersConfiguration();
    Http2FrameSizePolicy outboundFrameSizePolicy = config.frameSizePolicy();
    if (pushEnabled != null) {
      if (!this.connection.isServer() && pushEnabled.booleanValue())
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Client received a value of ENABLE_PUSH specified to other than 0", new Object[0]); 
      this.connection.remote().allowPushTo(pushEnabled.booleanValue());
    } 
    Long maxConcurrentStreams = settings.maxConcurrentStreams();
    if (maxConcurrentStreams != null)
      this.connection.local().maxActiveStreams((int)Math.min(maxConcurrentStreams.longValue(), 2147483647L)); 
    Long headerTableSize = settings.headerTableSize();
    if (headerTableSize != null)
      outboundHeaderConfig.maxHeaderTableSize((int)Math.min(headerTableSize.longValue(), 2147483647L)); 
    Long maxHeaderListSize = settings.maxHeaderListSize();
    if (maxHeaderListSize != null)
      outboundHeaderConfig.maxHeaderListSize(maxHeaderListSize.longValue()); 
    Integer maxFrameSize = settings.maxFrameSize();
    if (maxFrameSize != null)
      outboundFrameSizePolicy.maxFrameSize(maxFrameSize.intValue()); 
    Integer initialWindowSize = settings.initialWindowSize();
    if (initialWindowSize != null)
      flowController().initialWindowSize(initialWindowSize.intValue()); 
  }
  
  public ChannelFuture writeData(ChannelHandlerContext ctx, int streamId, ByteBuf data, int padding, boolean endOfStream, ChannelPromise promise) {
    try {
      Http2Stream stream = requireStream(streamId);
      switch (stream.state()) {
        case OPEN:
        case HALF_CLOSED_REMOTE:
          flowController().addFlowControlled(stream, new FlowControlledData(stream, data, padding, endOfStream, promise));
          return (ChannelFuture)promise;
      } 
      throw new IllegalStateException("Stream " + stream.id() + " in unexpected state " + stream.state());
    } catch (Throwable e) {
      data.release();
      return (ChannelFuture)promise.setFailure(e);
    } 
  }
  
  public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding, boolean endStream, ChannelPromise promise) {
    return writeHeaders(ctx, streamId, headers, 0, (short)16, false, padding, endStream, promise);
  }
  
  private static boolean validateHeadersSentState(Http2Stream stream, Http2Headers headers, boolean isServer, boolean endOfStream) {
    boolean isInformational = (isServer && HttpStatusClass.valueOf(headers.status()) == HttpStatusClass.INFORMATIONAL);
    if (((isInformational || !endOfStream) && stream.isHeadersSent()) || stream.isTrailersSent())
      throw new IllegalStateException("Stream " + stream.id() + " sent too many headers EOS: " + endOfStream); 
    return isInformational;
  }
  
  public ChannelFuture writeHeaders(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream, ChannelPromise promise) {
    try {
      Http2Stream stream = this.connection.stream(streamId);
      if (stream == null) {
        try {
          stream = this.connection.local().createStream(streamId, endOfStream);
        } catch (Http2Exception cause) {
          if (this.connection.remote().mayHaveCreatedStream(streamId)) {
            promise.tryFailure(new IllegalStateException("Stream no longer exists: " + streamId, cause));
            return (ChannelFuture)promise;
          } 
          throw cause;
        } 
      } else {
        switch (stream.state()) {
          case RESERVED_LOCAL:
            stream.open(endOfStream);
            break;
          case OPEN:
          case HALF_CLOSED_REMOTE:
            break;
          default:
            throw new IllegalStateException("Stream " + stream.id() + " in unexpected state " + stream
                .state());
        } 
      } 
      Http2RemoteFlowController flowController = flowController();
      if (!endOfStream || !flowController.hasFlowControlled(stream)) {
        boolean isInformational = validateHeadersSentState(stream, headers, this.connection.isServer(), endOfStream);
        if (endOfStream) {
          final Http2Stream finalStream = stream;
          ChannelFutureListener closeStreamLocalListener = new ChannelFutureListener() {
              public void operationComplete(ChannelFuture future) throws Exception {
                DefaultHttp2ConnectionEncoder.this.lifecycleManager.closeStreamLocal(finalStream, future);
              }
            };
          promise = promise.unvoid().addListener((GenericFutureListener)closeStreamLocalListener);
        } 
        ChannelFuture future = this.frameWriter.writeHeaders(ctx, streamId, headers, streamDependency, weight, exclusive, padding, endOfStream, promise);
        Throwable failureCause = future.cause();
        if (failureCause == null) {
          stream.headersSent(isInformational);
          if (!future.isSuccess())
            notifyLifecycleManagerOnError(future, ctx); 
        } else {
          this.lifecycleManager.onError(ctx, true, failureCause);
        } 
        return future;
      } 
      flowController.addFlowControlled(stream, new FlowControlledHeaders(stream, headers, streamDependency, weight, exclusive, padding, true, promise));
      return (ChannelFuture)promise;
    } catch (Throwable t) {
      this.lifecycleManager.onError(ctx, true, t);
      promise.tryFailure(t);
      return (ChannelFuture)promise;
    } 
  }
  
  public ChannelFuture writePriority(ChannelHandlerContext ctx, int streamId, int streamDependency, short weight, boolean exclusive, ChannelPromise promise) {
    return this.frameWriter.writePriority(ctx, streamId, streamDependency, weight, exclusive, promise);
  }
  
  public ChannelFuture writeRstStream(ChannelHandlerContext ctx, int streamId, long errorCode, ChannelPromise promise) {
    return this.lifecycleManager.resetStream(ctx, streamId, errorCode, promise);
  }
  
  public ChannelFuture writeSettings(ChannelHandlerContext ctx, Http2Settings settings, ChannelPromise promise) {
    this.outstandingLocalSettingsQueue.add(settings);
    try {
      Boolean pushEnabled = settings.pushEnabled();
      if (pushEnabled != null && this.connection.isServer())
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Server sending SETTINGS frame with ENABLE_PUSH specified", new Object[0]); 
    } catch (Throwable e) {
      return (ChannelFuture)promise.setFailure(e);
    } 
    return this.frameWriter.writeSettings(ctx, settings, promise);
  }
  
  public ChannelFuture writeSettingsAck(ChannelHandlerContext ctx, ChannelPromise promise) {
    return this.frameWriter.writeSettingsAck(ctx, promise);
  }
  
  public ChannelFuture writePing(ChannelHandlerContext ctx, boolean ack, long data, ChannelPromise promise) {
    return this.frameWriter.writePing(ctx, ack, data, promise);
  }
  
  public ChannelFuture writePushPromise(ChannelHandlerContext ctx, int streamId, int promisedStreamId, Http2Headers headers, int padding, ChannelPromise promise) {
    try {
      if (this.connection.goAwayReceived())
        throw Http2Exception.connectionError(Http2Error.PROTOCOL_ERROR, "Sending PUSH_PROMISE after GO_AWAY received.", new Object[0]); 
      Http2Stream stream = requireStream(streamId);
      this.connection.local().reservePushStream(promisedStreamId, stream);
      ChannelFuture future = this.frameWriter.writePushPromise(ctx, streamId, promisedStreamId, headers, padding, promise);
      Throwable failureCause = future.cause();
      if (failureCause == null) {
        stream.pushPromiseSent();
        if (!future.isSuccess())
          notifyLifecycleManagerOnError(future, ctx); 
      } else {
        this.lifecycleManager.onError(ctx, true, failureCause);
      } 
      return future;
    } catch (Throwable t) {
      this.lifecycleManager.onError(ctx, true, t);
      promise.tryFailure(t);
      return (ChannelFuture)promise;
    } 
  }
  
  public ChannelFuture writeGoAway(ChannelHandlerContext ctx, int lastStreamId, long errorCode, ByteBuf debugData, ChannelPromise promise) {
    return this.lifecycleManager.goAway(ctx, lastStreamId, errorCode, debugData, promise);
  }
  
  public ChannelFuture writeWindowUpdate(ChannelHandlerContext ctx, int streamId, int windowSizeIncrement, ChannelPromise promise) {
    return (ChannelFuture)promise.setFailure(new UnsupportedOperationException("Use the Http2[Inbound|Outbound]FlowController objects to control window sizes"));
  }
  
  public ChannelFuture writeFrame(ChannelHandlerContext ctx, byte frameType, int streamId, Http2Flags flags, ByteBuf payload, ChannelPromise promise) {
    return this.frameWriter.writeFrame(ctx, frameType, streamId, flags, payload, promise);
  }
  
  public void close() {
    this.frameWriter.close();
  }
  
  public Http2Settings pollSentSettings() {
    return this.outstandingLocalSettingsQueue.poll();
  }
  
  public Http2FrameWriter.Configuration configuration() {
    return this.frameWriter.configuration();
  }
  
  private Http2Stream requireStream(int streamId) {
    Http2Stream stream = this.connection.stream(streamId);
    if (stream == null) {
      String message;
      if (this.connection.streamMayHaveExisted(streamId)) {
        message = "Stream no longer exists: " + streamId;
      } else {
        message = "Stream does not exist: " + streamId;
      } 
      throw new IllegalArgumentException(message);
    } 
    return stream;
  }
  
  private final class FlowControlledData extends FlowControlledBase {
    private final CoalescingBufferQueue queue;
    
    private int dataSize;
    
    FlowControlledData(Http2Stream stream, ByteBuf buf, int padding, boolean endOfStream, ChannelPromise promise) {
      super(stream, padding, endOfStream, promise);
      this.queue = new CoalescingBufferQueue(promise.channel());
      this.queue.add(buf, promise);
      this.dataSize = this.queue.readableBytes();
    }
    
    public int size() {
      return this.dataSize + this.padding;
    }
    
    public void error(ChannelHandlerContext ctx, Throwable cause) {
      this.queue.releaseAndFailAll(cause);
      DefaultHttp2ConnectionEncoder.this.lifecycleManager.onError(ctx, true, cause);
    }
    
    public void write(ChannelHandlerContext ctx, int allowedBytes) {
      int queuedData = this.queue.readableBytes();
      if (!this.endOfStream) {
        if (queuedData == 0) {
          ChannelPromise channelPromise = ctx.newPromise().addListener((GenericFutureListener)this);
          ctx.write(this.queue.remove(0, channelPromise), channelPromise);
          return;
        } 
        if (allowedBytes == 0)
          return; 
      } 
      int writableData = Math.min(queuedData, allowedBytes);
      ChannelPromise writePromise = ctx.newPromise().addListener((GenericFutureListener)this);
      ByteBuf toWrite = this.queue.remove(writableData, writePromise);
      this.dataSize = this.queue.readableBytes();
      int writablePadding = Math.min(allowedBytes - writableData, this.padding);
      this.padding -= writablePadding;
      DefaultHttp2ConnectionEncoder.this.frameWriter().writeData(ctx, this.stream.id(), toWrite, writablePadding, (this.endOfStream && 
          size() == 0), writePromise);
    }
    
    public boolean merge(ChannelHandlerContext ctx, Http2RemoteFlowController.FlowControlled next) {
      FlowControlledData nextData;
      if (FlowControlledData.class != next.getClass() || Integer.MAX_VALUE - (nextData = (FlowControlledData)next)
        .size() < size())
        return false; 
      nextData.queue.copyTo((AbstractCoalescingBufferQueue)this.queue);
      this.dataSize = this.queue.readableBytes();
      this.padding = Math.max(this.padding, nextData.padding);
      this.endOfStream = nextData.endOfStream;
      return true;
    }
  }
  
  private void notifyLifecycleManagerOnError(ChannelFuture future, final ChannelHandlerContext ctx) {
    future.addListener((GenericFutureListener)new ChannelFutureListener() {
          public void operationComplete(ChannelFuture future) throws Exception {
            Throwable cause = future.cause();
            if (cause != null)
              DefaultHttp2ConnectionEncoder.this.lifecycleManager.onError(ctx, true, cause); 
          }
        });
  }
  
  private final class FlowControlledHeaders extends FlowControlledBase {
    private final Http2Headers headers;
    
    private final int streamDependency;
    
    private final short weight;
    
    private final boolean exclusive;
    
    FlowControlledHeaders(Http2Stream stream, Http2Headers headers, int streamDependency, short weight, boolean exclusive, int padding, boolean endOfStream, ChannelPromise promise) {
      super(stream, padding, endOfStream, promise);
      this.headers = headers;
      this.streamDependency = streamDependency;
      this.weight = weight;
      this.exclusive = exclusive;
    }
    
    public int size() {
      return 0;
    }
    
    public void error(ChannelHandlerContext ctx, Throwable cause) {
      if (ctx != null)
        DefaultHttp2ConnectionEncoder.this.lifecycleManager.onError(ctx, true, cause); 
      this.promise.tryFailure(cause);
    }
    
    public void write(ChannelHandlerContext ctx, int allowedBytes) {
      boolean isInformational = DefaultHttp2ConnectionEncoder.validateHeadersSentState(this.stream, this.headers, DefaultHttp2ConnectionEncoder.this.connection.isServer(), this.endOfStream);
      if (this.promise.isVoid())
        this.promise = ctx.newPromise(); 
      this.promise.addListener((GenericFutureListener)this);
      ChannelFuture f = DefaultHttp2ConnectionEncoder.this.frameWriter.writeHeaders(ctx, this.stream.id(), this.headers, this.streamDependency, this.weight, this.exclusive, this.padding, this.endOfStream, this.promise);
      Throwable failureCause = f.cause();
      if (failureCause == null)
        this.stream.headersSent(isInformational); 
    }
    
    public boolean merge(ChannelHandlerContext ctx, Http2RemoteFlowController.FlowControlled next) {
      return false;
    }
  }
  
  public abstract class FlowControlledBase implements Http2RemoteFlowController.FlowControlled, ChannelFutureListener {
    protected final Http2Stream stream;
    
    protected ChannelPromise promise;
    
    protected boolean endOfStream;
    
    protected int padding;
    
    FlowControlledBase(Http2Stream stream, int padding, boolean endOfStream, ChannelPromise promise) {
      if (padding < 0)
        throw new IllegalArgumentException("padding must be >= 0"); 
      this.padding = padding;
      this.endOfStream = endOfStream;
      this.stream = stream;
      this.promise = promise;
    }
    
    public void writeComplete() {
      if (this.endOfStream)
        DefaultHttp2ConnectionEncoder.this.lifecycleManager.closeStreamLocal(this.stream, (ChannelFuture)this.promise); 
    }
    
    public void operationComplete(ChannelFuture future) throws Exception {
      if (!future.isSuccess())
        error(DefaultHttp2ConnectionEncoder.this.flowController().channelHandlerContext(), future.cause()); 
    }
  }
}
