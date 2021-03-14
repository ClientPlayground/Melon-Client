package com.github.steveice10.netty.channel.kqueue;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.channel.ChannelConfig;
import com.github.steveice10.netty.channel.RecvByteBufAllocator;
import com.github.steveice10.netty.util.UncheckedBooleanSupplier;
import com.github.steveice10.netty.util.internal.ObjectUtil;

final class KQueueRecvByteAllocatorHandle implements RecvByteBufAllocator.ExtendedHandle {
  private final RecvByteBufAllocator.ExtendedHandle delegate;
  
  private final UncheckedBooleanSupplier defaultMaybeMoreDataSupplier = new UncheckedBooleanSupplier() {
      public boolean get() {
        return KQueueRecvByteAllocatorHandle.this.maybeMoreDataToRead();
      }
    };
  
  private boolean overrideGuess;
  
  private boolean readEOF;
  
  private long numberBytesPending;
  
  KQueueRecvByteAllocatorHandle(RecvByteBufAllocator.ExtendedHandle handle) {
    this.delegate = (RecvByteBufAllocator.ExtendedHandle)ObjectUtil.checkNotNull(handle, "handle");
  }
  
  public int guess() {
    return this.overrideGuess ? guess0() : this.delegate.guess();
  }
  
  public void reset(ChannelConfig config) {
    this.overrideGuess = ((KQueueChannelConfig)config).getRcvAllocTransportProvidesGuess();
    this.delegate.reset(config);
  }
  
  public void incMessagesRead(int numMessages) {
    this.delegate.incMessagesRead(numMessages);
  }
  
  public ByteBuf allocate(ByteBufAllocator alloc) {
    return this.overrideGuess ? alloc.ioBuffer(guess0()) : this.delegate.allocate(alloc);
  }
  
  public void lastBytesRead(int bytes) {
    this.numberBytesPending = (bytes < 0) ? 0L : Math.max(0L, this.numberBytesPending - bytes);
    this.delegate.lastBytesRead(bytes);
  }
  
  public int lastBytesRead() {
    return this.delegate.lastBytesRead();
  }
  
  public void attemptedBytesRead(int bytes) {
    this.delegate.attemptedBytesRead(bytes);
  }
  
  public int attemptedBytesRead() {
    return this.delegate.attemptedBytesRead();
  }
  
  public void readComplete() {
    this.delegate.readComplete();
  }
  
  public boolean continueReading(UncheckedBooleanSupplier maybeMoreDataSupplier) {
    return this.delegate.continueReading(maybeMoreDataSupplier);
  }
  
  public boolean continueReading() {
    return this.delegate.continueReading(this.defaultMaybeMoreDataSupplier);
  }
  
  void readEOF() {
    this.readEOF = true;
  }
  
  void numberBytesPending(long numberBytesPending) {
    this.numberBytesPending = numberBytesPending;
  }
  
  boolean maybeMoreDataToRead() {
    return (this.numberBytesPending != 0L || this.readEOF);
  }
  
  private int guess0() {
    return (int)Math.min(this.numberBytesPending, 2147483647L);
  }
}
