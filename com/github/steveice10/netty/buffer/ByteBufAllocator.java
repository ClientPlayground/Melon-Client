package com.github.steveice10.netty.buffer;

public interface ByteBufAllocator {
  public static final ByteBufAllocator DEFAULT = ByteBufUtil.DEFAULT_ALLOCATOR;
  
  ByteBuf buffer();
  
  ByteBuf buffer(int paramInt);
  
  ByteBuf buffer(int paramInt1, int paramInt2);
  
  ByteBuf ioBuffer();
  
  ByteBuf ioBuffer(int paramInt);
  
  ByteBuf ioBuffer(int paramInt1, int paramInt2);
  
  ByteBuf heapBuffer();
  
  ByteBuf heapBuffer(int paramInt);
  
  ByteBuf heapBuffer(int paramInt1, int paramInt2);
  
  ByteBuf directBuffer();
  
  ByteBuf directBuffer(int paramInt);
  
  ByteBuf directBuffer(int paramInt1, int paramInt2);
  
  CompositeByteBuf compositeBuffer();
  
  CompositeByteBuf compositeBuffer(int paramInt);
  
  CompositeByteBuf compositeHeapBuffer();
  
  CompositeByteBuf compositeHeapBuffer(int paramInt);
  
  CompositeByteBuf compositeDirectBuffer();
  
  CompositeByteBuf compositeDirectBuffer(int paramInt);
  
  boolean isDirectBufferPooled();
  
  int calculateNewCapacity(int paramInt1, int paramInt2);
}
