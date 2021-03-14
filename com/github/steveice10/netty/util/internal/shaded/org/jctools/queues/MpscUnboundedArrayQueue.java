package com.github.steveice10.netty.util.internal.shaded.org.jctools.queues;

import com.github.steveice10.netty.util.internal.shaded.org.jctools.util.PortableJvmInfo;

public class MpscUnboundedArrayQueue<E> extends BaseMpscLinkedArrayQueue<E> {
  long p0;
  
  long p1;
  
  long p2;
  
  long p3;
  
  long p4;
  
  long p5;
  
  long p6;
  
  long p7;
  
  long p10;
  
  long p11;
  
  long p12;
  
  long p13;
  
  long p14;
  
  long p15;
  
  long p16;
  
  long p17;
  
  public MpscUnboundedArrayQueue(int chunkSize) {
    super(chunkSize);
  }
  
  protected long availableInQueue(long pIndex, long cIndex) {
    return 2147483647L;
  }
  
  public int capacity() {
    return -1;
  }
  
  public int drain(MessagePassingQueue.Consumer<E> c) {
    return drain(c, 4096);
  }
  
  public int fill(MessagePassingQueue.Supplier<E> s) {
    long result = 0L;
    int capacity = 4096;
    while (true) {
      int filled = fill(s, PortableJvmInfo.RECOMENDED_OFFER_BATCH);
      if (filled == 0)
        return (int)result; 
      result += filled;
      if (result > 4096L)
        return (int)result; 
    } 
  }
  
  protected int getNextBufferSize(E[] buffer) {
    return LinkedArrayQueueUtil.length((Object[])buffer);
  }
  
  protected long getCurrentBufferCapacity(long mask) {
    return mask;
  }
}
