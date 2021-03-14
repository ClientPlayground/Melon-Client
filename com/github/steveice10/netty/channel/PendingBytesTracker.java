package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.util.internal.ObjectUtil;

abstract class PendingBytesTracker implements MessageSizeEstimator.Handle {
  private final MessageSizeEstimator.Handle estimatorHandle;
  
  private PendingBytesTracker(MessageSizeEstimator.Handle estimatorHandle) {
    this.estimatorHandle = (MessageSizeEstimator.Handle)ObjectUtil.checkNotNull(estimatorHandle, "estimatorHandle");
  }
  
  public final int size(Object msg) {
    return this.estimatorHandle.size(msg);
  }
  
  static PendingBytesTracker newTracker(Channel channel) {
    if (channel.pipeline() instanceof DefaultChannelPipeline)
      return new DefaultChannelPipelinePendingBytesTracker((DefaultChannelPipeline)channel.pipeline()); 
    ChannelOutboundBuffer buffer = channel.unsafe().outboundBuffer();
    MessageSizeEstimator.Handle handle = channel.config().getMessageSizeEstimator().newHandle();
    return (buffer == null) ? new NoopPendingBytesTracker(handle) : new ChannelOutboundBufferPendingBytesTracker(buffer, handle);
  }
  
  public abstract void incrementPendingOutboundBytes(long paramLong);
  
  public abstract void decrementPendingOutboundBytes(long paramLong);
  
  private static final class DefaultChannelPipelinePendingBytesTracker extends PendingBytesTracker {
    private final DefaultChannelPipeline pipeline;
    
    DefaultChannelPipelinePendingBytesTracker(DefaultChannelPipeline pipeline) {
      super(pipeline.estimatorHandle());
      this.pipeline = pipeline;
    }
    
    public void incrementPendingOutboundBytes(long bytes) {
      this.pipeline.incrementPendingOutboundBytes(bytes);
    }
    
    public void decrementPendingOutboundBytes(long bytes) {
      this.pipeline.decrementPendingOutboundBytes(bytes);
    }
  }
  
  private static final class ChannelOutboundBufferPendingBytesTracker extends PendingBytesTracker {
    private final ChannelOutboundBuffer buffer;
    
    ChannelOutboundBufferPendingBytesTracker(ChannelOutboundBuffer buffer, MessageSizeEstimator.Handle estimatorHandle) {
      super(estimatorHandle);
      this.buffer = buffer;
    }
    
    public void incrementPendingOutboundBytes(long bytes) {
      this.buffer.incrementPendingOutboundBytes(bytes);
    }
    
    public void decrementPendingOutboundBytes(long bytes) {
      this.buffer.decrementPendingOutboundBytes(bytes);
    }
  }
  
  private static final class NoopPendingBytesTracker extends PendingBytesTracker {
    NoopPendingBytesTracker(MessageSizeEstimator.Handle estimatorHandle) {
      super(estimatorHandle);
    }
    
    public void incrementPendingOutboundBytes(long bytes) {}
    
    public void decrementPendingOutboundBytes(long bytes) {}
  }
}
