package com.github.steveice10.netty.channel;

public class FixedRecvByteBufAllocator extends DefaultMaxMessagesRecvByteBufAllocator {
  private final int bufferSize;
  
  private final class HandleImpl extends DefaultMaxMessagesRecvByteBufAllocator.MaxMessageHandle {
    private final int bufferSize;
    
    public HandleImpl(int bufferSize) {
      this.bufferSize = bufferSize;
    }
    
    public int guess() {
      return this.bufferSize;
    }
  }
  
  public FixedRecvByteBufAllocator(int bufferSize) {
    if (bufferSize <= 0)
      throw new IllegalArgumentException("bufferSize must greater than 0: " + bufferSize); 
    this.bufferSize = bufferSize;
  }
  
  public RecvByteBufAllocator.Handle newHandle() {
    return new HandleImpl(this.bufferSize);
  }
  
  public FixedRecvByteBufAllocator respectMaybeMoreData(boolean respectMaybeMoreData) {
    super.respectMaybeMoreData(respectMaybeMoreData);
    return this;
  }
}
