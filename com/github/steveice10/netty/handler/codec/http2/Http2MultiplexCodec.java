package com.github.steveice10.netty.handler.codec.http2;

import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.Channel;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.ChannelFuture;
import com.github.steveice10.netty.channel.ChannelFutureListener;
import com.github.steveice10.netty.channel.ChannelHandler;
import com.github.steveice10.netty.channel.ChannelHandlerContext;
import com.github.steveice10.netty.channel.ChannelId;
import com.github.steveice10.netty.channel.ChannelMetadata;
import com.github.steveice10.netty.channel.ChannelOutboundBuffer;
import com.github.steveice10.netty.channel.ChannelOutboundInvoker;
import com.github.steveice10.netty.channel.ChannelPipeline;
import com.github.steveice10.netty.channel.ChannelProgressivePromise;
import com.github.steveice10.netty.channel.ChannelPromise;
import com.github.steveice10.netty.channel.DefaultChannelConfig;
import com.github.steveice10.netty.channel.DefaultChannelPipeline;
import com.github.steveice10.netty.channel.DefaultMaxMessagesRecvByteBufAllocator;
import com.github.steveice10.netty.channel.EventLoop;
import com.github.steveice10.netty.channel.MessageSizeEstimator;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.channel.VoidChannelPromise;
import com.github.steveice10.netty.channel.WriteBufferWaterMark;
import com.github.steveice10.netty.util.DefaultAttributeMap;
import com.github.steveice10.netty.util.ReferenceCountUtil;
import com.github.steveice10.netty.util.concurrent.Future;
import com.github.steveice10.netty.util.concurrent.GenericFutureListener;
import com.github.steveice10.netty.util.internal.StringUtil;
import com.github.steveice10.netty.util.internal.ThrowableUtil;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Queue;

