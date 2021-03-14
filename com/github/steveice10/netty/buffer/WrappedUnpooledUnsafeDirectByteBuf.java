package com.github.steveice10.netty.buffer;

import com.github.steveice10.netty.util.internal.PlatformDependent;
import java.nio.ByteBuffer;

final class WrappedUnpooledUnsafeDirectByteBuf extends UnpooledUnsafeDirectByteBuf {
  WrappedUnpooledUnsafeDirectByteBuf(ByteBufAllocator alloc, long memoryAddress, int size, boolean doFree) {
    super(alloc, PlatformDependent.directBuffer(memoryAddress, size), size, doFree);
  }
  
  protected void freeDirect(ByteBuffer buffer) {
    PlatformDependent.freeMemory(this.memoryAddress);
  }
}
