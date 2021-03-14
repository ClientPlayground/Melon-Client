package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;

class UnpooledUnsafeNoCleanerDirectByteBuf extends UnpooledUnsafeDirectByteBuf {
  UnpooledUnsafeNoCleanerDirectByteBuf(ByteBufAllocator alloc, int initialCapacity, int maxCapacity) {
    super(alloc, initialCapacity, maxCapacity);
  }
  
  protected ByteBuffer allocateDirect(int initialCapacity) {
    return PlatformDependent.allocateDirectNoCleaner(initialCapacity);
  }
  
  ByteBuffer reallocateDirect(ByteBuffer oldBuffer, int initialCapacity) {
    return PlatformDependent.reallocateDirectNoCleaner(oldBuffer, initialCapacity);
  }
  
  protected void freeDirect(ByteBuffer buffer) {
    PlatformDependent.freeDirectNoCleaner(buffer);
  }
  
  public ByteBuf capacity(int newCapacity) {
    checkNewCapacity(newCapacity);
    int oldCapacity = capacity();
    if (newCapacity == oldCapacity)
      return this; 
    ByteBuffer newBuffer = reallocateDirect(this.buffer, newCapacity);
    if (newCapacity < oldCapacity)
      if (readerIndex() < newCapacity) {
        if (writerIndex() > newCapacity)
          writerIndex(newCapacity); 
      } else {
        setIndex(newCapacity, newCapacity);
      }  
    setByteBuffer(newBuffer, false);
    return this;
  }
}
