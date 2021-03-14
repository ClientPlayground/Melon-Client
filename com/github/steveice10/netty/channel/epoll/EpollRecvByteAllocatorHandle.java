package com.github.steveice10.netty.channel.epoll;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.util.UncheckedBooleanSupplier;
import com.github.steveice10.netty.util.internal.ObjectUtil;

class EpollRecvByteAllocatorHandle implements RecvByteBufAllocator.ExtendedHandle {
  private final RecvByteBufAllocator.ExtendedHandle delegate;
  
  private final UncheckedBooleanSupplier defaultMaybeMoreDataSupplier = new UncheckedBooleanSupplier() {
      public boolean get() {
        return EpollRecvByteAllocatorHandle.this.maybeMoreDataToRead();
      }
    };
  
  private boolean isEdgeTriggered;
  
  private boolean receivedRdHup;
  
  EpollRecvByteAllocatorHandle(RecvByteBufAllocator.ExtendedHandle handle) {
    this.delegate = (RecvByteBufAllocator.ExtendedHandle)ObjectUtil.checkNotNull(handle, "handle");
  }
  
  final void receivedRdHup() {
    this.receivedRdHup = true;
  }
  
  final boolean isReceivedRdHup() {
    return this.receivedRdHup;
  }
  
  boolean maybeMoreDataToRead() {
    return ((this.isEdgeTriggered && lastBytesRead() > 0) || (!this.isEdgeTriggered && 
      lastBytesRead() == attemptedBytesRead()) || this.receivedRdHup);
  }
  
  final void edgeTriggered(boolean edgeTriggered) {
    this.isEdgeTriggered = edgeTriggered;
  }
  
  final boolean isEdgeTriggered() {
    return this.isEdgeTriggered;
  }
  
  public final ByteBuf allocate(ByteBufAllocator alloc) {
    return this.delegate.allocate(alloc);
  }
  
  public final int guess() {
    return this.delegate.guess();
  }
  
  public final void reset(ChannelConfig config) {
    this.delegate.reset(config);
  }
  
  public final void incMessagesRead(int numMessages) {
    this.delegate.incMessagesRead(numMessages);
  }
  
  public final void lastBytesRead(int bytes) {
    this.delegate.lastBytesRead(bytes);
  }
  
  public final int lastBytesRead() {
    return this.delegate.lastBytesRead();
  }
  
  public final int attemptedBytesRead() {
    return this.delegate.attemptedBytesRead();
  }
  
  public final void attemptedBytesRead(int bytes) {
    this.delegate.attemptedBytesRead(bytes);
  }
  
  public final void readComplete() {
    this.delegate.readComplete();
  }
  
  public final boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier) {
    return this.delegate.continueReading(maybeMoreDataSupplier);
  }
  
  public final boolean continueReading() {
    return this.delegate.continueReading(this.defaultMaybeMoreDataSupplier);
  }
}
