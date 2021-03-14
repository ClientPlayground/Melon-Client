package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufAllocator;
import com.github.steveice10.netty.util.UncheckedBooleanSupplier;
import com.github.steveice10.netty.util.internal.ObjectUtil;

public interface RecvByteBufAllocator {
  Handle newHandle();
  
  public static class DelegatingHandle implements Handle {
    private final RecvByteBufAllocator.Handle delegate;
    
    public DelegatingHandle(RecvByteBufAllocator.Handle delegate) {
      this.delegate = (RecvByteBufAllocator.Handle)ObjectUtil.checkNotNull(delegate, "delegate");
    }
    
    protected final RecvByteBufAllocator.Handle delegate() {
      return this.delegate;
    }
    
    public ByteBuf allocate(ByteBufAllocator alloc) {
      return this.delegate.allocate(alloc);
    }
    
    public int guess() {
      return this.delegate.guess();
    }
    
    public void reset(ChannelConfig config) {
      this.delegate.reset(config);
    }
    
    public void incMessagesRead(int numMessages) {
      this.delegate.incMessagesRead(numMessages);
    }
    
    public void lastBytesRead(int bytes) {
      this.delegate.lastBytesRead(bytes);
    }
    
    public int lastBytesRead() {
      return this.delegate.lastBytesRead();
    }
    
    public boolean continueReading() {
      return this.delegate.continueReading();
    }
    
    public int attemptedBytesRead() {
      return this.delegate.attemptedBytesRead();
    }
    
    public void attemptedBytesRead(int bytes) {
      this.delegate.attemptedBytesRead(bytes);
    }
    
    public void readComplete() {
      this.delegate.readComplete();
    }
  }
  
  public static interface ExtendedHandle extends Handle {
    boolean continueReading(UncheckedBooleanSupplier param1UncheckedBooleanSupplier);
  }
  
  @Deprecated
  public static interface Handle {
    ByteBuf allocate(ByteBufAllocator param1ByteBufAllocator);
    
    int guess();
    
    void reset(ChannelConfig param1ChannelConfig);
    
    void incMessagesRead(int param1Int);
    
    void lastBytesRead(int param1Int);
    
    int lastBytesRead();
    
    void attemptedBytesRead(int param1Int);
    
    int attemptedBytesRead();
    
    boolean continueReading();
    
    void readComplete();
  }
}