public class Http2MultiplexCodec extends Http2FrameCodec {
  private static final ChannelFutureListener CHILD_CHANNEL_REGISTRATION_LISTENER = new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        Http2MultiplexCodec.registerDone(future);
      }
    };
  
  private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);
  
  private static final ClosedChannelException CLOSED_CHANNEL_EXCEPTION = (ClosedChannelException)ThrowableUtil.unknownStackTrace(new ClosedChannelException(), DefaultHttp2StreamChannel.Http2ChannelUnsafe.class, "write(...)");
  
  private static final int MIN_HTTP2_FRAME_SIZE = 9;
  
  private final ChannelHandler inboundStreamHandler;
  
  private static final class FlowControlledFrameSizeEstimator implements MessageSizeEstimator {
    static final FlowControlledFrameSizeEstimator INSTANCE = new FlowControlledFrameSizeEstimator();
    
    static final MessageSizeEstimator.Handle HANDLE_INSTANCE = new MessageSizeEstimator.Handle() {
        public int size(Object msg) {
          return (msg instanceof Http2DataFrame) ? 
            
            (int)Math.min(2147483647L, ((Http2DataFrame)msg).initialFlowControlledBytes() + 9L) : 9;
        }
      };
    
    public MessageSizeEstimator.Handle newHandle() {
      return HANDLE_INSTANCE;
    }
  }
  
  private static final class Http2StreamChannelRecvByteBufAllocator extends DefaultMaxMessagesRecvByteBufAllocator {
    private Http2StreamChannelRecvByteBufAllocator() {}
    
    public DefaultMaxMessagesRecvByteBufAllocator.MaxMessageHandle newHandle() {
      return new DefaultMaxMessagesRecvByteBufAllocator.MaxMessageHandle() {
          public int guess() {
            return 1024;
          }
        };
    }
  }
  
  private int initialOutboundStreamWindow = 65535;
  
  private boolean parentReadInProgress;
  
  private int idCount;
  
  private DefaultHttp2StreamChannel head;
  
  private DefaultHttp2StreamChannel tail;
  
  volatile ChannelHandlerContext ctx;
  
  Http2MultiplexCodec(Http2ConnectionEncoder encoder, Http2ConnectionDecoder decoder, Http2Settings initialSettings, ChannelHandler inboundStreamHandler) {
    super(encoder, decoder, initialSettings);
    this.inboundStreamHandler = inboundStreamHandler;
  }
  
  private static void registerDone(ChannelFuture future) {
    if (!future.isSuccess()) {
      Channel childChannel = future.channel();
      if (childChannel.isRegistered()) {
        childChannel.close();
      } else {
        childChannel.unsafe().closeForcibly();
      } 
    } 
  }
  
  public final void handlerAdded0(ChannelHandlerContext ctx) throws Exception {
    if (ctx.executor() != ctx.channel().eventLoop())
      throw new IllegalStateException("EventExecutor must be EventLoop of Channel"); 
    this.ctx = ctx;
  }
  
  public final void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
    super.handlerRemoved0(ctx);
    DefaultHttp2StreamChannel ch = this.head;
    while (ch != null) {
      DefaultHttp2StreamChannel curr = ch;
      ch = curr.next;
      curr.next = null;
    } 
    this.head = this.tail = null;
  }
  
  Http2MultiplexCodecStream newStream() {
    return new Http2MultiplexCodecStream();
  }
  
  final void onHttp2Frame(ChannelHandlerContext ctx, Http2Frame frame) {
    if (frame instanceof Http2StreamFrame) {
      Http2StreamFrame streamFrame = (Http2StreamFrame)frame;
      onHttp2StreamFrame(((Http2MultiplexCodecStream)streamFrame.stream()).channel, streamFrame);
    } else if (frame instanceof Http2GoAwayFrame) {
      onHttp2GoAwayFrame(ctx, (Http2GoAwayFrame)frame);
      ctx.fireChannelRead(frame);
    } else if (frame instanceof Http2SettingsFrame) {
      Http2Settings settings = ((Http2SettingsFrame)frame).settings();
      if (settings.initialWindowSize() != null)
        this.initialOutboundStreamWindow = settings.initialWindowSize().intValue(); 
      ctx.fireChannelRead(frame);
    } else {
      ctx.fireChannelRead(frame);
    } 
  }
  
  final void onHttp2StreamStateChanged(ChannelHandlerContext ctx, Http2FrameStream stream) {
    ChannelFuture future;
    DefaultHttp2StreamChannel channel;
    Http2MultiplexCodecStream s = (Http2MultiplexCodecStream)stream;
    switch (stream.state()) {
      case READ_PROCESSED_BUT_STOP_READING:
      case READ_PROCESSED_OK_TO_PROCESS_MORE:
        if (s.channel != null)
          break; 
        future = ctx.channel().eventLoop().register(new DefaultHttp2StreamChannel(s, false));
        if (future.isDone()) {
          registerDone(future);
          break;
        } 
        future.addListener((GenericFutureListener)CHILD_CHANNEL_REGISTRATION_LISTENER);
        break;
      case READ_IGNORED_CHANNEL_INACTIVE:
        channel = s.channel;
        if (channel != null)
          channel.streamClosed(); 
        break;
    } 
  }
  
  final void onHttp2StreamWritabilityChanged(ChannelHandlerContext ctx, Http2FrameStream stream, boolean writable) {
    ((Http2MultiplexCodecStream)stream).channel.writabilityChanged(writable);
  }
  
  final Http2StreamChannel newOutboundStream() {
    return new DefaultHttp2StreamChannel(newStream(), true);
  }
  
  final void onHttp2FrameStreamException(ChannelHandlerContext ctx, Http2FrameStreamException cause) {
    Http2FrameStream stream = cause.stream();
    DefaultHttp2StreamChannel childChannel = ((Http2MultiplexCodecStream)stream).channel;
    try {
      childChannel.pipeline().fireExceptionCaught(cause.getCause());
    } finally {
      childChannel.unsafe().closeForcibly();
    } 
  }
  
  private void onHttp2StreamFrame(DefaultHttp2StreamChannel childChannel, Http2StreamFrame frame) {
    switch (childChannel.fireChildRead(frame)) {
      case READ_PROCESSED_BUT_STOP_READING:
        childChannel.fireChildReadComplete();
      case READ_PROCESSED_OK_TO_PROCESS_MORE:
        addChildChannelToReadPendingQueue(childChannel);
      case READ_IGNORED_CHANNEL_INACTIVE:
      case READ_QUEUED:
        return;
    } 
    throw new Error();
  }
  
  final void addChildChannelToReadPendingQueue(DefaultHttp2StreamChannel childChannel) {
    if (!childChannel.fireChannelReadPending) {
      assert childChannel.next == null;
      if (this.tail == null) {
        assert this.head == null;
        this.tail = this.head = childChannel;
      } else {
        this.tail.next = childChannel;
        this.tail = childChannel;
      } 
      childChannel.fireChannelReadPending = true;
    } 
  }
  
  private void onHttp2GoAwayFrame(ChannelHandlerContext ctx, final Http2GoAwayFrame goAwayFrame) {
    try {
      forEachActiveStream(new Http2FrameStreamVisitor() {
            public boolean visit(Http2FrameStream stream) {
              int streamId = stream.id();
              Http2MultiplexCodec.DefaultHttp2StreamChannel childChannel = ((Http2MultiplexCodec.Http2MultiplexCodecStream)stream).channel;
              if (streamId > goAwayFrame.lastStreamId() && Http2MultiplexCodec.this.connection().local().isValidStreamId(streamId))
                childChannel.pipeline().fireUserEventTriggered(goAwayFrame.retainedDuplicate()); 
              return true;
            }
          });
    } catch (Http2Exception e) {
      ctx.fireExceptionCaught(e);
      ctx.close();
    } 
  }
  
  public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    this.parentReadInProgress = false;
    onChannelReadComplete(ctx);
    channelReadComplete0(ctx);
  }
  
  public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    this.parentReadInProgress = true;
    super.channelRead(ctx, msg);
  }
  
  final void onChannelReadComplete(ChannelHandlerContext ctx) {
    try {
      DefaultHttp2StreamChannel current = this.head;
      while (current != null) {
        DefaultHttp2StreamChannel childChannel = current;
        if (childChannel.fireChannelReadPending) {
          childChannel.fireChannelReadPending = false;
          childChannel.fireChildReadComplete();
        } 
        childChannel.next = null;
        current = current.next;
      } 
    } finally {
      this.tail = this.head = null;
      flush0(ctx);
    } 
  }
  
  void flush0(ChannelHandlerContext ctx) {
    flush(ctx);
  }
  
  boolean onBytesConsumed(ChannelHandlerContext ctx, Http2FrameStream stream, int bytes) throws Http2Exception {
    return consumeBytes(stream.id(), bytes);
  }
  
  static class Http2MultiplexCodecStream extends Http2FrameCodec.DefaultHttp2FrameStream {
    Http2MultiplexCodec.DefaultHttp2StreamChannel channel;
  }
  
  private enum ReadState {
    READ_QUEUED, READ_IGNORED_CHANNEL_INACTIVE, READ_PROCESSED_BUT_STOP_READING, READ_PROCESSED_OK_TO_PROCESS_MORE;
  }
  
  private boolean initialWritability(Http2FrameCodec.DefaultHttp2FrameStream stream) {
    return (!Http2CodecUtil.isStreamIdValid(stream.id()) || isWritable(stream));
  }
  
  private final class DefaultHttp2StreamChannel extends DefaultAttributeMap implements Http2StreamChannel {
    private final Http2StreamChannelConfig config = new Http2StreamChannelConfig(this);
    
    private final Http2ChannelUnsafe unsafe = new Http2ChannelUnsafe();
    
    private final ChannelId channelId;
    
    private final ChannelPipeline pipeline;
    
    private final Http2FrameCodec.DefaultHttp2FrameStream stream;
    
    private final ChannelPromise closePromise;
    
    private final boolean outbound;
    
    private volatile boolean registered;
    
    private volatile boolean writable;
    
    private boolean outboundClosed;
    
    private boolean closePending;
    
    private boolean readInProgress;
    
    private Queue<Object> inboundBuffer;
    
    private boolean firstFrameWritten;
    
    private boolean streamClosedWithoutError;
    
    private boolean inFireChannelReadComplete;
    
    boolean fireChannelReadPending;
    
    DefaultHttp2StreamChannel next;
    
    DefaultHttp2StreamChannel(Http2FrameCodec.DefaultHttp2FrameStream stream, boolean outbound) {
      this.stream = stream;
      this.outbound = outbound;
      this.writable = Http2MultiplexCodec.this.initialWritability(stream);
      ((Http2MultiplexCodec.Http2MultiplexCodecStream)stream).channel = this;
      this.pipeline = (ChannelPipeline)new DefaultChannelPipeline(this) {
          protected void incrementPendingOutboundBytes(long size) {}
          
          protected void decrementPendingOutboundBytes(long size) {}
        };
      this.closePromise = this.pipeline.newPromise();
      this.channelId = new Http2StreamChannelId(parent().id(), ++Http2MultiplexCodec.this.idCount);
    }
    
    public Http2FrameStream stream() {
      return this.stream;
    }
    
    void streamClosed() {
      this.streamClosedWithoutError = true;
      if (this.readInProgress) {
        unsafe().closeForcibly();
      } else {
        this.closePending = true;
      } 
    }
    
    public ChannelMetadata metadata() {
      return Http2MultiplexCodec.METADATA;
    }
    
    public ChannelConfig config() {
      return (ChannelConfig)this.config;
    }
    
    public boolean isOpen() {
      return !this.closePromise.isDone();
    }
    
    public boolean isActive() {
      return isOpen();
    }
    
    public boolean isWritable() {
      return this.writable;
    }
    
    public ChannelId id() {
      return this.channelId;
    }
    
    public EventLoop eventLoop() {
      return parent().eventLoop();
    }
    
    public Channel parent() {
      return Http2MultiplexCodec.this.ctx.channel();
    }
    
    public boolean isRegistered() {
      return this.registered;
    }
    
    public SocketAddress localAddress() {
      return parent().localAddress();
    }
    
    public SocketAddress remoteAddress() {
      return parent().remoteAddress();
    }
    
    public ChannelFuture closeFuture() {
      return (ChannelFuture)this.closePromise;
    }
    
    public long bytesBeforeUnwritable() {
      return config().getWriteBufferHighWaterMark();
    }
    
    public long bytesBeforeWritable() {
      return 0L;
    }
    
    public Channel.Unsafe unsafe() {
      return this.unsafe;
    }
    
    public ChannelPipeline pipeline() {
      return this.pipeline;
    }
    
    public ByteBufAllocator alloc() {
      return config().getAllocator();
    }
    
    public Channel read() {
      pipeline().read();
      return this;
    }
    
    public Channel flush() {
      pipeline().flush();
      return this;
    }
    
    public ChannelFuture bind(SocketAddress localAddress) {
      return pipeline().bind(localAddress);
    }
    
    public ChannelFuture connect(SocketAddress remoteAddress) {
      return pipeline().connect(remoteAddress);
    }
    
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
      return pipeline().connect(remoteAddress, localAddress);
    }
    
    public ChannelFuture disconnect() {
      return pipeline().disconnect();
    }
    
    public ChannelFuture close() {
      return pipeline().close();
    }
    
    public ChannelFuture deregister() {
      return pipeline().deregister();
    }
    
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
      return pipeline().bind(localAddress, promise);
    }
    
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
      return pipeline().connect(remoteAddress, promise);
    }
    
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      return pipeline().connect(remoteAddress, localAddress, promise);
    }
    
    public ChannelFuture disconnect(ChannelPromise promise) {
      return pipeline().disconnect(promise);
    }
    
    public ChannelFuture close(ChannelPromise promise) {
      return pipeline().close(promise);
    }
    
    public ChannelFuture deregister(ChannelPromise promise) {
      return pipeline().deregister(promise);
    }
    
    public ChannelFuture write(Object msg) {
      return pipeline().write(msg);
    }
    
    public ChannelFuture write(Object msg, ChannelPromise promise) {
      return pipeline().write(msg, promise);
    }
    
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
      return pipeline().writeAndFlush(msg, promise);
    }
    
    public ChannelFuture writeAndFlush(Object msg) {
      return pipeline().writeAndFlush(msg);
    }
    
    public ChannelPromise newPromise() {
      return pipeline().newPromise();
    }
    
    public ChannelProgressivePromise newProgressivePromise() {
      return pipeline().newProgressivePromise();
    }
    
    public ChannelFuture newSucceededFuture() {
      return pipeline().newSucceededFuture();
    }
    
    public ChannelFuture newFailedFuture(Throwable cause) {
      return pipeline().newFailedFuture(cause);
    }
    
    public ChannelPromise voidPromise() {
      return pipeline().voidPromise();
    }
    
    public int hashCode() {
      return id().hashCode();
    }
    
    public boolean equals(Object o) {
      return (this == o);
    }
    
    public int compareTo(Channel o) {
      if (this == o)
        return 0; 
      return id().compareTo(o.id());
    }
    
    public String toString() {
      return parent().toString() + "(H2 - " + this.stream + ')';
    }
    
    void writabilityChanged(boolean writable) {
      assert eventLoop().inEventLoop();
      if (writable != this.writable && isActive()) {
        this.writable = writable;
        pipeline().fireChannelWritabilityChanged();
      } 
    }
    
    Http2MultiplexCodec.ReadState fireChildRead(Http2Frame frame) {
      assert eventLoop().inEventLoop();
      if (!isActive()) {
        ReferenceCountUtil.release(frame);
        return Http2MultiplexCodec.ReadState.READ_IGNORED_CHANNEL_INACTIVE;
      } 
      if (this.readInProgress && (this.inboundBuffer == null || this.inboundBuffer.isEmpty())) {
        RecvByteBufAllocator.ExtendedHandle allocHandle = this.unsafe.recvBufAllocHandle();
        this.unsafe.doRead0(frame, (RecvByteBufAllocator.Handle)allocHandle);
        return allocHandle.continueReading() ? Http2MultiplexCodec.ReadState.READ_PROCESSED_OK_TO_PROCESS_MORE : Http2MultiplexCodec.ReadState.READ_PROCESSED_BUT_STOP_READING;
      } 
      if (this.inboundBuffer == null)
        this.inboundBuffer = new ArrayDeque(4); 
      this.inboundBuffer.add(frame);
      return Http2MultiplexCodec.ReadState.READ_QUEUED;
    }
    
    void fireChildReadComplete() {
      assert eventLoop().inEventLoop();
      try {
        if (this.readInProgress) {
          this.inFireChannelReadComplete = true;
          this.readInProgress = false;
          unsafe().recvBufAllocHandle().readComplete();
          pipeline().fireChannelReadComplete();
        } 
      } finally {
        this.inFireChannelReadComplete = false;
      } 
    }
    
    private final class Http2ChannelUnsafe implements Channel.Unsafe {
      private final VoidChannelPromise unsafeVoidPromise = new VoidChannelPromise(Http2MultiplexCodec.DefaultHttp2StreamChannel.this, false);
      
      private RecvByteBufAllocator.ExtendedHandle recvHandle;
      
      private boolean writeDoneAndNoFlush;
      
      private boolean closeInitiated;
      
      public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        if (!promise.setUncancellable())
          return; 
        promise.setFailure(new UnsupportedOperationException());
      }
      
      public RecvByteBufAllocator.ExtendedHandle recvBufAllocHandle() {
        if (this.recvHandle == null)
          this.recvHandle = (RecvByteBufAllocator.ExtendedHandle)Http2MultiplexCodec.DefaultHttp2StreamChannel.this.config().getRecvByteBufAllocator().newHandle(); 
        return this.recvHandle;
      }
      
      public SocketAddress localAddress() {
        return Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().unsafe().localAddress();
      }
      
      public SocketAddress remoteAddress() {
        return Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().unsafe().remoteAddress();
      }
      
      public void register(EventLoop eventLoop, ChannelPromise promise) {
        if (!promise.setUncancellable())
          return; 
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered)
          throw new UnsupportedOperationException("Re-register is not supported"); 
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered = true;
        if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.outbound)
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().addLast(new ChannelHandler[] { Http2MultiplexCodec.access$700(this.this$1.this$0) }); 
        promise.setSuccess();
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelRegistered();
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isActive())
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelActive(); 
      }
      
      public void bind(SocketAddress localAddress, ChannelPromise promise) {
        if (!promise.setUncancellable())
          return; 
        promise.setFailure(new UnsupportedOperationException());
      }
      
      public void disconnect(ChannelPromise promise) {
        close(promise);
      }
      
      public void close(final ChannelPromise promise) {
        if (!promise.setUncancellable())
          return; 
        if (this.closeInitiated) {
          if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePromise.isDone()) {
            promise.setSuccess();
          } else if (!(promise instanceof VoidChannelPromise)) {
            Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePromise.addListener((GenericFutureListener)new ChannelFutureListener() {
                  public void operationComplete(ChannelFuture future) throws Exception {
                    promise.setSuccess();
                  }
                });
          } 
          return;
        } 
        this.closeInitiated = true;
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePending = false;
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.fireChannelReadPending = false;
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().isActive() && !Http2MultiplexCodec.DefaultHttp2StreamChannel.this.streamClosedWithoutError && Http2CodecUtil.isStreamIdValid(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream().id())) {
          Http2StreamFrame resetFrame = (new DefaultHttp2ResetFrame(Http2Error.CANCEL)).stream(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream());
          write(resetFrame, Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().voidPromise());
          flush();
        } 
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer != null)
          while (true) {
            Object msg = Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer.poll();
            if (msg == null)
              break; 
            ReferenceCountUtil.release(msg);
          }  
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.outboundClosed = true;
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePromise.setSuccess();
        promise.setSuccess();
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelInactive();
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isRegistered())
          deregister(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().voidPromise()); 
      }
      
      public void closeForcibly() {
        close(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().voidPromise());
      }
      
      public void deregister(ChannelPromise promise) {
        if (!promise.setUncancellable())
          return; 
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered) {
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered = true;
          promise.setSuccess();
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelUnregistered();
        } else {
          promise.setFailure(new IllegalStateException("Not registered"));
        } 
      }
      
      public void beginRead() {
        Object m;
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.readInProgress || !Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isActive())
          return; 
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.readInProgress = true;
        RecvByteBufAllocator.Handle allocHandle = Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().recvBufAllocHandle();
        allocHandle.reset(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.config());
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer == null || Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer.isEmpty()) {
          if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePending)
            Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe.closeForcibly(); 
          return;
        } 
        boolean continueReading;
        do {
          m = Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer.poll();
          if (m == null) {
            boolean bool = false;
            break;
          } 
          doRead0((Http2Frame)m, allocHandle);
        } while (continueReading = allocHandle.continueReading());
        if (continueReading && Http2MultiplexCodec.this.parentReadInProgress) {
          Http2MultiplexCodec.this.addChildChannelToReadPendingQueue(Http2MultiplexCodec.DefaultHttp2StreamChannel.this);
        } else {
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.readInProgress = false;
          allocHandle.readComplete();
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelReadComplete();
          flush();
          if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePending)
            Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe.closeForcibly(); 
        } 
      }
      
      void doRead0(Http2Frame frame, RecvByteBufAllocator.Handle allocHandle) {
        int numBytesToBeConsumed = 0;
        if (frame instanceof Http2DataFrame) {
          numBytesToBeConsumed = ((Http2DataFrame)frame).initialFlowControlledBytes();
          allocHandle.lastBytesRead(numBytesToBeConsumed);
        } else {
          allocHandle.lastBytesRead(9);
        } 
        allocHandle.incMessagesRead(1);
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelRead(frame);
        if (numBytesToBeConsumed != 0)
          try {
            this.writeDoneAndNoFlush |= Http2MultiplexCodec.this.onBytesConsumed(Http2MultiplexCodec.this.ctx, Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream, numBytesToBeConsumed);
          } catch (Http2Exception e) {
            Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireExceptionCaught(e);
          }  
      }
      
      public void write(Object msg, final ChannelPromise promise) {
        if (!promise.setUncancellable()) {
          ReferenceCountUtil.release(msg);
          return;
        } 
        if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isActive() || (Http2MultiplexCodec.DefaultHttp2StreamChannel.this
          
          .outboundClosed && (msg instanceof Http2HeadersFrame || msg instanceof Http2DataFrame))) {
          ReferenceCountUtil.release(msg);
          promise.setFailure(Http2MultiplexCodec.CLOSED_CHANNEL_EXCEPTION);
          return;
        } 
        try {
          if (msg instanceof Http2StreamFrame) {
            Http2StreamFrame frame = validateStreamFrame((Http2StreamFrame)msg).stream(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream());
            if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.firstFrameWritten && !Http2CodecUtil.isStreamIdValid(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream().id())) {
              if (!(frame instanceof Http2HeadersFrame)) {
                ReferenceCountUtil.release(frame);
                promise.setFailure(new IllegalArgumentException("The first frame must be a headers frame. Was: " + frame
                      
                      .name()));
                return;
              } 
              Http2MultiplexCodec.DefaultHttp2StreamChannel.this.firstFrameWritten = true;
              ChannelFuture channelFuture = write0(frame);
              if (channelFuture.isDone()) {
                firstWriteComplete(channelFuture, promise);
              } else {
                channelFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
                      public void operationComplete(ChannelFuture future) throws Exception {
                        Http2MultiplexCodec.DefaultHttp2StreamChannel.Http2ChannelUnsafe.this.firstWriteComplete(future, promise);
                      }
                    });
              } 
              return;
            } 
          } else {
            String msgStr = msg.toString();
            ReferenceCountUtil.release(msg);
            promise.setFailure(new IllegalArgumentException("Message must be an " + 
                  StringUtil.simpleClassName(Http2StreamFrame.class) + ": " + msgStr));
            return;
          } 
          ChannelFuture future = write0(msg);
          if (future.isDone()) {
            writeComplete(future, promise);
          } else {
            future.addListener((GenericFutureListener)new ChannelFutureListener() {
                  public void operationComplete(ChannelFuture future) throws Exception {
                    Http2MultiplexCodec.DefaultHttp2StreamChannel.Http2ChannelUnsafe.this.writeComplete(future, promise);
                  }
                });
          } 
        } catch (Throwable t) {
          promise.tryFailure(t);
        } finally {
          this.writeDoneAndNoFlush = true;
        } 
      }
      
      private void firstWriteComplete(ChannelFuture future, ChannelPromise promise) {
        Throwable cause = future.cause();
        if (cause == null) {
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.writabilityChanged(Http2MultiplexCodec.this.isWritable(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream));
          promise.setSuccess();
        } else {
          promise.setFailure(wrapStreamClosedError(cause));
          closeForcibly();
        } 
      }
      
      private void writeComplete(ChannelFuture future, ChannelPromise promise) {
        Throwable cause = future.cause();
        if (cause == null) {
          promise.setSuccess();
        } else {
          Throwable error = wrapStreamClosedError(cause);
          promise.setFailure(error);
          if (error instanceof ClosedChannelException)
            if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.config.isAutoClose()) {
              closeForcibly();
            } else {
              Http2MultiplexCodec.DefaultHttp2StreamChannel.this.outboundClosed = true;
            }  
        } 
      }
      
      private Throwable wrapStreamClosedError(Throwable cause) {
        if (cause instanceof Http2Exception && ((Http2Exception)cause).error() == Http2Error.STREAM_CLOSED)
          return (new ClosedChannelException()).initCause(cause); 
        return cause;
      }
      
      private Http2StreamFrame validateStreamFrame(Http2StreamFrame frame) {
        if (frame.stream() != null && frame.stream() != Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream) {
          String msgString = frame.toString();
          ReferenceCountUtil.release(frame);
          throw new IllegalArgumentException("Stream " + frame
              .stream() + " must not be set on the frame: " + msgString);
        } 
        return frame;
      }
      
      private ChannelFuture write0(Object msg) {
        ChannelPromise promise = Http2MultiplexCodec.this.ctx.newPromise();
        Http2MultiplexCodec.this.write(Http2MultiplexCodec.this.ctx, msg, promise);
        return (ChannelFuture)promise;
      }
      
      public void flush() {
        if (!this.writeDoneAndNoFlush)
          return; 
        try {
          if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inFireChannelReadComplete)
            Http2MultiplexCodec.this.flush0(Http2MultiplexCodec.this.ctx); 
        } finally {
          this.writeDoneAndNoFlush = false;
        } 
      }
      
      public ChannelPromise voidPromise() {
        return (ChannelPromise)this.unsafeVoidPromise;
      }
      
      public ChannelOutboundBuffer outboundBuffer() {
        return null;
      }
      
      private Http2ChannelUnsafe() {}
    }
    
    private final class Http2StreamChannelConfig extends DefaultChannelConfig {
      Http2StreamChannelConfig(Channel channel) {
        super(channel);
        setRecvByteBufAllocator((RecvByteBufAllocator)new Http2MultiplexCodec.Http2StreamChannelRecvByteBufAllocator());
      }
      
      public int getWriteBufferHighWaterMark() {
        return Math.min(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().config().getWriteBufferHighWaterMark(), Http2MultiplexCodec.this.initialOutboundStreamWindow);
      }
      
      public int getWriteBufferLowWaterMark() {
        return Math.min(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().config().getWriteBufferLowWaterMark(), Http2MultiplexCodec.this.initialOutboundStreamWindow);
      }
      
      public MessageSizeEstimator getMessageSizeEstimator() {
        return Http2MultiplexCodec.FlowControlledFrameSizeEstimator.INSTANCE;
      }
      
      public WriteBufferWaterMark getWriteBufferWaterMark() {
        int mark = getWriteBufferHighWaterMark();
        return new WriteBufferWaterMark(mark, mark);
      }
      
      public ChannelConfig setMessageSizeEstimator(MessageSizeEstimator estimator) {
        throw new UnsupportedOperationException();
      }
      
      @Deprecated
      public ChannelConfig setWriteBufferHighWaterMark(int writeBufferHighWaterMark) {
        throw new UnsupportedOperationException();
      }
      
      @Deprecated
      public ChannelConfig setWriteBufferLowWaterMark(int writeBufferLowWaterMark) {
        throw new UnsupportedOperationException();
      }
      
      public ChannelConfig setWriteBufferWaterMark(WriteBufferWaterMark writeBufferWaterMark) {
        throw new UnsupportedOperationException();
      }
      
      public ChannelConfig setRecvByteBufAllocator(RecvByteBufAllocator allocator) {
        if (!(allocator.newHandle() instanceof RecvByteBufAllocator.ExtendedHandle))
          throw new IllegalArgumentException("allocator.newHandle() must return an object of type: " + RecvByteBufAllocator.ExtendedHandle.class); 
        super.setRecvByteBufAllocator(allocator);
        return (ChannelConfig)this;
      }
    }
  }
  
  private final class Http2ChannelUnsafe implements Channel.Unsafe {
    private final VoidChannelPromise unsafeVoidPromise = new VoidChannelPromise(Http2MultiplexCodec.DefaultHttp2StreamChannel.this, false);
    
    private RecvByteBufAllocator.ExtendedHandle recvHandle;
    
    private boolean writeDoneAndNoFlush;
    
    private boolean closeInitiated;
    
    public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
      if (!promise.setUncancellable())
        return; 
      promise.setFailure(new UnsupportedOperationException());
    }
    
    public RecvByteBufAllocator.ExtendedHandle recvBufAllocHandle() {
      if (this.recvHandle == null)
        this.recvHandle = (RecvByteBufAllocator.ExtendedHandle)Http2MultiplexCodec.DefaultHttp2StreamChannel.this.config().getRecvByteBufAllocator().newHandle(); 
      return this.recvHandle;
    }
    
    public SocketAddress localAddress() {
      return Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().unsafe().localAddress();
    }
    
    public SocketAddress remoteAddress() {
      return Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().unsafe().remoteAddress();
    }
    
    public void register(EventLoop eventLoop, ChannelPromise promise) {
      if (!promise.setUncancellable())
        return; 
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered)
        throw new UnsupportedOperationException("Re-register is not supported"); 
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered = true;
      if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.outbound)
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().addLast(new ChannelHandler[] { Http2MultiplexCodec.access$700(this.this$1.this$0) }); 
      promise.setSuccess();
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelRegistered();
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isActive())
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelActive(); 
    }
    
    public void bind(SocketAddress localAddress, ChannelPromise promise) {
      if (!promise.setUncancellable())
        return; 
      promise.setFailure(new UnsupportedOperationException());
    }
    
    public void disconnect(ChannelPromise promise) {
      close(promise);
    }
    
    public void close(final ChannelPromise promise) {
      if (!promise.setUncancellable())
        return; 
      if (this.closeInitiated) {
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePromise.isDone()) {
          promise.setSuccess();
        } else if (!(promise instanceof VoidChannelPromise)) {
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePromise.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  promise.setSuccess();
                }
              });
        } 
        return;
      } 
      this.closeInitiated = true;
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePending = false;
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.fireChannelReadPending = false;
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.parent().isActive() && !Http2MultiplexCodec.DefaultHttp2StreamChannel.this.streamClosedWithoutError && Http2CodecUtil.isStreamIdValid(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream().id())) {
        Http2StreamFrame resetFrame = (new DefaultHttp2ResetFrame(Http2Error.CANCEL)).stream(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream());
        write(resetFrame, Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().voidPromise());
        flush();
      } 
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer != null)
        while (true) {
          Object msg = Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer.poll();
          if (msg == null)
            break; 
          ReferenceCountUtil.release(msg);
        }  
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.outboundClosed = true;
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePromise.setSuccess();
      promise.setSuccess();
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelInactive();
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isRegistered())
        deregister(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().voidPromise()); 
    }
    
    public void closeForcibly() {
      close(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().voidPromise());
    }
    
    public void deregister(ChannelPromise promise) {
      if (!promise.setUncancellable())
        return; 
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered) {
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.registered = true;
        promise.setSuccess();
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelUnregistered();
      } else {
        promise.setFailure(new IllegalStateException("Not registered"));
      } 
    }
    
    public void beginRead() {
      Object m;
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.readInProgress || !Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isActive())
        return; 
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.readInProgress = true;
      RecvByteBufAllocator.Handle allocHandle = Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe().recvBufAllocHandle();
      allocHandle.reset(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.config());
      if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer == null || Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer.isEmpty()) {
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePending)
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe.closeForcibly(); 
        return;
      } 
      boolean continueReading;
      do {
        m = Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inboundBuffer.poll();
        if (m == null) {
          boolean bool = false;
          break;
        } 
        doRead0((Http2Frame)m, allocHandle);
      } while (continueReading = allocHandle.continueReading());
      if (continueReading && Http2MultiplexCodec.this.parentReadInProgress) {
        Http2MultiplexCodec.this.addChildChannelToReadPendingQueue(Http2MultiplexCodec.DefaultHttp2StreamChannel.this);
      } else {
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.readInProgress = false;
        allocHandle.readComplete();
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelReadComplete();
        flush();
        if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.closePending)
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.unsafe.closeForcibly(); 
      } 
    }
    
    void doRead0(Http2Frame frame, RecvByteBufAllocator.Handle allocHandle) {
      int numBytesToBeConsumed = 0;
      if (frame instanceof Http2DataFrame) {
        numBytesToBeConsumed = ((Http2DataFrame)frame).initialFlowControlledBytes();
        allocHandle.lastBytesRead(numBytesToBeConsumed);
      } else {
        allocHandle.lastBytesRead(9);
      } 
      allocHandle.incMessagesRead(1);
      Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireChannelRead(frame);
      if (numBytesToBeConsumed != 0)
        try {
          this.writeDoneAndNoFlush |= Http2MultiplexCodec.this.onBytesConsumed(Http2MultiplexCodec.this.ctx, Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream, numBytesToBeConsumed);
        } catch (Http2Exception e) {
          Http2MultiplexCodec.DefaultHttp2StreamChannel.this.pipeline().fireExceptionCaught(e);
        }  
    }
    
    public void write(Object msg, final ChannelPromise promise) {
      if (!promise.setUncancellable()) {
        ReferenceCountUtil.release(msg);
        return;
      } 
      if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.isActive() || (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.outboundClosed && (msg instanceof Http2HeadersFrame || msg instanceof Http2DataFrame))) {
        ReferenceCountUtil.release(msg);
        promise.setFailure(Http2MultiplexCodec.CLOSED_CHANNEL_EXCEPTION);
        return;
      } 
      try {
        if (msg instanceof Http2StreamFrame) {
          Http2StreamFrame frame = validateStreamFrame((Http2StreamFrame)msg).stream(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream());
          if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.firstFrameWritten && !Http2CodecUtil.isStreamIdValid(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream().id())) {
            if (!(frame instanceof Http2HeadersFrame)) {
              ReferenceCountUtil.release(frame);
              promise.setFailure(new IllegalArgumentException("The first frame must be a headers frame. Was: " + frame.name()));
              return;
            } 
            Http2MultiplexCodec.DefaultHttp2StreamChannel.this.firstFrameWritten = true;
            ChannelFuture channelFuture = write0(frame);
            if (channelFuture.isDone()) {
              firstWriteComplete(channelFuture, promise);
            } else {
              channelFuture.addListener((GenericFutureListener)new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                      Http2MultiplexCodec.DefaultHttp2StreamChannel.Http2ChannelUnsafe.this.firstWriteComplete(future, promise);
                    }
                  });
            } 
            return;
          } 
        } else {
          String msgStr = msg.toString();
          ReferenceCountUtil.release(msg);
          promise.setFailure(new IllegalArgumentException("Message must be an " + StringUtil.simpleClassName(Http2StreamFrame.class) + ": " + msgStr));
          return;
        } 
        ChannelFuture future = write0(msg);
        if (future.isDone()) {
          writeComplete(future, promise);
        } else {
          future.addListener((GenericFutureListener)new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                  Http2MultiplexCodec.DefaultHttp2StreamChannel.Http2ChannelUnsafe.this.writeComplete(future, promise);
                }
              });
        } 
      } catch (Throwable t) {
        promise.tryFailure(t);
      } finally {
        this.writeDoneAndNoFlush = true;
      } 
    }
    
    private void firstWriteComplete(ChannelFuture future, ChannelPromise promise) {
      Throwable cause = future.cause();
      if (cause == null) {
        Http2MultiplexCodec.DefaultHttp2StreamChannel.this.writabilityChanged(Http2MultiplexCodec.this.isWritable(Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream));
        promise.setSuccess();
      } else {
        promise.setFailure(wrapStreamClosedError(cause));
        closeForcibly();
      } 
    }
    
    private void writeComplete(ChannelFuture future, ChannelPromise promise) {
      Throwable cause = future.cause();
      if (cause == null) {
        promise.setSuccess();
      } else {
        Throwable error = wrapStreamClosedError(cause);
        promise.setFailure(error);
        if (error instanceof ClosedChannelException)
          if (Http2MultiplexCodec.DefaultHttp2StreamChannel.this.config.isAutoClose()) {
            closeForcibly();
          } else {
            Http2MultiplexCodec.DefaultHttp2StreamChannel.this.outboundClosed = true;
          }  
      } 
    }
    
    private Throwable wrapStreamClosedError(Throwable cause) {
      if (cause instanceof Http2Exception && ((Http2Exception)cause).error() == Http2Error.STREAM_CLOSED)
        return (new ClosedChannelException()).initCause(cause); 
      return cause;
    }
    
    private Http2StreamFrame validateStreamFrame(Http2StreamFrame frame) {
      if (frame.stream() != null && frame.stream() != Http2MultiplexCodec.DefaultHttp2StreamChannel.this.stream) {
        String msgString = frame.toString();
        ReferenceCountUtil.release(frame);
        throw new IllegalArgumentException("Stream " + frame.stream() + " must not be set on the frame: " + msgString);
      } 
      return frame;
    }
    
    private ChannelFuture write0(Object msg) {
      ChannelPromise promise = Http2MultiplexCodec.this.ctx.newPromise();
      Http2MultiplexCodec.this.write(Http2MultiplexCodec.this.ctx, msg, promise);
      return (ChannelFuture)promise;
    }
    
    public void flush() {
      if (!this.writeDoneAndNoFlush)
        return; 
      try {
        if (!Http2MultiplexCodec.DefaultHttp2StreamChannel.this.inFireChannelReadComplete)
          Http2MultiplexCodec.this.flush0(Http2MultiplexCodec.this.ctx); 
      } finally {
        this.writeDoneAndNoFlush = false;
      } 
    }
    
    public ChannelPromise voidPromise() {
      return (ChannelPromise)this.unsafeVoidPromise;
    }
    
    public ChannelOutboundBuffer outboundBuffer() {
      return null;
    }
    
    private Http2ChannelUnsafe() {}
  }
}
