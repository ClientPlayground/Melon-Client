package com.github.steveice10.netty.channel;

import com.github.steveice10.netty.buffer.ByteBuf;
import com.github.steveice10.netty.buffer.ByteBufHolder;

public final class DefaultMessageSizeEstimator implements MessageSizeEstimator {
  private static final class HandleImpl implements MessageSizeEstimator.Handle {
    private final int unknownSize;
    
    private HandleImpl(int unknownSize) {
      this.unknownSize = unknownSize;
    }
    
    public int size(Object msg) {
      if (msg instanceof ByteBuf)
        return ((ByteBuf)msg).readableBytes(); 
      if (msg instanceof ByteBufHolder)
        return ((ByteBufHolder)msg).content().readableBytes(); 
      if (msg instanceof FileRegion)
        return 0; 
      return this.unknownSize;
    }
  }
  
  public static final MessageSizeEstimator DEFAULT = new DefaultMessageSizeEstimator(8);
  
  private final MessageSizeEstimator.Handle handle;
  
  public DefaultMessageSizeEstimator(int unknownSize) {
    if (unknownSize < 0)
      throw new IllegalArgumentException("unknownSize: " + unknownSize + " (expected: >= 0)"); 
    this.handle = new HandleImpl(unknownSize);
  }
  
  public MessageSizeEstimator.Handle newHandle() {
    return this.handle;
  }
}
